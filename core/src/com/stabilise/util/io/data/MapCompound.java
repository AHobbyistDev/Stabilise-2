package com.stabilise.util.io.data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import javaslang.control.Option;

import com.stabilise.util.box.*;

/**
 * Implementation for a DataCompound backed by a hash map. External code should
 * avoid referring to this class directly and use the more general DataCompound
 * wherever possible.
 */
public abstract class MapCompound extends AbstractCompound implements Iterable<Map.Entry<String, ITag>> {
    
    protected final Map<String, ITag> data = new LinkedHashMap<>();
    
    
    @Override
    public boolean contains(String name) {
        return data.containsKey(name);
    }
    
    @Override public boolean containsCompound(String name) { return data.get(name) instanceof DataCompound; }
    @Override public boolean containsList    (String name) { return data.get(name) instanceof DataList;     }
    @Override public boolean containsBool    (String name) { return data.get(name) instanceof BoolBox;      }
    @Override public boolean containsI8      (String name) { return data.get(name) instanceof I8Box;        }
    @Override public boolean containsI16     (String name) { return data.get(name) instanceof I16Box;       }
    @Override public boolean containsI32     (String name) { return data.get(name) instanceof I32Box;       }
    @Override public boolean containsI64     (String name) { return data.get(name) instanceof I64Box;       }
    @Override public boolean containsF32     (String name) { return data.get(name) instanceof F32Box;       }
    @Override public boolean containsF64     (String name) { return data.get(name) instanceof F64Box;       }
    @Override public boolean containsI8Arr   (String name) { return data.get(name) instanceof I8ArrBox;     }
    @Override public boolean containsI32Arr  (String name) { return data.get(name) instanceof I32ArrBox;    }
    @Override public boolean containsI64Arr  (String name) { return data.get(name) instanceof I64ArrBox;    }
    @Override public boolean containsF32Arr  (String name) { return data.get(name) instanceof F32ArrBox;    }
    @Override public boolean containsF64Arr  (String name) { return data.get(name) instanceof F64ArrBox;    }
    @Override public boolean containsString  (String name) { return data.get(name) instanceof StringBox;    }
    
    @Override
    public DataCompound childCompound(String name) {
        ITag c = data.get(name);
        return c instanceof DataCompound ? (DataCompound)c : putData(name, format().newCompound());
    }
    
    @Override
    public DataList childList(String name) {
        ITag c = data.get(name);
        return c instanceof DataList ? (DataList)c : putData(name, format().newList());
    }
    
    //@SuppressWarnings("unchecked")
    //protected <T extends ITag> T getRaw(String name, Class<T> c) {
    //    ITag t = data.get(name);
    //    return c.isInstance(t) ? (T)t : null;
    //}
    
