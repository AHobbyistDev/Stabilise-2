package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import com.stabilise.util.Checks;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.json.JsonCompound;
import com.stabilise.util.io.data.nbt.NBTCompound;
import com.stabilise.util.io.data.nbt.NBTList;
import com.stabilise.util.io.data.nbt.NBTType;

/**
 * Different file formats.
 */
public enum Format {
    
    /**
     * The standard NBT format, as used by Minecraft.
     */
    NBT(NBTCompound::new, NBTList::new) {
        
    	// We need to overwrite write the following dummy data in order to be compatible
    	// with minecraft.
    	
        @Override
        public void read(DataInStream in, DataCompound c) throws IOException {
            if(in.readByte() != NBTType.COMPOUND.id)
                throw new IOException("Root tag must be a named compound");
            in.readUTF(); // discard root name
            super.read(in, c);
        }
        
        @Override
        public void write(DataOutStream out, DataCompound c) throws IOException {
            out.writeByte(NBTType.COMPOUND.id);
            out.writeUTF(""); // dummy root name
            super.write(out, c);
        }
    },
    
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
    //BYTE_STREAM(ByteCompound::new, () -> { throw new UnsupportedOperationException(); }),
    
    /**
     * The JSON format that everyone knows and loves.
     */
    //JSON(JsonCompound::new, JsonList::new);
    JSON(JsonCompound::new, () -> {Checks.unsupported("JSON list is NYI"); return null;});
    
    // ------------------------------------------------------------------------
    
    private final Supplier<AbstractCompound> compoundSup;
    private final Supplier<AbstractDataList> listSup;
    
    private Format(Supplier<AbstractCompound> compoundSup, Supplier<AbstractDataList> listSup) {
        this.compoundSup = compoundSup;
        this.listSup = listSup;
    }
    
    /**
     * Creates a new compound of this format.
     */
    public DataCompound newCompound() {
        return compoundSup.get();
    }
    
    /**
     * Creates a new list of this format.
     */
    public DataList newList() {
        return listSup.get();
    }
    
    /** Convenience package-local method. */
    AbstractCompound newAbstractCompound() {
        return compoundSup.get();
    }
    
    /** Convenience package-local method. */
    AbstractDataList newAbstractList() {
        return listSup.get();
    }
    
    /**
     * Reads a DataCompound from the given input stream and returns it.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public DataCompound read(DataInStream in) throws IOException {
        DataCompound c = newCompound();
        read(in, c);
        return c;
    }
    
    /**
     * Reads from the given input stream into the given DataCompound. In
     * addition to invoking {@code c.readData(in)}, this method may perform
     * additional work peculiar to this format.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void read(DataInStream in, DataCompound c) throws IOException {
    	c.readData(in);
    }
    
    /**
     * Writes the given DataCompound to an output stream. In addition to
     * invoking {@code c.writeData(out)}, this method may perform additional
     * work peculiar to this format.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void write(DataOutStream out, DataCompound c) throws IOException {
        c.writeData(out);
    }
    
    /**
     * Returns true if this format is of the same type as the given format.
     */
    public boolean sameTypeAs(Format f) {
        return f == this;
    }
    
    
    
    // Thread-local default format stuff
    
    private static final ThreadLocal<Format> DEFAULTS = ThreadLocal.withInitial(() -> NBT);
    
    /**
     * Sets the default format for use by the current thread.
     * 
     * @throws NullPointerException if {@code format} is {@code null}.
     */
    public static void setDefaultFormat(Format format) {
        DEFAULTS.set(Objects.requireNonNull(format));
    }
    
    /**
     * Returns the default format in use by the current thread.
     */
    public static Format getDefaultFormat() {
        return DEFAULTS.get();
    }
    
}
