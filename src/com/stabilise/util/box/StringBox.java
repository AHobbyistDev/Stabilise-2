package com.stabilise.util.box;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.box.Boxes.MutBox;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;

public class StringBox extends MutBox<String> implements IBox {
    
    /**
     * Creates a new StringBox holding an empty string.
     */
    public StringBox() {
        super("");
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public StringBox(String value) {
        super(Objects.requireNonNull(value));
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    @Override
    public void set(String value) {
        super.set(Objects.requireNonNull(value));
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readUTF();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeUTF(value);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o){
        value = o.getString(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getString();
    }
    
    @Override
    public String toString() {
        return value;
    }
    
}
