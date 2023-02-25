package org.kava.arabica.test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class TestClient {

    private Socket socket = null;
    private boolean isConnected = false;

    public TestClient() {
    }

    public void connect(int port) throws IOException {
        this.connect("localhost", port);
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        isConnected = true;
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public int getRemotePort() {
        return socket.getPort();
    }

    public void close() throws IOException {
        socket.close();
        socket = null;
        isConnected = false;
    }

    public void send(byte[] bytes) throws IOException, NotConnectedException {
        this.send(bytes, 0, bytes.length);
    }

    public void send(byte[] bytes, int off, int length) throws IOException, NotConnectedException {
        checkConnection();
        socket.getOutputStream().write(bytes, off, length);
    }

    public void sendSlow(byte[] bytes, int timeout) throws IOException, NotConnectedException {
        checkConnection();
        for (byte b : bytes) {
            socket.getOutputStream().write(b);
            sleep(timeout);
        }
    }

    private void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] recv(final int lengthBytes) throws NotConnectedException, TimeoutException {
        checkConnection();
        var sendTask = CompletableFuture.supplyAsync(() -> {
            try {
                var length = lengthBytes;
                var input = new byte[length];
                int begin = 0;
                while (length > 0) {
                    int size = socket.getInputStream().read(input, begin, length);
                    if (size < 1) throw new IOException("Connection closed unexpectedly.");
                    begin += size;
                    length -= size;
                }
                return input;
            } catch (Throwable e) {
                // CompletableFuture doesn't allow throwing checked exceptions,
                //  so we have to wrap it in a CompletionException.
                // This class is used for testing purposes only, so it's fine
                throw new CompletionException(e);
            }
        });
        try {
            // The server task will be cancelled if it takes more than 2 seconds.
            // This prevents from blocking the test forever
            //  as server could send fewer bytes than expected.
            return sendTask.orTimeout(30, TimeUnit.SECONDS).get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConnection() throws NotConnectedException {
        if (!isConnected) throw new NotConnectedException();
    }
}