    /**
     * Gets the raw data value mapped to the given name. Obviously it is better
     * to use the proper get() or opt() family of methods for most cases, so
     * only use this if you know what you're doing.
     * 
     * <p>This method is public for convenience; external code should try to
     * avoid using this if possible.
     * 
     * @return null if a tag by the given name isn't present in this compound.
     */
    public ITag getData(String name) {
    	return data.get(name);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>For a MapCompound, <i>every</i> insertion into this compound goes
     * through this method.
     */
    @Override
    public <T extends ITag> T putData(String name, T t) {
        // TODO: convert compounds and lists to the right format
        data.put(Objects.requireNonNull(name), t);
        return t;
    }
    
    @Override
    protected void putInto(AbstractCompound target) {
        forEachTag(target::putData);
    }
    
    /**
     * Performs the given action on each key-tag pair in this compound.
     * Exposed for convenience.
     */
    public final void forEachTag(BiConsumer<String, ITag> action) {
        data.forEach((key, value) -> action.accept(key, value));
    }
    
    @Override public void put(String name, DataCompound data) { putData(name, (ITag)data.convert(format())); }
    @Override public void put(String name, DataList data) { putData(name, (ITag)data.convert(format())); }
    @Override public void put(String name, boolean data)  { putData(name, Boxes.box(data)); }
    @Override public void put(String name, byte data)     { putData(name, Boxes.box(data)); }
    @Override public void put(String name, short data)    { putData(name, Boxes.box(data)); }
    @Override public void put(String name, int data)      { putData(name, Boxes.box(data)); }
    @Override public void put(String name, long data)     { putData(name, Boxes.box(data)); }
    @Override public void put(String name, float data)    { putData(name, Boxes.box(data)); }
    @Override public void put(String name, double data)   { putData(name, Boxes.box(data)); }
    @Override public void put(String name, byte[] data)   { putData(name, Boxes.box(data)); }
    @Override public void put(String name, int[] data)    { putData(name, Boxes.box(data)); }
    @Override public void put(String name, long[] data)   { putData(name, Boxes.box(data)); }
    @Override public void put(String name, float[] data)  { putData(name, Boxes.box(data)); }
    @Override public void put(String name, double[] data) { putData(name, Boxes.box(data)); }
    @Override public void put(String name, String data)   { putData(name, Boxes.box(data)); }
    
    @Override
    public DataCompound getCompound(String name) {
        ITag t = data.get(name);
        return t instanceof DataCompound ? (DataCompound)t : format().newCompound();
    }
    
    @Override
    public DataList getList(String name) {
        ITag t = data.get(name);
        return t instanceof DataList ? (DataList)t : format().newList();
    }
    
    @Override
    public boolean getBool(String name) {
        ITag t = data.get(name);
        return t instanceof BoolBox ? ((BoolBox)t).get() : BoolBox.defaultValue();
    }
    
    @Override
    public byte getI8(String name) {
        ITag t = data.get(name);
        return t instanceof I8Box ? ((I8Box)t).get() : I8Box.defaultValue();
    }
    
    @Override
    public short getI16(String name) {
        ITag t = data.get(name);
        return t instanceof I16Box ? ((I16Box)t).get() : I16Box.defaultValue();
    }
    
    @Override
    public int getI32(String name) {
        ITag t = data.get(name);
        return t instanceof I32Box ? ((I32Box)t).get() : I32Box.defaultValue();
    }
    
    @Override
    public long getI64(String name) {
        ITag t = data.get(name);
        return t instanceof I64Box ? ((I64Box)t).get() : I64Box.defaultValue();
    }
    
    @Override
    public float getF32(String name) {
        ITag t = data.get(name);
        return t instanceof F32Box ? ((F32Box)t).get() : F32Box.defaultValue();
    }
    
    @Override
    public double getF64(String name) {
        ITag t = data.get(name);
        return t instanceof F64Box ? ((F64Box)t).get() : F64Box.defaultValue();
    }
    
    @Override
    public byte[] getI8Arr(String name) {
        ITag t = data.get(name);
        return t instanceof I8ArrBox ? ((I8ArrBox)t).get() : I8ArrBox.defaultValue();
    }
    
    @Override
    public int[] getI32Arr(String name) {
        ITag t = data.get(name);
        return t instanceof I32ArrBox ? ((I32ArrBox)t).get() : I32ArrBox.defaultValue();
    }
    
    @Override
    public long[] getI64Arr(String name) {
        ITag t = data.get(name);
        return t instanceof I64ArrBox ? ((I64ArrBox)t).get() : I64ArrBox.defaultValue();
    }
    
    @Override
    public float[] getF32Arr(String name) {
        ITag t = data.get(name);
        return t instanceof F32ArrBox ? ((F32ArrBox)t).get() : F32ArrBox.defaultValue();
    }
    
    @Override
    public double[] getF64Arr(String name) {
        ITag t = data.get(name);
        return t instanceof F64ArrBox ? ((F64ArrBox)t).get() : F64ArrBox.defaultValue();
    }
    
    @Override
    public String getString(String name) {
        ITag t = data.get(name);
        return t instanceof StringBox ? ((StringBox)t).get() : StringBox.defaultValue();
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends ITag> Option<T> opt(String name, Class<T> c) {
        ITag t = data.get(name);
        return c.isInstance(t) ? Option.some((T)t) : Option.none();
    }
    
    public Option<DataCompound> optCompound(String name) { return opt(name, DataCompound.class);                  }
    public Option<DataList>     optList    (String name) { return opt(name, DataList.class);                      }
    public Option<Boolean>      optBool    (String name) { return opt(name, BoolBox.class).map(BoolBox::get);     }
    public Option<Byte>         optI8      (String name) { return opt(name, I8Box.class).map(I8Box::get);         }
    public Option<Short>        optI16     (String name) { return opt(name, I16Box.class).map(I16Box::get);       }
    public Option<Integer>      optI32     (String name) { return opt(name, I32Box.class).map(I32Box::get);       }
    public Option<Long>         optI64     (String name) { return opt(name, I64Box.class).map(I64Box::get);       }
    public Option<Float>        optF32     (String name) { return opt(name, F32Box.class).map(F32Box::get);       }
    public Option<Double>       optF64     (String name) { return opt(name, F64Box.class).map(F64Box::get);       }
    public Option<byte[]>       optI8Arr   (String name) { return opt(name, I8ArrBox.class).map(I8ArrBox::get);   }
    public Option<int[]>        optI32Arr  (String name) { return opt(name, I32ArrBox.class).map(I32ArrBox::get); }
    public Option<long[]>       optI64Arr  (String name) { return opt(name, I64ArrBox.class).map(I64ArrBox::get); }
    public Option<float[]>      optF32Arr  (String name) { return opt(name, F32ArrBox.class).map(F32ArrBox::get); }
    public Option<double[]>     optF64Arr  (String name) { return opt(name, F64ArrBox.class).map(F64ArrBox::get); }
    public Option<String>       optString  (String name) { return opt(name, StringBox.class).map(b -> b.get());   }
    
    @Override
    public DataCompound convert(Format format) {
        if(format().sameTypeAs(format))
            return this;
        AbstractCompound c = (AbstractCompound) format.newCompound();
        c.putAll(this);
        return c;
    }
    
    /**
     * Clears this compound.
     */
    public void clear() {
    	data.clear();
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, this);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        o.optCompound(name).peek(c -> putAll((c)));
    }
    
    @Override
    public void write(DataList l) {
        l.add(this);
    }
    
    @Override
    public void read(DataList l) {
        putAll(l.getCompound());
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Iterator<Map.Entry<String, ITag>> itr = data.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<String, ITag> e = itr.next();
            sb.append(pre);
            sb.append("\"");
            sb.append(e.getKey());
            sb.append("\": ");
            if(e.getValue() instanceof MapCompound)
                sb.append(((MapCompound) e.getValue()).toString(pre));
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
    
    @Override
    public Iterator<Map.Entry<String, ITag>> iterator() {
    	return data.entrySet().iterator();
    }
    
}
