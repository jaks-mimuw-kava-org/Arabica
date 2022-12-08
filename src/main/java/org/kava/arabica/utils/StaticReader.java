package org.kava.arabica.utils;

import org.kava.lungo.Logger;
import org.kava.lungo.LoggerFactory;

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
            logger.error("Cannot open file \"%s\": %s", pathToFile, e.getMessage());
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
            logger.error("Cannot open binary file \"%s\": %s", pathToFile, e.getMessage());
            return null;
        }
    }
}
