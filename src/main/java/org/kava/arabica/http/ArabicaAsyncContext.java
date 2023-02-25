package org.kava.arabica.http;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArabicaAsyncContext implements AsyncContext {

    private final ArabicaHttpRequest request;

    private final ArabicaHttpResponse response;

    private long timeout = 10000;

    private final List<AsyncListener> listeners = new ArrayList<>();

    public ArabicaAsyncContext(ServletRequest req, ServletResponse res) {
        this.request = (ArabicaHttpRequest) req;
        this.response = (ArabicaHttpResponse) res;
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
        this.request.setAsyncStarted(false);
        for (AsyncListener listener : listeners) {
            try {
                listener.onComplete(new AsyncEvent(this, this.request, this.response));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        synchronized (this.response) {
            response.notify();
        }
    }

    @Override
    public void start(Runnable runnable) {
        this.request.setAsyncStarted(true);
        for (AsyncListener listener : listeners) {
            try {
                listener.onStartAsync(new AsyncEvent(this, this.request, this.response));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        new Thread(runnable).start();
    }

    @Override
    public void addListener(AsyncListener asyncListener) {
        listeners.add(asyncListener);
    }

    @Override
    public void addListener(AsyncListener asyncListener, ServletRequest servletRequest, ServletResponse servletResponse) {
        listeners.add(asyncListener);
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> aClass) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }
}
