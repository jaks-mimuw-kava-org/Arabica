package org.kava.arabica;

import org.kava.arabica.servlet.ArabicaServletURI;
import org.kava.lungo.Logger;
import org.kava.lungo.LoggerFactory;
import org.kava.arabica.servlet.ArabicaServlet;
import org.kava.arabica.utils.PropertyLoader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServletContainer {

    public static final Integer WORKERS = PropertyLoader.loadInteger("arabica.container.workers", 10);

    private final Logger logger = LoggerFactory.getLogger(ServletContainer.class);

    private final ServerSocket socket;

    private final ExecutorService executorService;

    private final Map<String, ArabicaServlet> servlets = new HashMap<>();

    public ServletContainer(int port) throws Exception {
        this.socket = new ServerSocket(port);
        this.executorService = Executors.newFixedThreadPool(WORKERS);

        logger.trace("Created server at port '%d' with '%d' workers", port, WORKERS);
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

    public void run() {
        logger.info("Starting server at port: " + this.socket.getLocalPort());
        while (true) {
            try {
                var client = this.socket.accept();
                this.executorService.submit(new HttpClientHandler(client, servlets));
            } catch (IOException e) {
                logger.error("Exception was caught. Stopping server. %s", e.getMessage());
                break;
            }
        }
    }
}
