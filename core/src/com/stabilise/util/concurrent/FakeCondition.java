package com.stabilise.util.concurrent;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * This class provides a Condition implementation which does absolutely nothing.
 */
public class FakeCondition implements Condition {
    
    public static final FakeCondition INSTANCE = new FakeCondition();
    
    
    private FakeCondition() {}
    
    @Override public void await() throws InterruptedException {}
    @Override public void awaitUninterruptibly() {}
    @Override public long awaitNanos(long nanosTimeout) throws InterruptedException { return nanosTimeout; }
    @Override public boolean await(long time, TimeUnit unit) throws InterruptedException { return true; }
    @Override public boolean awaitUntil(Date deadline) throws InterruptedException { return true; }
    @Override public void signal() {}
    @Override public void signalAll() {}
    
}
