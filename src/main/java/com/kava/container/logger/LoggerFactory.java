package com.kava.container.logger;

public class LoggerFactory {
    public static <T> Logger getLogger(Class<T> clazz) {
        return new Logger(clazz.getSimpleName());
    }
}