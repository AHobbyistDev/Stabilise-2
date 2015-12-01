package com.stabilise.util.concurrent.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A ReturnBox encapsulates the return value of a ReturnTask. This class
 * implements Box for user convenience.
 */
@ThreadSafe
final class ReturnBox<T> {
    
    private AtomicReference<T> value = new AtomicReference<>(null);
    
    /**
     * @throws ExecutionException if the value hasn't been set
     */
    T get(AtomicReference<Throwable> failCause) throws ExecutionException {
        T t = value.get();
        if(t == null)
            throw new ExecutionException("Task did not set the return value!", failCause.get());
        return t;
    }
    
    /**
     * @throws BadReturnValueException if t is null.
     */
    void set(T t) {
        if(t == null)
            throw new BadReturnValueException("Null return value!");
        if(!value.compareAndSet(null, t))
            throw new AssertionError("Return value already set!");
    }
    
}
