package org.kava.arabica;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;
import org.junit.jupiter.api.Test;
import org.kava.arabica.test.*;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kava.arabica.test.TestUtils.noThrow;

class ServletContainerTest {

    @Test
    void testSingleRequestResponse() {
        var env = noThrow(() -> TestEnvironment.newEnv(0, 1, List.of(TestServlet.class)));

        var testCase = new HttpTestCase(
                TestRequest.builder()
                        .method("GET")
                        .url("/reverse")
                        .version("HTTP/1.1")
                        .header("Content-Type", "text/plain")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "text/plain")
                        .body("!dlrow ,olleH".getBytes())
                        .build()
        );

        testCase.run(env, 0);

        env.servletContainer().stop();
        assertTrue(noThrow(() -> env.serverTask().get()));
    }

    @Test
    void testMultipleRequests() {
        var env = noThrow(() -> TestEnvironment.newEnv(0, 2, List.of(TestServlet.class)));

        var testCase = new HttpTestCase(
                TestRequest.builder()
                        .method("GET")
                        .url("/reverse")
                        .version("HTTP/1.1")
                        .header("Content-Type", "text/plain")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "text/plain")
                        .body("!dlrow ,olleH".getBytes())
                        .build()
        );

        var testCase2 = new HttpTestCase(
                TestRequest.builder()
                        .method("GET")
                        .url("/reverse")
                        .version("HTTP/1.1")
                        .header("Content-Type", "text/plain")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "text/plain")
                        .body("!dlrow ,olleH".getBytes())
                        .build()
        );

        var barrier = new CyclicBarrier(2);

        var client0Task = CompletableFuture.supplyAsync(() -> {
            noThrow(() -> barrier.await());
            testCase.run(env, 0);
            return Boolean.TRUE;
        });

        var client1Task = CompletableFuture.supplyAsync(() -> {
            noThrow(() -> barrier.await());
            testCase2.run(env, 1);
            return Boolean.TRUE;
        });

        assertTrue(noThrow(() -> client0Task.get()));
        assertTrue(noThrow(() -> client1Task.get()));

        env.servletContainer().stop();
        assertTrue(noThrow(() -> env.serverTask().get()));
    }

    @Test
    void testSingleAsyncRequestResponse() {
        var env = noThrow(() -> TestEnvironment.newEnv(0, 1, List.of(TestServletAsync.class)));

        var testCase = new HttpTestCase(
                TestRequest.builder()
                        .method("GET")
                        .url("/async_reverse")
                        .version("HTTP/1.1")
                        .header("Content-Type", "text/plain")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "text/plain")
                        .body("!dlrow ,olleH".getBytes())
                        .build()
        );

        testCase.run(env, 0);

        env.servletContainer().stop();
        assertTrue(noThrow(() -> env.serverTask().get()));
    }

//    @Test
//    void testMultipleAsyncRequests() {
//        var numberOfRequests = 100;
//        var env = noThrow(() -> TestEnvironment.newEnv(0, numberOfRequests, List.of(TestServletAsync.class)));
//
//        var clientTasks = new ArrayList<CompletableFuture<Boolean>>();
//
//        for (var i = 0; i < numberOfRequests; i++) {
//            var testCase = new HttpTestCase(
//                    TestRequest.builder()
//                            .method("GET")
//                            .url("/async_reverse")
//                            .version("HTTP/1.1")
//                            .header("Content-Type", "text/plain")
//                            .header("Content-Length", "4")
//                            .body("BABA".getBytes())
//                            .build(),
//                    TestResponse.builder()
//                            .version("HTTP/1.1")
//                            .status(200)
//                            .reason("OK")
//                            .header("Connection", "keep-alive")
//                            .header("Content-Length", "4")
//                            .header("Content-Type", "text/plain")
//                            .body("ABAB".getBytes())
//                            .build()
//            );
//            int finalI = i;
//            var clientTask = CompletableFuture.supplyAsync(() -> {
//                testCase.run(env, finalI);
//                return Boolean.TRUE;
//            });
//
//            clientTasks.add(clientTask);
//        }
//        for (var clientTask : clientTasks) {
//            assertTrue(noThrow(() -> clientTask.get()));
//        }
//
//        env.servletContainer().stop();
//        assertTrue(noThrow(() -> env.serverTask().get()));
//    }

    @Test
    void testMultipleAsyncSleepRequests() {
        var numberOfRequests = 10;
        var env = noThrow(() -> TestEnvironment.newEnv(0, numberOfRequests, List.of(TestServletSleepAsync.class)));

        var clientTasks = new ArrayList<CompletableFuture<Boolean>>();

        for (var i = 0; i < numberOfRequests; i++) {
            var testCase = new HttpTestCase(
                    TestRequest.builder()
                            .method("GET")
                            .url("/async_sleep")
                            .version("HTTP/1.1")
                            .header("Content-Type", "text/plain")
                            .header("Content-Length", "4")
                            .body("BABA".getBytes())
                            .build(),
                    TestResponse.builder()
                            .version("HTTP/1.1")
                            .status(200)
                            .reason("OK")
                            .header("Connection", "keep-alive")
                            .header("Content-Length", "4")
                            .header("Content-Type", "text/plain")
                            .body("ABAB".getBytes())
                            .build()
            );
            int finalI = i;
            var clientTask = CompletableFuture.supplyAsync(() -> {
                testCase.run(env, finalI);
                return Boolean.TRUE;
            });

            clientTasks.add(clientTask);
        }
        for (var clientTask : clientTasks) {
            assertTrue(noThrow(() -> clientTask.get()));
        }

        env.servletContainer().stop();
        assertTrue(noThrow(() -> env.serverTask().get()));
    }

    @Test
    void testMultipleAsyncRequestsAsyncClient() {
        try {
            var servletContainer = new ServletContainer();
            servletContainer.registerServlet(TestServletAsync.class);

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

            var port = servletContainer.getPort();
            var numberOfRequests = 100;
            var responses = new ArrayList<Future>();


            while (numberOfRequests > 0) {
                var client = Dsl.asyncHttpClient();
                var request = client.prepareGet(String.format("http://localhost:%d/async_reverse", port));
                var response = request.execute();
                responses.add(response);
                numberOfRequests--;
            }

            for (var response : responses) {
                response.get();
            }

            servletContainer.stop();
            assertTrue(noThrow(() -> serverTask.get()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
