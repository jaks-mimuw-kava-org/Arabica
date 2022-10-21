package com.kava.container;

import com.kava.container.logger.Logger;
import com.kava.container.logger.LoggerFactory;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServletContainer {

    private final Logger logger = LoggerFactory.getLogger(ServletContainer.class);

    private final ServerSocket socket;

    private final ExecutorService executorService;

    private final Map<String, KavaServlet> servlets = new HashMap<>();

    public ServletContainer(int port) throws Exception {
        this.socket = new ServerSocket(port);
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public void registerServlet(Class<? extends KavaServlet> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        KavaServlet servlet = clazz.getDeclaredConstructor().newInstance();
        var isServlet = clazz.isAnnotationPresent(KavaServletURI.class);
        if (isServlet) {
            var uri = clazz.getAnnotation(KavaServletURI.class);
            logger.info("Registering new servlet: " + uri.value() + " " + servlet);
            servlets.put(uri.value(), servlet);
        }
    }

    public void run() {
        logger.info("Starting server at port: " + this.socket.getLocalPort());
        while (true) {
            try {
                var client = this.socket.accept();
                this.executorService.submit(new HttpClientHandler(client, servlets));
            }
            catch (IOException e) {
                logger.error("Exception was caught. Stopping server. " + e.getMessage());
                break;
            }
        }
    }
}
