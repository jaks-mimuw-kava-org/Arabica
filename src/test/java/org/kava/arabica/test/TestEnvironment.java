package org.kava.arabica.test;

import org.kava.arabica.ServletContainer;
import org.kava.arabica.servlet.ArabicaServlet;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;

public record TestEnvironment(ServletContainer servletContainer, CompletableFuture<Boolean> serverTask, TestClient[] testClients, int port) {
    public static TestEnvironment newEnv(int port, int workersCount, Class<? extends ArabicaServlet>[] servlets) throws EnvSetupException {
        try {
            var servletContainer = new ServletContainer(port);
            for (var servlet : servlets) {
                servletContainer.registerServlet(servlet);
            }

            final CyclicBarrier barrier = new CyclicBarrier(2);
            servletContainer.setOnStart(barrier::await);

            var serverTask = CompletableFuture.supplyAsync(() -> {
                try {
                    servletContainer.start();
                    return Boolean.TRUE;
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            });
            barrier.await();

            // Setting the real port if it was 0
            port = servletContainer.getPort();

            var testClients = new TestClient[workersCount];
            for (int i = 0; i < workersCount; i++) {
                testClients[i] = new TestClient();
                testClients[i].connect(port);
            }

            return new TestEnvironment(
                    servletContainer,
                    serverTask,
                    testClients,
                    port
            );
        } catch (Exception e) {
            throw new EnvSetupException("Environment couldn't be setup properly.", e);
        }
    }
}
