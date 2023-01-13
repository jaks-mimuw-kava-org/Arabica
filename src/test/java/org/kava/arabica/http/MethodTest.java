package org.kava.arabica.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodTest {
    @Test
    @DisplayName("Test static `of` constructor")
    void testOfStaticConstructor() {
        assertEquals(Method.GET, Method.of("GET"));
        assertEquals(Method.POST, Method.of("POST"));
        assertEquals(Method.OTHER, Method.of("OTHER"));
    }
}
