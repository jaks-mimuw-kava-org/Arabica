package org.kava.arabica.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

class HttpVersionTest {
    @Test
    @DisplayName("Test HTTP Version constants")
    void testHttpProtocolConstants() {
        assertEquals("HTTP/1.1", HttpVersion.HTTP_1_1);
        assertEquals("HTTP/2", HttpVersion.HTTP_2);
    }

    @Test
    @DisplayName("Test static `of::(String)` constructor")
    void testOfStaticConstructor() {
        assertEquals(HttpClient.Version.HTTP_1_1, HttpVersion.of("HTTP/1.1"));
        assertEquals(HttpClient.Version.HTTP_2, HttpVersion.of("HTTP/2"));
        assertNull(HttpVersion.of("HTTP/3"));
    }

    @Test
    @DisplayName("Test HttpClient.Version to String conversion")
    void testHttpClientVersionToString() {
        assertEquals("HTTP/1.1", HttpVersion.of(HttpClient.Version.HTTP_1_1));
        assertEquals("HTTP/2", HttpVersion.of(HttpClient.Version.HTTP_2));
    }
}
