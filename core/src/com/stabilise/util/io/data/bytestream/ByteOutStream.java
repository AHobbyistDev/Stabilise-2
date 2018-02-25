package com.stabilise.util.io.data.bytestream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;


class ByteOutStream extends OutputStream {
    
    private final ByteCompound obj;
    private byte[] buf;
    private int index = 0;
    
    ByteOutStream(ByteCompound obj) {
        this.obj = obj;
        this.buf = obj.buf;
    }
    
    @Override
    public void write(int b) throws IOException {
        resizeIfNeeded();
        buf[index++] = (byte)b;
        obj.size++;
    }
    
    private void resizeIfNeeded() {
        if(index >= buf.length) {
            buf = Arrays.copyOf(buf, buf.length * 2);
            obj.buf = buf;
        }
    }
    
}
