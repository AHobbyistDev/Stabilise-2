package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskGroup extends TaskUnit {
    
    private final List<TaskUnit> subtasks = new ArrayList<>();
    private int remainingSubtasks = 0;
    private final Lock subtaskLock = new ReentrantLock();
    private final Condition subtaskCondition = subtaskLock.newCondition();
    
    public TaskGroup() {
        super(null, "", 0);
    }
    
    @Override
    protected boolean execute() {
        for(TaskUnit t : subtasks) {
            t.parent = this;
            executor.execute(t);
        }
        
        return remainingSubtasks == 0;
    }
    
    private TaskGroup addSubtask(TaskUnit t) {
        subtasks.add(t);
        remainingSubtasks++;
        return this;
    }
    
    private void onSubtaskCompletion() {
        synchronized(subtasks) {
            if(--remainingSubtasks != 0)
                return;
        }
        finish();
    }
    
}
