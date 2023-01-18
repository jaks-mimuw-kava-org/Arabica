package org.kava.arabica.async;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.kava.arabica.test.TestUtils.noThrow;

class CyclicBufferTest {

    @Test
    void testGetCapacity() {
        var buffer = new CyclicBuffer(10);
        assertEquals(10, buffer.getCapacity());
    }

    @Test
    void testGetFreeSpace() {
        var buffer = new CyclicBuffer(10);
        assertEquals(10, buffer.getFreeSpace());
    }

    @Test
    void testGetUsedSpace() {
        var buffer = new CyclicBuffer(10);
        assertEquals(0, buffer.getUsedSpace());
    }

    @Test
    void testGet() {
        var buffer = new CyclicBuffer(10);
        buffer.put((byte) 42);
        assertEquals(42, buffer.get());
    }

    @Test
    void testGetMany() {
        var buffer = new CyclicBuffer(10);
        buffer.put((byte) 42);
        buffer.put((byte) 73);
        buffer.put((byte) 34);
        assertEquals(42, buffer.get());
        assertEquals(73, buffer.get());
        buffer.put((byte) 42);
        assertEquals(34, buffer.get());
        assertEquals(42, buffer.get());
    }

    @Test
    void testGetThrow() {
        var buffer = new CyclicBuffer(10);
        assertThrows(IllegalStateException.class, buffer::get);
    }

    @Test
    void testPut() {
        var buffer = new CyclicBuffer(10);
        buffer.put((byte) 1);
        assertEquals(1, buffer.getUsedSpace());
        assertEquals(9, buffer.getFreeSpace());
    }

    @Test
    void testPutFull() {
        var buffer = new CyclicBuffer(1);
        buffer.put((byte) 1);
        assertThrows(IllegalStateException.class, () -> buffer.put((byte) 2));
    }

    @Test
    void testPutArray() {
        var buffer = new CyclicBuffer(10);
        buffer.put(new byte[]{1, 2, 3});
        assertEquals(3, buffer.getUsedSpace());
        assertEquals(7, buffer.getFreeSpace());
    }

