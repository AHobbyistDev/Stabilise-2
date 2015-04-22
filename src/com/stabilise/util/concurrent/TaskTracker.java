package com.stabilise.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.util.annotation.ThreadSafe;

/**
 * A TaskTracker is designed to provide a means of communication between
 * threads as to the progress towards completion of a task. In a typical
 * implementation, worker threads simply invoke {@link #increment()} for each
 * 'part' of the task they complete.
 * 
 * <p>As is implied, this class is thread-safe.
 */
@ThreadSafe
public class TaskTracker implements Tracker {
	
	/** Task status. Never null. */
	private volatile String status;
	
	private final AtomicInteger numPartsCompleted = new AtomicInteger(0);
	private final int numPartsToComplete;
	
	
	/**
	 * Creates a new TaskTracker with 1 part to complete.
	 * 
	 * @param status The initial status of the task.
	 * 
	 * @throws NullPointerException if {@code status} is {@code null}.
	 */
	public TaskTracker(String status) {
		this(status, 1);
	}
	
	/**
	 * Creates a new TaskTracker.
	 * 
	 * @param status The initial status of the task.
	 * @param parts The number of parts to complete.
	 * 
	 * @throws NullPointerException if {@code status} is {@code null}.
	 * @throws IllegalArgumentException if {@code parts < 0}.
	 */
	public TaskTracker(String status, int parts) {
		if(status == null)
			throw new NullPointerException("status is null");
		if(parts < 0)
			throw new IllegalArgumentException("parts < 0");
		this.status = status;
		numPartsToComplete = parts;
	}
	
	/**
	 * Sets the status of the task.
	 * 
	 * <p>Memory consistency effects: actions by a thread which sets the
	 * status happen-before subsequent {@link #getStatus() reads} of the
	 * status.
	 * 
	 * @param status The name.
	 * 
	 * @throws NullPointerException if {@code status} is {@code null}.
	 */
	public void setStatus(String status) {
		if(status == null)
			throw new IllegalArgumentException("status is null!");
		this.status = status;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Memory consistency effects: actions by the thread which set the
	 * status happen-before actions in the current thread.
	 */
	@Override
	public String getStatus() {
		return status;
	}
	
	/**
	 * Registers a part of the task as completed, as if by {@link
	 * #increment(int) increment(1)}.
	 * 
	 * <p>Memory consistency effects: actions in a thread prior to invoking
	 * this method happen before actions in another thread which invokes {@link
	 * #percentComplete()} or {@link #completed()}.
	 */
	public void increment() {
		increment(1);
	}
	
	/**
	 * Registers a specified number of parts of the task as completed. Note
	 * that negative values are technically permitted.
	 * 
	 * <p>Memory consistency effects: actions in a thread prior to invoking
	 * this method happen before actions in another thread which invokes {@link
	 * #percentComplete()} or {@link #completed()}.
	 */
	public void increment(int parts) {
		numPartsCompleted.getAndAdd(parts);
	}
	
	@Override
	public int parts() {
		return numPartsToComplete;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Memory consistency effects: actions in a thread prior to invoking {@link
	 * #increment()} or {@link #increment(int)} happen-before actions in the
	 * current thread.
	 */
	@Override
	public int partsCompleted() {
		return numPartsCompleted.get();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Note that the returned value may not necessarily be within the range of
	 * {@code 0.0} to {@code 1.0} if the task behaves contrary to the general
	 * contract of this class.
	 * 
	 * <p>Memory consistency effects: actions in a thread prior to invoking
	 * {@link #increment()} or {@link #increment(int)} happen-before actions in
	 * the current thread.
	 */
	@Override
	public float percentComplete() {
		return numPartsToComplete == 0f
				? 1f
				: (float)numPartsCompleted.get() / numPartsToComplete;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Note that this may never return {@code true} if implementing code does
	 * not ensure to invoke {@link #increment()} or {@link #increment(int)}
	 * appropriately.
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	public boolean completed() {
		return numPartsCompleted.get() >= numPartsToComplete;
	}
	
	/**
	 * Gets a string representation of this task tracker. The returned String
	 * takes the form:
	 * 
	 * <blockquote>
	 * {@code STATUS... X%}
	 * </blockquote>
	 * 
	 * where STATUS is the status of the task, set either in the constructor or
	 * by {@link #setStatus(String)}, and X is the percentage towards
	 * completion of the task, equivalent to:
	 * 
	 * <blockquote>
	 * {@code (int)(100*percentComplete())}
	 * </blockquote>
	 * 
	 * <p>Memory consistency effects: actions in a thread prior to invoking
	 * {@link #increment()} or {@link #increment(int)} happen-before actions in
	 * the current thread.
	 */
	@Override
	public String toString() {
		return status + "... " + ((int)(100*percentComplete())) + "%";
	}
	
}
