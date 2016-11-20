package com.stabilise.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple OutputStream which simply counts the number of bytes passed to it.
 */
public class ByteCountingStream extends OutputStream {
    
    private int count = 0;
    
    @Override
    public void write(int b) throws IOException {
        count++;
    }
    
    /**
     * Returns the number of bytes written.
     */
    public int byteCount() {
        return count;
    }
    
}
