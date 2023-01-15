package org.kava.arabica.async;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

public class CyclicBuffer {
    private byte[] buffer;
    private int head;
    private int tail;

    private boolean full;

    public CyclicBuffer(int capacity) {
        buffer = new byte[capacity];
        head = 0;
        tail = 0;
        full = false;
    }

    public int getCapacity() {
        return buffer.length;
    }

    public int getFreeSpace() {
        return buffer.length - getUsedSpace();
    }

    public int getUsedSpace() {
        if (full) return buffer.length;
        return (tail - head + buffer.length) % buffer.length;
    }

    public byte get() {
        if (getUsedSpace() == 0) {
            throw new IllegalStateException("Buffer is empty");
        }

        var result = buffer[head];
        head = (head + 1) % buffer.length;

        full = false;

        return result;
    }

    private int getFreeSpaceThrowIfCannotPut(int count) throws IllegalStateException {
        int freeSpace = getFreeSpace();
        if (freeSpace < count) {
            throw new IllegalStateException("Buffer is full");
        }
        return freeSpace;
    }

    public void put(byte b) {
        int freeSpace = getFreeSpaceThrowIfCannotPut(1);

        putUnsafe(b);

        if (freeSpace == 1) {
            full = true;
        }
    }

    private void putUnsafe(byte b) {
        buffer[tail] = b;
        tail = (tail + 1) % buffer.length;
    }

    public void put(byte[] data, int length) {
        int freeSpace = getFreeSpaceThrowIfCannotPut(length);
        for (int i = 0; i < length; i++) {
            putUnsafe(data[i]);
        }
        if (freeSpace == length) {
            full = true;
        }
    }

    public void put(byte[] bytes) {
        put(bytes, bytes.length);
    }

    public void putExtend(byte[] bytes) {
        putExtend(bytes, bytes.length);
    }

    public void putExtend(byte[] bytes, int length) {
        while (getFreeSpace() < length) {
            this.resize(this.buffer.length * 2);
        }

        put(bytes, length);
    }

    public byte[] get(int count) {
        if (count > getUsedSpace()) {
            throw new IllegalStateException("Buffer does not contain enough data");
        }

        var result = new byte[count];
        for (int i = 0; i < count; i++) {
            result[i] = get();
        }

        return result;
    }

    public void drop(int count) {
        if (count > getUsedSpace()) {
            throw new IllegalStateException("Buffer does not contain enough data");
        }

        for (int i = 0; i < count; i++) {
            get();
        }
    }

    public void extend(int extension) {
        resize(this.buffer.length + extension);
    }

    public void resize(int newCapacity) {
        if (newCapacity < getUsedSpace()) {
            throw new IllegalStateException("New capacity is too small");
        }

        var newBuffer = new byte[newCapacity];
        var usedSpace = getUsedSpace();
        for (int i = 0; i < usedSpace; i++) {
            newBuffer[i] = get();
        }

        buffer = newBuffer;
        head = 0;
        tail = usedSpace;
        full = usedSpace == newCapacity;
    }

    public void clear() {
        head = 0;
        tail = 0;
        full = false;
    }

    public byte[] peak(int length) {
        if (length > getUsedSpace()) {
            throw new IllegalStateException("Buffer does not contain enough data");
        }

        var result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = buffer[(head + i) % buffer.length];
        }

        return result;
    }

    public void forEach(BiFunction<Byte, Integer, Boolean> action) {
        for (int i = 0; i < getUsedSpace(); i++) {
            if (!action.apply(buffer[(head + i) % buffer.length], i)) {
                break;
            }
        }
    }

    public <T> T[] peak(int length, BiFunction<Byte, Integer, T> f, T[] result) {
        if (length > getUsedSpace()) {
            throw new IllegalStateException("Buffer does not contain enough data");
        }

        if (result.length < length) {
            throw new IllegalArgumentException("Result array is too small");
        }

        for (int i = 0; i < length; i++) {
            result[i] = f.apply(buffer[(head + i) % buffer.length], i);
        }

        return result;
    }

    public ByteBuffer getBuffer() {
        return ByteBuffer.wrap(peak(getUsedSpace()));
    }
}
