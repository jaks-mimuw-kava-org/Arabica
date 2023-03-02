package org.kava.arabica.test;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kava.arabica.http.ArabicaAsyncListener;

import java.io.IOException;
import java.net.Socket;

@WebServlet(value = "/async_reverse", asyncSupported = true)
public class TestServletAsync extends HttpServlet {

    private static final int REMOTE_PORT = 10000;


    private byte[] remoteReverse(byte[] bytes) throws IOException {
        try (var socket = new Socket("localhost", REMOTE_PORT)) {
            var inputStream = socket.getInputStream();
            var outputStream = socket.getOutputStream();
            outputStream.write((byte) bytes.length);
            outputStream.write(bytes);
            outputStream.write((byte) bytes.length);

            return inputStream.readAllBytes();
        }
        catch (IOException err) {
            throw new IOException();
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var asyncContext = req.startAsync(req, resp);
        asyncContext.addListener(new ArabicaAsyncListener());
        asyncContext.start(() -> {
            try {
                var body = req.getInputStream().readAllBytes();
                body = remoteReverse(body);
                resp.getOutputStream().write(body);
                resp.setContentLength(body.length);
                resp.setContentType("text/plain");
                resp.setStatus(200);
            }
            catch (IOException err) {
                resp.setStatus(418);
            }
            asyncContext.complete();
        });
    }
}
