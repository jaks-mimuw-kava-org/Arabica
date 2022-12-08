package org.kava.arabica;

import org.kava.arabica.http.ArabicaHttpRequest;
import org.kava.arabica.http.ArabicaHttpResponse;
import org.kava.arabica.servlet.ArabicaServlet;
import org.kava.arabica.servlet.ArabicaServletURI;
import org.kava.arabica.utils.StaticReader;

import java.util.List;

@ArabicaServletURI("/favicon.ico")
public class IconServlet extends ArabicaServlet {

    byte[] iconAsBytes;

    public IconServlet(String path) {
        iconAsBytes = StaticReader.readFileAsBytesFromResources(path);
    }

    public IconServlet() {
        this("static/favicon.ico");
    }

    @Override
    public void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        response.setRawBody(iconAsBytes);
        response.setStatusCode(200);
        response.setRequest(request);

        response.modifyHeaders().put("Content-Type", List.of("image/svg+xml"));
    }

    @Override
    public void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
