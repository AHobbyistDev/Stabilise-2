package com.stabilise.util.io;

import java.io.IOException;
import java.util.Arrays;


public class CachedFileSource extends FileSource {
    
    /** Source being wrapped. nulled out once we've read the whole thing. */
    private FileSource src;
    
    /** The cached file itself. */
    private byte[] fileBytes;
    /** Number of bytes in {@code fileBytes} being used. Index of the first
     * available byte. */
    private int len = 0;
    
    
    private CachedFileSource(FileSource src) throws IOException {
        // +1 for 1-byte safezone in readAll() so we can more easily tell
        // whether or not we've read all of the file without expanding
        // fileBytes and attempting to read another chunk.
        fileBytes = new byte[src.estimatedBytes() + 1];
    }
    
    
    @Override
    public int estimatedBytes() throws IOException {
        return fileBytes.length;
    }
    
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Wrap this cached source before reading");
    }
    
    /**
     * As {@link #read(byte[], int, int)}, but reads from {@link #fileBytes}
     * starting at {@code idx}, and reads more bytes from the underlying
     * FileSource if necessary. To be used by attached readers.
     */
    private int read(int idx, byte[] buf, int off, int length) throws IOException {
        if(src != null) {
            int len = this.len;
            int available = len - idx;
            int extraNeeded = length - available;
            while(extraNeeded > 0) {
                int canRead = fileBytes.length - len;
                int count = 0;
                while((count = src.read(fileBytes, len, canRead)) > 0) {
                    len += count;
                    if(len == buf.length)
                        buf = Arrays.copyOf(buf, 2 * len); // double capacity
                }
            }
        }
    }
    
    /**
     * Reads all bytes from the source of this CachedFileSource and closes the
     * source. Does nothing if the file has already been completely cached.
     * 
     * <p>This CachedFileSource effectively becomes immutable after this method
     * is invoked and thus may be safely published to multiple threads.
     * 
     * @return this CachedFileSource
     */
    public CachedFileSource readAll() throws IOException {
        if(src == null)
            return this;
        
        byte[] buf = fileBytes;
        int len = this.len;
        
        if(buf.length == 0)
            buf = new byte[1024]; // 1kb initial; arbitrary
        
        int canRead = buf.length - len;
        int count = 0;
        while((count = src.read(buf, len, canRead)) > 0) {
            len += count;
            if(len == buf.length)
                buf = Arrays.copyOf(buf, 2 * len); // double capacity
        }
        
        this.fileBytes = buf;
        this.len = len;
        this.src = null;
        
        return this;
    }
    
    @Override
    public void close() throws IOException {
        // do nothing
    }
    
}
