package org.kava.arabica.servlet;

import org.kava.arabica.http.ArabicaHttpRequest;
import org.kava.arabica.http.ArabicaHttpResponse;

public abstract class ArabicaServlet {
    public abstract void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response);
    public abstract void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response);
}
