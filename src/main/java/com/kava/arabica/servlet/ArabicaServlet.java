package com.kava.arabica.servlet;

import com.kava.arabica.http.ArabicaHttpRequest;
import com.kava.arabica.http.ArabicaHttpResponse;

public abstract class ArabicaServlet {
    public abstract void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response);
    public abstract void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response);
}
