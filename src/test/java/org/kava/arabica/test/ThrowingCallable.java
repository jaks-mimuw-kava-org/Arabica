package org.kava.arabica.test;

public interface ThrowingCallable<T> {
    T call() throws Throwable;
}
