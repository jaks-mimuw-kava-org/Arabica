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
    private final HttpServletRequest request;
    private final ArabicaServletOutputStream outputStream = new ArabicaServletOutputStream(new CyclicBuffer(1024));
    @Getter
    private final Map<String, List<String>> headers = new HashMap<>();
    @Getter
    private int statusCode;
    @Getter
    private String message;
    private PrintWriter writer = null;

    @Getter
    private boolean isReady = false;

    public ArabicaHttpResponse(HttpServletRequest request) {
        this.request = request;
    }

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
        this.message = getMessageOfCode(sc);
    }

    public void ready() {
        this.isReady = true;
    }



    private String getMessageOfCode(int sc) {
        return switch(sc) {
            case 100 -> "Continue";
            case 101 -> "Switching Protocols";
            case 102 -> "Processing";
            case 103 -> "Early Hints";
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 203 -> "Non-Authoritative Information";
            case 204 -> "No Content";
            case 205 -> "Reset Content";
            case 206 -> "Partial Content";
            case 207 -> "Multi-Status";
            case 208 -> "Already Reported";
            case 226 -> "IM Used";
            case 300 -> "Multiple Choices";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 303 -> "See Other";
            case 304 -> "Not Modified";
            case 305 -> "Use Proxy";
            case 306 -> "unused";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 402 -> "Payment Required";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 406 -> "Not Acceptable";
            case 407 -> "Proxy Authentication Required";
            case 408 -> "Request Timeout";
            case 409 -> "Conflict";
            case 410 -> "Gone";
            case 411 -> "Length Required";
            case 412 -> "Precondition Failed";
            case 413 -> "Payload Too Large";
            case 414 -> "URI Too Long";
            case 415 -> "Unsupported Media Type";
            case 416 -> "Range Not Satisfiable";
            case 417 -> "Expectation Failed";
            case 418 -> "I'm a teapot";
            case 421 -> "Misdirected Request";
            case 422 -> "Unprocessable Entity";
            case 423 -> "Locked";
            case 424 -> "Failed Dependency";
            case 425 -> "Too Early";
            case 426 -> "Upgrade Required";
            case 428 -> "Precondition Required";
            case 429 -> "Too Many Requests";
            case 431 -> "Request Header Fields Too Large";
            case 451 -> "Unavailable For Legal Reasons";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            case 505 -> "HTTP Version Not Supported";
            case 506 -> "Variant Also Negotiates";
            case 507 -> "Insufficient Storage";
            case 508 -> "Loop Detected";
            case 510 -> "Not Extended";
            case 511 -> "Network Authentication Required";
            default -> "Unidentified";
        };
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
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    public ArabicaServletOutputStream getArabicaOutputStream() {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
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
