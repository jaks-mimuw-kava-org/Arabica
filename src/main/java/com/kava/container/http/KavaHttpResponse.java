package com.kava.container.http;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Optional;

public class KavaHttpResponse implements HttpResponse<String> {
    private int statusCode;
    private HttpRequest request;
    private String body;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public int statusCode() {
        return this.statusCode;
    }

    @Override
    public HttpRequest request() {
        return this.request;
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return HttpHeaders.of(new HashMap<>(), (s, s2) -> true);
    }

    @Override
    public String body() {
        return this.body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return this.request.uri();
    }

    @Override
    public HttpClient.Version version() {
        return HttpVersion.getDefault();
    }
}
