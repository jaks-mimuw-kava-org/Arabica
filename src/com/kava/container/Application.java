package com.kava.container;

import com.kava.container.prod.RealServlet;

public class Application {
    public static void main(String[] args) throws Exception {
        var container = new ServletContainer(8080);
        container.registerServlet(RealServlet.class);
        container.run();
    }
}
