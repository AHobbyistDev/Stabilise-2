package com.stabilise.util.io.beta;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.box.*;

@SuppressWarnings("unused")
public abstract class DataSender implements DataObject, Closeable {
    
    public static enum Format {
        NBT, JSON, BYTE_ARRAY;
    }
    
    private boolean writing = false;
    
    private FileHandle target;
    private Format format;
    private boolean write;
    
    public DataSender() {
        
    }
    
    public void open(Format format, boolean write) throws IOException {
        if(writing)
            throw new IllegalStateException();
        
        this.format = Objects.requireNonNull(format);
        this.write = write;
    }
    
    public void close() throws IOException {
        if(!writing)
            throw new IllegalStateException();
        
        format = null;
    }
    
}
