package com.example;

import com.kava.container.utils.StaticReader;
import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;

@KavaServletURI("/info")
public class KavaExplained extends KavaServlet {
    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setBody(StaticReader.readFileFromResources("static/welcome.html"));
        response.setStatusCode(200);
        response.setRequest(request);
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
