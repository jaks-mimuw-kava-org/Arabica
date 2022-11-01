package com.kava.container;

import com.kava.container.http.HttpVersion;
import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.logger.Logger;
import com.kava.container.logger.LoggerFactory;
import com.kava.container.servlet.KavaServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class HttpClientHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HttpClientHandler.class);

    private static final Pattern HTTP_FIRST_LINE = Pattern.compile("([A-Z]+) ([^ ]+) ([^ ]+)");
    private static final Pattern HTTP_HEADER = Pattern.compile("([^:]+): (.+)");
    private final Socket client;

    private final Map<String, KavaServlet> servlets;

    public HttpClientHandler(Socket client, Map<String, KavaServlet> servlets) {
        this.client = client;
        this.servlets = servlets;
    }

    private void sendMessage(OutputStream outputStream, String message) throws IOException {
        outputStream.write(message.getBytes(StandardCharsets.UTF_8));
    }

    private void sendMessage(OutputStream outputStream, byte[] message) throws IOException {
        outputStream.write(message);
    }

    private String joinHeaders(HttpHeaders headers) {
        return headers.map().entrySet().stream()
                .map(entry -> format("%s: %s", entry.getKey(), String.join(", ", entry.getValue())))
                .collect(Collectors.joining("\r\n"));
    }

    @Override
    public void run() {
        BufferedReader input = null;
        OutputStream output = null;
        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = client.getOutputStream();

            try {
                var kavaHttpRequest = readRequest(input);
                var kavaHttpResponse = new KavaHttpResponse();
                kavaHttpResponse.modifyHeaders().put("Content-Type", List.of("text/html"));
                kavaHttpResponse.modifyHeaders().put("Connection", List.of("keep-alive"));

                logger.info("Starting request: " + kavaHttpRequest.method() + " " + kavaHttpRequest.uri());
                processRequest(kavaHttpRequest, kavaHttpResponse);
                logger.info("Ending request: " + kavaHttpResponse.statusCode());

                String headers = joinHeaders(kavaHttpResponse.headers());

                sendMessage(output, format(
                        "%s %d %s\r\n%s\r\n\r\n",
                        HttpVersion.of(kavaHttpResponse.version()),
                        kavaHttpResponse.statusCode(),
                        "OK",
                        headers));

                if (kavaHttpResponse.hasRawBody()) {
                    sendMessage(output, kavaHttpResponse.rawBody());
                } else if (kavaHttpResponse.hasBody()) {
                    sendMessage(output, kavaHttpResponse.body());
                }

                output.flush();
                client.close();

                logger.debug("Closing connection");
            }
            catch (Exception e) {
                logger.error(e, "Error while handling client");
            }
        }
        catch (IOException e) {
            logger.error(e, "I don't think we really care.");
        }
    }

    private void processRequest(KavaHttpRequest request, KavaHttpResponse response) {
        try {
            for (Map.Entry<String, KavaServlet> entry : servlets.entrySet()) {
                var uri = entry.getKey();
                var handler = entry.getValue();
                if (uri.equals(request.uri().getPath())) {
                    var httpMethod = request.method();
                    var methodName = "do" + httpMethod;
                    var handlerClass = handler.getClass();
                    var handlerMethod = Arrays.stream(handlerClass.getMethods())
                            .filter(method -> method.getName().equals(methodName))
                            .findAny().orElseThrow();
                    handlerMethod.invoke(handler, request, response);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private HTTPFirstLine readFirstLine(BufferedReader input) throws IOException {
        var firstLine = input.readLine();
        var matcher = HTTP_FIRST_LINE.matcher(firstLine);
        if (!matcher.matches()) throw new IllegalArgumentException();
        var method = matcher.group(1);
        var url = matcher.group(2);
        var version = matcher.group(3);
        return new HTTPFirstLine(method, url, version);
    }

    private String readBody(BufferedReader input, Map<String, List<String>> headers) throws IOException {
        var lengths = headers.get("Content-Length");
        if (lengths == null || lengths.size() != 1) return "";

        StringBuilder bodyBuilder = new StringBuilder();
        for (int bodySize = Integer.parseInt(lengths.get(0)); bodySize > 0; bodySize--) {
            bodyBuilder.append((char) input.read());
        }

        return bodyBuilder.toString();
    }

    private KavaHttpRequest readRequest(BufferedReader input) throws IOException, URISyntaxException {
        var firstLine = readFirstLine(input);
        var headers = new HashMap<String, List<String>>();

        String lastLine = null;
        while (lastLine == null || !lastLine.isEmpty()) {
            lastLine = input.readLine();
            var headerMatcher = HTTP_HEADER.matcher(lastLine);
            if (headerMatcher.matches()) {
                var headerName = headerMatcher.group(1);
                var headerValue = headerMatcher.group(2);

                headers.computeIfAbsent(headerName, s -> new ArrayList<>()).add(headerValue);
            }
        }

        String body = readBody(input, headers);

        return new KavaHttpRequest(
                firstLine.method, firstLine.url, firstLine.version, body,
                HttpHeaders.of(headers, (s, s2) -> true)
        );
    }

    private record HTTPFirstLine(String method, String url, String version) {}
}
