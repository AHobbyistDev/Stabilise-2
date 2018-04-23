package com.stabilise.util.collect;

/**
 * @see WeightingArrayList
 */
public interface IWeightProvider {
    
    /**
     * Returns the weight of this item, to be used by a {@link
     * WeightingArrayList}. Elements of such a list are ordered from lowest
     * weight to highest weight.
     */
    int getWeight();
    
}
