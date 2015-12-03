package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

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
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readBool(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
