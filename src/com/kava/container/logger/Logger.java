package com.kava.container.logger;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final String prefix;
    private final PrintStream output = System.out;

    public Logger(String prefix) {
        this.prefix = prefix;
    }

    private enum Level {
        ERROR,
        WARNING,
        INFO,
        DEBUG,
        VERBOSE
    }

    private void print(String message, Level level) {
        output.printf("[%s]{%s} %10s: %s\n", this.getDateAndTime(), level, this.prefix, message);
    }

    private String getDateAndTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm"));
    }

    public void debug(String message) {
        print(message, Level.DEBUG);
    }

    public void info(String message) {
        print(message, Level.INFO);
    }

    public void error(String message) {
        print(message, Level.ERROR);
    }

    public void warning(String message) {
        print(message, Level.WARNING);
    }

    public void verbose(String message) {
        print(message, Level.VERBOSE);
    }
}