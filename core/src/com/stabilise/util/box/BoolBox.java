package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;

public class BoolBox implements IData {
    
	// Please don't go changing these
    public static final BoolBox TRUE  = new BoolBox(true);
    public static final BoolBox FALSE = new BoolBox(false);
    
    
    /** Returns false. */
    public static boolean defaultValue() { return false; }
    
    
    
    /**
     * Returns a BoolBox encapsulating the specified value. This method may be
     * preferable to constructing a new BoolBox as it reuses {@link #TRUE} and
     * {@link #FALSE}.
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
        return Boolean.toString(value);
    }
    
    
    
    @Override
    public DataType type() {
        return DataType.BOOL;
    }
    
    @Override
    public boolean canConvertToType(DataType type) {
        switch(type) {
            case BOOL:
            case I8:
            case I16:
            case I32:
            case I64:
            case F32:
            case F64:
            case I8ARR:
            case I32ARR:
            case I64ARR:
            case F32ARR:
            case F64ARR:
            case STRING:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public IData convertToType(DataType type) {
        switch(type) {
            case BOOL:
                return new BoolBox(value);
            case I8:
                return new I8Box((byte) (value ? 1 : 0));
            case I16:
                return new I16Box((short) (value ? 1 : 0));
            case I32:
                return new I32Box(value ? 1 : 0);
            case I64:
                return new I64Box(value ? 1 : 0);
            case F32:
                return new F32Box(value ? 1f : 0f);
            case F64:
                return new F64Box(value ? 1D : 0D);
            case I8ARR:
                return new I8ArrBox(new byte[] {(byte) (value ? 1 : 0)});
            case I32ARR:
                return new I32ArrBox(new int[] {value ? 1 : 0});
            case I64ARR:
                return new I64ArrBox(new long[] {value ? 1 : 0});
            case F32ARR:
                return new F32ArrBox(new float[] {value ? 1f : 0f});
            case F64ARR:
                return new F64ArrBox(new double[] {value ? 1d : 0d});
            case STRING:
                return new StringBox(Boolean.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: Bool --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return true; }
    //@Override public boolean isLong()    { return true; }
    //@Override public boolean isDouble()  { return true; }
    //@Override public boolean isString()  { return true; }
    
    //@Override public boolean getAsBoolean() { return value;                   }
    //@Override public long    getAsLong()    { return value ? 1L : 0L;         }
    //@Override public double  getAsDouble()  { return value ? 1D : 0D;         }
    //@Override public String  getAsString()  { return Boolean.toString(value); }
    
    @Override
    public BoolBox duplicate() {
        return new BoolBox(value);
    }
    
}
