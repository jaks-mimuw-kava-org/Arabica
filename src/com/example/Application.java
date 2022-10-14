package com.example;

import com.kava.container.ServletContainer;

public class Application {
    public static void main(String[] args) throws Exception {
        var container = new ServletContainer(8080);
        container.registerServlet(RealServlet.class);
        container.registerServlet(HelloWorld.class);
        container.run();
    }
}
