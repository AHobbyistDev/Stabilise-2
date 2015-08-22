package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;
import com.stabilise.util.concurrent.event.RetainedEventDispatcher;


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
	
	private static final Event EVENT_STOP = new Event("stop");
	private static final Event EVENT_CANCEL = new Event("cancel");
	private static final Event EVENT_FAIL = new Event("fail");
	private static final Event EVENT_COMPLETE = new Event("complete");
	
	protected final AtomicReference<TaskState> state =
			new AtomicReference<>(TaskState.UNSTARTED);
	
	protected final TaskTracker tracker;
	protected final EventDispatcher events;
	
	protected Task parentTask = null;
	protected Runnable thisTask = null;
	protected Task nextTask = null;
	
	protected Executor executor = null;
	
	protected final Lock doneLock = new ReentrantLock();
	protected final Condition doneCondition = doneLock.newCondition();
	
	/** The thread on which this task is executing. */
	protected volatile Thread thread = null;
	
	private volatile boolean cancelled = false;
	
	
	public Task(Executor exec, String status, int parts) {
		this.tracker = new TaskTracker(status, parts);
		this.events = new RetainedEventDispatcher(exec);
	}
	
	@Override
	public void run() {
		//if(!started.compareAndSet(false, true))
		//	throw new IllegalStateException("already started");
		
		try {
			if(execute())
				finish();
		} catch(Exception e) {
			events.post(EVENT_STOP);
			events.post(EVENT_FAIL);
		}
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
		} else {
			events.post(EVENT_STOP);
			events.post(EVENT_COMPLETE);
		}
	}
	
	/**
	 * Cancels this task.
	 */
	public void cancel() {
		if(!tracker.stopped()) {
			cancelled = true;
			interrupt();
		}
	}
	
	/**
	 * Interrupts this task.
	 */
	protected void interrupt() {
		if(!Thread.currentThread().equals(thread) && thread != null)
			thread.interrupt();
	}
	
	/**
	 * Checks for cancellation. This should be periodically invoked to ensure
	 * responsiveness to a cancel request.
	 * 
	 * @throws InterruptedException if this task was interrupted.
	 */
	protected void checkCancel() throws InterruptedException {
		if(cancelled || Thread.interrupted())
			throw new InterruptedException("Task cancelled");
	}
	
	public void onStop(EventHandler handler) {
		events.addListener(EVENT_STOP, handler);
	}
	
	public void onCancel(EventHandler handler) {
		events.addListener(EVENT_CANCEL, handler);
	}
	
	public void onFail(EventHandler handler) {
		events.addListener(EVENT_FAIL, handler);
	}
	
	public void onComplete(EventHandler handler) {
		events.addListener(EVENT_COMPLETE, handler);
	}
	
}
