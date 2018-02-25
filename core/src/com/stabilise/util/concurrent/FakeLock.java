package com.stabilise.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * This class provides a Lock implementation which does absolutely nothing.
 */
public class FakeLock implements Lock {
    
    public static final FakeLock INSTANCE = new FakeLock();
    
    
    private FakeLock() {}
    
    @Override public void lock() {}
    @Override public void lockInterruptibly() throws InterruptedException { }
    @Override public boolean tryLock() { return true; }
    @Override public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return true;
    }
    @Override public void unlock() {}
    @Override public Condition newCondition() { return FakeCondition.INSTANCE; }
    
}
