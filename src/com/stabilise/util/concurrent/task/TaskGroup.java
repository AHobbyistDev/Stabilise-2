package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class TaskGroup extends TaskUnit {
    
    private final List<TaskUnit> subtasks = new ArrayList<>();
    private int remainingSubtasks = 0;
    
    public TaskGroup(Executor exec, PrototypeTracker protoTracker) {
        super(exec, null, protoTracker);
    }
    
    @Override
    protected void build() {
        super.build();
        for(TaskUnit t : subtasks) // also build subtasks
            t.build();
    }
    
    @Override
    protected boolean execute() {
        for(TaskUnit t : subtasks) {
            executor.execute(t);
        }
        
        return remainingSubtasks == 0;
    }
    
    public TaskGroup addSubtask(TaskUnit t) {
        subtasks.add(t);
        remainingSubtasks++;
        return this;
    }
    
    public void onSubtaskFinish() {
        synchronized(subtasks) {
            if(--remainingSubtasks != 0)
                return;
        }
        finish();
    }
    
    @Override
    void cancel() {
        for(TaskUnit subtask : subtasks) {
            subtask.cancel();
        }
    }
    
}
