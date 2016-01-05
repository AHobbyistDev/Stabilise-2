package com.stabilise.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;


public abstract class CachedFileSource extends FileSource {
    
    protected CachedFileSource() {}
    
    /**
     * Throws UnsupportedOperationException.
     */
    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("Wrap this cached source before reading");
    }
    
    /**
     * Throws UnsupportedOperationException.
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Wrap this cached source before reading");
    }
    
    /**
     * As {@link #read(byte[], int, int)}, but reads from {@link #fileBytes}
     * starting at {@code idx}, and reads more bytes from the underlying
     * FileSource if necessary. To be used by attached readers.
     */
    abstract int read(int idx, byte[] buf, int off, int len) throws IOException;
    
    /**
     * Reads all bytes from the source of this CachedFileSource and closes the
     * source. Does nothing if the file has already been completely cached.
     * 
     * <p>This CachedFileSource effectively becomes immutable after this method
     * is invoked and thus may be safely published to multiple threads.
     * 
     * @return this CachedFileSource
     */
    public abstract CachedFileSource readAll() throws IOException;
    
    /**
     * Creates and returns a new FileSource wrapping this cached source. The
     * returned FileSource provides a means to read from this source as a
     * stream.
     */
    public final FileSource sourceFrom() {
        return new CachedSourceReader(this);
    }
    
    /**
     * Throws UnsupportedOperationException.
     */
    @Override
    public final FileSource withChecksum(String algorithm) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Calculates the MD5 checksum of the underlying file. Note that the
     * returned checksum may be incorrect if the file hasn't yet been
     * loaded/cached in its entirety.
     */
    public final byte[] calculateChecksum() {
        try {
            return calculateChecksum("MD5");
        } catch(NoSuchAlgorithmException e) {
            throw new Error();
        }
    }
    
    /**
     * Calculates the checksum of the underlying file. Note that the returned
     * checksum may be incorrect if the file hasn't yet been loaded/cached in
     * its entirety.
     * 
     * @throws NullPointerException if {@code algorithm} is {@code null}.
     * @throws NoSuchAlgorithmException if the {@code algorithm} parameter
     * specifies an invalid algorithm.
     */
    public abstract byte[] calculateChecksum(String algorithm) throws NoSuchAlgorithmException;
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates and returns a CachedFileSource reading from the given file.
     * 
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static CachedFileSource createFromFile(File file) throws IOException {
        return new CachedFileSourceImpl(new FileInputStream(Objects.requireNonNull(file)));
    }
    
    /**
     * Creates and returns a CachedFileSource reading from the given
     * InputStream.
     * 
     * @throws NullPointerException if {@code is} is {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static CachedFileSource createFromStream(InputStream is) throws IOException {
        if(is instanceof CachedFileSource)
            return (CachedFileSource)is;
        return new CachedFileSourceImpl(Objects.requireNonNull(is));
    }
    
    /**
     * Creates a new CachedFileSource wrapping the given byte array. The
     * provided array is <i>not</i> clones.
     * 
     * @throws NullPointerException if {@code bytes} is {@code null}.
     */
    public static CachedFileSource createFromRawBytes(byte[] bytes) {
        return new RawBytesSource(bytes);
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    private static class CachedFileSourceImpl extends CachedFileSource {
        
        /** Source being wrapped. nulled out once we've read the whole thing. */
        private InputStream src;
        
        /** The cached file itself. */
        protected byte[] fileBytes;
        /** Number of bytes in {@code fileBytes} being used. Index of the first
         * available byte. */
        protected int len = 0;
        
        
        protected CachedFileSourceImpl(InputStream src) throws IOException {
            // +1 for 1-byte safezone in readAll() so we can more easily tell
            // whether or not we've read all of the file without expanding
            // fileBytes and attempting to read another chunk.
            fileBytes = new byte[src.available() + 1];
        }
        
        @Override
        public int available() throws IOException {
            return fileBytes.length + (src != null ? src.available() : 0);
        }
        
        /**
         * As {@link #read(byte[], int, int)}, but reads from {@link #fileBytes}
         * starting at {@code idx}, and reads more bytes from the underlying
         * FileSource if necessary. To be used by attached readers.
         */
        int read(int idx, byte[] buf, int off, int length) throws IOException {
            int available = len - idx;
            
            if(src != null) {
                int extraNeeded = length - available;
                boolean endReached = false;
                
                if(extraNeeded > 0) {
                    int canRead = fileBytes.length - len;
                    int count = 0;
                    
                    while(((count = src.read(fileBytes, len, canRead)) > 0 || canRead == 0) && extraNeeded > 0) {
                        if((canRead != 0 && count == 0) || count < 0) {
                            endReached = true;
                        } else {
                            len += count;
                            extraNeeded -= count;
                            if(len == fileBytes.length)
                                fileBytes = Arrays.copyOf(fileBytes, 2* len);
                            canRead = fileBytes.length - len;
                        }
                    }
                    
                    available = len - idx;
                }
                
                if(endReached) {
                    src.close();
                    src = null;
                }
            }
            
            int count = Math.min(length, available);
            System.arraycopy(fileBytes, idx, buf, off, count);
            
            return count;
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
            while((count = src.read(buf, len, canRead)) > 0 || canRead == 0) {
                len += count;
                if(len == buf.length)
                    buf = Arrays.copyOf(buf, 2 * len); // double capacity
                canRead = buf.length - len;
            }
            
            this.fileBytes = buf;
            this.len = len;
            
            src.close();
            src = null;
            
            return this;
        }
        
        @Override
        public void close() throws IOException {
            if(src != null) {
                src.close();
                src = null;
            }
        }
        
        @Override
        public byte[] calculateChecksum(String algorithm) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(fileBytes, 0, len);
            return md.digest();
        }
        
    }
    
    private static class RawBytesSource extends CachedFileSource {
        
        protected final byte[] bytes;
        
        public RawBytesSource(byte[] bytes) {
            this.bytes = bytes;
        }
        
        @Override
        int read(int idx, byte[] buf, int off, int len) throws IOException {
            int count = Math.min(len, bytes.length - idx);
            System.arraycopy(bytes, idx, buf, off, count);
            return count;
        }
        
        @Override
        public CachedFileSource readAll() throws IOException {
            return this;
        }
        
        @Override
        public byte[] calculateChecksum(String algorithm) throws NoSuchAlgorithmException {
            return MessageDigest.getInstance(algorithm).digest(bytes);
        }
        
    }
    
    private static class CachedSourceReader extends FileSource {
        
        private final CachedFileSource src;
        private int idx = 0;
        
        private CachedSourceReader(CachedFileSource src) {
            this.src = src;
        }
        
        @Override
        public int read() throws IOException {
            byte[] buf = new byte[1];
            int count = src.read(idx++, buf, 0, 1);
            return count == 0 ? -1 : (int)buf[0] & 0xFF;
        }
        
        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int count = src.read(idx, buf, off, len);
            idx += count;
            return count;
        }
        
    }
    
}
