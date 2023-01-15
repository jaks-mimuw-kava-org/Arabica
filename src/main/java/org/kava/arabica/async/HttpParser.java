package org.kava.arabica.async;

import org.kava.lungo.Level;
import org.kava.lungo.Logger;
import org.kava.lungo.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class HttpParser {

    private final Logger logger = LoggerFactory.getLogger(HttpParser.class);
    private final HashMap<String, List<String>> headers = new HashMap<>();
    private String method;
    private String path;
    private String version;
    private byte[] body;
    private State state = State.FIRST;
    private Integer _contentLength = null;

    {
        logger.setLevel(Level.TRACE);
    }

    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
    }

    public boolean hasBody() {
        return body != null;
    }

    public void appendBody(byte[] data) {
        if (body == null) {
            body = data;
        } else {
            var newBody = new byte[body.length + data.length];
            System.arraycopy(body, 0, newBody, 0, body.length);
            System.arraycopy(data, 0, newBody, body.length, data.length);
            body = newBody;
        }
    }

    public Integer getBodyExpectedLength() {
        if (_contentLength == null) {
            _contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", List.of("0")).get(0));
        }

        return _contentLength;
    }

    private int bodyLength() {
        return hasBody() ? body.length : 0;
    }

    public Integer getBodyRemainingLength() {
        return getBodyExpectedLength() - bodyLength();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, List<String>> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void parse(Client client) {
        //noinspection StatementWithEmptyBody
        while (this.parseSingleInternal(client) && client.getInput().getUsedSpace() > 0) ;
    }

    private boolean parseSingleInternal(Client client) {
        var input = client.getInput();
        byte[] data;

        if (this.state == State.BODY) {
            data = input.get(input.getUsedSpace());
        } else {
            var state = new BiFunction<Byte, Integer, Boolean>() {
                boolean isCR = false;
                boolean isLF = false;
                int counter = 0;

                boolean isCRLF() {
                    return isCR && isLF;
                }

                @Override
                public Boolean apply(Byte aByte, Integer integer) {
                    if (aByte == '\r') {
                        isCR = true;
                        return Boolean.TRUE;
                    }

                    if (isCR && aByte == '\n') {
                        isLF = true;
                        return Boolean.FALSE;
                    }

                    ++counter;

                    return Boolean.TRUE;
                }
            };

            input.forEach(state);

            if (!state.isCRLF()) {
                return false;
            }

            data = input.get(state.counter);
            input.drop(2); // We don't need CRLF anymore
        }

        this.state.parse(data, this);
        return !isReady();
    }

    public boolean isReady() {
        return state == State.BODY && bodyLength() == getBodyExpectedLength();
    }

    private enum State {
        FIRST, HEADERS, BODY;

        private static final Pattern HEADER_PATTERN = Pattern.compile("(?<key>[^:]+):\\s*(?<value>.*)");
        private static final Pattern FIRST_LINE = Pattern.compile("^(?<method>\\w+) (?<path>\\S+) (?<version>\\S+)$");

        public void parse(byte[] date, HttpParser parser) {
            switch (this) {
                case FIRST -> parseFirstLine(date, parser);
                case HEADERS -> parseHeaders(date, parser);
                case BODY -> parseBody(date, parser);
            }
        }

        private void parseBody(byte[] date, HttpParser parser) {
            parser.appendBody(date);
        }

        private void parseHeaders(byte[] date, HttpParser parser) {
            if (date.length == 0) {
                parser.state = BODY;
                return;
            }

            var header = new String(date, StandardCharsets.UTF_8);
            var matcher = HEADER_PATTERN.matcher(header);

            if (!matcher.matches()) {
                throw new IllegalStateException("Invalid header: " + header);
            }

            parser.addHeader(matcher.group("key"), matcher.group("value"));
        }

        private void parseFirstLine(byte[] date, HttpParser parser) {
            String firstLine = new String(date, StandardCharsets.UTF_8);
            var matcher = FIRST_LINE.matcher(firstLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Invalid first line: " + firstLine);
            }

            parser.setMethod(matcher.group("method"));
            parser.setPath(matcher.group("path"));
            parser.setVersion(matcher.group("version"));

            parser.state = HEADERS;
        }
    }
}
