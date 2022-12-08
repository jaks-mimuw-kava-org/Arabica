package com.example;

import org.kava.arabica.http.ArabicaHttpRequest;
import org.kava.arabica.http.ArabicaHttpResponse;
import org.kava.arabica.servlet.ArabicaServlet;
import org.kava.arabica.servlet.ArabicaServletURI;

@ArabicaServletURI("/")
public class HelloWorld extends ArabicaServlet {

    @Override
    public void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        response.setStatusCode(200);
        response.setRequest(request);
        response.setBody("<h3>Hello, World!</h3>");
    }

    @Override
    public void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        throw new UnsupportedOperationException("Method doPOST is not implemented.");
    }
}
