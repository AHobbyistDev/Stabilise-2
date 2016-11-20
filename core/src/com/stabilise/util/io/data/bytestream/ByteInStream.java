package com.stabilise.util.io.data.bytestream;

import java.io.IOException;
import java.io.InputStream;


class ByteInStream extends InputStream {
    
    private final ByteCompound obj;
    private final byte[] buf;
    private int index = 0;
    
    public ByteInStream(ByteCompound obj) {
        this.obj = obj;
        this.buf = obj.buf;
    }
    
    @Override
    public int read() throws IOException {
        return index < obj.size ? buf[index++] & 0xFF : -1;
    }
    
}
