package org.kava.arabica.test;

import org.kava.arabica.utils.ThrowingRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.kava.arabica.utils.StringFormatter.args;
import static org.kava.arabica.utils.StringFormatter.named;

public record HttpTestCase(TestRequest request, TestResponse response) {

    public static <T> T noThrow(ThrowingCallable<T> callable) {
        AtomicReference<T> result = new AtomicReference<>();
        assertDoesNotThrow(() -> result.set(callable.call()));
        return result.get();
    }

    public static void noThrow(ThrowingRunnable runnable) {
        assertDoesNotThrow(runnable::run);
    }

    public void run(TestEnvironment env) {
        var client = env.testClients()[0];
        var port = env.port();

        { // Sending the request
            sendLine(client, named("${method} ${url} ${version}", args(
                    "method", this.request().method(),
                    "url", this.request().url(),
                    "version", this.request().version()
            )));

            for (var entry : this.request().headers().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                for (var value : entry.getValue()) {
                    sendLine(client, named("${key}: ${value}", args(
                            "key", entry.getKey(),
                            "value", value
                    )));
                }
            }

            sendLine(client, "");
            send(client, this.request().body());
        }
        { // Checking the result
            var expected = named("${version} ${status} ${reason}\r\n", args(
                    "version", this.response().version(),
                    "status", this.response().status(),
                    "reason", this.response().reason()
            ));
            var response = noThrow(() -> client.recv(expected.length()));
            assertEquals(expected, new String(response));
        }
        {
            var expectedHeaders = this.response().headers().entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream().map(value -> named("${key}: ${value}\r\n", args(
                            "key", entry.getKey(),
                            "value", value
                    ))))
                    .map(String::getBytes)
                    .toArray(byte[][]::new);
            Arrays.sort(expectedHeaders, new BytesComparator());
            var lengthSum = Arrays.stream(expectedHeaders).map(h -> h.length).reduce(Integer::sum).orElse(0);
            noThrow(() -> {
                var headers = client.recv(lengthSum);
                var headersRecv = splitByCRLF(headers).orElseThrow(() -> new RuntimeException("Invalid headers"));
                Arrays.sort(headersRecv, new BytesComparator());
                assertArrayEquals(expectedHeaders, headersRecv);
            });
        }
        {
            var expectedEndOfHeaders = new byte[] {
                    '\r', '\n'
            };
            noThrow(() -> {
                var separator = client.recv(expectedEndOfHeaders.length);
                assertArrayEquals(expectedEndOfHeaders, separator);
            });
        }
        {
            var expectedBody = this.response().body();
            noThrow(() -> {
                var body = client.recv(expectedBody.length);
                assertArrayEquals(expectedBody, body);
            });
        }
    }

    public void send(TestClient client, String message) {
        noThrow(() -> client.send(message.getBytes()));
    }

    public void send(TestClient client, byte[] message) {
        noThrow(() -> client.send(message));
    }

    public void sendLine(TestClient client, String message) {
        noThrow(() -> client.send((message + "\r\n").getBytes()));
    }

    public void sendLine(TestClient client, byte[] message) {
        noThrow(() -> {
            client.send(message);
            client.send(new byte[]{'\r', '\n'});
        });
    }

    // TODO: This should be rewritten
    // IDEA: search until '\r\n'. Create byte[] of already known size. Move the bytes. Repeat until end.
    private Optional<byte[][]> splitByCRLF(byte[] arr) {
        var result = new ArrayList<List<Byte>>();
        var it = 0;

        while (it < arr.length) {
            var list = new ArrayList<Byte>();
            while (it + 1 < arr.length && arr[it] != '\r' && arr[it + 1] != '\n') {
                list.add(arr[it]);
                it++;
                if (it >= arr.length) {
                    return Optional.empty();
                }
            }
            if (it + 1 >= arr.length) {
                return Optional.empty();
            }
            list.add(arr[it]);
            list.add(arr[it + 1]);
            it += 2;
            result.add(list);
        }

        return Optional.of(result.stream().map(l -> {
            var arr2 = new byte[l.size()];
            for (var i = 0; i < l.size(); i++) {
                arr2[i] = l.get(i);
            }
            return arr2;
        }).toArray(byte[][]::new));
    }

    private static class BytesComparator implements Comparator<byte[]> {

        @Override
        public int compare(byte[] arr1, byte[] arr2) {
            if (arr1.length != arr2.length) {
                return arr1.length - arr2.length;
            }
            int len = arr1.length;
            for (int i = 0; i < len; i++) {
                if (arr1[i] != arr2[i]) {
                    return arr1[i] - arr2[i];
                }
            }
            return 0;
        }
    }
}
