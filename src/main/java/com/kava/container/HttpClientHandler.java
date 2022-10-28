package com.kava.container;

import com.kava.container.http.HttpVersion;
import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.http.Method;
import com.kava.container.logger.Logger;
import com.kava.container.logger.LoggerFactory;
import com.kava.container.servlet.KavaServlet;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
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

    @Override
    public void run() {
        BufferedReader input = null;
        BufferedWriter output = null;
        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

            try {
                var kavaHttpRequest = readRequest(input);
                var kavaHttpResponse = new KavaHttpResponse();

                logger.info("Starting request: " + kavaHttpRequest.method() + " " + kavaHttpRequest.uri());
                processRequest(kavaHttpRequest, kavaHttpResponse);
                logger.info("Ending request: " + kavaHttpResponse.statusCode());

                // TODO: refactor - make it work for now
                String headers = kavaHttpResponse.headers().map().entrySet().stream().map(stringListEntry -> {
                    var header = stringListEntry.getKey();
                    var sb = new StringBuilder();
                    for (var value : stringListEntry.getValue()) {
                        sb.append(header).append(": ").append(value).append("\r\n");
                    }
                    return sb.toString();
                }).collect(Collectors.joining());

                output.write(format(
                        "%s %d %s\r\nConnection: keep-alive\r\nContent-Type: text/html\r\n%s\r\n\r\n",
                        HttpVersion.of(kavaHttpResponse.version()),
                        kavaHttpResponse.statusCode(),
                        "OK",
                        headers));

                if (kavaHttpResponse.hasRawBody()) {
                    client.getOutputStream().write(kavaHttpResponse.rawBody());
                    client.getOutputStream().flush();
                } else {
                    output.write(kavaHttpResponse.body());
                    output.flush();
                }

                // client.close();

                logger.debug("Closing connection");
            }
            catch (Exception e) {
                logger.error(e, "Error while handling client");
            }
        }
        catch (IOException e) {
            logger.error(e, "I don't think we really care.");
        } finally {
            closeIfNotNull(input);
            closeIfNotNull(output);
        }
    }

    private void closeIfNotNull(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.error(e, "Oh my god, total disaster! Like we care at all.");
            }
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
