package com.example;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;

@KavaServletURI("/info")
public class KavaExplained extends KavaServlet {
    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setBody("<h1>Welcome to <b>Kava</b>!</h1></br>Brand new Java web framework!");
        response.setStatusCode(200);
        response.setRequest(request);
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
