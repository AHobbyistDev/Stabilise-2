package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskGroup extends Task {
	
	private boolean hasSubtasks = false;
	private List<Task> subtasks = null;
	private int remainingSubtasks = 0;
	private Lock subtaskLock = null;
	private Condition subtaskCondition = null;
	
	public TaskGroup() {
		super(null, "", 0);
	}
	
	@Override
	protected boolean execute() {
		if(executor == null)
			executor = EXEC_CURR_THREAD;
		
		for(Task t : subtasks) {
			t.parentTask = this;
			executor.execute(t);
		}
		
		
		return executor == EXEC_CURR_THREAD;
	}
	
	private void addSubtask(Task t) {
		//if(started.get())
		//	throw new IllegalStateException();
		
		if(!hasSubtasks) {
			// Lazily initialise
			hasSubtasks = true;
			subtasks = new ArrayList<>();
			subtaskLock = new ReentrantLock();
			subtaskCondition = subtaskLock.newCondition();
		}
		
		subtasks.add(t);
		remainingSubtasks++;
	}
	
	private void onSubtaskCompletion() {
		synchronized(subtasks) {
			if(--remainingSubtasks != 0)
				return;
		}
		finish();
	}
	
	@Override
	protected void interrupt() {
		
	}
	
}
