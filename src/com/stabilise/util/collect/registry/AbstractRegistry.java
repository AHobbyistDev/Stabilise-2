package com.stabilise.util.collect.registry;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.NotThreadSafe;

/**
 * This class provides a simple base for a registry to extend.
 * 
 * <p>Subclasses should invoke {@link #checkLock()} before registering an
 * object, and increment {@link #size} whenever something is successfully
 * registered. Furthermore, the {@link #dupePolicy duplicate policy} should be
 * followed appropriately, and exceptions should be documented.
 * 
 * @param <E> The type of object to register.
 */
@NotThreadSafe
abstract class AbstractRegistry<E> implements Iterable<E> {
    
    /** The name of this registry. */
    private final String name;
    protected final DuplicatePolicy dupePolicy;
    protected final Log log;
    
    /** The number of entries in this registry. Increment this when an object
     * is registered. */
    protected int size = 0; 
    
    private boolean locked = false;
    
    
    /**
     * @throws NullPointerException if {@code params} is {@code null}.
     */
    protected AbstractRegistry(RegistryParams params) {
        this.name = params.name;
        this.dupePolicy = params.dupePolicy;
        
        log = Log.getAgent(name);
    }
    
    /**
     * @return The number of entries in this registry.
     */
    public final int size() {
        return size;
    }
    
    /**
     * Locks this registry. Once this is done, attempting to register anything
     * else will result in an {@code IllegalStateException} being thrown.
     * Attempting to lock an already locked registry does nothing.
     */
    public void lock() {
        if(!locked) {
            locked = true;
            log.postFineDebug("Locked!");
        }
    }
    
    /**
     * Returns {@code true} iff this registry has been {@link #lock() locked}.
     */
    public final boolean isLocked() {
        return locked;
    }
    
    /**
     * This should be invoked before something is registered, to ensure it's
     * allowed.
     * 
     * @throws IllegalStateException if this registry is locked.
     */
    protected final void checkLock() {
        if(locked)
            throw new IllegalStateException("\"" + name + "\" is locked;"
                    + " cannot register more entries!");
    }
    
    @Override
    public String toString() {
        return "\"" + name + "\":[" + size + " entries]";
    }
    
}
