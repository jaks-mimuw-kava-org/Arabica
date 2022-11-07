package com.example;

import com.kava.arabica.utils.StaticReader;
import com.kava.arabica.http.ArabicaHttpRequest;
import com.kava.arabica.http.ArabicaHttpResponse;
import com.kava.arabica.servlet.ArabicaServlet;
import com.kava.arabica.servlet.ArabicaServletURI;

@ArabicaServletURI("/info")
public class KavaExplained extends ArabicaServlet {
    @Override
    public void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        response.setBody(StaticReader.readFileFromResources("static/welcome.html"));
        response.setStatusCode(200);
        response.setRequest(request);
    }

    @Override
    public void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
