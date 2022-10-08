package com.kava.container.prod.model;

import static java.lang.String.format;

public record Book(String title, String author) {
    public String toJSON() {
        return format("{%s:%s,%s:%s}",
                quote("title"), title,
                quote("author"), author
        );
    }

    public static String quote(String name) {
        return format("\"%s\"", name);
    }

    public static Book fromJSON(String json) {
        String title = null;
        String author = null;

        json = removeSpaces(json);
        json = json.replace("{", "");
        json = json.replace("}", "");
        String[] attrs = json.split(",");
        for (var attr : attrs) {
            String[] kv = attr.split(":");
            String key = kv[0].replaceAll("\"", "");
            String val = kv[1].replaceAll("\"", "");
            if (key.equals("title")) {
                title = val;
            } else if (key.equals("author")) {
                author = val;
            }
        }

        return new Book(title, author);
    }

    private static String removeSpaces(String str) {
        int bracketCount = 0;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '"')
                bracketCount++;
            else if (Character.isWhitespace(str.charAt(i)) && bracketCount % 2 == 0) {
                continue;
            }

            sb.append(str.charAt(i));
        }

        return sb.toString();
    }
}
