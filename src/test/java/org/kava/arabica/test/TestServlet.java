package org.kava.arabica.test;

import org.kava.arabica.http.ArabicaHttpRequest;
import org.kava.arabica.http.ArabicaHttpResponse;
import org.kava.arabica.servlet.ArabicaServlet;
import org.kava.arabica.servlet.ArabicaServletURI;

import java.util.List;

@ArabicaServletURI("/reverse")
public class TestServlet extends ArabicaServlet {

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
    public void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        var body = request.body();
        reverse(body);
        response.setRawBody(body);
        response.modifyHeaders().put("Content-Type", List.of("bytes"));
        response.setStatusCode(200);
    }

    @Override
    public void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        var body = request.body();
        reverse(body);
        response.setRawBody(body);
        response.modifyHeaders().put("Content-Type", List.of("bytes"));
        response.setStatusCode(200);
    }
}
