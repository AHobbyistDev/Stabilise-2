package com.stabilise.util.box;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.Checks;
import com.stabilise.util.box.Boxes.MutBox;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;

public class StringBox extends MutBox<String> implements IData {
    
    /** Returns an empty string */
    public static String defaultValue() { return ""; }
    
    
    
    /**
     * Creates a new StringBox holding an empty string.
     */
    public StringBox() {
        super(defaultValue());
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
    public void read(String name, DataCompound o){
        value = o.getString(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getString();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    
    
    
    @Override
    public DataType type() {
        return DataType.STRING;
    }
    
    @Override
    public boolean canConvertToType(DataType type) {
        switch(type) {
            case STRING:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public IData convertToType(DataType type) {
        switch(type) {
            case STRING:
                return new StringBox(value);
            default:
                throw new RuntimeException("Illegal conversion: String --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return false; }
    //@Override public boolean isLong()    { return false; }
    //@Override public boolean isDouble()  { return false; }
    //@Override public boolean isString()  { return true;  }
    
    //@Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert string to boolean... yet"); }
    //@Override public long    getAsLong()    { throw Checks.ISE("Can't convert string to long... yet");    }
    //@Override public double  getAsDouble()  { throw Checks.ISE("Can't convert string to double... yet");  }
    //@Override public String  getAsString()  { return value;                                               }
    
    @Override
    public StringBox duplicate() {
        return new StringBox(value);
    }
    
}
