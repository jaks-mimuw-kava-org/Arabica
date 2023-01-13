package org.kava.arabica.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFormatter {
    StringFormatter() {}

    public static String named(String template, Map<String, Object> namedArgs) {
        StringBuilder format = new StringBuilder(template);
        List<Object> newArgs = new ArrayList<>();

        Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            String paramName = "${" + key + "}";
            int index = format.indexOf(paramName);
            if (index != -1) {
                format.replace(index, index + paramName.length(), "%s");
                newArgs.add(namedArgs.get(key));
            }
        }

        return String.format(format.toString(), newArgs.toArray());
    }

    public static Map<String, Object> namedArgs(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even.");
        }

        var result = new HashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("Argument at index " + i + " must be a String.");
            }
            result.put((String) args[i], args[i + 1]);
        }

        return result;
    }

    public static Map<String, Object> args(Object... args) {
        return namedArgs(args);
    }
}
