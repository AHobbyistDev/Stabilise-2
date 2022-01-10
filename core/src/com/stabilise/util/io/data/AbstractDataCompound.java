package com.stabilise.util.io.data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javaslang.control.Option;

import com.stabilise.util.box.*;

/**
 * Base implementation for a DataCompound backed by a hash map. External code
 * should never refer to this class directly.
 */
public abstract class AbstractDataCompound implements DataCompound {
    
    // There's a whole lot of repetition going on in this class, but I'm afraid
    // there isn't much I can do about that, unfortunately.
    
    /** We back the data with a LinkedHashMap so that iteration occurs in the
     * order of data insertion. In particular, we want the data to be written
     * (in write()) in a predictable order. */
    protected final Map<String, IData> data = new LinkedHashMap<>();
    protected boolean strict = true;
    
    
    
    @Override
    public IData getData(String name) {
        return data.get(name);
    }
    
    @Override
    public void putData(String name, IData d) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(d);
        if(d instanceof DataCompound)
            data.put(name, ((DataCompound)d).convert(format()));
        else if(d instanceof DataList)
            data.put(name, ((DataList)d).convert(format()));
        else
            data.put(name, d);
        setDirty();
    }
    
    /**
     * Like putData() but with fewer checks and no format conversions, to be
     * used internally when the given values can be trusted
     */
    private void putData2(String name, IData d) {
        data.put(Objects.requireNonNull(name), d);
        setDirty();
    }
    
    
    @Override
    public boolean contains(String name) {
        return data.containsKey(name);
    }
    
    private boolean containsType(String name, DataType type) {
        IData d = data.get(name);
        if(d == null)
            return false;
        return strict ? d.isType(type) : d.canConvertToType(type);
    }
    
    @Override public boolean containsCompound(String name) { return containsType(name, DataType.COMPOUND);  }
    @Override public boolean containsList    (String name) { return containsType(name, DataType.LIST);      }
    @Override public boolean containsBool    (String name) { return containsType(name, DataType.BOOL);      }
    @Override public boolean containsI8      (String name) { return containsType(name, DataType.I8);        }
    @Override public boolean containsI16     (String name) { return containsType(name, DataType.I16);       }
    @Override public boolean containsI32     (String name) { return containsType(name, DataType.I32);       }
    @Override public boolean containsI64     (String name) { return containsType(name, DataType.I64);       }
    @Override public boolean containsF32     (String name) { return containsType(name, DataType.F32);       }
    @Override public boolean containsF64     (String name) { return containsType(name, DataType.F64);       }
    @Override public boolean containsI8Arr   (String name) { return containsType(name, DataType.I8ARR);     }
    @Override public boolean containsI32Arr  (String name) { return containsType(name, DataType.I32ARR);    }
    @Override public boolean containsI64Arr  (String name) { return containsType(name, DataType.I64ARR);    }
    @Override public boolean containsF32Arr  (String name) { return containsType(name, DataType.F32ARR);    }
    @Override public boolean containsF64Arr  (String name) { return containsType(name, DataType.F64ARR);    }
    @Override public boolean containsString  (String name) { return containsType(name, DataType.STRING);    }
    
    @Override public void put(String name, DataCompound data) { putData2(name, data.convert(format())); }
    @Override public void put(String name, DataList data) { putData2(name, data.convert(format())); }
    @Override public void put(String name, boolean data)  { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, byte data)     { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, short data)    { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, int data)      { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, long data)     { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, float data)    { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, double data)   { putData2(name, Boxes.box(data)); }
    @Override public void put(String name, byte[] data)   { putData2(name, Boxes.box(Objects.requireNonNull(data))); }
    @Override public void put(String name, int[] data)    { putData2(name, Boxes.box(Objects.requireNonNull(data))); }
    @Override public void put(String name, long[] data)   { putData2(name, Boxes.box(Objects.requireNonNull(data))); }
    @Override public void put(String name, float[] data)  { putData2(name, Boxes.box(Objects.requireNonNull(data))); }
    @Override public void put(String name, double[] data) { putData2(name, Boxes.box(Objects.requireNonNull(data))); }
    @Override public void put(String name, String data)   { putData2(name, Boxes.box(Objects.requireNonNull(data))); }
    
    @Override
    public DataCompound getCompound(String name) {
        IData d = data.get(name);
        if(d instanceof DataCompound)
            return d.asCompound();
        else if(!strict && d != null && d.canConvertToType(DataType.COMPOUND))
            return d.convertToType(DataType.COMPOUND).asCompound();
        else
            return format().newCompound();
    }
    
    @Override
    public DataList getList(String name) {
        IData d = data.get(name);
        if(d instanceof DataList)
            return d.asList();
        else if(!strict && d != null && d.canConvertToType(DataType.LIST))
            return d.convertToType(DataType.LIST).asList();
        else
            return format().newList();
    }
    
    // A lot of these methods have unnecessary object creation in the relaxed
    // branches by means of the convertToType().get() invocations, but ah well,
    // maybe if we're lucky the JVM will learn to sidestep that.
    
    @Override
    public boolean getBool(String name) {
        IData d = data.get(name);
        if(d instanceof BoolBox)
            return d.asBool().get();
        else if(!strict && d != null && d.canConvertToType(DataType.BOOL))
            return d.convertToType(DataType.BOOL).asBool().get();
        else
            return BoolBox.defaultValue();
    }
    
    @Override
    public byte getI8(String name) {
        IData d = data.get(name);
        if(d instanceof I8Box)
            return d.asI8().get();
        else if(!strict && d != null && d.canConvertToType(DataType.I8))
            return d.convertToType(DataType.I8).asI8().get();
        else
            return I8Box.defaultValue();
    }
    
    @Override
    public short getI16(String name) {
        IData d = data.get(name);
        if(d instanceof I16Box)
            return d.asI16().get();
        else if(!strict && d != null && d.canConvertToType(DataType.I16))
            return d.convertToType(DataType.I16).asI16().get();
        else
            return I16Box.defaultValue();
    }
    
    @Override
    public int getI32(String name) {
        IData d = data.get(name);
        if(d instanceof I32Box)
            return d.asI32().get();
        else if(!strict && d != null && d.canConvertToType(DataType.I32))
            return d.convertToType(DataType.I32).asI32().get();
        else
            return I32Box.defaultValue();
    }
    
    @Override
    public long getI64(String name) {
        IData d = data.get(name);
        if(d instanceof I64Box)
            return d.asI64().get();
        else if(!strict && d != null && d.canConvertToType(DataType.I64))
            return d.convertToType(DataType.I64).asI64().get();
        else
            return I64Box.defaultValue();
    }
    
    @Override
    public float getF32(String name) {
        IData d = data.get(name);
        if(d instanceof F32Box)
            return d.asF32().get();
        else if(!strict && d != null && d.canConvertToType(DataType.F32))
            return d.convertToType(DataType.F32).asF32().get();
        else
            return F32Box.defaultValue();
    }
    
    @Override
    public double getF64(String name) {
        IData d = data.get(name);
        if(d instanceof F64Box)
            return d.asF64().get();
        else if(!strict && d != null && d.canConvertToType(DataType.F64))
            return d.convertToType(DataType.F64).asF64().get();
        else
            return F64Box.defaultValue();
    }
    
    @Override
    public byte[] getI8Arr(String name) {
        IData d = data.get(name);
        if(d instanceof I8ArrBox)
            return d.asI8Arr().get();
        else if(!strict && d != null && d.canConvertToType(DataType.I8ARR))
            return d.convertToType(DataType.I8ARR).asI8Arr().get();
        else
            return I8ArrBox.defaultValue();
    }
    
    @Override
    public int[] getI32Arr(String name) {
        IData d = data.get(name);
        if(d instanceof I32ArrBox)
            return d.asI32Arr().get();
        else if(!strict && d != null && d.canConvertToType(DataType.BOOL))
            return d.convertToType(DataType.BOOL).asI32Arr().get();
        else
            return I32ArrBox.defaultValue();
    }
    
    @Override
    public long[] getI64Arr(String name) {
        IData d = data.get(name);
        if(d instanceof I64ArrBox)
            return d.asI64Arr().get();
        else if(!strict && d != null && d.canConvertToType(DataType.I64ARR))
            return d.convertToType(DataType.I64ARR).asI64Arr().get();
        else
            return I64ArrBox.defaultValue();
    }
    
    @Override
    public float[] getF32Arr(String name) {
        IData d = data.get(name);
        if(d instanceof F32ArrBox)
            return d.asF32Arr().get();
        else if(!strict && d != null && d.canConvertToType(DataType.F32ARR))
            return d.convertToType(DataType.F32ARR).asF32Arr().get();
        else
            return F32ArrBox.defaultValue();
    }
    
    @Override
    public double[] getF64Arr(String name) {
        IData d = data.get(name);
        if(d instanceof F64ArrBox)
            return d.asF64Arr().get();
        else if(!strict && d != null && d.canConvertToType(DataType.F64ARR))
            return d.convertToType(DataType.F64ARR).asF64Arr().get();
        else
            return F64ArrBox.defaultValue();
    }
    
    @Override
    public String getString(String name) {
        IData d = data.get(name);
        if(d instanceof StringBox)
            return d.asString().get();
        else if(!strict && d != null && d.canConvertToType(DataType.STRING))
            return d.convertToType(DataType.STRING).asString().get();
        else
            return StringBox.defaultValue();
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends IData> Option<T> opt(String name, Class<T> c, DataType type) {
        IData d = data.get(name);
        if(c.isInstance(d))
            return Option.some((T) d);
        else if(!strict && d != null && d.canConvertToType(type))
            return Option.some((T) d.convertToType(type));
        else
            return Option.none();
    }
    
    @Override public Option<DataCompound> optCompound(String name) { return opt(name, DataCompound.class, DataType.COMPOUND);                }
    @Override public Option<DataList>     optList    (String name) { return opt(name, DataList.class, DataType.LIST);                        }
    @Override public Option<Boolean>      optBool    (String name) { return opt(name, BoolBox.class, DataType.BOOL).map(BoolBox::get);       }
    @Override public Option<Byte>         optI8      (String name) { return opt(name, I8Box.class, DataType.I8).map(I8Box::get);             }
    @Override public Option<Short>        optI16     (String name) { return opt(name, I16Box.class, DataType.I16).map(I16Box::get);          }
    @Override public Option<Integer>      optI32     (String name) { return opt(name, I32Box.class, DataType.I32).map(I32Box::get);          }
    @Override public Option<Long>         optI64     (String name) { return opt(name, I64Box.class, DataType.I64).map(I64Box::get);          }
    @Override public Option<Float>        optF32     (String name) { return opt(name, F32Box.class, DataType.F32).map(F32Box::get);          }
    @Override public Option<Double>       optF64     (String name) { return opt(name, F64Box.class, DataType.F64).map(F64Box::get);          }
    @Override public Option<byte[]>       optI8Arr   (String name) { return opt(name, I8ArrBox.class, DataType.I8ARR).map(I8ArrBox::get);    }
    @Override public Option<int[]>        optI32Arr  (String name) { return opt(name, I32ArrBox.class, DataType.I32ARR).map(I32ArrBox::get); }
    @Override public Option<long[]>       optI64Arr  (String name) { return opt(name, I64ArrBox.class, DataType.I64ARR).map(I64ArrBox::get); }
    @Override public Option<float[]>      optF32Arr  (String name) { return opt(name, F32ArrBox.class, DataType.F32ARR).map(F32ArrBox::get); }
    @Override public Option<double[]>     optF64Arr  (String name) { return opt(name, F64ArrBox.class, DataType.F64ARR).map(F64ArrBox::get); }
    @Override public Option<String>       optString  (String name) { return opt(name, StringBox.class, DataType.STRING).map(StringBox::get); }
    
    @Override
    public DataCompound childCompound(String name) {
        Objects.requireNonNull(name);
        IData d = data.get(name);
        if(d instanceof DataCompound) {
            return (DataCompound) d;
        } else if(!strict && d != null && d.canConvertToType(DataType.COMPOUND)) {
            return (DataCompound) d.convertToType(DataType.COMPOUND);
        } else {
            DataCompound c = format().newCompound();
            putData(name, c);
            return c;
        }
    }
    
    @Override
    public DataList childList(String name) {
        Objects.requireNonNull(name);
        IData d = data.get(name);
        if(d instanceof DataList) {
            return (DataList) d;
        } else if(!strict && d != null && d.canConvertToType(DataType.LIST)) {
            return (DataList) d.convertToType(DataType.LIST);
        } else {
            DataList l = format().newList();
            putData(name, l);
            return l;
        }
    }
    
    @Override
    public void clear() {
    	data.clear();
    }
    
    @Override
    public Iterator<Map.Entry<String, IData>> iterator() {
    	return data.entrySet().iterator();
    }
    
    @Override
    public void forEach(BiConsumer<String, IData> action) {
        data.forEach(action);
    }
    
    @Override
    public DataCompound setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }
    
    @Override
    public boolean isStrict() {
        return strict;
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    /**
     * Called when this compound is modified. Subclasses (namely JsonCompound)
     * may use this to set a dirty flag.
     */
    protected void setDirty() {
        // nothing in the default impl
    }
    
    /**
     * toString() with a prefix passed so that this can recursively call itself
     * when one MapCompound is nested in another
     */
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Iterator<Map.Entry<String, IData>> itr = data.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<String, IData> e = itr.next();
            sb.append(pre);
            sb.append("\"");
            sb.append(e.getKey());
            sb.append("\": ");
            if(e.getValue() instanceof AbstractDataCompound)
                sb.append(((AbstractDataCompound) e.getValue()).toString(pre));
            else
                sb.append(e.getValue().toString());
            if(itr.hasNext())
                sb.append(',');
            sb.append('\n');
        }
        
        sb.append(prefix);
        sb.append("]");
        
        return sb.toString();
    }
    
}
