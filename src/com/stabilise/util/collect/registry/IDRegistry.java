package com.stabilise.util.collect.registry;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.collect.BiObjectIntMap;

/**
 * An IDRegistry extends upon a normal Registry as to additionally associate an
 * ID with every registered object.
 * 
 * <p>This class is based on a similar one from the decompiled Minecraft 1.7.10
 * source.
 */
@NotThreadSafe
public class IDRegistry<K, V> extends Registry<K, V> {
    
    private final BiObjectIntMap<V> idMap;
    private final Map<V, K> inverseMap;
    private int maxID;
    
    
    /**
     * Creates a new IDRegistry.
     * 
     * @throws NullPointerException if {@code params} is {@code null}.
     */
    public IDRegistry(RegistryParams params) {
        super(params);
        
        idMap = new BiObjectIntMap<>(params.capacity);
        inverseMap = ((BiMap<K, V>)objects).inverse();
    }
    
    @Override
    protected Map<K, V> createUnderlyingMap(int capacity) {
        return HashBiMap.create(capacity);
    }
    
    /**
     * Registers an object.
     * 
     * @return {@code true} if the object was successfully registered;
     * {@code false} otherwise.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IllegalArgumentException if either {@code id} or {@code key} is
     * are already mapped to an entry and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public boolean register(int id, K key, V object) {
        if(idMap.getObject(id) != null && dupePolicy.handle(log, "Duplicate id \"" + id + "\""))
            return false;
        if(!super.register(key, object))
            return false;
        idMap.put(id, object);
        if(id > maxID)
            maxID = id;
        return true;
    }
    
    /**
     * Throws an UnsupportedOperationException if this method is invoked.
     * Remember to use {@link #register(int, Object, Object)} for an
     * IDRegistry!
     */
    @Override
    public final boolean register(K name, V object) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Attempted to use register(K, V)! Use register(int, K, V) "
                + "instead!"
        );
    }
    
    /**
     * Gets the object mapped to the specified ID.
     * 
     * @return The object, or {@code null} if {@code key} lacks a mapping.
     * @throws IndexOutOfBoundsException if {@code key < 0}.
     */
    public V get(int id) {
        return idMap.getObject(id);
    }
    
    /**
     * Gets the key of the given object.
     * 
     * @return The object's key, or {@code null} if the object isn't
     * registered.
     */
    public K getKey(V object) {
        return inverseMap.get(object);
    }
    
    /**
     * Gets the ID for the given object.
     * 
     * @return The object's ID, or {@code -1} if the object isn't registered.
     */
    public int getID(V object) {
        return idMap.getKey(object);
    }
    
    /**
     * Checks for whether or not an object with the specified ID has been
     * registered.
     * 
     * @param id The ID.
     * 
     * @return {@code true} if an object with the given ID exists;
     * {@code false} otherwise.
     */
    public boolean containsID(int id) {
        return idMap.containsKey(id);
    }
    
    @Override
    public void lock() {
        if(!isLocked())
            idMap.clampSize(maxID + 1);
        super.lock();
    }
    
    @Override
    public Iterator<V> iterator() {
        return idMap.iterator();
    }
    
}