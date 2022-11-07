package com.kava.arabica.http;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArabicaHttpResponse implements HttpResponse<String> {
    private int statusCode;
    private HttpRequest request;
    private String body;

    private byte[] rawBody;

    private final Map<String, List<String>> headers = new HashMap<>();

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
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
        return HttpHeaders.of(headers, (s, s2) -> true);
    }

    public Map<String, List<String>> modifyHeaders() {
        return this.headers;
    }

    @Override
    public String body() {
        return this.body;
    }

    public byte[] rawBody() {
        return this.rawBody;
    }

    public boolean hasRawBody() {
        return this.rawBody != null;
    }

    public boolean hasBody() {
        return this.body != null;
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
