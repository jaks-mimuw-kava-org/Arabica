package org.kava.arabica.async;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import lombok.Getter;

import java.io.IOException;

public class ArabicaServletOutputStream extends ServletOutputStream {

    @Getter
    private final CyclicBuffer buffer;

    public ArabicaServletOutputStream(CyclicBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean isReady() {
        return buffer.getFreeSpace() > 0;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int b) throws IOException {
        buffer.putExtend(new byte[]{(byte) b});
    }
}
