package org.kava.arabica.utils;

import java.io.IOException;

@FunctionalInterface
public interface IOThrowingSupplier<T> {
    T get() throws IOException;
}
