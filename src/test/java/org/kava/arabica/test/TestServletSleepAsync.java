package org.kava.arabica.test;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.kava.arabica.http.ArabicaAsyncListener;

import java.io.IOException;

@WebServlet(value = "/async_sleep", asyncSupported = true)

public class TestServletSleepAsync extends HttpServlet {
        private void reverse(byte[] arr) {
            int b = 0;
            int e = arr.length - 1;
            while (b < e) {
                byte temp = arr[b];
                arr[b] = arr[e];
                arr[e] = temp;
                b++;
                e--;
            }
        }

        private void remoteReverse(AsyncContext asyncContext, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            var client = Dsl.asyncHttpClient();

            var request = client.prepareGet("https://6wrlmkp9u2.execute-api.us-east-1.amazonaws.com/?sleep=500");

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException err) {
                err.printStackTrace();
            }
            request.execute(new AsyncCompletionHandler<>() {
                @Override
                public Object onCompleted(Response response) throws Exception {
                    var body = req.getInputStream().readAllBytes();
                    reverse(body);
                    resp.getOutputStream().write(body);
                    resp.setContentLength(body.length);
                    resp.setContentType("text/plain");
                    resp.setStatus(200);
                    asyncContext.complete();
                    return response;
                }
            });

        }
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            var asyncContext = req.startAsync(req, resp);
            asyncContext.addListener(new ArabicaAsyncListener());
            asyncContext.start(() -> {
                try {
                    remoteReverse(asyncContext, req, resp);
                }
                catch (IOException err) {
                    resp.setStatus(418);
                }
            });
        }
}
