package org.kava.arabica.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.kava.arabica.utils.StringFormatter.args;

class StringFormatterTest {
    @Test
    @DisplayName("One named arguments in `format` string")
    public void testSingleFormat() {
        String format = "Hello, ${name}!";
        String expected = "Hello, John!";
        String actual = StringFormatter.named(format, args("name", "John"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Two named arguments in `format` string")
    public void testTwoArgumentsFormat() {
        String format = "Hello, ${name}! You are ${age} years old.";
        String expected = "Hello, John! You are 25 years old.";
        String actual = StringFormatter.named(format, args("name", "John", "age", 25));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Two named arguments in `format` string, but reversed")
    public void testTwoReversedArguments() {
        String format = "Hello, ${name}! You are ${age} years old.";
        String expected = "Hello, John! You are 25 years old.";
        String actual = StringFormatter.named(format, args("age", 25, "name", "John"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Tree named arguments in `format` string")
    public void testThreeArguments() {
        String format = "Hello, ${name}! You are ${age} years old. You live in ${city}.";
        String expected = "Hello, John! You are 25 years old. You live in London.";
        String actual = StringFormatter.named(format, args("name", "John", "age", 25, "city", "London"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Tree named arguments in `format` string shuffled")
    public void testThreeShuffledArguments() {
        String format = "Hello, ${name}! You are ${age} years old. You live in ${city}.";
        String expected = "Hello, John! You are 25 years old. You live in London.";
        String actual = StringFormatter.named(format, args("age", 25, "city", "London", "name", "John"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test wrong argument count in `args`")
    public void testArgumentCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            args("age", 25, "city");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            args("city");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            args("age", 25, "city", 20, "abc");
        });
    }

    @Test
    @DisplayName("Test wrong argument type in `args`")
    public void testArgumentType() {
        assertThrows(IllegalArgumentException.class, () -> {
            args("age", 25, 15, 12);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            args(11, 25, "city", 12);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            args(new int[2], 40, "city", 12);
        });
    }
}
