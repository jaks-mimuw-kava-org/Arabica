package com.example;

import com.example.model.Book;
import com.kava.arabica.http.ArabicaHttpRequest;
import com.kava.arabica.http.ArabicaHttpResponse;
import com.kava.arabica.logger.Logger;
import com.kava.arabica.logger.LoggerFactory;
import com.kava.arabica.servlet.ArabicaServlet;
import com.kava.arabica.servlet.ArabicaServletURI;
import com.kava.arabica.utils.StaticReader;
import sun.misc.Signal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.model.Book.quote;
import static java.lang.String.format;

@ArabicaServletURI("/library")
public class LibraryServlet extends ArabicaServlet {

    private static final Logger logger = LoggerFactory.getLogger(LibraryServlet.class);

    private List<Book> books = new ArrayList<>();

    @Override
    public void doGET(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        response.setStatusCode(200);
        response.setRequest(request);

        String allBooks = books.stream()
                .map(book -> format("<li>[%s] <b>%s</b></li>", book.author(), book.getTruncatedName()))
                .collect(Collectors.joining());

        String form = StaticReader.readFileFromResources("static/book_form.html");

        response.setBody(format("<h2>Books:</h2><ul>%s</ul>%s", allBooks, form));
    }

    @Override
    public void doPOST(ArabicaHttpRequest request, ArabicaHttpResponse response) {
        var body = request.body();
        String[] kvs = body.split("[&=]");
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < kvs.length; i+= 2) {
            json.append(quote(kvs[i])).append(":").append(quote(kvs[i + 1]));
            if (i + 2 < kvs.length) {
                json.append(",");
            }
        }
        json.append("}");

        var book = Book.fromJSON(json.toString());
        books.add(book);

        response.setStatusCode(200);
        response.setRequest(request);
        response.setBody("<h1>Added!</h1><a href=\"/library\">Go back!</a>");
    }

    public LibraryServlet() {
        loadFromMemory();
        Signal.handle(new Signal("INT"), signal -> {
            saveToMemory();
            System.exit(0);
        });
    }

    private static final String FILE_NAME = "books.by";

    public void saveToMemory() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(books);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromMemory() {
        try {
            File file = new File(FILE_NAME);
            if (file.exists()) {
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                books = (List<Book>) in.readObject();
                logger.info("Loaded books from memory! Number of books: %d", books.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
