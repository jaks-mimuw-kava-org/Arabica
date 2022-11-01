package com.example;

import com.kava.container.ServletContainer;

public class Application {
    public static void main(String[] args) throws Exception {
        var container = new ServletContainer(8080);
        container.registerServlet(LibraryServlet.class);
        container.registerServlet(HelloWorld.class);
        container.registerServlet(KavaExplained.class);
        container.registerIcon("static/favicon.ico");
        container.run();
    }
}
