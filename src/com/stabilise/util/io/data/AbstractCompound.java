package com.stabilise.util.io.data;

public abstract class AbstractCompound implements Tag, DataCompound {
    
    protected boolean writeMode;
    
    public abstract <T extends Tag> T put(String name, T t);
    
    @Override
    public void io(String name, Exportable data) {
        data.io(getCompound(name), writeMode);
    }
    
    @Override
    public void io(String name, ValueExportable data) {
        data.io(name, this, writeMode);
    }
    
    @Override
    public void setReadMode() {
        writeMode = false;
    }
    
    @Override
    public void setWriteMode() {
        writeMode = true;
    }
    
    protected void checkCanRead() {
        if(writeMode)
            throw new IllegalStateException("Not in reader mode!");
    }
    
    protected void checkCanWrite() {
        if(!writeMode)
            throw new IllegalStateException("Not in writer mode!");
    }
    
}
