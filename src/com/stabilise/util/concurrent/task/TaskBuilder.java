package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Executor;

import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;

@NotThreadSafe
public class TaskBuilder {
    
    private final Executor executor;
    
    private final TaskTracker tracker;
    
    private TaskUnit first = null;
    private TaskUnit tail = null;
    
    private TaskUnit focus = null;
    private TaskGroup group = null;
    
    private boolean built = false;
    
    public TaskBuilder(Executor executor, String startName) {
        this.executor = Objects.requireNonNull(executor);
        this.tracker = new TaskTracker(0, Objects.requireNonNull(startName));
    }
    
    public TaskBuilder andThen(Runnable r) {
        return andThen(Tasks.wrap(r));
    }
    
    public TaskBuilder andThen(TaskRunnable t) {
        return andThen(t, "", 1, false);
    }
    
    public TaskBuilder andThen(TaskRunnable t, int parts) {
        return andThen(t, "", parts, true);
    }
    
    private TaskBuilder andThen(TaskRunnable t, String name, int parts,
            boolean partsSpecified) {
        checkState();
        if(group != null && focus == null)
            throw new IllegalStateException("can't do a task after nothing in a group!");
        TaskUnit unit = new TaskUnit(executor, t, name, parts, partsSpecified);
        if(focus != null)
            focus.next = unit;
        focus = unit;
        if(group != null)
            unit.group = group;
        else
            tail = unit;
        return this;
    }
    
    public <E extends Event> TaskBuilder onEvent(E e, EventHandler<? super E> h) {
        checkState();
        requireFocus();
        focus.addListener(e, h);
        return this;
    }
    
    public TaskBuilder andThenGroup() {
        checkState();
        requireNoGroup();
        group = new TaskGroup(executor);
        if(tail != null) {
            tail.next = group;
            tail = group;
        }
        focus = null;
        return this;
    }
    
    public TaskBuilder endGroup() {
        checkState();
        requireGroup();
        focus = group;
        group = null;
        return this;
    }
    
    public TaskBuilder subtask(Runnable r) {
        return subtask(Tasks.wrap(r));
    }
    
    public TaskBuilder subtask(TaskRunnable t) {
        return subtask(t, "", 1, false);
    }
    
    public TaskBuilder subtask(TaskRunnable t, int parts) {
        return subtask(t, "", parts, true);
    }
    
    private TaskBuilder subtask(TaskRunnable t, String name, int parts,
            boolean partsSpecified) {
        checkState();
        requireGroup();
        TaskUnit unit = new TaskUnit(executor, t, name, parts, partsSpecified);
        group.addSubtask(unit);
        focus = unit;
        return this;
    }
    
    public Task build() {
        checkState();
        requireNoGroup();
        if(first == null)
            throw new IllegalStateException("no task units");
        
        built = true;
        return new Task(tracker, first).start(executor);
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
