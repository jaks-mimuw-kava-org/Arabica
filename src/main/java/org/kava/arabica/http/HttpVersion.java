package org.kava.arabica.http;

import java.net.http.HttpClient;

public class HttpVersion {
    private HttpVersion() {}

    public static final String HTTP_1_1 = "HTTP/1.1";
    public static final String HTTP_2 = "HTTP/2";

    public static HttpClient.Version getDefault() {
        return HttpClient.Version.HTTP_1_1;
    }

    public static HttpClient.Version of(String str) {
        return switch (str) {
            case HTTP_1_1 -> HttpClient.Version.HTTP_1_1;
            case HTTP_2 -> HttpClient.Version.HTTP_2;
            default -> null;
        };
    }

    public static String of(HttpClient.Version version) throws NullPointerException {
        return switch (version) {
            case HTTP_1_1 -> HTTP_1_1;
            case HTTP_2 -> HTTP_2;
        };
    }
}
