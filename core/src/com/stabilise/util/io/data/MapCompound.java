package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

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
        //return get(name, DataCompound.class)
        //        .orElseGet(() -> putData(name, format().newAbstractCompound()));
    }
    
    @Override
    public DataList childList(String name) {
        ITag c = data.get(name);
        return c instanceof DataList ? (DataList)c : putData(name, format().newList());
        //return get(name, DataList.class)
        //        .orElseGet(() -> putData(name, format().newAbstractList()));
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends ITag> Option<T> get(String name, Class<T> c) {
        ITag t = data.get(name);
        return c.isInstance(t) ? Option.some((T)t) : Option.none();
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
        data.put(Objects.requireNonNull(name), t);
        return t;
    }
    
    @Override
    public void putAll(AbstractCompound c) {
        forEachTag((n, t) -> c.putData(n, t));
    }
    
    /**
     * Performs the given action on each key-tag pair in this compound.
     * Exposed for convenience.
     */
    public final void forEachTag(BiConsumer<String, ITag> action) {
        data.entrySet().forEach(e -> action.accept(e.getKey(), e.getValue()));
    }
    
    // DON'T YOU JUST LOVE HOW WONDERFULLY REPETITIVE THIS IS
    
    @Override public void put(String name, DataCompound data) { putData(name, (ITag)data.convert(format())); }
    @Override public void put(String name, DataList data) { putData(name, (ITag)data.convert(format())); }
    @Override public void put(String name, boolean data)  { putData(name, box(data)); }
    @Override public void put(String name, byte data)     { putData(name, box(data)); }
    @Override public void put(String name, short data)    { putData(name, box(data)); }
    @Override public void put(String name, int data)      { putData(name, box(data)); }
    @Override public void put(String name, long data)     { putData(name, box(data)); }
    @Override public void put(String name, float data)    { putData(name, box(data)); }
    @Override public void put(String name, double data)   { putData(name, box(data)); }
    @Override public void put(String name, byte[] data)   { putData(name, box(data)); }
    @Override public void put(String name, int[] data)    { putData(name, box(data)); }
    @Override public void put(String name, long[] data)   { putData(name, box(data)); }
    @Override public void put(String name, float[] data)  { putData(name, box(data)); }
    @Override public void put(String name, double[] data) { putData(name, box(data)); }
    @Override public void put(String name, String data)   { putData(name, box(data)); }
    
    @Override
    public DataCompound getCompound(String name) {
        return get(name, DataCompound.class)
                .orElseGet(() -> format().newAbstractCompound());
    }
    
    @Override
    public DataList getList(String name) {
        return get(name, DataList.class)
                .orElseGet(() -> format().newAbstractList());
    }
    
    @Override
    public boolean getBool(String name) {
        ITag b = data.get(name);
        return b instanceof BoolBox ? ((BoolBox)b).get() : BoolBox.defaultValue();
    }
    
    @Override
    public byte getI8(String name) {
        ITag b = data.get(name);
        return b instanceof I8Box ? ((I8Box)b).get() : I8Box.defaultValue();
    }
    
    @Override
    public short getI16(String name) {
        ITag b = data.get(name);
        return b instanceof I16Box ? ((I16Box)b).get() : I16Box.defaultValue();
    }
    
    @Override
    public int getI32(String name) {
        ITag b = data.get(name);
        return b instanceof I32Box ? ((I32Box)b).get() : I32Box.defaultValue();
    }
    
    @Override
    public long getI64(String name) {
        ITag b = data.get(name);
        return b instanceof I64Box ? ((I64Box)b).get() : I64Box.defaultValue();
    }
    
    @Override
    public float getF32(String name) {
        ITag b = data.get(name);
        return b instanceof F32Box ? ((F32Box)b).get() : F32Box.defaultValue();
    }
    
    @Override
    public double getF64(String name) {
        ITag b = data.get(name);
        return b instanceof F64Box ? ((F64Box)b).get() : F64Box.defaultValue();
    }
    
    @Override
    public byte[] getI8Arr(String name) {
        ITag b = data.get(name);
        return b instanceof I8ArrBox ? ((I8ArrBox)b).get() : I8ArrBox.defaultValue();
    }
    
    @Override
    public int[] getI32Arr(String name) {
        ITag b = data.get(name);
        return b instanceof I32ArrBox ? ((I32ArrBox)b).get() : I32ArrBox.defaultValue();
    }
    
    @Override
    public long[] getI64Arr(String name) {
        ITag b = data.get(name);
        return b instanceof I64ArrBox ? ((I64ArrBox)b).get() : I64ArrBox.defaultValue();
    }
    
    @Override
    public float[] getF32Arr(String name) {
        ITag b = data.get(name);
        return b instanceof F32ArrBox ? ((F32ArrBox)b).get() : F32ArrBox.defaultValue();
    }
    
    @Override
    public double[] getF64Arr(String name) {
        ITag b = data.get(name);
        return b instanceof F64ArrBox ? ((F64ArrBox)b).get() : F64ArrBox.defaultValue();
    }
    
    @Override
    public String getString(String name) {
        ITag b = data.get(name);
        return b instanceof StringBox ? ((StringBox)b).get() : StringBox.defaultValue();
    }
    
    public Option<DataCompound> optCompound(String name) { return get(name, DataCompound.class);                }
    public Option<DataList>     optList    (String name) { return get(name, DataList.class);                    }
    public Option<Boolean>      optBool    (String name) { return get(name, BoolBox.class).map(b -> b.get());   }
    public Option<Byte>         optI8      (String name) { return get(name, I8Box.class).map(b -> b.get());     }
    public Option<Short>        optI16     (String name) { return get(name, I16Box.class).map(b -> b.get());    }
    public Option<Integer>      optI32     (String name) { return get(name, I32Box.class).map(b -> b.get());    }
    public Option<Long>         optI64     (String name) { return get(name, I64Box.class).map(b -> b.get());    }
    public Option<Float>        optF32     (String name) { return get(name, F32Box.class).map(b -> b.get());    }
    public Option<Double>       optF64     (String name) { return get(name, F64Box.class).map(b -> b.get());    }
    public Option<byte[]>       optI8Arr   (String name) { return get(name, I8ArrBox.class).map(b -> b.get());  }
    public Option<int[]>        optI32Arr  (String name) { return get(name, I32ArrBox.class).map(b -> b.get()); }
    public Option<long[]>       optI64Arr  (String name) { return get(name, I64ArrBox.class).map(b -> b.get()); }
    public Option<float[]>      optF32Arr  (String name) { return get(name, F32ArrBox.class).map(b -> b.get()); }
    public Option<double[]>     optF64Arr  (String name) { return get(name, F64ArrBox.class).map(b -> b.get()); }
    public Option<String>       optString  (String name) { return get(name, StringBox.class).map(b -> b.get()); }
    
    @Override
    public DataCompound convert(Format format) {
        if(format().sameTypeAs(format))
            return this;
        AbstractCompound c = (AbstractCompound) format.newCompound();
        putAll(c);
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
        o.optCompound(name).peek(c -> ((AbstractCompound)c).putAll(this));
    }
    
    @Override
    public void write(DataList l) {
        l.add(this);
    }
    
    @Override
    public void read(DataList l) {
        ((AbstractCompound) l.getCompound()).putAll(this);
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
