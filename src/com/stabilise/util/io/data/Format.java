package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.function.Supplier;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.bytestream.ByteCompound;
import com.stabilise.util.io.data.json.JsonCompound;
import com.stabilise.util.io.data.nbt.NBTCompound;
import com.stabilise.util.io.data.nbt.NBTType;

/**
 * Different file formats.
 */
public enum Format {
    
    /**
     * The standard NBT format, as used by Minecraft.
     */
    NBT(NBTCompound::new) {
        
        @Override
        public DataCompound read(DataInStream in) throws IOException {
            NBTCompound c = new NBTCompound();
            if(in.readByte() != NBTType.COMPOUND.id)
                throw new IOException("Root tag must be a named compound");
            in.readUTF(); // discard root name
            c.readData(in);
            return c;
        }
        
        @Override
        public void write(DataCompound o, DataOutStream out) throws IOException {
            out.writeByte(NBTType.COMPOUND.id);
            out.writeUTF(""); // dummy root name
            o.writeData(out);
        }
    },
    
    /**
     * A slightly simplified NBT format which doesn't write the ID and tag name
     * for the root compound tag.
     */
    NBT_SIMPLE(NBTCompound::new),
    
    /**
     * This format writes data as a byte stream. Under this format, field names
     * are ignored and the data is simply written sequentially. This provides
     * optimisation for space, but is generally harder to work with.
     * 
     * <p>This format is <i>extremely</i> volatile, and readers/writers must
     * take extreme care to read/write the data in exactly the same way, as a
     * desync in protocol could cause corruption of data.
     * 
     * <p>Furthermore, this format generally doesn't play nice with other
     * formats; a compound of this format cannot be converted to other formats,
     * and compounds of other formats do not translate perfectly into this
     * format for technical reasons (see: NBTList).
     */
    BYTE_STREAM(ByteCompound::new),
    
    /**
     * The JSON format that everyone knows and loves.
     */
    JSON(JsonCompound::new);
    
    // ------------------------------------------------------------------------
    
    private final Supplier<DataCompound> sup;
    
    private Format(Supplier<DataCompound> sup) {
        this.sup = sup;
    }
    
    /**
     * Creates a new compound of this format. The returned compound is not
     * guaranteed to be in either read or write mode.
     */
    public DataCompound create() {
        return sup.get();
    }
    
    /**
     * Creates a new compound in read/write mode.
     * 
     * @param writeMode true for write mode, false for read mode.
     */
    public DataCompound create(boolean writeMode) {
        DataCompound c = create();
        if(writeMode)
            c.setWriteMode();
        else
            c.setReadMode();
        return c;
    }
    
    /**
     * Reads a DataObject from the given input stream and returns it.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public DataCompound read(DataInStream in) throws IOException {
        DataCompound o = create();
        o.readData(in);
        return o;
    }
    
    /**
     * Writes the given DataObject to an output stream.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void write(DataCompound o, DataOutStream out) throws IOException {
        o.writeData(out);
    }
    
}
