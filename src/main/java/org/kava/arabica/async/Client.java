package org.kava.arabica.async;

import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private final CyclicBuffer input;
    private final CyclicBuffer output;
    private final HttpParser parser = new HttpParser();
    private final AtomicBoolean handled = new AtomicBoolean(false);
    private final AtomicBoolean registered = new AtomicBoolean(false);
    private boolean sentToBeHandled = false;

    public Client(int bufferSize) {
        input = new CyclicBuffer(bufferSize);
        output = new CyclicBuffer(bufferSize);
    }

    public CyclicBuffer getInput() {
        return input;
    }

    public CyclicBuffer getOutput() {
        return output;
    }

    public HttpParser getParser() {
        return parser;
    }

    public boolean isHandled() {
        return this.handled.get();
    }

    public void setHandled(boolean handled) {
        this.handled.set(handled);
    }

    public boolean isRegistered() {
        return registered.get();
    }

    public void setRegistered(boolean registered) {
        this.registered.set(registered);
    }

    public boolean sentToBeHandled() {
        return sentToBeHandled;
    }

    public void setSentToBeHandled(boolean sentToBeHandled) {
        this.sentToBeHandled = sentToBeHandled;
    }
}
