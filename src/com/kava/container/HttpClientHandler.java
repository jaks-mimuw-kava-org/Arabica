package com.kava.container;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.logger.Logger;
import com.kava.container.logger.LoggerFactory;
import com.kava.container.servlet.KavaServlet;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpClientHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HttpClientHandler.class);

    private static final Pattern HTTP_FIRST_LINE = Pattern.compile("([A-Z]+) ([^ ]+) ([^ ]+)");
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
            var firstLine = input.readLine();
            var matcher = HTTP_FIRST_LINE.matcher(firstLine);
            if (!matcher.matches()) throw new IllegalArgumentException();
            var method = matcher.group(1);
            var url = matcher.group(2);
            var version = matcher.group(3);
            String lastLine = null;
            int bodySize = 0;
            while (lastLine == null || !lastLine.isEmpty()) {
                lastLine = input.readLine();
                if (lastLine.startsWith("Content-Length: ")) {
                    bodySize = Integer.parseInt(lastLine.replaceFirst("Content-Length: ", ""));
                }
            }
            StringBuilder bodyB = new StringBuilder();
            while (bodySize > 0) {
                bodyB.append((char) input.read());
                bodySize--;
            }
            String body = bodyB.toString();

            try {
                var kavaHttpRequest = new KavaHttpRequest(method, url, version, body);
                var kavaHttpResponse = new KavaHttpResponse();

                logger.info("Starting request: " + method + " " + url);
                processRequest(kavaHttpRequest, kavaHttpResponse);
                logger.info("Ending request: " + kavaHttpResponse.statusCode());

                output.write(String.format(
                        "%s %d %s\r\nConnection: close\r\nContent-Type: text/html\r\n\r\n%s",
                        parseVersion(kavaHttpResponse.version()),
                        kavaHttpResponse.statusCode(),
                        "OK",
                        kavaHttpResponse.body()));

                output.flush();
                client.close();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        catch (IOException e) {
            System.err.println("I don't think we really care.");
        } finally {
            closeIfNotNull(input);
            closeIfNotNull(output);
        }
    }

    private String parseVersion(HttpClient.Version version) {
        return switch (version) {
            case HTTP_1_1 -> "HTTP/1.1";
            case HTTP_2 -> "HTTP/2";
        };
    }

    private void closeIfNotNull(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                System.err.println("Oh my god, total disaster! Like we care at all.");
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
}
