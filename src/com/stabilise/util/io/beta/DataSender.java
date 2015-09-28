package com.stabilise.util.io.beta;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.box.*;

public class DataSender implements Closeable {
    
    public static enum Format {
        NBT, JSON, RAW;
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
    
    public void io(String name, ByteArrayBox data) throws IOException {
        
    }
    
    public void io(String name, ByteBox data) throws IOException {
        
    }
    
    public void io(String name, CharBox data) throws IOException {
        
    }
    
    public void io(String name, DoubleBox data) throws IOException {
        
    }
    
    public void io(String name, FloatBox data) throws IOException {
        
    }
    
    public void io(String name, IntArrayBox data) throws IOException {
        
    }
    
    public void io(String name, IntBox data) throws IOException {
        
    }
    
    public void io(String name, LongBox data) throws IOException {
        
    }
    
    public void io(String name, ShortBox data) throws IOException {
        
    }
    
    public void io(String name, StringBox data) throws IOException {
        
    }
    
}
