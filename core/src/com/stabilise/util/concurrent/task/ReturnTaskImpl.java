package com.stabilise.util.concurrent.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ReturnTask, much like a {@link Future}, encapsulates an asynchronous
 * computation. Asides from its associated return value, a ReturnTask is
 * otherwise equivalent to an ordinary Task.
 */
public class ReturnTaskImpl<T> extends TaskImpl implements ReturnTask<T> {
    
    private final ReturnBox<T> retVal;
    
    
    /**
     * Instantiated only by TaskBuilder.
     */
    ReturnTaskImpl(Executor exec, TaskTracker tracker, TaskUnit firstUnit,
            ReturnBox<T> retVal) {
        super(exec, tracker, firstUnit);
        this.retVal = retVal;
    }
    
    // Overridden as to return ReturnTask<T> instead of Task
    @Override
    public ReturnTaskImpl<T> start() {
        super.start();
        return this;
    }
    
    @Override
    public T get() throws InterruptedException, ExecutionException {
        if(!await()) {
            Throwable t = failCause.get();
            throw new ExecutionException("Task did not complete successfully ("
                        + (t == null ? "no reason given" : t.getMessage()) + ")",
                        failCause.get());
        }
        return retVal.get(failCause);
    }
    
    @Override
    public T get(long time, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if(!await(time, unit))
            throw new TimeoutException();
        if(failed())
            throw new ExecutionException("Task did not complete successfully", failCause.get());
        return retVal.get(failCause);
    }
    
    @Override
    public T tryGet() throws ExecutionException {
        if(!completed())
            throw new IllegalStateException("Task not completed");
        return retVal.get(failCause);
    }
    
}
