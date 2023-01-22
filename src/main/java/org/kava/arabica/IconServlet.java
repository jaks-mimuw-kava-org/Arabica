package org.kava.arabica;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kava.arabica.utils.StaticReader;

import java.io.IOException;

@WebServlet(name = "IconServlet", urlPatterns = {"/favicon.ico"})
public class IconServlet extends HttpServlet {

    byte[] iconAsBytes;

    private final String path;

    public IconServlet(String path) {
        this.path = path;
    }

    public IconServlet() {
        this("static/favicon.ico");
    }

    @Override
    public void init() throws ServletException {
        iconAsBytes = StaticReader.readFileAsBytesFromResources(path);
        if (iconAsBytes == null) {
            throw new ServletException("Could not read icon from resources at path " + path);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getOutputStream().write(iconAsBytes);
        resp.getOutputStream().flush();
        resp.setContentType("image/svg+xml");
        resp.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(405);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
