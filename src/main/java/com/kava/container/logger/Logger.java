package com.kava.container.logger;

import com.kava.container.utils.PropertyLoader;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static java.lang.String.format;

public class Logger {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String LEVEL_DEFINE = "kava.logger.level";

    private final String prefix;
    private final PrintStream output = System.out;

    private static final Level LEVEL = PropertyLoader.loadEnum(LEVEL_DEFINE, Level.INFO, Level.class);

    public Logger(String prefix) {
        this.prefix = prefix;
    }

    private enum Level {
        ERROR,
        WARNING,
        INFO,
        DEBUG,
        VERBOSE;

        public boolean isLoggable() {
            return this.ordinal() <= LEVEL.ordinal();
        }

        private static final int MAX_LENGTH = Arrays.stream(values())
                .map(Enum::name)
                .map(String::length)
                .max(Integer::compareTo)
                .orElse(0);

        @Override
        public String toString() {
            return " ".repeat(MAX_LENGTH - this.name().length()) + this.name();
        }
    }

    private void print(String message, Level level) {
        if (!level.isLoggable()) return;
        output.printf("[%s]{%s} %s: %s\n", this.getDateAndTime(), level, this.prefix, message);
    }

    private String getDateAndTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    private void printParsed(Level level, String message, Object... args) {
        print(format(message, args), level);
    }

    public void debug(String message, Object... args) {
        printParsed(Level.DEBUG, message, args);
    }

    public void info(String message, Object... args) {
        printParsed(Level.INFO, message, args);
    }

    public void error(String message, Object... args) {
        printParsed(Level.ERROR, message, args);
    }

    public void error(Exception e, String message, Object... args) {
        printParsed(Level.ERROR, message + ": " + e.getMessage(), args);
    }

    public void warning(String message, Object... args) {
        printParsed(Level.WARNING, message, args);
    }

    public void verbose(String message, Object... args) {
        printParsed(Level.VERBOSE, message, args);
    }
}
