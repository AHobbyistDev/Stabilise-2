package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.bytestream.ByteCompound;
import com.stabilise.util.io.data.json.JsonCompound;
import com.stabilise.util.io.data.json.JsonList;
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
    NBT(EquivalenceClass.NBT, NBTCompound::new, NBTList::new) {
        
        @Override
        public DataCompound read(DataInStream in) throws IOException {
            if(in.readByte() != NBTType.COMPOUND.id)
                throw new IOException("Root tag must be a named compound");
            in.readUTF(); // discard root name
            return super.read(in);
        }
        
        @Override
        public void write(DataCompound o, DataOutStream out) throws IOException {
            out.writeByte(NBTType.COMPOUND.id);
            out.writeUTF(""); // dummy root name
            super.write(o, out);
        }
    },
    
    /**
     * A slightly simplified NBT format which doesn't write the ID and tag name
     * for the root compound tag.
     */
    NBT_SIMPLE(EquivalenceClass.NBT, NBTCompound::new, NBTList::new),
    
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
    BYTE_STREAM(EquivalenceClass.BYTES, ByteCompound::new, () -> { throw new UnsupportedOperationException(); }),
    
    /**
     * The JSON format that everyone knows and loves.
     */
    JSON(EquivalenceClass.JSON, JsonCompound::new, JsonList::new);
    
    // ------------------------------------------------------------------------
    
    private final EquivalenceClass equivClass;
    private final Supplier<AbstractCompound> compoundSup;
    private final Supplier<AbstractDataList> listSup;
    
    private Format(EquivalenceClass equivClass, Supplier<AbstractCompound> compoundSup,
            Supplier<AbstractDataList> listSup) {
        this.equivClass = equivClass;
        this.compoundSup = compoundSup;
        this.listSup = listSup;
    }
    
    /**
     * Creates a new compound of this format. The returned compound is not
     * guaranteed to be in either read or write mode.
     */
    public DataCompound newCompound() {
        return compoundSup.get();
    }
    
    /**
     * Creates a new compound in read/write mode.
     * 
     * @param writeMode true for write mode, false for read mode.
     */
    public DataCompound newCompound(boolean writeMode) {
        DataCompound c = newCompound();
        if(writeMode)
            c.setWriteMode();
        else
            c.setReadMode();
        return c;
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
     * Reads a DataObject from the given input stream and returns it.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public DataCompound read(DataInStream in) throws IOException {
        DataCompound o = newCompound();
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
    
    /**
     * Returns true if this format is of the same type as the given format.
     * e.g. {@link #NBT} is of the same type as {@link #NBT_SIMPLE}.
     */
    public boolean sameTypeAs(Format f) {
        return f.equivClass == equivClass;
    }
    
    private static enum EquivalenceClass {
        NBT, JSON, BYTES;
    }
    
    private static final ThreadLocal<Format> DEFAULTS =
            ThreadLocal.withInitial(() -> NBT_SIMPLE);
    
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
