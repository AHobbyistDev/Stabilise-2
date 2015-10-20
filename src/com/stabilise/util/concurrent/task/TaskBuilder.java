package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;

import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;

/**
 * A TaskBuilder builds a task.
 * 
 * <p>The type parameters of this class should be invisible and irrelevant to
 * the end-user, as this class should only ever occur amidst invocation chains.
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
    
    private boolean built = false;
    
    
    /**
     * @throws IllegalStateException if either the executor wasn't set on the
     * builder.
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
    
    public TaskBuilder<R, T> andThen(Runnable r) {
        return andThen(Tasks.wrap(r));
    }
    
    public TaskBuilder<R, T> andThen(TaskRunnable t) {
        return andThen(t, null, 0, false);
    }
    
    public TaskBuilder<R, T> andThen(long parts, TaskRunnable t) {
        return andThen(t, null, parts, true);
    }
    
    private TaskBuilder<R, T> andThen(TaskRunnable t, String name, long parts,
            boolean partsSpecified) {
        checkState();
        if(group != null && focus == null)
            throw new IllegalStateException("can't do a task after nothing in a group!");
        TaskUnit unit;
        if(group == null) {
            unit = new TaskUnit(executor, t, tracker.child(parts, name));
            tail = unit;
        } else {
            unit = new TaskUnit(executor, t, group.protoTracker.child(parts, name));
            unit.group = group;
        }
        if(first == null)
            first = unit;
        if(focus != null)
            focus.next = unit;
        focus = unit;
        return this;
    }
    
    public <E extends Event> TaskBuilder<R, T> onEvent(E e, EventHandler<? super E> h) {
        checkState();
        requireFocus();
        focus.addListener(e, h);
        return this;
    }
    
    public TaskBuilder<R, T> andThenGroup() {
        return andThenGroup(null, ReportStrategy.all());
    }
    
    public TaskBuilder<R, T> andThenGroup(String status, ReportStrategy subtaskReportStrategy) {
        checkState();
        requireNoGroup();
        group = new TaskGroup(executor, tracker.child(0, status));
        if(first == null)
            first = group;
        if(tail != null) {
            tail.next = group;
            tail = group;
        }
        focus = null;
        return this;
    }
    
    public TaskBuilder<R, T> endGroup() {
        checkState();
        requireGroup();
        focus = group;
        group = null;
        return this;
    }
    
    public TaskBuilder<R, T> subtask(Runnable r) {
        return subtask(Tasks.wrap(r));
    }
    
    public TaskBuilder<R, T> subtask(TaskRunnable t) {
        return subtask(t, "", 1, false);
    }
    
    public TaskBuilder<R, T> subtask(TaskRunnable t, int parts) {
        return subtask(t, "", parts, true);
    }
    
    private TaskBuilder<R, T> subtask(TaskRunnable t, String name, int parts,
            boolean partsSpecified) {
        checkState();
        requireGroup();
        TaskUnit unit = new TaskUnit(executor, t, group.protoTracker.child(parts, name));
        group.addSubtask(unit);
        focus = unit;
        return this;
    }
    
    /**
     * Builds and returns the Task, but does not start it.
     * 
     * @throws IllegalStateException if the task has already been built, a
     * group has not been closed, or no task units have been declared.
     */
    public T build() {
        checkState();
        requireNoGroup();
        if(first == null)
            throw new IllegalStateException("no task units");
        built = true;
        
        tracker.buildHeirarchy();
        first.buildHierarchy();
        
        @SuppressWarnings("unchecked")
        T t = (T) (retBox == null
                ? new Task(executor, tracker.get(), first)
                : new ReturnTask<R>(executor, tracker.get(), first, retBox));
        
        return t;
    }
    
    /**
     * Builds, starts and returns the Task.
     * 
     * @throws IllegalStateException if the task has already been built, a
     * group has not been closed, or no task units have been declared.
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
    
    private void requireGroup() {
        if(group == null)
            throw new IllegalStateException("group not started");
    }
    
    private void requireNoGroup() {
        if(group != null)
            throw new IllegalStateException("group already started");
    }
    
}
