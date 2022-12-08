package org.kava.arabica;

import org.kava.arabica.http.HttpVersion;
import org.kava.arabica.http.ArabicaHttpRequest;
import org.kava.arabica.http.ArabicaHttpResponse;
import org.kava.lungo.Logger;
import org.kava.lungo.LoggerFactory;
import org.kava.arabica.servlet.ArabicaServlet;

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

    private final Map<String, ArabicaServlet> servlets;

    public HttpClientHandler(Socket client, Map<String, ArabicaServlet> servlets) {
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
                var arabicaHttpRequest = readRequest(input);
                var arabicaHttpResponse = new ArabicaHttpResponse();
                arabicaHttpResponse.modifyHeaders().put("Content-Type", List.of("text/html"));
                arabicaHttpResponse.modifyHeaders().put("Connection", List.of("keep-alive"));

                logger.info("Starting request: " + arabicaHttpRequest.method() + " " + arabicaHttpRequest.uri());
                processRequest(arabicaHttpRequest, arabicaHttpResponse);
                logger.info("Ending request: " + arabicaHttpResponse.statusCode());

                String headers = joinHeaders(arabicaHttpResponse.headers());

                sendMessage(output, format(
                        "%s %d %s\r\n%s\r\n\r\n",
                        HttpVersion.of(arabicaHttpResponse.version()),
                        arabicaHttpResponse.statusCode(),
                        "OK",
                        headers));

                if (arabicaHttpResponse.hasRawBody()) {
                    sendMessage(output, arabicaHttpResponse.rawBody());
                } else if (arabicaHttpResponse.hasBody()) {
                    sendMessage(output, arabicaHttpResponse.body());
                }

                output.flush();
                client.close();

                logger.debug("Closing connection");
            }
            catch (Exception e) {
                logger.error("Error while handling client: %s", e.getMessage());
            }
        }
        catch (IOException e) {
            logger.error("I don't think we really care. %s", e.getMessage());
        }
    }

    private void processRequest(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        try {
            for (Map.Entry<String, ArabicaServlet> entry : servlets.entrySet()) {
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

    private ArabicaHttpRequest readRequest(BufferedReader input) throws IOException, URISyntaxException {
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

        return new ArabicaHttpRequest(
                firstLine.method, firstLine.url, firstLine.version, body,
                HttpHeaders.of(headers, (s, s2) -> true)
        );
    }

    private record HTTPFirstLine(String method, String url, String version) {}
}
