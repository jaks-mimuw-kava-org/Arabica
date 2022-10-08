package com.kava.container.servlet;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;

public abstract class KavaServlet {
    public abstract void doGET(KavaHttpRequest request, KavaHttpResponse response);
    public abstract void doPOST(KavaHttpRequest request, KavaHttpResponse response);
}
