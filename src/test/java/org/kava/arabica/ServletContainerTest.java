package org.kava.arabica;

import org.junit.jupiter.api.Test;
import org.kava.arabica.test.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kava.arabica.test.HttpTestCase.noThrow;

class ServletContainerTest {

    @Test
    void testSingleRequestResponse() {
        var env = noThrow(() -> TestEnvironment.newEnv(0, 1, new Class[]{TestServlet.class}));

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

        testCase.run(env);

        env.servletContainer().stop();
        assertTrue(noThrow(() -> env.serverTask().get()));
    }
}
