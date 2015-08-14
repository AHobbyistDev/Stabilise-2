package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Task implements Runnable {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	protected static final Executor EXEC_CURR_THREAD = r -> r.run();
	
	/**
	 * Different states the task may occupy.
	 */
	protected static enum TaskState {
		/** Indicates that the task has yet to be performed. */
		UNSTARTED,
		/** Indicates that the task is currently being performed. */
		RUNNING,
		/** Indicates that the task has stopped. */
		STOPPED,
		/** Indicates that the task has completed successfully, and
		 * has stopped running. */
		COMPLETED;
	};
	
	public enum StopType {
		CANCEL, EXCEPTION;
	}
	
	protected final AtomicReference<TaskState> state =
			new AtomicReference<>(TaskState.UNSTARTED);
	
	protected final TaskTracker tracker;
	
	protected Task parentTask = null;
	protected Runnable thisTask = null;
	protected Task nextTask = null;
	
	protected Executor executor = null;
	
	protected final Lock doneLock = new ReentrantLock();
	protected final Condition doneCondition = doneLock.newCondition();
	
	/** The thread on which this task is executing. */
	protected volatile Thread thread = null;
	
	private volatile boolean cancelled = false;
	
	public Task(String status, int parts) {
		this.tracker = new TaskTracker(status, parts);
	}
	
	public void run() {
		//if(!started.compareAndSet(false, true))
		//	throw new IllegalStateException("already started");
		
		if(execute())
			finish();
	}
	
	protected boolean execute() {
		if(thisTask != null)
			thisTask.run();
		return true;
	}
	
	protected void finish() {
		if(nextTask != null) {
			nextTask.parentTask = this;
			nextTask.run();
		}
	}
	
	public void cancel() {
		cancelled = true;
		interrupt();
	}
	
	protected void interrupt() {
		if(!Thread.currentThread().equals(thread) && thread != null)
			thread.interrupt();
	}
	
	protected void checkCancel() throws InterruptedException {
		if(cancelled)
			throw new InterruptedException("Task cancelled");
	}
	
	public void waitForAll() {
		
	}
	
}
