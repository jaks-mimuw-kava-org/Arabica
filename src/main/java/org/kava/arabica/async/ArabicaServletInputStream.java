package org.kava.arabica.async;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;

// This class is a wrapper around CycleBuffer that implements ServletInputStream
public class ArabicaServletInputStream extends ServletInputStream {

    private final CyclicBuffer buffer;

    public ArabicaServletInputStream(CyclicBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean isFinished() {
        return buffer.getUsedSpace() == 0;
    }

    @Override
    public boolean isReady() {
        return buffer.getUsedSpace() > 0;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
        if (buffer.getUsedSpace() == 0) {
            return -1;
        }

        return buffer.get();
    }
}
