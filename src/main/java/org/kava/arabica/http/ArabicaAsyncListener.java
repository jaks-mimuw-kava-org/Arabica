package org.kava.arabica.http;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import org.kava.arabica.ServletContainer;
import org.kava.arabica.utils.PropertyLoader;
import org.kava.lungo.Level;
import org.kava.lungo.Logger;
import org.kava.lungo.LoggerFactory;

import java.io.IOException;

public class ArabicaAsyncListener implements AsyncListener {

    public static final Level LOG_LEVEL = PropertyLoader.loadEnum("arabica.container.log.level", Level.DEBUG, Level.class);

    private final Logger logger = LoggerFactory.getLogger(ArabicaAsyncListener.class, LOG_LEVEL);

    @Override
    public void onComplete(AsyncEvent asyncEvent) throws IOException {
        logger.info(String.format("Servlet async completed: '%s'", Thread.currentThread().getName()));
    }

    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        logger.info("Servlet async timeout.");
    }

    @Override
    public void onError(AsyncEvent asyncEvent) throws IOException {
        logger.error("Servlet async error.");
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        logger.info(String.format("Servlet async started: '%s'", Thread.currentThread().getName()));
    }
}
