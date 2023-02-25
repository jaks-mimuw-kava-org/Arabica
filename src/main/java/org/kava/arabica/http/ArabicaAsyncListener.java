package org.kava.arabica.http;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
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
        logger.info("Asynchronous Servlet Request '%s': completed.", asyncEvent.getAsyncContext().getRequest());
    }

    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        logger.info("Asynchronous Servlet Request '%s': timeout.", asyncEvent.getAsyncContext().getRequest());
    }

    @Override
    public void onError(AsyncEvent asyncEvent) throws IOException {
        logger.error("Asynchronous Servlet Request '%s': error.", asyncEvent.getAsyncContext().getRequest());
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        logger.info("Asynchronous Servlet Request '%s': started.", asyncEvent.getAsyncContext().getRequest(), asyncEvent.getAsyncContext().getRequest());
    }
}
