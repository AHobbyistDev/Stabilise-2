package com.stabilise.util.box;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.box.Boxes.ABox;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

public class StringBox extends ABox<String> implements IBox {
    
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
        super.set(in.readUTF());
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeUTF(get());
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, get());
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        super.set(o.readString(name));
    }
    
    @Override
    public String toString() {
        return get();
    }
    
}
