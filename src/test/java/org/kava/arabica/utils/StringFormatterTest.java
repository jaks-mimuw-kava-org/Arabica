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
        String actual = StringFormatter.format(format, args("name", "John"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Two named arguments in `format` string")
    public void testTwoArgumentsFormat() {
        String format = "Hello, ${name}! You are ${age} years old.";
        String expected = "Hello, John! You are 25 years old.";
        String actual = StringFormatter.format(format, args("name", "John", "age", 25));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Two named arguments in `format` string, but reversed")
    public void testTwoReversedArguments() {
        String format = "Hello, ${name}! You are ${age} years old.";
        String expected = "Hello, John! You are 25 years old.";
        String actual = StringFormatter.format(format, args("age", 25, "name", "John"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Tree named arguments in `format` string")
    public void testThreeArguments() {
        String format = "Hello, ${name}! You are ${age} years old. You live in ${city}.";
        String expected = "Hello, John! You are 25 years old. You live in London.";
        String actual = StringFormatter.format(format, args("name", "John", "age", 25, "city", "London"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Tree named arguments in `format` string shuffled")
    public void testThreeShuffledArguments() {
        String format = "Hello, ${name}! You are ${age} years old. You live in ${city}.";
        String expected = "Hello, John! You are 25 years old. You live in London.";
        String actual = StringFormatter.format(format, args("age", 25, "city", "London", "name", "John"));
        assertEquals(expected, actual);
    }
}
