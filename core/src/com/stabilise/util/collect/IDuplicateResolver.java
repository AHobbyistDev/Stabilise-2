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
    
    /**
     * @see #OVERWRITE
     * @see #REJECT
     * @see #KEEP_BOTH
     */
    public static enum Action {
        /** Overwrite the old object with the new one. */
        OVERWRITE,
        /** Reject the new object and keep the old one. */
        REJECT,
        /** Keep both; retain the old object and insert the new too. */
        KEEP_BOTH
    }
    
    
    /**
     * Resolves a detected duplicate. This is invoked on an element of a {@link
     * WeightingArrayList} when a newly-inserted object is {@link
     * Object#equals(Object) equal} to it. If this returns
     * 
     * <ul>
     * <li>{@link Action#OVERWRITE}, then the new object replaces this one,
     * <li>{@link Action#REJECT}, then the new object is rejected and not added
     *     to the list; this one remains,
     * <li>{@link Action#KEEP_BOTH}, then the new object is added to the list,
     *     and this object remains.
     * </ul>
     */
    Action resolve(T other);
    
}
