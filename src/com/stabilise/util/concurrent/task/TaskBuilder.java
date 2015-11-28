package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;

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
    
    /** Prototype tracker of the all-encapsulating Task to return. */
    private final PrototypeTracker tracker;
    
    /** The first task unit to run. This can't be null when the task is built. */
    private TaskUnit first = null;
    /** The final task unit to run. */
    private TaskUnit tail = null;

    private TaskGroup group = null;
    /** The current "focus" task. This is needed for {@link
     * #onEvent(Event, EventHandler)}, since we add listeners to the focus.
     * The focus is different from the tail in that subtasks of a group can be
     * made the focus, but they can never be made the tail. */
    private TaskUnit focus = null;
    
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
        this.tracker = new PrototypeTracker(0, builder.name, builder.strat);
        this.retBox = box;
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed.
     * 
     * @param r The unit to run.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group has been opened and a subtask has not yet been specified.
     * @throws NullPointerException if {@code r} is {@code null}.
     */
    public TaskBuilder<R, T> andThen(Runnable r) {
        return andThen(Tasks.wrap(r));
    }
    
    /**
     * Sequentially runs the given task unit once the prior one has completed.
     * 
     * @param t The unit to run.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group has been opened and a subtask has not yet been specified.
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
     * @throws IllegalStateException if the task has already been built, or a
     * group has been opened and a subtask has not yet been specified.
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
     * group has been opened and a subtask has not yet been specified, or a
     * value-returning unit (i.e. a Callable or TaskCallable) has already been
     * set.
     * @throws NullPointerException if {@code c} is {@code null}.
     */
    public TaskBuilder<R, T> andThenReturn(Callable<? extends R> c) {
        return andThenReturn(0, Tasks.wrap(c));
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
     * group has been opened and a subtask has not yet been specified, or a
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
     * group has been opened and a subtask has not yet been specified, or a
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
     * @throws IllegalStateException if the task has already been built, or a
     * group has been opened and a subtask has not yet been specified.
     * @throws NullPointerException if {@code t} is {@code null}.
     * @throws IllegalArgumentException if {@code parts < 0 || parts ==
     * Long.MAX_VALUE}
     */
    private TaskBuilder<R, T> andThen(TaskRunnable t, String name, long parts) {
        checkState();
        if(group != null && focus == null)
            throw new IllegalStateException("can't do a task after nothing in a group!");
        Objects.requireNonNull(t);
        TaskUnit unit;
        if(group == null) {
            unit = new TaskUnit(t, tracker.child(parts, name));
            tail = unit;
        } else {
            unit = new TaskUnit(t, group.protoTracker.child(parts, name));
            unit.group = group;
        }
        if(first == null)
            first = unit;
        if(focus != null)
            focus.next = unit;
        focus = unit;
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
     * @see EventDispatcher#addListener(Event, EventHandler)
     */
    public <E extends Event> TaskBuilder<R, T> onEvent(E e, EventHandler<? super E> h) {
        checkState();
        requireFocus();
        focus.addListener(executor, e, h);
        return this;
    }
    
    /**
     * Sequentially runs the given task group once the prior unit or group has
     * completed. To add units to the group, use {@link #subtask(TaskRunnable)
     * subtask}{@code ()}. All units in a group will be executed in parallel.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group is already open.
     * Long.MAX_VALUE}
     */
    public TaskBuilder<R, T> andThenGroup() {
        return andThenGroup(null, ReportStrategy.all());
    }
    
    /**
     * Sequentially runs the given task group once the prior unit or group has
     * completed. To add units to the group, use {@link #subtask(TaskRunnable)
     * subtask}{@code ()}. All units in a group will be executed in parallel.
     * 
     * @param name The name of the group. If this is null, {@link
     * TaskTracker#DEFAULT_STATUS} is used instead.
     * @param subtaskStrategy The report strategy to apply to the constituent
     * subtasks of this group.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group is already open.
     * @throws NullPointerException if {@code subtaskStrategy} is {@code null}.
     */
    public TaskBuilder<R, T> andThenGroup(String name, ReportStrategy subtaskStrategy) {
        checkState();
        requireNoGroup();
        group = new TaskGroup(executor, tracker.child(0, name, subtaskStrategy));
        if(first == null)
            first = group;
        if(tail != null) {
            tail.next = group;
            tail = group;
        }
        focus = null;
        return this;
    }
    
    /**
     * Closes a group.
     * 
     * @throws IllegalStateException if the task has already been built, or
     * there is no group to close.
     */
    public TaskBuilder<R, T> endGroup() {
        checkState();
        requireGroup();
        focus = group;
        group = null;
        return this;
    }
    
    // Note we do not expose any variants of subtask() which allow the user to
    // specify a name - this is simply because a name is meaningless for a
    // group's subtask.
    
    /**
     * Creates a subtask of the current group.
     * 
     * @param r The subtask.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group has not been opened.
     * @throws NullPointerException if {@code r} is {@code null}.
     */
    public TaskBuilder<R, T> subtask(Runnable r) {
        return subtask(Tasks.wrap(r));
    }
    
    /**
     * Creates a subtask of the current group.
     * 
     * @param t The subtask.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group has not been opened.
     * @throws NullPointerException if {@code t} is {@code null}.
     */
    public TaskBuilder<R, T> subtask(TaskRunnable t) {
        return subtask(t, null, 0);
    }
    
    /**
     * Creates a subtask of the current group.
     * 
     * @param parts The number of parts in the subtask.
     * @param t The subtask.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group has not been opened.
     * @throws NullPointerException if {@code t} is {@code null}.
     * @throws IllegalArgumentException if {@code parts < 0 || parts ==
     * Long.MAX_VALUE}.
     */
    public TaskBuilder<R, T> subtask(long parts, TaskRunnable t) {
        return subtask(t, null, parts);
    }
    
    /**
     * Creates a subtask of the current group.
     * 
     * @param t The subtask.
     * @param name The initial name of the task. If this is null we use {@link
     * TaskTracker#DEFAULT_STATUS}.
     * @param parts The number of parts in the subtask. This should be 0 if the
     * user did not specify a parts count.
     * 
     * @throws IllegalStateException if the task has already been built, or a
     * group has not been opened.
     * @throws NullPointerException if {@code t} is {@code null}.
     * @throws IllegalArgumentException if {@code parts < 0 || parts ==
     * Long.MAX_VALUE}.
     */
    private TaskBuilder<R, T> subtask(TaskRunnable t, String name, long parts) {
        checkState();
        requireGroup();
        TaskUnit unit = new TaskUnit(t, group.protoTracker.child(parts, name));
        group.addSubtask(unit);
        focus = unit;
        return this;
    }
    
    /**
     * Builds and returns the Task, but does not start it.
     * 
     * @throws IllegalStateException if the task has already been built, a
     * group has not been closed, no task units have been declared, or we're
     * building a value-returning task but no value-returning unit was {@link
     * #andThenReturn(TaskCallable) set}.
     */
    public T build() {
        checkState();
        requireNoGroup();
        if(first == null)
            throw new IllegalStateException("No task units");
        if(isReturnTask() && !callableSet)
            throw new IllegalStateException("Value-returning task unit not set!");
        
        built = true;
        
        tracker.buildHeirarchy();
        first.buildHierarchy();
        
        @SuppressWarnings("unchecked")
        T t = (T) (isReturnTask()
                ? new ReturnTask<R>(executor, tracker.get(), first, retBox)
                : new Task(executor, tracker.get(), first));
        
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
    
    /** @throws IllegalStateException if no focus. */
    private void requireFocus() {
        if(focus == null)
            throw new IllegalStateException();
    }
    
    /** @throws IllegalStateException if no group. */
    private void requireGroup() {
        if(group == null)
            throw new IllegalStateException("group not started");
    }
    
    /** @throws IllegalStateException if group. */
    private void requireNoGroup() {
        if(group != null)
            throw new IllegalStateException("group already started");
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
        
        @Override
        public long getParts() {
            return callable.getParts();
        }
        
    }
    
}
