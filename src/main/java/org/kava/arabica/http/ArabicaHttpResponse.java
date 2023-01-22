package org.kava.arabica.http;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.kava.arabica.async.ArabicaServletOutputStream;
import org.kava.arabica.async.CyclicBuffer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ArabicaHttpResponse implements HttpServletResponse {
    @Getter
    private int statusCode;
    @Getter
    private String message;

    @Getter
    private final HttpServletRequest request;

    private final ArabicaServletOutputStream outputStream = new ArabicaServletOutputStream(new CyclicBuffer(1024));
    private PrintWriter writer = null;

    public ArabicaHttpResponse(HttpServletRequest request) {
        this.request = request;
    }

    @Getter
    private final Map<String, List<String>> headers = new HashMap<>();

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.statusCode = sc;
        this.message = msg;
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.statusCode = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.statusCode = 301;
        this.message = "Redirect";
        this.headers.put("Location", List.of(location));
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String name, String value) {
        this.headers.put(name, new ArrayList<>(List.of(value)));
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.headers.put(name, new ArrayList<>(List.of(Integer.toString(value))));
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.headers.computeIfAbsent(name, k -> new ArrayList<>()).add(String.valueOf(value));
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public void setStatus(int sc) {
        this.statusCode = sc;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name).get(0);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.headers.get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        return this.headers.get("Content-Type").get(0);
    }

    @Override
    public void setContentType(String type) {
        this.headers.put("Content-Type", List.of(type));
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(outputStream);
        }
        return writer;
    }

    @Override
    public void setContentLength(int len) {
        this.headers.put("Content-Length", List.of(Integer.toString(len)));
    }

    @Override
    public void setContentLengthLong(long len) {
        this.headers.put("Content-Length", List.of(Long.toString(len)));
    }

    @Override
    public int getBufferSize() {
        return 0; // TODO: implement
    }

    @Override
    public void setBufferSize(int size) {
        // TODO: implement
    }

    @Override
    public void flushBuffer() throws IOException {
        // TODO: implement
    }

    @Override
    public void resetBuffer() {
        // TODO: implement
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException();
    }
}
