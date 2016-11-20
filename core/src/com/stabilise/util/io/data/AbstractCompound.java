package com.stabilise.util.io.data;

/**
 * Skeletal implementation for {@link DataCompound}. Every {@code DataCompound}
 * should be an instance of this class; however, client code should never
 * refer to this class directly and should always use {@code DataCompound}
 * instead.
 */
public abstract class AbstractCompound implements ITag, DataCompound {
    
    protected boolean writeMode; // only really used for ByteCompound anyway
    
    
    /**
     * Adds a tag to this compound, and returns the tag. If a tag with the
     * specified name already exists, it will be overwritten. Throws NPE if
     * name is null.
     * 
     * <p>This method is public for convenience; as client code should never
     * use this class directly, this method should similarly not be used.
     */
    public abstract <T extends ITag> T putData(String name, T t);
    
    /**
     * Puts everything in this compound into {@code c}. This method should
     * never be used by client code.
     */
    public abstract void putAll(AbstractCompound c);
    
    @Override
    public void setReadMode() {
        writeMode = false;
    }
    
    @Override
    public void setWriteMode() {
        writeMode = true;
    }
    
    /**
     * Throws IllegalStateException if not in read mode.
     */
    protected void checkCanRead() {
        if(writeMode)
            throw new IllegalStateException("Not in reader mode!");
    }
    
    /**
     * Throws IllegalStateException if not in write mode.
     */
    protected void checkCanWrite() {
        if(!writeMode)
            throw new IllegalStateException("Not in writer mode!");
    }
    
    @Override
    public DataCompound copy(Format format) {
        AbstractCompound clone = format.newAbstractCompound();
        putAll(clone);
        return clone;
    }
    
}
