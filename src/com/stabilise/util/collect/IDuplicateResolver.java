package com.stabilise.util.collect;


/**
 * An IDuplicateResolver is an interface built into the {@link
 * WeightingArrayList} collection; if two {@link Object#equals(Object) equal}
 * objects are inserted, {@link #resolve(Object)} is invoked on the object
 * already present in the list to determine what to do with the new one.
 * 
 * @param <T> Generally the type of the implementing class, e.g. {@code
 * class A implements IDuplicateResolver<A>}
 */
public interface IDuplicateResolver<T> {
    
    public static enum Action {
        /** Overwrite the old object with the new one. */
        OVERWRITE,
        /** Reject the new object and keep the old one. */
        REJECT,
        /** Keep both; retain the old object and insert the new too. */
        KEEP_BOTH
    }
    
    Action resolve(T other);
    
}
