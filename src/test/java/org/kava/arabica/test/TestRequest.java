package org.kava.arabica.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TestRequest(String method,
                          String url,
                          String version,
                          Map<String, List<String>> headers,
                          byte[] body) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String method;
        private String url;
        private String version;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, new ArrayList<>(List.of(value)));
            return this;
        }

        public Builder appendHeader(String key, String value) {
            this.headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public TestRequest build() {
            return new TestRequest(method, url, version, headers, body);
        }
    }
}
