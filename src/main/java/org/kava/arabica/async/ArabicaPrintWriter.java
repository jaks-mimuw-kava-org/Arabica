package org.kava.arabica.async;

import java.io.OutputStream;
import java.io.PrintWriter;

public class ArabicaPrintWriter extends PrintWriter {
    public ArabicaPrintWriter(OutputStream out) {
        super(out);
    }
}
