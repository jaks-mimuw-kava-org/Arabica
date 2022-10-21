package com.kava.container.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

public class KavaHttpRequest extends HttpRequest {
    private final Method method;
    private final URI uri;
    private final HttpClient.Version version;

    private final String body;

    public KavaHttpRequest(String method, String uri, String version, String body) throws URISyntaxException {
        this.method = Method.of(method);
        this.uri = new URI(uri);
        this.version = switch (version) {
            case "HTTP/1.1":
                yield HttpClient.Version.HTTP_1_1;
            case "HTTP/2":
                yield HttpClient.Version.HTTP_2;
            default:
                yield null;
        };
        this.body = body;
    }

    @Override
    public Optional<BodyPublisher> bodyPublisher() {
        return Optional.empty();
    }

    @Override
    public String method() {
        return this.method.toString();
    }

    @Override
    public Optional<Duration> timeout() {
        return Optional.empty();
    }

    @Override
    public boolean expectContinue() {
        return false;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public Optional<HttpClient.Version> version() {
        return Optional.ofNullable(this.version);
    }

    @Override
    public HttpHeaders headers() {
        return HttpHeaders.of(new HashMap<>(), (s, s2) -> true);
    }

    public String body() {
        return body;
    }
}
