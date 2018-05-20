package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

public class BoolBox implements ITag {
    
	// Please don't go changing these
    public static final BoolBox TRUE  = new BoolBox(true);
    public static final BoolBox FALSE = new BoolBox(false);
    
    
    /** Returns false. */
    public static boolean defaultValue() { return false; }
    
    
    
    /**
     * Returns a BoolBox encapsulating the specified value. This method may be
     * preferable to constructing a new BoolBox as it reuses {@link TRUE} and
     * {@link FALSE}.
     * 
     * <p>Warning: ONLY use this if you don't plan on changing the boxed value,
     * or things will go very wrong.
     */
    public static BoolBox valueOf(boolean bool) {
        return bool ? TRUE : FALSE;
    }
    
    
    private boolean value;
    
    
    /**
     * Creates a new BooleanBox holding the value false.
     */
    public BoolBox() {
        this(defaultValue());
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
    public void read(String name, DataCompound o) {
        value = o.getBool(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getBool();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
    
    
    
    
    @Override
    public boolean isCompatibleType(ITag other) {
        return other.isBoolean();
    }
    
    @Override
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        return new BoolBox(other.getAsBoolean());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value;                   }
    @Override public long    getAsLong()    { return value ? 1L : 0L;         }
    @Override public double  getAsDouble()  { return value ? 1D : 0D;         }
    @Override public String  getAsString()  { return Boolean.toString(value); }
    
}
