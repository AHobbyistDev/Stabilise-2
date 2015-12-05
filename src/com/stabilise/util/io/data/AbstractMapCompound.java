package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

import java.util.LinkedHashMap;
import java.util.Map;
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


public abstract class AbstractMapCompound extends AbstractCompound {
    
    protected final Map<String, Tag> data = new LinkedHashMap<>();
    
    @Override
    public boolean contains(String name) {
        return data.containsKey(name);
    }
    
    @Override public abstract DataCompound getCompound(String name);
    @Override public abstract DataList getList(String name);
    
    @SuppressWarnings("unchecked")
    protected <T extends Tag> Option<T> get(String name, Class<T> c) {
        Object o = data.get(name);
        return c.isInstance(o) ? Option.some((T)o) : Option.none();
    }
    
    @Override
    public <T extends Tag> T put(String name, T t) {
        data.put(name, t);
        return t;
    }
    
    protected final void forEachTag(BiConsumer<String, Tag> action) {
        for(Map.Entry<String, Tag> pair : data.entrySet()) {
            action.accept(pair.getKey(), pair.getValue());
        }
    }
    
    // DON'T YOU JUST LOVE HOW WONDERFULLY REPETITIVE THIS IS
    
    @Override public void put(String name, boolean data) { put(name, box(data)); }
    @Override public void put(String name, byte data) { put(name, box(data)); }
    @Override public void put(String name, char data) { put(name, box(data)); }
    @Override public void put(String name, double data) { put(name, box(data)); }
    @Override public void put(String name, float data) { put(name, box(data)); }
    @Override public void put(String name, int data) { put(name, box(data)); }
    @Override public void put(String name, long data) { put(name, box(data)); }
    @Override public void put(String name, short data) { put(name, box(data)); }
    @Override public void put(String name, String data) { put(name, box(data)); }
    @Override public void put(String name, byte[] data) { put(name, box(data)); }
    @Override public void put(String name, int[] data) { put(name, box(data)); }
    
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
    
    @Override
    public DataCompound convert(Format format) {
        if(format == format()) return this;
        AbstractCompound c = (AbstractCompound) format.create(true);
        for(Map.Entry<String, Tag> e : data.entrySet())
            c.put(e.getKey(), e.getValue());
        c.setReadMode();
        return c;
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Map.Entry<String, Tag> e : data.entrySet()) {
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
