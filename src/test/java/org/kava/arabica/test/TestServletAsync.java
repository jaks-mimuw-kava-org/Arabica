package org.kava.arabica.test;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kava.arabica.http.ArabicaAsyncListener;

@WebServlet(value = "/async_reverse", asyncSupported = true)
public class TestServletAsync extends HttpServlet {

    private void slowReverse(byte[] arr) throws InterruptedException {
        int b = 0;
        int e = arr.length - 1;
        while (b < e) {
            Thread.sleep(500);
            byte temp = arr[b];
            arr[b] = arr[e];
            arr[e] = temp;
            b++;
            e--;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var asyncContext = req.startAsync(req, resp);
        asyncContext.addListener(new ArabicaAsyncListener());
        asyncContext.start(() -> {
                try {
                    var body = req.getInputStream().readAllBytes();
                    slowReverse(body);
                    resp.getOutputStream().write(body);
                    resp.setContentLength(body.length);
                    resp.setContentType("text/plain");
                    resp.setStatus(200);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                asyncContext.complete();
            }
        );
    }
}
