package org.kava.arabica;

import org.junit.jupiter.api.Test;
import org.kava.arabica.servlet.ArabicaServlet;
import org.kava.arabica.test.*;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;

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
                        .header("Content-Type", "bytes")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "bytes")
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
                        .header("Content-Type", "bytes")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "bytes")
                        .body("!dlrow ,olleH".getBytes())
                        .build()
        );

        var testCase2 = new HttpTestCase(
                TestRequest.builder()
                        .method("GET")
                        .url("/reverse")
                        .version("HTTP/1.1")
                        .header("Content-Type", "bytes")
                        .header("Content-Length", "13")
                        .body("Hello, world!".getBytes())
                        .build(),
                TestResponse.builder()
                        .version("HTTP/1.1")
                        .status(200)
                        .reason("OK")
                        .header("Connection", "keep-alive")
                        .header("Content-Length", "13")
                        .header("Content-Type", "bytes")
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
}
