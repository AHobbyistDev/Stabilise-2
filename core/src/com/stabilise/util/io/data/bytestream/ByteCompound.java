package com.stabilise.util.io.data.bytestream;

import java.io.IOException;

import javaslang.control.Option;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.IOUtil.IORunnable;
import com.stabilise.util.io.data.AbstractCompound;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ITag;

@Incomplete
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
    
    // From ValueExportable
    @Override
    public void io(String name, DataCompound o, boolean write) {
        throw new UnsupportedOperationException("NYI");
    }
    
    // From ValueExportable
    @Override
    public void io(DataList l, boolean write) {
        throw new UnsupportedOperationException("NYI");
    }
    
    @Override
    public void setWriteMode() {
        if(writeMode) return;
        super.setWriteMode();
        reader = null;
        writer = new DataOutStream(new ByteOutStream(this));
        size = 0;
    }
    
    @Override
    public void setReadMode() {
        if(!writeMode) return;
        super.setReadMode();
        writer = null;
        reader = new DataInStream(new ByteInStream(this));
    }
    
    @Override
    public boolean contains(String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public DataCompound createCompound(String name) {
        return this;
    }
    
    @Override
    public DataList createList(String name) {
        return new ByteList(this);
    }
    
    private void tryWrite(IORunnable r) {
        try {
            r.run();
        } catch(IOException e) {
            throw new AssertionError("This shouldn't happen!", e);
        }
    }
    
    @Override
    public <T extends ITag> T putData(String name, T t) {
        checkCanWrite();
        // Due to this, to ensure compatibility, we need to make sure that all
        // all put() and all get() methods thus do so in the same way as the
        // Box classes.
        tryWrite(() -> t.writeData(writer));
        return t;
    }
    
    // WOW I LOVE REPETITION DON'T YOU???
    
    @Override
    public void put(String name, boolean data) {
        checkCanWrite();
        tryWrite(() -> writer.writeBoolean(data));
    }
    
    @Override
    public void put(String name, byte data) {
        checkCanWrite();
        tryWrite(() -> writer.writeByte(data));
    }
    
    @Override
    public void put(String name, char data) {
        checkCanWrite();
        tryWrite(() -> writer.writeChar(data));
    }
    
    @Override
    public void put(String name, double data) {
        checkCanWrite();
        tryWrite(() -> writer.writeDouble(data));
    }
    
    @Override
    public void put(String name, float data) {
        checkCanWrite();
        tryWrite(() -> writer.writeFloat(data));
    }
    
    @Override
    public void put(String name, int data) {
        checkCanWrite();
        tryWrite(() -> writer.writeInt(data));
    }
    
    @Override
    public void put(String name, long data) {
        checkCanWrite();
        tryWrite(() -> writer.writeLong(data));
    }
    
    @Override
    public void put(String name, short data) {
        checkCanWrite();
        tryWrite(() -> writer.writeShort(data));
    }
    
    @Override
    public void put(String name, String data) {
        checkCanWrite();
        tryWrite(() -> writer.writeUTF(data));
    }
    
    @Override
    public void put(String name, byte[] data) {
        checkCanWrite();
        tryWrite(() -> {
            writer.writeInt(data.length);
            writer.write(data);
        });
    }
    
    @Override
    public void put(String name, int[] data) {
        checkCanWrite();
        tryWrite(() -> {
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
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public byte getByte(String name) {
        checkCanRead();
        try {
            return reader.readByte();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public char getChar(String name) {
        checkCanRead();
        try {
            return reader.readChar();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public double getDouble(String name) {
        checkCanRead();
        try {
            return reader.readDouble();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public float getFloat(String name) {
        checkCanRead();
        try {
            return reader.readFloat();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int getInt(String name) {
        checkCanRead();
        try {
            return reader.readInt();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public long getLong(String name) {
        checkCanRead();
        try {
            return reader.readLong();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public short getShort(String name) {
        checkCanRead();
        try {
            return reader.readShort();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String getString(String name) {
        checkCanRead();
        try {
            return reader.readUTF();
        } catch(IOException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }
    
    // Promoting to package-private visibility
    void checkCanRead2() { checkCanRead(); }
    void checkCanWrite2() { checkCanWrite(); }
    
    @Override
    public Format format() {
        return Format.BYTE_STREAM;
    }
    
    @Override
    public DataCompound convert(Format format) {
        if(format == format()) return this;
        throw new UnsupportedOperationException("Cannot convert from the "
                + "byte array format to another!");
    }
    
    @Override
    public String toString() {
        return "ByteCompound[" + size + " bytes]";
    }

    @Override
    public void put(String name, DataCompound data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void put(String name, DataList data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public DataCompound getCompound(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataList getList(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<DataCompound> optCompound(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<DataList> optList(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Boolean> optBool(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Byte> optByte(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Character> optChar(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Double> optDouble(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Float> optFloat(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Integer> optInt(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Long> optLong(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<Short> optShort(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<String> optString(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<byte[]> optByteArr(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Option<int[]> optIntArr(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(AbstractCompound c) {
        // TODO Auto-generated method stub
        
    }
    
}
