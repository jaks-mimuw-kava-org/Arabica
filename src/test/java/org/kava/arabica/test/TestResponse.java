package org.kava.arabica.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TestResponse(String version,
                           int status,
                           String reason,
                           Map<String, List<String>> headers,
                           byte[] body) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String version;
        private int status;
        private String reason;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
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

        public TestResponse build() {
            return new TestResponse(version, status, reason, headers, body);
        }
    }
}
