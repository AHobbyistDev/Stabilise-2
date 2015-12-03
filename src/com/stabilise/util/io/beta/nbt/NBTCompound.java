package com.stabilise.util.io.beta.nbt;

import static com.stabilise.util.box.Boxes.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javaslang.control.Option;

import com.stabilise.util.box.*;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.Sendable;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;
import com.stabilise.util.io.beta.Exportable;
import com.stabilise.util.io.beta.ValueExportable;


public class NBTCompound implements DataObject {
    
    private boolean write;
    private final Map<String, Sendable> data = new LinkedHashMap<>();
    
    
    public NBTCompound() {
        write = true;
    }
    
    NBTCompound(NBTCompound parent) {
        write = parent.write;
    }
    
    public static void writeTag(DataOutStream out, String name, Sendable tag) throws IOException {
        out.writeByte(NBTType.tagID(tag));
        out.writeUTF(name);
        tag.writeData(out);
    }
    
    public static NBTCompound readTag(DataInStream in) throws IOException {
        NBTCompound c = new NBTCompound();
        if(in.readByte() != NBTType.tagID(c))
            throw new IOException("Root tag must be a named compound");
        in.readUTF(); // get rid of unwanted name
        c.readData(in);
        return c;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        for(Map.Entry<String, Sendable> tag : data.entrySet()) {
            writeTag(out, tag.getKey(), tag.getValue());
        }
        
        out.writeByte(0); // 0 == compound end
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        byte id;
        while((id = in.readByte()) != 0) { // 0 == compound end
            String name = in.readUTF();
            Sendable tag = NBTType.createTag(id);
            tag.readData(in);
            data.put(name, tag);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> Option<T> get(String name, Class<T> c) {
        Object o = data.get(name);
        return c.isInstance(o) ? Option.some((T)o) : Option.none();
    }
    
    private <T extends Sendable> T put(String name, T t) {
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
        return get(name, BoolBox.class).orElseGet(() -> box(false)).get();
    }
    
    @Override
    public byte readByte(String name) throws IOException {
        return get(name, ByteBox.class).orElseGet(() -> box((byte)0)).get();
    }
    
    @Override
    public char readChar(String name) throws IOException {
        return get(name, CharBox.class).orElseGet(() -> box((char)0)).get();
    }
    
    @Override
    public double readDouble(String name) throws IOException {
        return get(name, DoubleBox.class).orElseGet(() -> box(0.0)).get();
    }
    
    @Override
    public float readFloat(String name) throws IOException {
        return get(name, FloatBox.class).orElseGet(() -> box(0f)).get();
    }
    
    @Override
    public int readInt(String name) throws IOException {
        return get(name, IntBox.class).orElseGet(() -> box(0)).get();
    }
    
    @Override
    public long readLong(String name) throws IOException {
        return get(name, LongBox.class).orElseGet(() -> box(0L)).get();
    }
    
    @Override
    public short readShort(String name) throws IOException {
        return get(name, ShortBox.class).orElseGet(() -> box((short)0)).get();
    }
    
    @Override
    public String readString(String name) throws IOException {
        return get(name, StringBox.class).orElseGet(() -> box("")).get();
    }
    
    @Override
    public byte[] readByteArr(String name) throws IOException {
        return get(name, ByteArrBox.class).orElseGet(() -> box(new byte[0])).get();
    }
    
    @Override
    public int[] readIntArr(String name) throws IOException {
        return get(name, IntArrBox.class).orElseGet(() -> box(new int[0])).get();
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Map.Entry<String, Sendable> e : data.entrySet()) {
            sb.append(pre);
            sb.append("\"");
            sb.append(e.getKey());
            sb.append("\": ");
            if(e.getValue() instanceof NBTCompound)
                sb.append(((NBTCompound) e.getValue()).toString(pre));
            else
                sb.append(e.getValue().toString());
            sb.append(",\n");
        }
        
        sb.append(prefix);
        sb.append("]");

        return sb.toString();
    }
    
}
