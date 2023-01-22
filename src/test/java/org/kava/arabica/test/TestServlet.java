package org.kava.arabica.test;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/reverse")
public class TestServlet extends HttpServlet {

    private void reverse(byte[] arr) {
        int b = 0;
        int e = arr.length - 1;
        while (b < e) {
            byte temp = arr[b];
            arr[b] = arr[e];
            arr[e] = temp;
            b++;
            e--;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var body = req.getInputStream().readAllBytes();
        reverse(body);
        resp.getOutputStream().write(body);
        resp.setContentLength(body.length);
        resp.setContentType("text/plain");
        resp.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var body = req.getInputStream().readAllBytes();
        reverse(body);
        resp.getOutputStream().write(body);
        resp.setContentLength(body.length);
        resp.setContentType("text/plain");
        resp.setStatus(200);
    }
}
