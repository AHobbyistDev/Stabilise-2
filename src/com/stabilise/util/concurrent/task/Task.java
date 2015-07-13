package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;


public class Task implements Runnable {
	
	public enum StopType {
		CANCEL, EXCEPTION;
	}
	
	private AtomicBoolean started = new AtomicBoolean(false);
	
	private Task parentTask = null;
	private Consumer<TaskHandle> thisTask = null;
	private Task nextTask = null;
	
	private Executor executor = null;
	
	private boolean hasSubtasks = false;
	private List<Task> subtasks = null;
	private int remainingSubtasks = 0;
	private Lock subtaskLock = null;
	private Condition subtaskCondition = null;
	
	private final Lock doneLock = new ReentrantLock();
	private final Condition doneCondition = doneLock.newCondition();
	
	public Task() {
		
	}
	
	private void addSubtask(Task t) {
		if(started.get())
			throw new IllegalStateException();
		
		if(!hasSubtasks) {
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
			if(--remainingSubtasks == 0)
				;
		}
	}
	
	public void run() {
		if(!started.compareAndSet(false, true))
			throw new IllegalStateException("already started");
		
		if(thisTask != null)
			thisTask.accept(new TaskHandle());
		
		if(hasSubtasks) {
			if(executor == null)
				executor = currentThreadExecutor();
			
			for(Task t : subtasks) {
				t.parentTask = this;
				executor.execute(t);
			}
		} else {
			finish();
		}
	}
	
	private void finish() {
		if(nextTask != null) {
			nextTask.parentTask = this;
			nextTask.run();
		}
	}
	
	public void cancel() {
		
	}
	
	public void waitForAll() {
		
	}
	
	private static Executor currentThreadExecutor() {
		return r -> r.run();
	}
	
	public class TaskHandle {
		
		private TaskHandle() {}
		
	}
	
	public static class TaskBuilder {
		
		private boolean bubbleExceptionsToParentTask = false;
		
		private TaskBuilder() {}
		
	}
	
}
