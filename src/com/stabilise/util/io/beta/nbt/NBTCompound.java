package com.stabilise.util.io.beta.nbt;

import static com.stabilise.util.box.Boxes.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javaslang.control.Option;

import com.stabilise.util.box.*;
import com.stabilise.util.collect.registry.Registries;
import com.stabilise.util.collect.registry.Registry;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;
import com.stabilise.util.io.beta.Exportable;
import com.stabilise.util.io.beta.ValueExportable;


public class NBTCompound implements DataObject {
    
    static final byte
            COMPOUND_END = 0x0,
            BYTE         = 0x1,
            SHORT        = 0x2,
            INT          = 0x3,
            LONG         = 0x4,
            FLOAT        = 0x5,
            DOUBLE       = 0x6,
            BYTE_ARRAY   = 0x7,
            STRING       = 0x8,
            LIST         = 0x9,
            COMPOUND     = 0xA,
            INT_ARRAY    = 0xB;
    
    private static final Registry<Class<?>, Byte> ids = Registries.registry();
    
    static {
        ids.register(Byte.class, BYTE);
        ids.register(Short.class, SHORT);
        ids.register(Integer.class, INT);
        ids.register(Long.class, LONG);
        ids.register(Float.class, FLOAT);
        ids.register(Double.class, DOUBLE);
        ids.register(byte[].class, BYTE_ARRAY);
        ids.register(int[].class, INT_ARRAY);
        ids.register(String.class, STRING);
        ids.register(NBTCompound.class, COMPOUND);
        ids.register(NBTList.class, LIST);
        
        ids.lock();
    }
    
    
    private boolean write;
    private final Map<String, Object> data = new LinkedHashMap<>();
    
    public NBTCompound() {
        write = true;
    }
    
    NBTCompound(NBTCompound parent) {
        write = parent.write;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(COMPOUND);
        
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        
    }
    
    @SuppressWarnings("unchecked")
    private <T> Option<T> get(String name, Class<T> c) {
        Object o = data.get(name);
        return c.isInstance(o) ? Option.some((T)o) : Option.none();
    }
    
    private <T> T put(String name, T t) {
        data.put(name, t);
        return t;
    }
    
    @Override
    public DataObject object(String name) {
        return get(name, NBTCompound.class).orElseGet(() -> put(name, new NBTCompound(this))); 
    }
    
    @Override
    public DataList list(String name) {
        return get(name, NBTList.class).orElseGet(() -> put(name, new NBTList()));
    }
    
    @Override
    public void io(String name, Exportable data) throws IOException {
        data.io(object(name), write);
    }
    
    @Override
    public void io(String name, ValueExportable data) throws IOException {
        data.io(name, this, write);
    }
    
    // LOOK AT HOW WONDERFULLY REPETITIVE THIS IS
    
    @Override public void write(String name, boolean data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, byte data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, char data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, double data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, float data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, int data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, long data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, short data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, String data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, byte[] data) throws IOException { this.data.put(name, box(data)); }
    @Override public void write(String name, int[] data) throws IOException { this.data.put(name, box(data)); }
    
    @Override
    public boolean readBool(String name) throws IOException {
        return get(name, Boolean.class).orElse(false);
    }
    
    @Override
    public byte readByte(String name) throws IOException {
        return get(name, Byte.class).orElse((byte)0);
    }
    
    @Override
    public char readChar(String name) throws IOException {
        return get(name, Character.class).orElse((char)0);
    }
    
    @Override
    public double readDouble(String name) throws IOException {
        return get(name, Double.class).orElse(0.0);
    }
    
    @Override
    public float readFloat(String name) throws IOException {
        return get(name, Float.class).orElse(0f);
    }
    
    @Override
    public int readInt(String name) throws IOException {
        return get(name, Integer.class).orElse(0);
    }
    
    @Override
    public long readLong(String name) throws IOException {
        return get(name, Long.class).orElse(0L);
    }
    
    @Override
    public short readShort(String name) throws IOException {
        return get(name, Short.class).orElse((short)0);
    }
    
    @Override
    public String readString(String name) throws IOException {
        return get(name, String.class).orElse("");
    }
    
    @Override
    public byte[] readByteArr(String name) throws IOException {
        return get(name, byte[].class).orElse(new byte[0]);
    }
    
    @Override
    public int[] readIntArr(String name) throws IOException {
        return get(name, int[].class).orElse(new int[0]);
    }
    
}
