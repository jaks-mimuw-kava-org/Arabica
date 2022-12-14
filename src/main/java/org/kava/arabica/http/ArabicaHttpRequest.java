package org.kava.arabica.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Optional;

public class ArabicaHttpRequest extends HttpRequest {
    private final Method method;
    private final URI uri;
    private final HttpClient.Version version;

    private final byte[] body;

    private final HttpHeaders httpHeaders;

    public ArabicaHttpRequest(String method, String uri, String version, HttpHeaders headers, byte[] body) throws URISyntaxException {
        this.method = Method.of(method);
        this.uri = new URI(uri);
        this.version = HttpVersion.of(version);
        this.body = body;
        this.httpHeaders = headers;
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
        return this.httpHeaders;
    }

    public byte[] body() {
        return body;
    }

    public String bodyAsString() throws UnsupportedEncodingException {
        var encoding = httpHeaders.firstValue("Content-Encoding").orElse("UTF-8");
        return new String(body, encoding);
    }
}
