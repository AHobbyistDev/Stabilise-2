package com.stabilise.entity2;

import java.util.Arrays;

/**
 * testing stuff
 */
public class WorldDemo {
    
    /** Default entity arrays size. */
    private static final int defSize = 64;
    /** If the percentage of slots which are empty exceeds this threshold, we
     * flatten the entity arrays. */
    private static final float flattenThreshold = 0.25f;
    
    /** World age. */
    private int ticks = 0;
    
    /** Next unique ID to produce. */
    private int nextUID = 0;
    /** Largest in-use ID. */
    private int maxID = 0;
    /** Smallest free ID to avoid traversal from array beginning when searching
     * for empty slots. */
    private int minFree = 0;
    
    /** Reset to 0 and incremented each tick for every EmptyCore in the main
     * list. */
    int emptySlots = 0;
    
    private final EmptyCore emptyCore;
    private ECore[] eCores;
    
    
    public WorldDemo() {
        eCores = new ECore[defSize];
        
        emptyCore = new EmptyCore();
        Arrays.fill(eCores, emptyCore);
    }
    
    public void update() {
        ticks++;
        
        emptySlots = 0;
        
        for(int i = 0; i < maxID; i++) {
            if(eCores[i].update(this)) {
                // Entity is kill; kill entity
            }
        }
        
        if((float)emptySlots / maxID >= flattenThreshold) {
            flatten();
        }
    }
    
    public void newEntity() {
        
    }
    
    public ECore getEntity(int id, int uid) {
        ECore e = eCores[id];
        return e.uid() == uid ? e : emptyCore;
    }
    
    /** Flattens the entity array. O(n). */
    private void flatten() {
        
    }
    
}
