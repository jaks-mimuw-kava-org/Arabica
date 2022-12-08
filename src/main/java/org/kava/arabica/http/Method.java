package org.kava.arabica.http;

import java.util.Arrays;

public enum Method {
    GET,
    POST,
    OTHER;

    public static Method of(String method) {
        return Arrays.stream(Method.values())
                .filter(_method -> _method.toString().equals(method))
                .findAny()
                .orElse(OTHER);
    }
}
