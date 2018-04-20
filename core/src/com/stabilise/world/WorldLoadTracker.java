package com.stabilise.world;

/**
 * A simple class which manages tracking the number of regions or slices of a
 * world which have been loaded and generated. Used for producing a nice load
 * bar while the world is loading.
 */
public class WorldLoadTracker {
    
    private volatile long total = 0, done = 0;
    private long totalDone = 0;
    
    
    /**
     * Resets tracked operations.
     */
    public synchronized void reset() {
        total = done = total - done;
    }
    
    /**
     * Indicates that a new loading operation has begun.
     */
    public synchronized void startLoadOp() {
        total++;
    }
    
    /**
     * Indicates that a loading operation has ended.
     */
    public synchronized void endLoadOp() {
        done++;
        totalDone++;
        
        notifyAll();
    }
    
    /**
     * Returns the number of completed loading operations.
     */
    public long numDone() {
        return done;
    }
    
    /**
     * Returns the total number of loading operations.
     */
    public long numTotal() {
        return total;
    }
    
    /**
     * Blocks the current thread until the next loading operation has
     * completed. Returns immediately if there are no active loading
     * operations.
     * 
     * @throws InterruptedException if the current thread was interrupted
     * while waiting.
     */
    public synchronized void waitUntilNext() throws InterruptedException {
        long old = totalDone;
        while(done != total && old == totalDone)
            wait();
    }
    
    /**
     * Blocks the current thread until all loading operations have completed.
     * 
     * @throws InterruptedException if the current thread was interrupted
     * while waiting.
     */
    public synchronized void waitUntilDone() throws InterruptedException {
        while(done != total)
            wait();
    }
    
    /**
     * Checks for whether or not all loads have completed.
     */
    public synchronized boolean isDone() {
        return done == total;
    }
    
}
