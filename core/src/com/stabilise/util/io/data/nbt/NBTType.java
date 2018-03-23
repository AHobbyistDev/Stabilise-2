package com.stabilise.util.io.data.nbt;

import java.util.function.Supplier;

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
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.io.data.ITag;


public enum NBTType {
    
    BYTE      (1,  ByteBox.class),
    SHORT     (2,  ShortBox.class),
    INT       (3,  IntBox.class),
    LONG      (4,  LongBox.class),
    FLOAT     (5,  FloatBox.class),
    DOUBLE    (6,  DoubleBox.class),
    BYTE_ARRAY(7,  ByteArrBox.class),
    STRING    (8,  StringBox.class),
    LIST      (9,  NBTList.class, NBTList::new),
    COMPOUND  (10, NBTCompound.class, NBTCompound::new),
    INT_ARRAY (11, IntArrBox.class),
    BOOLEAN   (12, BoolBox.class),
    CHAR      (13, CharBox.class);
    
    
    public final byte id;
    private final Class<? extends ITag> type;
    private final Supplier<ITag> fac;
    
    private NBTType(int id, Class<? extends ITag> type) {
        this(id, type, null);
    }
    
    private NBTType(int id, Class<? extends ITag> type, Supplier<ITag> fac) {
        this.id = (byte)id;
        this.type = type;
        this.fac = fac;
    }
    
    private static final TypeFactory<ITag> registry =
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
    public static ITag createTag(byte id) {
        return registry.create(id);
    }
    
    /**
     * Returns the ID of the given tag, or -1 if {@code s} isn't a valid tag.
     */
    public static byte tagID(ITag s) {
        return (byte) registry.getID(s.getClass());
    }
    
    /**
     * Returns the tag name for a tag with the specified ID.
     */
    public static String name(byte id) {
        return NBTType.values()[id-1].toString();
    }
    
}
