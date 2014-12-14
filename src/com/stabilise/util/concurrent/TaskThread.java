package com.stabilise.util.concurrent;

import java.util.concurrent.ExecutionException;

/**
 * A TaskThread provides a simple implementation of a Thread which wraps a
 * {@code Task}, to avoid repetitious code (i.e. declaring both a {@code Task}
 * and {@code Thread} and monitoring them separately) in ordinary cases.
 * 
 * <p>This class contains wrappers for all exposed methods of a {@code Task},
 * for easy delegated management.
 */
public class TaskThread extends Thread {
	
	/** The task wrapped by this TaskThread. */
	public final Task task;
	
	
	/**
	 * Creates a new TaskThread.
	 * 
	 * @param task The Task to run.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code task == null}.
	 */
	public TaskThread(Task task) {
		super(task);
		if(task == null)
			throw new IllegalArgumentException("task is null!");
		this.task = task;
	}
	
	@Override
	public void run() {
		task.run();
	}
	
	/**
	 * @see Task#cancel()
	 */
	public void cancel() {
		task.cancel();
	}
	
	/**
	 * @see Task#stopped()
	 */
	public boolean stopped() {
		return task.stopped();
	}
	
	/**
	 * @see Task#completed()
	 */
	public boolean completed() {
		return task.completed();
	}
	
	/**
	 * @see Task#completedWithReattempt()
	 */
	public boolean completedWithReattempt() {
		return task.completedWithReattempt();
	}
	
	/**
	 * @see Task#percentComplete()
	 */
	public float percentComplete() {
		return task.percentComplete();
	}
	
	/**
	 * @see Task#waitUntilStopped()
	 */
	public void waitUntilStopped() throws InterruptedException, ExecutionException {
		// Perhaps instead consider invoking join() on this thread?
		task.waitUntilStopped();
	}
	
	/**
	 * @see Task#waitUninterruptibly()
	 */
	public void waitUninterruptibly() throws ExecutionException {
		task.waitUninterruptibly();
	}
	
	/**
	 * @see Task#getThrowable()
	 */
	public Throwable getThrowable() {
		return task.getThrowable();
	}
	
	/**
	 * @see Task#throwThrowable()
	 */
	public void throwThrowable() throws Throwable {
		task.throwThrowable();
	}
	
	/**
	 * @see Task#setName(String)
	 */
	public void setTaskName(String name) {
		task.setName(name);
	}
	
	/**
	 * @see Task#getName()
	 */
	public String getTaskName() {
		return task.getName();
	}
	
	/**
	 * @see Task#toString()
	 */
	public String taskToString() {
		return task.toString();
	}
}
