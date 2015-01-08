package com.stabilise.util.concurrent;

/**
 * A TaskTracker is designed to provide a means of communication between
 * threads as to the progress towards completion of a task. In a typical
 * implementation, worker threads simply invoke {@link #increment()} for each
 * 'part' of the task they complete.
 * 
 * <p>As is implied, this class is thread-safe.
 */
public class TaskTracker {
	
	/** Task name. */
	private volatile String name;
	
	private Object mutex = new Object();
	private volatile int numPartsCompleted = 0;
	private float numPartsToComplete; // stored as a float to save on conversion time in percentComplete()
	
	
	/**
	 * Creates a new TaskTracker with a task name of "Loading" and 1 part to
	 * complete.
	 */
	public TaskTracker() {
		this(1);
	}
	
	/**
	 * Creates a new TaskTracker with 1 part to complete.
	 * 
	 * @param name The name of the task.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public TaskTracker(String name) {
		this(name, 1);
	}
	
	/**
	 * Creates a new TaskTracker with a task name of "Loading".
	 * 
	 * @param parts The number of parts to complete.
	 * 
	 * @throws IllegalArgumentException if {@code parts < 0}.
	 */
	public TaskTracker(int parts) {
		this("Loading", parts);
	}
	
	/**
	 * Creates a new TaskTracker.
	 * 
	 * @param name The name of the task.
	 * @param parts The number of parts to complete.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws IllegalArgumentException if {@code parts < 0}.
	 */
	public TaskTracker(String name, int parts) {
		if(name == null)
			throw new NullPointerException("name is null");
		if(parts < 0)
			throw new IllegalArgumentException("parts < 0");
		this.name = name;
		numPartsToComplete = (float)parts;
	}
	
	/**
	 * Sets the name of the task.
	 * 
	 * @param name The desired name.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public void setName(String name) {
		if(name == null)
			throw new IllegalArgumentException("name is null!");
		this.name = name;
	}
	
	/**
	 * Gets the name of the task.
	 * 
	 * <p>Memory consistency effects: actions by the thread which set the name
	 * happen-before actions in the current thread.
	 * 
	 * @return The name of the task.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Registers a part of the task as completed, as if by {@link
	 * #increment(int) increment(1)}.
	 * 
	 * <p>Memory consistency effects: actions in a thread prior to invoking
	 * this method happen before actions in another thread which invokes {@link
	 * #parts()}, {@link #percentComplete()} or {@link #completed()}.
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
	 * #parts()}, {@link #percentComplete()} or {@link #completed()}.
	 */
	public void increment(int parts) {
		synchronized(mutex) {
			numPartsCompleted += parts;
		}
	}
	
	/**
	 * @return The number of parts in the task, as defined in the constructor.
	 */
	public int parts() {
		return (int)numPartsToComplete;
	}
	
	/**
	 * Memory consistency effects: actions in a thread prior to invoking {@link
	 * #increment()} or {@link #increment(int)} happen-before actions in the
	 * current thread.
	 * 
	 * @return The number of parts of the task completed.
	 */
	public int partsCompleted() {
		return numPartsCompleted;
	}
	
	/**
	 * Gets the percentage of the task which has been completed thus far. Note
	 * that the returned value may not necessarily be within the range of
	 * {@code 0.0} to {@code 1.0} if the task behaves contrary to the general
	 * contract of this class.
	 * 
	 * <p>Memory consistency effects: actions in a thread prior to invoking
	 * {@link #increment()} or {@link #increment(int)} happen-before actions in
	 * the current thread.
	 * 
	 * @return The percentage, from {@code 0.0} (inclusive) to {@code 1.0}
	 * (inclusive).
	 */
	public float percentComplete() {
		return numPartsToComplete == 0f ? 1f : numPartsCompleted / numPartsToComplete;
	}
	
	/**
	 * Checks for whether or not the task has been completed. Note that this
	 * may never return {@code true} if implementing code does not ensure to
	 * invoke {@link #increment()} or {@link #increment(int)} appropriately.
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	public boolean completed() {
		return numPartsCompleted >= numPartsToComplete;
	}
	
	/**
	 * Gets a string representation of this task tracker. The returned String
	 * takes the form:
	 * 
	 * <blockquote>
	 * {@code NAME... X%}
	 * </blockquote>
	 * 
	 * where NAME is the name of the task, set either in the constructor or by
	 * {@link #setName(String)}, and X is the percentage towards completion of
	 * the task, equivalent to:
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
		return name + "... " + ((int)(100*percentComplete())) + "%";
	}
	
}
