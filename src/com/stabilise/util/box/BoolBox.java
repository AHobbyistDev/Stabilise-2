package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;

public class BoolBox implements IBox {
    
    private boolean value;
    
    
    /**
     * Creates a new BooleanBox holding the value false.
     */
    public BoolBox() {
        this(false);
    }
    
    public BoolBox(boolean value) {
        this.value = value;
    }
    
    public boolean get()           { return value;       }
    public void set(boolean value) { this.value = value; }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readBoolean();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeBoolean(value);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }

    @Override
    public void read(String name, DataCompound o) {
        value = o.getBool(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }

    @Override
    public void read(DataList l) {
        value = l.getBool();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }

}
