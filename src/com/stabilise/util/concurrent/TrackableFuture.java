package com.stabilise.util.concurrent;

import java.util.concurrent.Future;

/**
 * Unifying intergace for {@code Future} and {@code Tracker}.
 */
public interface TrackableFuture<V> extends Future<V>, Tracker {
    
    /**
     * Blocks the current thread until the task has stopped, due to either
     * completion or failure.
     * 
     * <p>This method uses {@link Future#get()} rather than {@link
     * Tracker#waitUntilDone()}, 
     */
    default void waitUntilStopped() {
        try {
            get();
        } catch(Exception ignored) {}
    }
    
}
