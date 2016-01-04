package com.stabilise.util.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


public abstract class FileSource implements Closeable {
    
    protected FileSource() {}
    
    /**
     * Returns an estimate of the number of bytes remaining in this source.
     */
    public abstract int estimatedBytes() throws IOException;
    
    /**
     * Reads the next chunk of bytes from the file into the given buffer.
     * 
     * @return The number of bytes written to {@code buf}, or {@code -1} if the
     * end of this source has been reached.
     * @throws IOException if an I/O error occurs.
     * @see InputStream#read(byte[])
     */
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }
    
    /**
     * Reads {@code length}-many bytes into {@code buf}, starting at
     * {@code offset}.
     * 
     * @throws IOException if an I/O error occurs.
     * @see InputStream#read(byte[], int, int)
     */
    public abstract int read(byte[] buf, int off, int len) throws IOException;
    
    /**
     * Closes this FileSource.
     * 
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public abstract void close() throws IOException;
    
    /**
     * Returns a FileSource equivalent to this one, but which additionally
     * calculates the MD5 checksum of the file being read.
     * 
     * <p>Note that if this method is invoked after the file has been
     * partially read, the calculated checksum may be incorrect.
     * 
     * <p>This FileSource is returned if it is already calculating the MD5
     * checksum.
     */
    public final FileSource withChecksum() {
        try {
            return withChecksum("MD5");
        } catch(NoSuchAlgorithmException e) {
            throw new Error();
        }
    }
    
    /**
     * Returns a FileSource equivalent to this one, but which additionally
     * calculates the checksum of the file being read.
     * 
     * <p>Note that if this method is invoked after the file has been
     * partially read, the calculated checksum may be incorrect.
     * 
     * <p>This FileSource is returned if it is already calculating the checksum
     * using the specified algorithm.
     * 
     * @param algorithm The checksum algorithm to use.
     * 
     * @throws NullPointerException if {@code algorithm} is {@code null}.
     * @throws NoSuchAlgorithmException if the {@code algorithm} parameter
     * specifies an invalid algorithm.
     * @see java.security.MessageDigest
     */
    public FileSource withChecksum(String algorithm) throws NoSuchAlgorithmException {
        return new ChecksumWrapper(this, algorithm);
    }
    
    /**
     * Calculates and returns the checksum of the file. It may not be safe to
     * invoke this multiple times.
     * 
     * @throws UnsupportedOperationException if this FileSource did not
     * calculate the checksum.
     */
    public byte[] checksum() {
        throw new UnsupportedOperationException();
    }
    
    protected boolean hasChecksum() {
        return false;
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates and returns a FileSource reading from the given file.
     * 
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static FileSource createFromFile(File file) throws IOException {
        return new FileSourceFromStream(new FileInputStream(Objects.requireNonNull(file)));
    }
    
    /**
     * Creates and returns a FileSource reading from the given InputStream.
     * 
     * @throws NullPointerException if {@code is} is {@code null}.
     */
    public static FileSource createFromStream(InputStream is) {
        return new FileSourceFromStream(Objects.requireNonNull(is));
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    private static class FileSourceFromStream extends FileSource {
        
        private final InputStream in;
        
        public FileSourceFromStream(InputStream in) {
            this.in = in;
        }
        
        @Override
        public int estimatedBytes() throws IOException {
            return in.available();
        }
        
        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            return in.read(buf, off, len);
        }
        
        @Override
        public void close() throws IOException {
            in.close();
        }
        
    }
    
    private static class ChecksumWrapper extends FileSource {
        
        private final FileSource src;
        private final String algorithm;
        private final MessageDigest md;
        
        public ChecksumWrapper(FileSource src, String algorithm) throws NoSuchAlgorithmException {
            this.src = src; // trusted to be non-null
            this.algorithm = Objects.requireNonNull(algorithm);
            this.md = MessageDigest.getInstance(algorithm);
        }
        
        @Override
        public int estimatedBytes() throws IOException {
            return src.estimatedBytes();
        }
        
        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int count = src.read(buf, off, len);
            if(count > 0)
                md.update(buf, off, count);
            return count;
        }
        
        @Override
        public void close() throws IOException {
            src.close();
        }
        
        @Override
        public FileSource withChecksum(String algorithm) throws NoSuchAlgorithmException {
            if(this.algorithm.equals(algorithm))
                return this;
            return new ChecksumWrapper(this, algorithm);
        }
        
        @Override
        public byte[] checksum() {
            return md.digest();
        }
        
        @Override
        protected boolean hasChecksum() {
            return true;
        }
        
    }
    
    
    
}
