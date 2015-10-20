package com.stabilise.util.collect;

/**
 * A Pair is a container which holds a pair of objects.
 * 
 * @param <L> The type of the left object.
 * @param <R> The type of the right object.
 */
public class Pair<L, R> {
    
    public L left;
    public R right;
    
    /**
     * Creates a new Pair.
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
}
