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
    
    public void io(String name, Exportable data) throws IOException {
        
    }
    
    public void write(String name, byte[] data) throws IOException {
        
    }
    
    public void write(String name, byte data) throws IOException {
        
    }
    
    public void write(String name, char data) throws IOException {
        
    }
    
    public void write(String name, double data) throws IOException {
        
    }
    
    public void write(String name, float data) throws IOException {
        
    }
    
    public void write(String name, int[] data) throws IOException {
        
    }
    
    public void write(String name, int data) throws IOException {
        
    }
    
    public void write(String name, long data) throws IOException {
        
    }
    
    public void write(String name, short data) throws IOException {
        
    }
    
    public void write(String name, String data) throws IOException {
        
    }
    
}
