package com.kava.container.utils;

import java.io.IOException;

public class StaticReader {
    public static String readFile(String fileName) {
        var in = StaticReader.class.getClassLoader().getResourceAsStream(fileName);
        if (in == null) {
            return "File not found: " + fileName;
        }
        try {
            return new String(in.readAllBytes());
        } catch (IOException e) {
            return "Error reading file: " + fileName;
        }
    }

    public static byte[] readFileAsBytes(String path) {
        var in = StaticReader.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
