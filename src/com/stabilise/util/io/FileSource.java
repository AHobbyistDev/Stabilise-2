package com.stabilise.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


public abstract class FileSource extends InputStream {
    
    protected FileSource() {}
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException if this is a CachedFileSource.
     * @throws IOException {@inheritDoc}
     */
    @Override
    public abstract int read() throws IOException;
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException if this is a CachedFileSource.
     * @throws NullPointerException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public int read(byte[] buf) throws IOException {
        return super.read(buf);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException if this is a CachedFileSource.
     * @throws NullPointerException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public abstract int read(byte[] buf, int off, int len) throws IOException;
    
    /**
     * Returns a FileSource equivalent to this one, but which additionally
     * calculates the MD5 checksum of the file being read.
     * 
     * <p>Note that if this method is invoked after the file has been
     * partially read, the calculated checksum may be incorrect.
     * 
     * <p>This FileSource is returned if it is already calculating the MD5
     * checksum.
     * 
     * <p>Also note that if this is a CachedFileSource, this method will, for
     * technical reasons (read: it's annoying to try to implement), throw an
     * UnsupportedOperationException.
     * 
     * @throws UnsupportedOperationException if this is a cached file source.
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
     * <p>Also note that if this is a CachedFileSource, this method will, for
     * technical reasons (read: it's annoying to try to implement), throw an
     * UnsupportedOperationException.
     * 
     * @param algorithm The checksum algorithm to use.
     * 
     * @throws NullPointerException if {@code algorithm} is {@code null}.
     * @throws NoSuchAlgorithmException if the {@code algorithm} parameter
     * specifies an invalid algorithm.
     * @throws UnsupportedOperationException if this is a cached file source.
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
    
    /**
     * Wraps this FileSource in a CachedFileSource. Closing the returned
     * CachedFileSource will close this source.
     * 
     * <p>Returns this FileSource if it's already cached.
     */
    public CachedFileSource cached() throws IOException {
        if(this instanceof CachedFileSource)
            return (CachedFileSource)this;
        return CachedFileSource.createFromStream(this);
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
     * @throws IOException if an I/O error occurs.
     */
    public static FileSource createFromStream(InputStream is) throws IOException {
        if(is instanceof FileSource)
            return (FileSource)is;
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
        public int available() throws IOException {
            return in.available();
        }
        
        @Override
        public int read() throws IOException {
            return in.read();
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
        public int available() throws IOException {
            return src.available();
        }
        
        @Override
        public int read() throws IOException {
            int next = src.read();
            if(next != -1)
                md.update((byte)next);
            return next;
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