    @Test
    void testPutArrayFull() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        assertThrows(IllegalStateException.class, () -> buffer.put(new byte[]{4, 5, 6}, 3));
    }

    @Test
    void testPutArrayPartialThrow() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        assertThrows(IllegalStateException.class, () -> buffer.put(new byte[]{4, 5, 6}, 2));
    }

    @Test
    void testPutArrayPartial() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 2);
        buffer.put(new byte[]{4, 5, 6}, 1);
        assertEquals(3, buffer.getUsedSpace());
        assertEquals(0, buffer.getFreeSpace());
    }

    @Test
    void testExceptionNotBreakBuffer() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 2);
        assertThrows(IllegalStateException.class, () -> buffer.put(new byte[]{4, 5, 6}, 2));
        assertEquals(2, buffer.getUsedSpace());
        assertEquals(1, buffer.getFreeSpace());

        assertEquals(1, buffer.get());
        assertEquals(2, buffer.get());
        assertThrows(IllegalStateException.class, buffer::get);

        buffer.put(new byte[]{4, 5, 6}, 2);
        assertEquals(2, buffer.getUsedSpace());
        assertEquals(1, buffer.getFreeSpace());

        assertEquals(4, buffer.get());
        assertEquals(5, buffer.get());
        assertThrows(IllegalStateException.class, buffer::get);
    }

    @Test
    void testDropAll() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        buffer.drop(3);
        assertEquals(0, buffer.getUsedSpace());
        assertEquals(3, buffer.getFreeSpace());
    }

    @Test
    void testDropNone() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        buffer.drop(0);
        assertEquals(3, buffer.getUsedSpace());
        assertEquals(0, buffer.getFreeSpace());
    }

    @Test
    void testDropPartial() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        buffer.drop(2);
        assertEquals(1, buffer.getUsedSpace());
        assertEquals(2, buffer.getFreeSpace());
    }

    @Test
    void testResize() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        buffer.resize(6);
        assertEquals(6, buffer.getCapacity());
        assertEquals(3, buffer.getUsedSpace());
        assertEquals(3, buffer.getFreeSpace());
    }

    @Test
    void testExtend() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        buffer.extend(3);
        assertEquals(6, buffer.getCapacity());
        assertEquals(3, buffer.getUsedSpace());
        assertEquals(3, buffer.getFreeSpace());
    }

    @Test
    void testExtendThrow() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        assertThrows(IllegalStateException.class, () -> buffer.resize(2));
    }

    @Test
    void testExtendThrow2() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        assertThrows(IllegalStateException.class, () -> buffer.resize(1));
    }

    @Test
    void testClear() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        buffer.clear();
        assertEquals(3, buffer.getCapacity());
        assertEquals(0, buffer.getUsedSpace());
        assertEquals(3, buffer.getFreeSpace());
    }

    @Test
    void testPeak() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        assertArrayEquals(new byte[0], buffer.peak(0));
        assertArrayEquals(new byte[]{1, 2, 3}, buffer.peak(3));
        assertArrayEquals(new byte[]{1, 2}, buffer.peak(2));
        assertArrayEquals(new byte[]{1}, buffer.peak(1));
    }

    @Test
    void testPeakThrow() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        assertThrows(IllegalStateException.class, () -> buffer.peak(4));
    }

    @Test
    void testForEach() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        var list = new ArrayList<Byte>();
        buffer.forEach((b, i) -> list.add(b));
        assertEquals(3, list.size());
        assertEquals((byte) 1, list.get(0));
        assertEquals((byte) 2, list.get(1));
        assertEquals((byte) 3, list.get(2));
    }

    @Test
    void testForEachEmpty() {
        var buffer = new CyclicBuffer(3);
        var list = new ArrayList<Byte>();
        buffer.forEach((b, i) -> list.add(b));
        assertEquals(0, list.size());
    }

    @Test
    void testForEachEarlier() {
        var buffer = new CyclicBuffer(5);
        buffer.put(new byte[]{1, 2, 3, 4, 5}, 5);
        AtomicInteger counter = new AtomicInteger();
        buffer.forEach((b, i) -> {
            counter.getAndIncrement();
            return i <= 2;
        });
        assertEquals(4, counter.get());
    }

    @Test
    void testPeakMapper() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        var resultArr = new String[3];
        var result = buffer.peak(3, (aByte, integer) -> aByte + "-" + integer.toString(), resultArr);
        assertArrayEquals(new String[]{"1-0", "2-1", "3-2"}, result);
    }

    @Test
    void testPeakOverflow() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        var resultArr = new String[3];
        assertThrows(IllegalStateException.class, () -> buffer.peak(4, (aByte, integer) -> aByte + "-" + integer.toString(), resultArr));
    }

    @Test
    void testPeakResultTooSmall() {
        var buffer = new CyclicBuffer(3);
        buffer.put(new byte[]{1, 2, 3}, 3);
        var resultArr = new String[2];
        assertThrows(IllegalArgumentException.class, () -> buffer.peak(3, (aByte, integer) -> aByte + "-" + integer.toString(), resultArr));
    }

    @Test
    void testBigBuffer() {
        var buffer = new CyclicBuffer(2048);
        noThrow(() ->
                IntStream.generate(() -> 1)
                        .limit(2048)
                        .forEach(i -> buffer.put((byte) i))
        );
        assertEquals(2048, buffer.getUsedSpace());
        assertEquals(0, buffer.getFreeSpace());
        assertEquals(2048, buffer.getCapacity());
        var resultArr = new String[2048];
        var result = noThrow(() -> buffer.peak(2048, (aByte, integer) -> aByte + "-" + integer.toString(), resultArr));
        assertArrayEquals(IntStream.range(0, 2048).mapToObj(i -> 1 + "-" + i).toArray(), result);
    }
}
