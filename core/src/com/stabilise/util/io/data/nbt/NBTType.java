package com.stabilise.util.io.data.nbt;

import java.util.function.Supplier;

import com.stabilise.util.box.*;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.io.data.IData;


/**
 * Enumerates different types of NBT tags.
 */
public enum NBTType {
    
    BYTE        (1,  I8Box.class),
    SHORT       (2,  I16Box.class),
    INT         (3,  I32Box.class),
    LONG        (4,  I64Box.class),
    FLOAT       (5,  F32Box.class),
    DOUBLE      (6,  F64Box.class),
    BYTE_ARRAY  (7,  I8ArrBox.class),
    STRING      (8,  StringBox.class),
    LIST        (9,  NBTList.class, NBTList::new),
    COMPOUND    (10, NBTCompound.class, NBTCompound::new),
    INT_ARRAY   (11, I32ArrBox.class),
    LONG_ARRAY  (12, I64ArrBox.class),
    /** Note: everything here onwards isn't part of the original NBT
     * specification, so I'm leaving a gap in IDs in case minecraft expands. */
    FLOAT_ARRAY (20, F32ArrBox.class),
    DOUBLE_ARRAY(21, F64ArrBox.class),
    BOOLEAN     (22, BoolBox.class);
    
    
    public final byte id;
    private final Class<? extends IData> type;
    private final Supplier<IData> fac;
    
    NBTType(int id, Class<? extends IData> type) {
        this(id, type, null);
    }
    
    NBTType(int id, Class<? extends IData> type, Supplier<IData> fac) {
        this.id = (byte)id;
        this.type = type;
        this.fac = fac;
    }
    
    private static final TypeFactory<IData> registry =
            new TypeFactory<>(new RegistryParams("NBTTypeRegistry"));
    
    static {
        for(NBTType t : NBTType.values()) {
            if(t.fac == null)
                registry.registerUnsafe(t.id, t.type);
            else
            	registry.register(t.id, t.type, t.fac);
        }
        registry.lock();
    }
    
    /**
     * Creates an NBT tag with the specified ID.
     */
    public static IData createTag(byte id) {
        return registry.create(id);
    }
    
    /**
     * Returns the ID of the given tag, or -1 if {@code s} isn't a valid tag.
     */
    public static byte tagID(IData s) {
        return (byte) registry.getID(s.getClass());
    }
    
    /**
     * Returns the tag name for a tag with the specified ID.
     */
    public static String name(byte id) {
        return NBTType.values()[id-1].toString();
    }
    
}
