package com.kava.container.utils;

import com.kava.container.logger.Logger;
import com.kava.container.logger.LoggerFactory;

import java.io.IOException;

public class StaticReader {

    private static final Logger logger = LoggerFactory.getLogger(StaticReader.class);

    public static String readFileFromResources(String pathToFile) {
        try (var in = StaticReader.class.getClassLoader().getResourceAsStream(pathToFile)) {
            if (in == null) {
                logger.error("File not found \"%s\"", pathToFile);
                return null;
            }
            try {
                return new String(in.readAllBytes());
            } catch (IOException e) {
                logger.error("Error reading file \"%s\"", pathToFile);
                return null;
            }
        } catch (IOException e) {
            logger.error(e, "Cannot open file \"%s\"", pathToFile);
            return null;
        }
    }

    public static byte[] readFileAsBytesFromResources(String pathToFile) {
        try (var in = StaticReader.class.getClassLoader().getResourceAsStream(pathToFile)) {
            if (in == null) {
                logger.error("Binary file not found \"%s\"", pathToFile);
                return null;
            }
            try {
                return in.readAllBytes();
            } catch (IOException e) {
                logger.error("Error reading binary file \"%s\"", pathToFile);
                return null;
            }
        } catch (IOException e) {
            logger.error(e, "Cannot open binary file \"%s\"", pathToFile);
            return null;
        }
    }
}
