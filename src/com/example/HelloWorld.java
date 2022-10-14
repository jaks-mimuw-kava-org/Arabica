package com.example;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;

@KavaServletURI("/")
public class HelloWorld extends KavaServlet {

    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setStatusCode(200);
        response.setRequest(request);
        response.setBody("<h3>Hello, World!</h3>");
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
        throw new UnsupportedOperationException("Method doPOST is not implemented.");
    }
}