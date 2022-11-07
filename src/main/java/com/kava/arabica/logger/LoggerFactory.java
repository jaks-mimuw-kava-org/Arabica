package com.kava.arabica.logger;

public class LoggerFactory {
    public static <T> Logger getLogger(Class<T> clazz) {
        return new Logger(clazz.getSimpleName());
    }
}
