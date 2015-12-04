package com.stabilise.util.io.data.bytestream;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.IOUtil.IORunnable;
import com.stabilise.util.io.data.AbstractCompound;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;


public class ByteCompound extends AbstractCompound {
    
    byte[] buf;
    int size;
    DataInStream reader;
    DataOutStream writer;
    
    
    public ByteCompound() {
        this(1024); // 1kb
    }
    
    public ByteCompound(int len) {
        size = len;
        buf = new byte[len];
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        size = in.readInt();
        buf = new byte[size];
        in.readFully(buf);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(size);
        out.write(buf, 0, size);
    }
    
    public void setWriteMode() {
        writeMode = true;
        reader = null;
        writer = new DataOutStream(new ByteOutStream(this));
        size = 0;
    }
    
    public void setReadMode() {
        writeMode = false;
        writer = null;
        reader = new DataInStream(new ByteInStream(this));
    }
    
    @Override
    public boolean contains(String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public DataCompound getCompound(String name) {
        return this;
    }
    
    @Override
    public DataList getList(String name) {
        return new ByteList(this);
    }
    
    private void tryDo(IORunnable r) {
        try {
            r.run();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    // WOW I LOVE REPETITION DON'T YOU???
    
    @Override
    public void put(String name, boolean data) {
        checkCanWrite();
        tryDo(() -> writer.writeBoolean(data));
    }
    
    @Override
    public void put(String name, byte data) {
        checkCanWrite();
        tryDo(() -> writer.writeByte(data));
    }
    
    @Override
    public void put(String name, char data) {
        checkCanWrite();
        tryDo(() -> writer.writeChar(data));
    }
    
    @Override
    public void put(String name, double data) {
        checkCanWrite();
        tryDo(() -> writer.writeDouble(data));
    }
    
    @Override
    public void put(String name, float data) {
        checkCanWrite();
        tryDo(() -> writer.writeFloat(data));
    }
    
    @Override
    public void put(String name, int data) {
        checkCanWrite();
        tryDo(() -> writer.writeInt(data));
    }
    
    @Override
    public void put(String name, long data) {
        checkCanWrite();
        tryDo(() -> writer.writeLong(data));
    }
    
    @Override
    public void put(String name, short data) {
        checkCanWrite();
        tryDo(() -> writer.writeShort(data));
    }
    
    @Override
    public void put(String name, String data) {
        checkCanWrite();
        tryDo(() -> writer.writeString(data));
    }
    
    @Override
    public void put(String name, byte[] data) {
        checkCanWrite();
        tryDo(() -> {
            writer.writeInt(data.length);
            writer.write(data);
        });
    }
    
    @Override
    public void put(String name, int[] data) {
        checkCanWrite();
        tryDo(() -> {
            writer.writeInt(data.length);
            writer.writeIntArray(data);
        });
    }
    
    @Override
    public boolean getBool(String name) {
        checkCanRead();
        try {
            return reader.readBoolean();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public byte getByte(String name) {
        checkCanRead();
        try {
            return reader.readByte();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public char getChar(String name) {
        checkCanRead();
        try {
            return reader.readChar();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public double getDouble(String name) {
        checkCanRead();
        try {
            return reader.readDouble();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public float getFloat(String name) {
        checkCanRead();
        try {
            return reader.readFloat();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public int getInt(String name) {
        checkCanRead();
        try {
            return reader.readInt();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public long getLong(String name) {
        checkCanRead();
        try {
            return reader.readLong();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public short getShort(String name) {
        checkCanRead();
        try {
            return reader.readShort();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public String getString(String name) {
        checkCanRead();
        try {
            return reader.readString();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public byte[] getByteArr(String name) {
        checkCanRead();
        try {
            byte[] arr = new byte[reader.readInt()];
            reader.read(arr);
            return arr;
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public int[] getIntArr(String name) {
        checkCanRead();
        try {
            int[] arr = new int[reader.readInt()];
            reader.readIntArray(arr);
            return arr;
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    // Promoting to package-private visibility
    void checkCanRead2() { checkCanRead(); }
    void checkCanWrite2() { checkCanWrite(); }
    
}
