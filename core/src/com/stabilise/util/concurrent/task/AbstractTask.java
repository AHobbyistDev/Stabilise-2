package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Provides a basic common implementation of the {@code Task} interface.
 */
abstract class AbstractTask implements Task {
    
    protected final Executor exec;
    
    protected final TaskTracker tracker;
    
    private final Lock      lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    
    
    /**
     * It is implicitly trusted that neither of the arguments are null.
     */
    protected AbstractTask(Executor exec, TaskTracker tracker) {
        this.exec = exec;
        this.tracker = tracker;
    }
    
    @Override
    public boolean stopped() {
        return tracker.getState() != State.RUNNING;
    }
    
    @Override
    public boolean completed() {
        return tracker.getState() == State.COMPLETED;
    }
    
    @Override
    public boolean failed() {
        return tracker.getState() == State.FAILED;
    }
    
    /**
     * Notifies any thread waiting in any of the await() methods.
     */
    protected void signalAll() {
        lock.lock();
        try {
            cond.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public boolean await() throws InterruptedException {
        lock.lock();
        try {
            while(!stopped())
                cond.await();
        } finally {
            lock.unlock();
        }
        return completed();
    }
    
    @Override
    public boolean awaitUninterruptibly() {
        lock.lock();
        try {
            while(!stopped())
                cond.awaitUninterruptibly();
        } finally {
            lock.unlock();
        }
        return completed();
    }
    
    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        if(stopped())
            return true;
        lock.lock();
        try {
            return cond.await(time, unit);
        } finally {
            lock.unlock();
        }
    }
    
    // TaskView methods
    
    @Override
    public String status() {
        return tracker.getStatus();
    }
    
    @Override
    public long partsCompleted() {
        return tracker.getPartsCompleted();
    }
    
    @Override
    public long totalParts() {
        return tracker.getTotalParts();
    }
    
    /**
     * Returns a string representation of this task.
     * 
     * <p>This implementation behaves as if by:
     * 
     * <pre>
     * return getStatus() + "... " + percentCompleted() + "%";
     * </pre>
     * 
     * which returns a string of the form:
     *
     * <pre>"Working... 50%"</pre>
     */
    @Override
    public String toString() {
        return status() + "... " + percentCompleted() + "%";
    }
    
}
