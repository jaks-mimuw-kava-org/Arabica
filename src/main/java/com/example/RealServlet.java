package com.example;

import com.example.model.Book;
import com.kava.container.http.KavaHttpRequest;
import com.kava.container.http.KavaHttpResponse;
import com.kava.container.servlet.KavaServlet;
import com.kava.container.servlet.KavaServletURI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.model.Book.quote;
import static java.lang.String.format;

@KavaServletURI("/library")
public class RealServlet extends KavaServlet {

    private final List<Book> books = new ArrayList<>();

    @Override
    public void doGET(KavaHttpRequest request, KavaHttpResponse response) {
        response.setStatusCode(200);
        response.setRequest(request);

        String allBooks = books.stream()
                .map(book -> format("<li>[%s] <b>%s</b></li>", book.author(), book.title()))
                .collect(Collectors.joining());

        String form = "<form method=\"post\">" +
                "<label for=\"title\">Title:</label><br>" +
                "<input type=\"text\" name=\"title\" id=\"title\"><br>" +
                "<label for=\"author\">Author:</label><br>" +
                "<input type=\"text\" name=\"author\" id=\"author\"><br>" +
                "<input type=\"submit\" value=\"Submit\"><br>" +
                "</form>";

        response.setBody(format("<h2>Books:</h2><ul>%s</ul>%s", allBooks, form));
    }

    @Override
    public void doPOST(KavaHttpRequest request, KavaHttpResponse response) {
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
}
