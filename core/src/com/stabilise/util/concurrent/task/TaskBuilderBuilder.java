package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Builds a TaskBuilder.
 */
public final class TaskBuilderBuilder {
    
    // I never thought I'd see the day that I'd see fit to make a
    // BuilderBuilder... but here I am, and here we are.
    
    
    /** The executor with which to run the task. This must be set via {@link
     * #executor(Executor)}, or begin() will throw an exception. */
    Executor executor    = null;
    /** The name of the task to create. By default this is {@link
     * TaskTracker#DEFAULT_STATUS} and so setting it via name() isn't
     * compulsory. */
    String name          = TaskTracker.DEFAULT_STATUS;
    /** The strategy to apply to all top-level task units. This doesn't need
     * to be set - if this is null when begin() is invoked, we default to
     * {@link ReportStrategy#all()}. */
    ReportStrategy strat = null;
    /** The ReturnBox boxing the designated return value. This is set on one of
     * the overloaded variants of begin(). If set, the created TaskBuilder will
     * end up returning a ReturnTask boxing retBox; if not, it'll return an
     * ordinary Task. */
    ReturnBox<?> retBox  = null;
    
    private boolean built = false;
    
    
    TaskBuilderBuilder() {} // package-private constructor
    
    private void checkState() {
        if(built)
            throw new IllegalStateException("Already began building!");
    }
    
    /**
     * Sets the task executor.
     * 
     * @throws IllegalStateException if task building has already began, or the
     * executor has already been set.
     * @throws NullPointerException if {@code executor} is {@code null}.
     */
    public TaskBuilderBuilder executor(Executor executor) {
        checkState();
        if(this.executor != null)
            throw new IllegalStateException("Executor already set");
        this.executor = Objects.requireNonNull(executor);
        return this;
    }
    
    /**
     * Sets the task name.
     * 
     * @throws IllegalStateException if task building has already began.
     * @throws NullPointerException if {@code name} is {@code null}.
     */
    public TaskBuilderBuilder name(String name) {
        checkState();
        this.name = Objects.requireNonNull(name);
        return this;
    }
    
    /**
     * Sets the ReportStrategy to apply to top-level task units.
     * 
     * @throws IllegalStateException if task building has already began, or the
     * strategy has already been set.
     * @throws NullPointerException if {@code strategy} is {@code null}.
     */
    public TaskBuilderBuilder strategy(ReportStrategy strategy) {
        checkState();
        if(this.strat != null)
            throw new IllegalStateException("Strategy already set");
        this.strat = Objects.requireNonNull(strategy);
        return this;
    }
    
    /**
     * Begins building a {@link TaskOld}.
     * 
     * @throws IllegalStateException if task building has already began, or the
     * executor hasn't been set.
     */
    public TaskBuilder<Void, Task> begin() {
        checkState();
        return new TaskBuilder<Void, Task>(this);
    }
    
    /**
     * Begins building a {@link ReturnTask}.
     * 
     * @throws IllegalStateException if task building has already began, or the
     * executor hasn't been set.
     */
    public <T> TaskBuilder<T, ReturnTask<T>> beginReturn() {
        checkState();
        this.retBox = new ReturnBox<T>();
        return new TaskBuilder<T, ReturnTask<T>>(this);
    }
    
    /**
     * Begins building a {@link ReturnTask}.
     * 
     * <p>This method is provided as an alternative to {@link #beginReturn()}
     * to coerce the compiler to properly infer type arguments (since it fails
     * to do so otherwise and requires casting).
     * 
     * @throws IllegalStateException if task building has already began, or the
     * executor hasn't been set.
     */
    public <T> TaskBuilder<T, ReturnTask<T>> beginReturn(Class<T> clazz) {
        return beginReturn();
    }
    
}
