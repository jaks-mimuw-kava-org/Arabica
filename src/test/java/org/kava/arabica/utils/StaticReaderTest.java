package org.kava.arabica.utils;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class StaticReaderTest {
    @Test
    void testFromResources() {
        var fileContent = StaticReader.readFileFromResources("reader/simple.txt");
        assertNotNull(fileContent);
        // Test file [reader/simple.txt] was created on Windows, so it has CRLF line endings.
        // If it was edited on Linux (e.g. in development process), it would have LF line endings.
        // So, we need to check for both line endings.
        fileContent = fileContent.replaceAll("\r\n", "\n");
        assertEquals("i like bread.\n", fileContent);
    }

    @Test
    void testMissingFile() {
        // This file does not exist
        var fileContent = StaticReader.readFileFromResources("reader/missing.txt");
        assertNull(fileContent);
    }

    @Test
    void testBinaryFile() {
        var fileContent = StaticReader.readFileAsBytesFromResources("reader/test.bin");
        assertNotNull(fileContent);
        byte[] bread = "i like bread".getBytes();
        byte[] bytes = HexFormat.of().parseHex("0004ffb6");
        byte[] java = "java is the best".getBytes();
        byte[] expected = new byte[bread.length + bytes.length + java.length];
        System.arraycopy(bread, 0, expected, 0, bread.length);
        System.arraycopy(bytes, 0, expected, bread.length, bytes.length);
        System.arraycopy(java, 0, expected, bread.length + bytes.length, java.length);
        assertArrayEquals(expected, fileContent);
    }

    @Test
    void testMissingBinaryFile() {
        // This file does not exist
        var fileContent = StaticReader.readFileAsBytesFromResources("reader/missing.bin");
        assertNull(fileContent);
    }
}
