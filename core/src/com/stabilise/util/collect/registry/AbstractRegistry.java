package com.stabilise.util.collect.registry;

import java.util.Iterator;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.util.Log;

/**
 * This package-private class provides a simple base for a registry
 * implementation to extend.
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
    protected final String name;
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
    
    /**
     * Returns a verbose string representation of this registry.
     */
    public String toStringVerbose() {
        StringBuilder sb = new StringBuilder();
        int size = size();
        sb.append('"').append(name).append("\":[").append(size);
        sb.append(size == 1 ? " entry] {\n" : " entries] {\n");
        for(Iterator<E> i = iterator(); i.hasNext();) {
            sb.append("    ");
            sb.append(i.next().toString());
            if(i.hasNext()) sb.append(',');
            sb.append('\n');
        }
        return sb.append("}").toString();
    }
    
}
