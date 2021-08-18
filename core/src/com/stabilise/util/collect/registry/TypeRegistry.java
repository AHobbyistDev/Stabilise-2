package com.stabilise.util.collect.registry;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.util.collect.Array;

/**
 * A TypeRegistry is a specialised registry which offers the following mapping
 * profile:
 * 
 * <ul>
 * <li>{@code Class<T>} -> ID, and
 * <li>ID -> Value.
 * </ul>
 * 
 * <p>This class is typically used as an instantiation registry, in which case
 * the value mapped to each class is of type {@link Supplier}{@code <T>}. See
 * {@link TypeFactory} for this implementation.
 * 
 * @param <T> The object type.
 * @param <V> The type of value to map to each class.
 * 
 * @see TypeFactory
 * @see GeneralTypeFactory
 */
@NotThreadSafe
public class TypeRegistry<T, V> extends AbstractRegistry<Class<? extends T>> {
    
    /** Maps Class -> ID. */
    private final Map<Class<? extends T>, Integer> idMap;
    /** Maps ID -> Value. */
    private final Array<V> values;
    /** Max ID registered. -1 indicates nothing is registered. */
    private int maxID = -1;
    
    
    /**
     * @throws NullPointerException if {@code params} is {@code null}.
     */
    public TypeRegistry(RegistryParams params) {
        super(params);
        
        values = new Array<>(params.capacity);
        idMap = new IdentityHashMap<>(params.capacity);
    }
    
    /**
     * Registers a class.
     * 
     * @param id The ID of the class.
     * @param objClass The class.
     * @param value The mapped value.
     * 
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if either {@code id} or the class have
     * already been registered and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public void register(int id, Class<? extends T> objClass, V value) {
        checkLock();
        if(values.getSemiSafe(id) != null && dupePolicy.handle(log, "Duplicate id " + id))
            return;
        if(idMap.containsKey(objClass) && dupePolicy.handle(log, "Duplicate class " + objClass.getSimpleName()))
            return;
        values.setWithExpand(id, value, 2f);
        idMap.put(objClass, id);
        size++;
        if(id > maxID)
            maxID = id;
    }
    
    /**
     * Gets the value mapped to the given ID.
     * 
     * @return The value, or {@code null} if {@code id} lacks a mapping.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     */
    public V get(int id) {
        return values.getSemiSafe(id);
    }
    
    /**
     * Gets the value mapped to the given ID.
     * 
     * @param defaultValue The value to return if {@code id} lacks a mapping.
     * May be null.
     * 
     * @return Either the value or {@code defaultValue}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     */
    protected V getOrDefault(int id, V defaultValue) {
        V v = values.getSemiSafe(id);
        return v != null ? v : defaultValue;
    }
    
    /**
     * Gets the ID of the specified object class.
     * 
     * @return The ID, or {@code -1} if the object class has not been
     * registered.
     */
    public int getID(Class<? extends T> objClass) {
        Integer i = idMap.get(objClass);
        return i == null ? -1 : i.intValue();
    }
    
    /**
     * Checks for whether or not this registry contains a mapping for the
     * specified ID.
     * 
     * @return {@code true} if {@code id} has a mapping; {@code false}
     * otherwise.
     */
    public boolean containsID(int id) {
        return values.getSafe(id) != null;
    }
    
    @Override
    public void lock() {
        if(!isLocked())
            values.resize(maxID + 1);
        super.lock();
    }
    
    @Override
    public Iterator<Class<? extends T>> iterator() {
        return idMap.keySet().iterator();
    }
    
    @Override
    public String toStringVerbose() {
        StringBuilder sb = new StringBuilder();
        int size = size();
        sb.append('"').append(name).append("\":[").append(size);
        sb.append(size == 1 ? " entry] {\n" : " entries] {\n");
        for(Iterator<Map.Entry<Class<? extends T>,Integer>> i = idMap.entrySet().iterator(); i.hasNext();) {
            sb.append("    ");
            Map.Entry<Class<? extends T>,Integer> e = i.next();
            sb.append(e.getValue()).append(": ").append(e.getKey());
            sb.append(": ").append(values.get(e.getValue()));
            if(i.hasNext()) sb.append(',');
            sb.append('\n');
        }
        return sb.append("}").toString();
    }
    
}
