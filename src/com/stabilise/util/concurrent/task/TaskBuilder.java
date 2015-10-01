package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Executor;

import com.stabilise.util.annotation.NotThreadSafe;

@NotThreadSafe
public class TaskBuilder {
    
    private final Executor executor;
    
    private final String startName;
    
    private TaskUnit first = null;
    private TaskUnit tail = null;
    
    private TaskUnit curFocus = null;
    private TaskGroup curGroup = null;
    
    private boolean built = false;
    
    public TaskBuilder(Executor executor, String startName) {
        this.executor = Objects.requireNonNull(executor);
        this.startName = Objects.requireNonNull(startName);
    }
    
    public TaskBuilder andThen(TaskRunnable t) {
        checkState();
        return this;
    }
    
    public TaskBuilder andThen(TaskRunnable t, int parts) {
        return this;
    }
    
    public TaskBuilder onStop(Runnable r) {
        checkState();
        requireFocus();
        return this;
    }
    
    public TaskBuilder onComplete(Runnable r) {
        checkState();
        requireFocus();
        return this;
    }
    
    public TaskBuilder onCancel(Runnable r) {
        checkState();
        requireFocus();
        return this;
    }
    
    public TaskBuilder onFail(Runnable r) {
        checkState();
        requireFocus();
        return this;
    }
    
    public TaskBuilder andThenGroup() {
        checkState();
        if(curGroup != null)
            throw new IllegalStateException("already grouped");
        curGroup = new TaskGroup();
        return this;
    }
    
    public TaskBuilder subtask(TaskRunnable t) {
        checkState();
        if(curGroup == null)
            throw new IllegalStateException("group not started");
        return this;
    }
    
    public TaskBuilder subTask(TaskRunnable t, int parts) {
        checkState();
        if(curGroup == null)
            throw new IllegalStateException("group not started");
        return this;
    }
    
    public TaskBuilder endGroup() {
        checkState();
        if(curGroup == null)
            throw new IllegalStateException("group not started");
        return this;
    }
    
    public Task build() {
        checkState();
        if(first == null)
            throw new IllegalStateException("no task units");
        if(curGroup != null)
            throw new IllegalStateException("group not ended");
        
        built = true;
        return new Task();
    }
    
    /** @throws IllegalStateException if already built. */
    private void checkState() {
        if(built)
            throw new IllegalStateException("Already built");
    }
    
    /** @throws IllegalStateException if no focus. */
    private void requireFocus() {
        if(curFocus == null)
            throw new IllegalStateException();
    }
    
    public static Task exec(Executor executor, String name, TaskRunnable t) {
        return new TaskBuilder(executor, name).andThen(t).build();
    }
    
}
