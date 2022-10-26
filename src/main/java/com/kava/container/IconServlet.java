package com.kava.container;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;
import com.kava.container.utils.StaticReader;

import java.util.List;

@KavaServletURI("/favicon.ico")
public class IconServlet extends KavaServlet {

    byte[] iconAsBytes;

    public IconServlet(String path) {
        iconAsBytes = StaticReader.readFileAsBytes(path);
    }

    public IconServlet() {
        this("static/favicon.ico");
    }

    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setRawBody(iconAsBytes);
        response.setStatusCode(200);
        response.setRequest(request);

        response.modifyHeaders().put("Content-Type", List.of("image/x-icon"));
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
