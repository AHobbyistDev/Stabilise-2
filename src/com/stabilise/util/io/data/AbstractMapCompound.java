package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import javaslang.control.Option;

import com.stabilise.util.box.BoolBox;
import com.stabilise.util.box.ByteArrBox;
import com.stabilise.util.box.ByteBox;
import com.stabilise.util.box.CharBox;
import com.stabilise.util.box.DoubleBox;
import com.stabilise.util.box.FloatBox;
import com.stabilise.util.box.IntArrBox;
import com.stabilise.util.box.IntBox;
import com.stabilise.util.box.LongBox;
import com.stabilise.util.box.ShortBox;
import com.stabilise.util.box.StringBox;

/**
 * Skeletal implementation for a DataCompound backed by a hash map. This class
 * should never be referred to directly by client code.
 */
public abstract class AbstractMapCompound extends AbstractCompound {
    
    protected final Map<String, ITag> data = new LinkedHashMap<>();
    
    
    @Override
    public boolean contains(String name) {
        return data.containsKey(name);
    }
    
    @Override
    public DataCompound createCompound(String name) {
        return get(name, DataCompound.class)
                .orElseGet(() -> putData(name, (AbstractCompound) format().newCompound()));
    }
    
    @Override
    public DataList createList(String name) {
        return get(name, DataList.class)
                .orElseGet(() -> putData(name, (AbstractDataList) format().newList()));
    }
    
    @SuppressWarnings("unchecked")
    protected <T> Option<T> get(String name, Class<T> c) {
        Object o = data.get(name);
        return c.isInstance(o) ? Option.some((T)o) : Option.none();
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>For an AbstractMapCompound, <i>every</i> insertion into this compound
     * goes through this method.
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
    
    protected final void forEachTag(BiConsumer<String, ITag> action) {
        for(Map.Entry<String, ITag> pair : data.entrySet()) {
            action.accept(pair.getKey(), pair.getValue());
        }
    }
    
    // DON'T YOU JUST LOVE HOW WONDERFULLY REPETITIVE THIS IS
    
    @Override public void put(String name, DataCompound data) { put(name, data.convert(format())); }
    @Override public void put(String name, DataList data) { put(name, data.convert(format())); }
    @Override public void put(String name, boolean data) { putData(name, box(data)); }
    @Override public void put(String name, byte data) { putData(name, box(data)); }
    @Override public void put(String name, char data) { putData(name, box(data)); }
    @Override public void put(String name, double data) { putData(name, box(data)); }
    @Override public void put(String name, float data) { putData(name, box(data)); }
    @Override public void put(String name, int data) { putData(name, box(data)); }
    @Override public void put(String name, long data) { putData(name, box(data)); }
    @Override public void put(String name, short data) { putData(name, box(data)); }
    @Override public void put(String name, String data) { putData(name, box(data)); }
    @Override public void put(String name, byte[] data) { putData(name, box(data)); }
    @Override public void put(String name, int[] data) { putData(name, box(data)); }
    
    @Override
    public DataCompound getCompound(String name) {
        return get(name, DataCompound.class)
                .orElseGet(() -> (AbstractCompound) format().newCompound());
    }
    
    @Override
    public DataList getList(String name) {
        return get(name, DataList.class)
                .orElseGet(() -> (AbstractDataList) format().newList());
    }
    
    @Override
    public boolean getBool(String name) {
        return get(name, BoolBox.class).orElse(BoolBox.FALSE).get();
    }
    
    @Override
    public byte getByte(String name) {
        return get(name, ByteBox.class).orElseGet(() -> box((byte)0)).get();
    }
    
    @Override
    public char getChar(String name) {
        return get(name, CharBox.class).orElseGet(() -> box((char)0)).get();
    }
    
    @Override
    public double getDouble(String name) {
        return get(name, DoubleBox.class).orElseGet(() -> box(0.0)).get();
    }
    
    @Override
    public float getFloat(String name) {
        return get(name, FloatBox.class).orElseGet(() -> box(0f)).get();
    }
    
    @Override
    public int getInt(String name) {
        return get(name, IntBox.class).orElseGet(() -> box(0)).get();
    }
    
    @Override
    public long getLong(String name) {
        return get(name, LongBox.class).orElseGet(() -> box(0L)).get();
    }
    
    @Override
    public short getShort(String name) {
        return get(name, ShortBox.class).orElseGet(() -> box((short)0)).get();
    }
    
    @Override
    public String getString(String name) {
        return get(name, StringBox.class).orElseGet(() -> box("")).get();
    }
    
    @Override
    public byte[] getByteArr(String name) {
        return get(name, ByteArrBox.class).orElseGet(() -> box(new byte[0])).get();
    }
    
    @Override
    public int[] getIntArr(String name) {
        return get(name, IntArrBox.class).orElseGet(() -> box(new int[0])).get();
    }
    
    public Option<DataCompound> optCompound(String name) { return get(name, DataCompound.class); }
    public Option<DataList>     optList    (String name) { return get(name, DataList.class); }
    public Option<Boolean>      optBool    (String name) { return get(name, BoolBox.class).map(b -> b.get()); }
    public Option<Byte>         optByte    (String name) { return get(name, ByteBox.class).map(b -> b.get()); }
    public Option<Character>    optChar    (String name) { return get(name, CharBox.class).map(b -> b.get()); }
    public Option<Double>       optDouble  (String name) { return get(name, DoubleBox.class).map(b -> b.get()); }
    public Option<Float>        optFloat   (String name) { return get(name, FloatBox.class).map(b -> b.get()); }
    public Option<Integer>      optInt     (String name) { return get(name, IntBox.class).map(b -> b.get()); }
    public Option<Long>         optLong    (String name) { return get(name, LongBox.class).map(b -> b.get()); }
    public Option<Short>        optShort   (String name) { return get(name, ShortBox.class).map(b -> b.get()); }
    public Option<String>       optString  (String name) { return get(name, StringBox.class).map(b -> b.get()); }
    public Option<byte[]>       optByteArr (String name) { return get(name, ByteArrBox.class).map(b -> b.get()); }
    public Option<int[]>        optIntArr  (String name) { return get(name, IntArrBox.class).map(b -> b.get()); }
    
    @Override
    public DataCompound convert(Format format) {
        if(format().sameTypeAs(format)) {
            setReadMode();
            return this;
        }
        AbstractCompound c = (AbstractCompound) format.newCompound(true);
        putAll(c);
        c.setReadMode();
        return c;
    }
    
    // From ValueExportable
    @Override
    public void io(String name, DataCompound o, boolean write) {
        if(write)
            o.put(name, this);
        else
            o.optCompound(name).peek((c) -> ((AbstractCompound)c).putAll(this));
    }
    
    // From ValueExportable
    @Override
    public void io(DataList l, boolean write) {
        if(write)
            l.add(this);
        else
            ((AbstractCompound) l.getCompound()).putAll(this);
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Map.Entry<String, ITag> e : data.entrySet()) {
            sb.append(pre);
            sb.append("\"");
            sb.append(e.getKey());
            sb.append("\": ");
            if(e.getValue() instanceof AbstractMapCompound)
                sb.append(((AbstractMapCompound) e.getValue()).toString(pre));
            else
                sb.append(e.getValue().toString());
            sb.append(",\n");
        }
        
        sb.append(prefix);
        sb.append("]");

        return sb.toString();
    }
    
}
