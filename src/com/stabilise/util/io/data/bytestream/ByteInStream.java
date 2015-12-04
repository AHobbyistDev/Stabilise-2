package com.stabilise.util.io.data.bytestream;

import java.io.IOException;
import java.io.InputStream;


class ByteInStream extends InputStream {
    
    private final byte[] buf;
    private int index = 0;
    
    public ByteInStream(ByteCompound obj) {
        this.buf = obj.buf;
    }
    
    @Override
    public int read() throws IOException {
        return index < buf.length ? buf[index++] : -1;
    }
    
}
