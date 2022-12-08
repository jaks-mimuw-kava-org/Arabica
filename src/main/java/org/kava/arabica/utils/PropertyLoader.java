package org.kava.arabica.utils;

import java.util.Optional;
import java.util.function.Function;

public class PropertyLoader {

    public static String load(String key) {
        return System.getProperty(key);
    }

    public static String load(String key, String defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .orElse(defaultValue);
    }

    public static Integer loadInteger(String key, Integer defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    public static Boolean loadBoolean(String key, Boolean defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    public static Long loadLong(String key, Long defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .map(Long::parseLong)
                .orElse(defaultValue);
    }

    public static Double loadDouble(String key, Double defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .map(Double::parseDouble)
                .orElse(defaultValue);
    }

    public static Float loadFloat(String key, Float defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .map(Float::parseFloat)
                .orElse(defaultValue);
    }

    public static <T extends Enum<T>> T loadEnum(String key, T defaultValue, Class<T> enumClass) {
        return Optional.ofNullable(System.getProperty(key))
                .map(value -> Enum.valueOf(enumClass, value))
                .orElse(defaultValue);
    }

    public static <T> T loadAndParse(String key, T defaultValue, Function<String, T> parser) {
        return Optional.ofNullable(System.getProperty(key))
                .map(parser)
                .orElse(defaultValue);
    }
}
