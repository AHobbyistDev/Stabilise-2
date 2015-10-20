package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.box.Box;
import com.stabilise.util.concurrent.Tasks;

/**
 * A ReturnBox encapsulates the return value of a ReturnTask. This class
 * implements Box for user convenience.
 * 
 * <h3>How to use</h3>
 * 
 * <p>To use a ReturnBox, simply instantiate one, pass it to {@link
 * TaskBuilderBuilder#begin(ReturnBox) begin()} when building your task, and
 * set its value at some point during the task. For example:
 * 
 * <pre>
 * ReturnBox<String> result = new ReturnBox<>(); // our box
 * ReturnTask<String> t = Task.builder()
 *     .executor(r -> new Thread(r).start()) // i.e. run on a new thread
 *     .begin()
 *     .andThen(() -> result.set("Hello, world!"))
 *     .start();
 * System.out.println(t.get()); // prints "Hello, world!"
 * </pre>
 * 
 * <p>Note, however, that in the case of simple tasks such as that one, the
 * above code fragment can be simplified greatly by using {@link
 * Tasks#exec(Supplier)} as such:
 * 
 * <pre>
 * System.out.println(Tasks.exec(() -> "Hello, world!").get());
 * </pre>
 */
@ThreadSafe
public final class ReturnBox<T> implements Box<T> {
    
    private AtomicReference<T> value = new AtomicReference<>(null);
    
    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public T get() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot get ReturnBox value!");
    }
    
    /**
     * @throws ExecutionException if the value hasn't been set
     */
    T innerGet() throws ExecutionException {
        T t = value.get();
        if(t == null)
            throw new ExecutionException("Task did not set the return value!", null);
        return t;
    }
    
    /**
     * @throws NullPointerException if t is null.
     * @throws IllegalStateException if the value has already been set.
     */
    @Override
    public void set(T t) {
        Objects.requireNonNull(t);
        if(!value.compareAndSet(null, t))
            throw new IllegalStateException("Already set!");
    }
    
}
