package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventHandler;

/**
 * A TaskBuilder builds a task.
 * 
 * <p>The type parameters of this class should normally be invisible and
 * irrelevant to the end-user, as this class should only ever occur amidst
 * method chaining.
 * 
 * @param <R> The return type of the task to generate. If a builder is
 * constructing a non-returning task, this will be {@code Void}.
 * @param <T> The type of task to return. This will be {@code Task} if this
 * builder is constructing a non-returning task, or {@code ReturnTask<R>}
 * otherwise.
 */
@NotThreadSafe
public final class TaskBuilder<R, T extends Task> {
    
    private final Executor executor;
    
    private final Prototype root;
    
    /** The first task unit to run. This can't be null when the task is built. */
    private Prototype first = null;
    /** The final task unit to run. */
    private Prototype tail = null;
    
    /** If null, we return a Task; otherwise we return a ReturnTask. */
    private final ReturnBox<R> retBox;
    private boolean callableSet = false;
    
    private boolean built = false;
    
    
    /**
     * @throws IllegalStateException if the executor wasn't set on the builder.
     */
    TaskBuilder(TaskBuilderBuilder builder) {
        if(builder.executor == null)
            throw new IllegalStateException("Executor not set");
        if(builder.strat == null)
            builder.strat = ReportStrategy.all();
        
        @SuppressWarnings("unchecked")
        ReturnBox<R> box = (ReturnBox<R>) builder.retBox;
        
        this.executor = builder.executor;
        this.root = new Prototype(null, 0, builder.name, builder.strat);
        this.retBox = box;
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed.
     * 
     * @param r The unit to run.
     * 
     * @throws IllegalStateException if the task has already been built.
     * @throws NullPointerException if {@code r} is {@code null}.
     */
    public TaskBuilder<R, T> andThen(Runnable r) {
        return andThen(t -> r.run());
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed.
     * 
     * @param t The unit to run.
     * 
     * @throws IllegalStateException if the task has already been built.
     * @throws NullPointerException if {@code t} is {@code null}.
     */
    public TaskBuilder<R, T> andThen(TaskRunnable t) {
        return andThen(t, null, 0);
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed.
     * 
     * @param parts The number of parts in the task.
     * @param t The unit to run.
     * 
     * @throws IllegalStateException if the task has already been built.
     * @throws NullPointerException if {@code t} is {@code null}.
     * @throws IllegalArgumentException if {@code parts < 0 || parts ==
     * Long.MAX_VALUE}
     */
    public TaskBuilder<R, T> andThen(long parts, TaskRunnable t) {
        return andThen(t, null, parts);
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed,
     * and uses its return value as the return value of the task.
     * 
     * @param c The unit to run.
     * 
     * @throws UnsupportedOperationException if this builder is not building a
     * value-returning task.
     * @throws IllegalStateException if the task has already been built, or a
     * value-returning unit (i.e. a Callable or TaskCallable) has already been
     * set.
     * @throws NullPointerException if {@code c} is {@code null}.
     */
    public TaskBuilder<R, T> andThenReturn(Callable<? extends R> c) {
        return andThenReturn(0, t -> c.call());
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed,
     * and uses its return value as the return value of the task.
     * 
     * @param t The unit to run.
     * 
     * @throws UnsupportedOperationException if this builder is not building a
     * value-returning task.
     * @throws IllegalStateException if the task has already been built, or a
     * value-returning unit (i.e. a Callable or TaskCallable) has already been
     * set.
     * @throws NullPointerException if {@code t} is {@code null}.
     */
    public TaskBuilder<R, T> andThenReturn(TaskCallable<? extends R> t) {
        return andThenReturn(0, t);
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed,
     * and uses its return value as the return value of the task.
     * 
     * @param parts The number of parts in the task.
     * @param t The unit to run.
     * 
     * @throws UnsupportedOperationException if this builder is not building a
     * value-returning task.
     * @throws IllegalStateException if the task has already been built, or a
     * value-returning unit (i.e. a Callable or TaskCallable) has already been
     * set.
     * @throws NullPointerException if {@code t} is {@code null}.
     * @throws IllegalArgumentException if {@code parts < 0 || parts ==
     * Long.MAX_VALUE}
     */
    public TaskBuilder<R, T> andThenReturn(long parts, TaskCallable<? extends R> t) {
        requireReturn();
        if(callableSet)
            throw new IllegalStateException("Value-returning unit already set!");
        callableSet = true;
        return andThen(new TaskCallableWrapper<R>(t, retBox), null, parts);
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed.
     * 
     * @param t The unit to run.
     * @param name The name of the task unit. If this is null, {@link
     * TaskTracker#DEFAULT_STATUS} is used instead.
     * @param parts The number of parts in the task.
     * 
     * @throws IllegalStateException if the task has already been built.
     * @throws NullPointerException if {@code t} is {@code null}.
     * @throws IllegalArgumentException if {@code parts < 0 || parts ==
     * Long.MAX_VALUE}
     */
    private TaskBuilder<R, T> andThen(TaskRunnable t, String name, long parts) {
        checkState();
        Prototype proto = root.child(Objects.requireNonNull(t), parts, name);
        if(first == null)
            first = proto;
        if(tail != null)
            tail.next = proto;
        tail = proto;
        return this;
    }
    
    /**
     * Registers a multi-use event listener on the most recent unit declared.
     * {@link TaskEvent}s are automatically posted during the lifecycle of
     * a task unit.
     * 
     * @param e The event to listen for.
     * @param h The handler to invoke when the specified event is posted.
     * 
     * @throws IllegalStateException if the task has already been built, or no
     * task units have been declared.
     * @see EventDispatcher#addListener(Event, EventHandler)
     */
    public <E extends Event> TaskBuilder<R, T> onEvent(E e, EventHandler<? super E> h) {
        checkState();
        if(tail == null)
            throw new IllegalStateException("No task to register an event on!");
        tail.events.addListener(executor, e, h);
        return this;
    }
    
    /**
     * Builds and returns the Task, but does not start it.
     * 
     * @throws IllegalStateException if the task has already been built, no
     * task units have been declared, or we're building a value-returning task
     * but no value-returning unit was set.
     */
    public T build() {
        checkState();
        if(first == null)
            throw new IllegalStateException("No task units");
        if(isReturnTask() && !callableSet)
            throw new IllegalStateException("Value-returning task unit not set!");
        
        built = true;
        
        root.buildHeirarchy(executor);
        
        @SuppressWarnings("unchecked")
        T t = (T) (isReturnTask()
                ? new ReturnTask<R>(executor, root.get(), first.unit, retBox)
                : new Task         (executor, root.get(), first.unit));
        
        return t;
    }
    
    /**
     * Builds, starts and returns the Task.
     * 
     * @throws IllegalStateException if the task has already been built, a
     * group has not been closed, no task units have been declared, or we're
     * building a value-returning task but no value-returning unit was {@link
     * #andThenReturn(TaskCallable) set}.
     */
    public T start() {
        @SuppressWarnings("unchecked")
        T t = (T) build().start();
        return t;
    }
    
    /** @throws IllegalStateException if already built. */
    private void checkState() {
        if(built)
            throw new IllegalStateException("Already built");
    }
    
    /** @return true if we're constructing a ReturnTask; false if an ordinary
     * task. */
    private boolean isReturnTask() {
        return retBox != null;
    }
    
    /** throws UnsupportedOperationException if not a return task */
    private void requireReturn() {
        if(!isReturnTask())
            throw new UnsupportedOperationException("This is not a value-returning"
                    + " task!");
    }
    
    /** Wraps a TaskCallable in a TaskRunnable. */
    private static class TaskCallableWrapper<T> implements TaskRunnable {
        
        private final TaskCallable<? extends T> callable;
        private final ReturnBox<T> retVal;
        
        private TaskCallableWrapper(TaskCallable<? extends T> callable, ReturnBox<T> retVal) {
            this.callable = callable;
            this.retVal = retVal;
        }
        
        @Override
        public void run(TaskHandle handle) throws Exception {
            retVal.set(callable.run(handle));
        }
        
    }
    
}
