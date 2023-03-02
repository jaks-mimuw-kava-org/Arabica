package org.kava.arabica.http;

import jakarta.servlet.*;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ArabicaAsyncContext implements AsyncContext {
    private static final long DEFAULT_TIMEOUT = 60 * 1000L;

    private final ArabicaHttpRequest request;

    private final ArabicaHttpResponse response;

    private final List<AsyncListener> listeners;

    private CompletableFuture<Void> task;

    private long timeout;

    public ArabicaAsyncContext(ServletRequest request, ServletResponse response) {
        this.request = (ArabicaHttpRequest) request;
        this.response = (ArabicaHttpResponse) response;
        this.listeners = new ArrayList<>();
        this.timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public ServletRequest getRequest() {
        return request;
    }

    @Override
    public ServletResponse getResponse() {
        return response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return true;
    }

    @Override
    public void dispatch() {

    }

    @Override
    public void dispatch(String s) {

    }

    @Override
    public void dispatch(ServletContext servletContext, String s) {

    }

    @Override
    public void complete() {
        this.response.ready();
        this.request.setAsyncCompleted(true);

        this.task = this.task.thenApply((Void t) -> {
            try {
                this.response.lock();
                this.response.sendToClient();
                this.response.unlock();
            } catch (ClosedChannelException e) {
                throw new RuntimeException(e);
            }
            return null;
        });


        for (var listener : listeners) {
            try {
                listener.onComplete(new AsyncEvent(this));
            }
            catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        this.task = CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return null;
        });
        for (var listener : listeners) {
            try {
                listener.onStartAsync(new AsyncEvent(this));
            }
            catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    @Override
    public void addListener(AsyncListener asyncListener) {
        this.listeners.add(asyncListener);
    }

    @Override
    public void addListener(AsyncListener asyncListener, ServletRequest servletRequest, ServletResponse servletResponse) {
        addListener(asyncListener);
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }
}
