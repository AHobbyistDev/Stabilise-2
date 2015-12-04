package com.stabilise.util.io.data;

public abstract class AbstractCompound implements DataCompound {
    
    protected boolean writeMode;
    
    @Override
    public void io(String name, Exportable data) {
        data.io(getCompound(name), writeMode);
    }
    
    @Override
    public void io(String name, ValueExportable data) {
        data.io(name, this, writeMode);
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
