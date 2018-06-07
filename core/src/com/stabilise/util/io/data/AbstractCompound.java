package com.stabilise.util.io.data;

import com.stabilise.util.Checks;

/**
 * Skeletal implementation for {@link DataCompound}. Every {@code DataCompound}
 * should be an instance of this class; however, external code should never
 * refer to this class directly and should always use {@code DataCompound}
 * instead.
 */
public abstract class AbstractCompound implements ITag, DataCompound {
    
    /**
     * Adds a tag to this compound, and returns the tag. If a tag with the
     * specified name already exists, it will be overwritten. Throws NPE if
     * name is null.
     * 
     * <p>This method is public for convenience; external code should probably
     * try to avoid using this.
     */
    public abstract <T extends ITag> T putData(String name, T t);
    
    /**
     * Puts everything from the given compound into this compound.
     */
    public void putAll(DataCompound c) {
        ((AbstractCompound)c).putInto(this);
    }
    
    /**
     * Puts everything in this compound into the given compound.
     */
    protected abstract void putInto(AbstractCompound target);
    
    @Override
    public DataCompound copy(Format format) {
        AbstractCompound clone = format.newAbstractCompound();
        clone.putAll(this);
        return clone;
    }
    
    
    
    @Override
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        throw Checks.ISE("Can't convert " + other.getClass().getSimpleName() + " to compound type.");
    }
    
}
