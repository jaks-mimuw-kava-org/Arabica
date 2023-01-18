package org.kava.arabica.test;

import org.kava.arabica.utils.ThrowingRunnable;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestUtils {
    public static <T> T noThrow(ThrowingCallable<T> callable) {
        AtomicReference<T> result = new AtomicReference<>();
        assertDoesNotThrow(() -> result.set(callable.call()));
        return result.get();
    }

    public static void noThrow(ThrowingRunnable runnable) {
        assertDoesNotThrow(runnable::run);
    }
}
