package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;

class TaskGroup extends TaskUnit {
    
    private final Executor executor;
    
    private final List<TaskUnit> subtasks = new ArrayList<>();
    private int remainingSubtasks = 0;
    
    @GuardedBy("subtasks") private boolean subtasksCompletedSuccessfully = true;
    
    public TaskGroup(Executor exec, PrototypeTracker protoTracker) {
        super(null, protoTracker);
        this.executor = exec;
    }
    
    @Override
    protected void build() {
        super.build();
        for(TaskUnit t : subtasks) // also build subtasks
            t.build();
    }
    
    @Override
    protected void setThread() {} // do nothing
    
    @Override
    protected boolean execute() {
        for(TaskUnit t : subtasks) {
            t.setTask(owner);
            executor.execute(t);
        }
        
        return remainingSubtasks == 0;
    }
    
    /**
     * Adds a subtask to this group. As this is only invoked from TaskBuilder,
     * it is implicitly trusted that t is not null.
     */
    void addSubtask(TaskUnit t) {
        t.group = this;
        subtasks.add(t);
        remainingSubtasks++;
    }
    
    void onSubtaskFinish(boolean success) {
        synchronized(subtasks) {
            if(!success)
                subtasksCompletedSuccessfully = false;
            if(--remainingSubtasks != 0)
                return;
        }
        if(subtasksCompletedSuccessfully)
            finish();
        else
            fail(null);
    }
    
    @Override
    void cancel() {
        for(TaskUnit subtask : subtasks) {
            subtask.cancel();
        }
    }
    
}
