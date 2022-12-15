package org.kava.arabica;

import lombok.AccessLevel;
import lombok.Getter;
import org.kava.arabica.async.Client;
import org.kava.arabica.async.CyclicBuffer;
import org.kava.arabica.async.HttpParser;
import org.kava.arabica.http.ArabicaHttpRequest;
import org.kava.arabica.http.ArabicaHttpResponse;
import org.kava.arabica.http.HttpVersion;
import org.kava.arabica.servlet.ArabicaServlet;
import org.kava.arabica.servlet.ArabicaServletURI;
import org.kava.arabica.utils.PropertyLoader;
import org.kava.lungo.Level;
import org.kava.lungo.Logger;
import org.kava.lungo.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ServletContainer {

    public static final int DEFAULT_PORT = 40301;
    public static final Integer WORKERS = PropertyLoader.loadInteger("arabica.container.workers", 10);

    private final Logger logger = LoggerFactory.getLogger(ServletContainer.class);
    private final ExecutorService executorService;
    private final Map<String, ArabicaServlet> servlets = new HashMap<>();
    private final int port;
    @Getter(AccessLevel.PRIVATE)
    private final ReentrantLock lock = new ReentrantLock();

    private final ConcurrentHashMap<SocketChannel, Client> clients = new ConcurrentHashMap<>();
    private volatile boolean stopped = true;

    public ServletContainer(int port) {
        this.executorService = Executors.newFixedThreadPool(WORKERS);
        this.port = port;

        logger.trace("Created server at port '%d' with '%d' workers", port, WORKERS);
    }

    public ServletContainer() {
        this(DEFAULT_PORT);
    }

    public void registerServlet(Class<? extends ArabicaServlet> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArabicaServlet servlet = clazz.getDeclaredConstructor().newInstance();
        var isServlet = clazz.isAnnotationPresent(ArabicaServletURI.class);
        if (isServlet) {
            var uri = clazz.getAnnotation(ArabicaServletURI.class);
            logger.info("Registering new servlet: '%s' '%s'", uri.value(), servlet);
            servlets.put(uri.value(), servlet);
        } else {
            logger.error("Servlet '%s' is not annotated with '%s'. Skipping.", clazz.getName(), ArabicaServletURI.class.getName());
        }
    }

    public void registerIcon(String path) {
        logger.info("Registering icon: '%s'", path);
        servlets.put("/favicon.ico", new IconServlet(path));
    }


    public void start() throws IOException, InterruptedException {
        setStopped(false);

        try (var selector = Selector.open()) {
            try (var channel = ServerSocketChannel.open()) {
                channel.configureBlocking(false);
                channel.bind(new InetSocketAddress(port));
                channel.register(selector, SelectionKey.OP_ACCEPT);

                logger.info("Server started on port %d", port);

                while (!getStopped()) {
                    handleSelectors(selector, channel);
                    parseRequests(selector);
                }
            }
        } finally {
            setStopped(true);
        }
    }

    private void parseRequests(Selector selector) throws ClosedChannelException {
        logger.trace("Parsing requests");
        Set<Client> toRemove = new HashSet<>();
        for (var client : clients.values()) {
            logger.trace("Parsing request for client %s", client);
            if (client.getParser().isReady()) {
                continue;
            }
            try {
                client.getParser().parse(client);
            } catch (Exception e) {
                toRemove.add(client);
            }
        }
        logger.trace("Parsing requests finished");

        clients.values().removeAll(toRemove);

        for (var clientEntry : clients.entrySet()) {
            var client = clientEntry.getValue();
            var channel = clientEntry.getKey();

            if (client.getParser().isReady() && !client.isHandled() && !client.sentToBeHandled()) {
                var method = client.getParser().getMethod();
                var path = client.getParser().getPath();
                var version = client.getParser().getVersion();

                var headers = client.getParser().getHeaders();
                var body = client.getParser().getBody();

                logger.info("Received request: %s %s %s", method, path, version);
                logger.info("Headers: %s", headers);
                logger.info("Body: %s", body != null ? new String(body) : "null");

                executorService.submit(() -> {
                    try {
                        logger.debug("Handling request: %s %s %s", method, path, version);
                        handleHTTP(client.getParser(), client.getOutput());
                        logger.debug("Request handled");
                        channel.register(selector, SelectionKey.OP_WRITE);
                        selector.wakeup();
                        logger.debug("Registered client %s for writing", client);
                    } catch (URISyntaxException | InvocationTargetException | IllegalAccessException |
                             ClosedChannelException e) {
                        logger.error("Error handling request: %s %s %s", method, path, version);
                        throw new RuntimeException(e);
                    } catch (RuntimeException e) {
                        logger.error("EEEEEEError handling request: %s %s %s", method, path, version);
                        logger.error("%s", e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                    client.setHandled(true);
                });

                client.setSentToBeHandled(true);
            }
            else if (client.isHandled() && !client.isRegistered()) {
                channel.register(selector, SelectionKey.OP_WRITE);
            }
        }
    }

    private void handleHTTP(HttpParser parser, CyclicBuffer output) throws URISyntaxException, InvocationTargetException, IllegalAccessException {
        var method = parser.getMethod();
        var path = parser.getPath();

        var servlet = servlets.get(path);
        if (servlet == null) {
            logger.error("No servlet found for path '%s'", path);
            return;
        }

        var request = generateRequest(parser);
        var response = new ArabicaHttpResponse();
        response.modifyHeaders().put("Content-Type", List.of("text/html"));
        response.modifyHeaders().put("Connection", List.of("keep-alive"));

        var methodName = "do" + method;
        var handlerClass = servlet.getClass();
        var handlerMethod = Arrays.stream(handlerClass.getMethods())
                .filter(_method -> _method.getName().equals(methodName))
                .findAny().orElseThrow();
        handlerMethod.invoke(servlet, request, response);

        writeResponseToBuffer(response, output);
    }

    private void writeResponseToBuffer(ArabicaHttpResponse response, CyclicBuffer output) {
        var CRLF = "\r\n".getBytes();

        var firstLine = String.format("%s %d %s", HttpVersion.of(response.version()), response.statusCode(), "OK");
        output.putExtend(firstLine.getBytes());
        output.putExtend(CRLF);

        var headers = joinHeaders(response.headers());
        output.putExtend(headers.getBytes());
        output.putExtend(CRLF);
        output.putExtend(CRLF);

        if (response.hasRawBody()) {
            output.putExtend(response.rawBody());
        } else if (response.hasBody()) {
            output.putExtend(response.body().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String joinHeaders(HttpHeaders headers) {
        return headers.map().entrySet().stream()
                .map(entry -> format("%s: %s", entry.getKey(), String.join(", ", entry.getValue())))
                .collect(Collectors.joining("\r\n"));
    }

    private ArabicaHttpRequest generateRequest(HttpParser parser) throws URISyntaxException {
        var method = parser.getMethod();
        var path = parser.getPath();
        var version = parser.getVersion();

        var headers = parser.getHeaders();
        var body = parser.getBody();

        var parsedHeaders = HttpHeaders.of(headers, (k, v) -> true);
        return new ArabicaHttpRequest(method, path, version, parsedHeaders, body);
    }

    private void handleSelectors(Selector selector, ServerSocketChannel channel) throws IOException {
        if (selector.select() <= 0) {
            return;
        }

        logger.trace("Selector selected - checking keys");

        var selectionKeys = selector.selectedKeys();
        var iterator = selectionKeys.iterator();
        while (iterator.hasNext()) {
            logger.trace("Checking key");
            var key = iterator.next();
            iterator.remove();

            var clientChannel = key.channel();

            if (key.isAcceptable()) {
                logger.trace("Acceptable");
                handleAccept(channel, selector);
            }

            if (clientChannel.isOpen() && key.isReadable()) {
                logger.trace("Readable");
                handleRead((SocketChannel) clientChannel);
            }

            if (clientChannel.isOpen() && key.isWritable()) {
                logger.trace("Writable");
                handleWrite((SocketChannel) clientChannel);
            }
            logger.trace("Key handled");
        }
        logger.trace("Selector checked");
    }

    private void handleWrite(SocketChannel channel) throws IOException {
        var currentBuffer = clients.get(channel).getOutput();
        logger.trace("Can write %d bytes", currentBuffer.getUsedSpace());
        if (currentBuffer.getUsedSpace() == 0) {
            return;
        }

        logger.trace("Writing to channel %s", channel);

        try {
            var buffer = currentBuffer.getBuffer();
            int howManySent = channel.write(buffer);
            logger.trace("Sent %d bytes to channel %s", howManySent, channel);
            currentBuffer.drop(howManySent);
        } catch (IOException e) {
            logger.error("Error writing to channel %s", channel);
            removeChannel(channel);
        }

        if (currentBuffer.getUsedSpace() == 0) {
            removeChannel(channel);
        }
    }

    private void handleRead(SocketChannel channel) throws IOException {
        var currentBuffer = clients.get(channel).getInput();
        logger.debug("Current buffer: %s", currentBuffer);
        ByteBuffer bb = ByteBuffer.allocate(currentBuffer.getFreeSpace());
        int howManyRead = channel.read(bb);

        switch (howManyRead) {
            case 0 -> logger.trace("Nothing read");
            case -1 -> {
                logger.trace("Closed connection with %s", channel.getRemoteAddress());
                removeChannel(channel);
            }
            default -> {
                currentBuffer.putExtend(bb.array(), howManyRead);
                String result = howManyRead > 0 ? new String(bb.array(), 0, howManyRead) : "<empty>";
                logger.trace("Received: %s with length: %d from %s", escape(result), howManyRead, channel.getRemoteAddress());
            }
        }
    }

    private void removeChannel(SocketChannel channel) throws IOException {
        clients.remove(channel);
        channel.close();
    }

    private String escape(String str) {
        return str.replace("\r", "\\r").replace("\n", "\\n");
    }

    private void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        var channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        logger.debug("Accepted connection from %s", channel.getRemoteAddress());

        clients.put(channel, new Client(1024));
    }

    public void stop() {
        setStopped(true);
    }

    private boolean getStopped() {
        return operateUnderLock(() -> this.stopped);
    }

    private void setStopped(boolean stopped) {
        operateUnderLock(() -> this.stopped = stopped);
    }

    private <T> T operateUnderLock(Supplier<T> supplier) {
        lock.lock();
        T t = supplier.get();
        lock.unlock();
        return t;
    }
}
