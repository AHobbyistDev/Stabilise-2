package com.stabilise.util.io.beta;

import static com.stabilise.util.box.Boxes.box;

import java.util.LinkedHashMap;
import java.util.Map;

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
import com.stabilise.util.io.Sendable;


public abstract class AbstractMapObject extends AbstractDataObject {
    
    protected final Map<String, Sendable> data = new LinkedHashMap<>();
    
    @Override public abstract DataObject object(String name);
    @Override public abstract DataList list(String name);
    
    @SuppressWarnings("unchecked")
    protected <T extends Sendable> Option<T> get(String name, Class<T> c) {
        Object o = data.get(name);
        return c.isInstance(o) ? Option.some((T)o) : Option.none();
    }
    
    protected <T extends Sendable> T put(String name, T t) {
        data.put(name, t);
        return t;
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
        return get(name, BoolBox.class).orElseGet(() -> box(false)).get();
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
    
}
