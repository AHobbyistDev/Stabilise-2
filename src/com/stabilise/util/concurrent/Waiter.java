package com.stabilise.util.concurrent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.function.BooleanSupplier;

/**
 * A {@code Waiter} offers a compromise between polled condition testing and
 * timed condition testing (e.g., {@link Condition#await(long, TimeUnit)}), the
 * former of which does not include a form of timeout, and the latter of which
 * blocks until either the condition is met or the wait time has expired.
 */
public class Waiter {
    
    /**
     * Possible return values from {@link Waiter#poll()}.
     */
    public enum WaitState {
        /** Indicates that the target of a {@code Waiter} has not completed,
         * nor has it timed out. */
        INCOMPLETE,
        /** Indicates that the target of a {@code Waiter} has completed without
         * timing out. */
        COMPLETE,
        /** Indicates that the target of a {@code Waiter} did not complete
         * before timing out. */
        TIMEOUT;
    }
    
    private final long endTime;
    private final BooleanSupplier condition;
    private WaitState last = WaitState.INCOMPLETE;
    
    
    /**
     * Creates a new Waiter.
     * 
     * @param condition The condition upon which to wait. This Waiter will
     * continue to wait until this condition returns {@code true}.
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws NullPointerException if either {@code condition} or {@code unit}
     * are {@code null}.
     * @throws IllegalArgumentException if {@code time <= 0}.
     */
    public Waiter(BooleanSupplier condition, long time, TimeUnit unit) {
        this.condition = Objects.requireNonNull(condition);
        if(time <= 0)
            throw new IllegalArgumentException();
        this.endTime = System.currentTimeMillis() + unit.toMillis(time);
    }
    
    /**
     * Polls the condition.
     * 
     * @return {@link WaitState#COMPLETE COMPLETE} if the condition has been
     * fulfilled, or {@link WaitState#TIMEOUT TIMEOUT} if the condition took
     * too long, or {@link WaitState#INCOMPLETE INCOMPLETE} if the condition
     * has not yet been fulfilled.
     */
    public WaitState poll() {
        if(last != WaitState.INCOMPLETE)
            return last;
        if(System.currentTimeMillis() >= endTime)
            return last = WaitState.TIMEOUT;
        if(condition.getAsBoolean())
            return last = WaitState.COMPLETE;
        return WaitState.INCOMPLETE;
    }
    
}
