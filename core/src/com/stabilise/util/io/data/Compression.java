package com.stabilise.util.io.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * An assortment of compression formats.
 */
public enum Compression {
    
    UNCOMPRESSED {
        @Override
        public InputStream wrap(InputStream in) throws IOException {
            return in;
        }
        
        @Override
        public OutputStream wrap(OutputStream out) throws IOException {
            return out;
        }
    },
    GZIP {
        @Override
        public InputStream wrap(InputStream in) throws IOException {
            return new GZIPInputStream(in);
        }
        
        @Override
        public OutputStream wrap(OutputStream out) throws IOException {
            return new GZIPOutputStream(out);
        }
    },
    ZLIB {
        @Override
        public InputStream wrap(InputStream in) throws IOException {
            return new InflaterInputStream(in);
        }
        
        @Override
        public OutputStream wrap(OutputStream out) throws IOException {
            return new DeflaterOutputStream(out);
        }
    };
    
    /**
     * Wraps the given InputStream in a compressing stream.
     */
    public abstract InputStream wrap(InputStream in) throws IOException;
    
    /**
     * Wraps the given OutputStream in a decompressing stream.
     */
    public abstract OutputStream wrap(OutputStream out) throws IOException;
    
}
