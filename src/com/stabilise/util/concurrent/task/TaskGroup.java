package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TaskGroup extends TaskUnit {
    
    private final List<TaskUnit> subtasks = new ArrayList<>();
    private int remainingSubtasks = 0;
    private final Lock subtaskLock = new ReentrantLock();
    private final Condition subtaskCondition = subtaskLock.newCondition();
    
    public TaskGroup(Executor exec) {
        super(exec, null, "", 0, false);
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
    
    public void onSubtaskCompletion() {
        synchronized(subtasks) {
            if(--remainingSubtasks != 0)
                return;
        }
        finish();
    }
    
}
