package com.stabilise.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A TaskTracker is designed to provide a means of communication between
 * threads as to the progress towards completion of a task. In a typical
 * implementation, worker threads simply invoke {@link #increment()} for each
 * 'part' of the task they complete.
 * 
 * <p>As is implied, this class is thread-safe.
 */
public class TaskTracker {
	
	/** Whether or not the task has been started. This is set to true when
	 * {@link #increment()} is invoked to prevent partsToComplete from
	 * further increasing. */
	private volatile boolean started = false;
	/** The name of the task. */
	private volatile String name;
	/** The number of parts to the task which have been completed. */
	private AtomicInteger partsCompleted = new AtomicInteger();
	/** The number of parts which are to be completed. */
	private int partsToComplete;
	
	
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
	 * @throws IllegalArgumentException Thrown if {@code name} is {@code null}.
	 */
	public TaskTracker(String name) {
		this(name, 1);
	}
	
	/**
	 * Creates a new TaskTracker with a task name of "Loading".
	 * 
	 * @param parts The number of parts to complete.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code parts < 0}.
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
	 * @throws IllegalArgumentException Thrown if {@code name} is {@code null}
	 * or {@code parts < 0}.
	 */
	public TaskTracker(String name, int parts) {
		if(name == null)
			throw new IllegalArgumentException("name is null");
		if(parts < 0)
			throw new IllegalArgumentException("parts < 0");
		this.name = name;
		partsToComplete = parts;
	}
	
	/**
	 * Adds a defined number of parts to the TaskTracker.
	 * 
	 * @param parts The number of parts to add.
	 * 
	 * @throws IllegalStateException Thrown if the task has already started
	 * (i.e. if {@link #increment()} has been invoked at least once).
	 * @throws IllegalArgumentException Thrown if {@code parts < 1}.
	 */
	public void addParts(int parts) {
		if(started)
			throw new IllegalStateException("Cannot add more parts once the task has started!");
		if(parts < 1)
			throw new IllegalArgumentException("Cannot add < 1 parts!");
		partsToComplete += parts;
	}
	
	/**
	 * Sets the name of the task.
	 * 
	 * @param name The name of the task.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} is {@code null}.
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
	 * Registers a part of the task as completed.
	 * 
	 * <!-- TODO: Memory consistency effects. I'm still a little fuzzy on the
	 * details for Atomic objects. -->
	 */
	public void increment() {
		started = true;
		partsCompleted.getAndIncrement();
	}
	
	/**
	 * Gets the percentage of the task which has been completed thus far. Note
	 * that the returned value may be greater than {@code 1.0} if
	 * {@link #increment()} is invoked an incorrect number of times.
	 * 
	 * @return The percentage, from {@code 0.0} to {@code 1.0}.
	 */
	public float percentComplete() {
		// Though there exists the possibility for an inconsistent view of
		// partsToComplete, this shouldn't matter as it should only be changed
		// before the task has started and this should only be invoked once the
		// task has started; hence no need to strive for atomicity.
		return partsToComplete == 0f ? 1f : (float)partsCompleted.get() / partsToComplete;
	}
	
	/**
	 * Checks for whether or not the task has been completed. Note that this
	 * may never return {@code true} if implementing code does not ensure to
	 * invoke {@link #increment()} for every part the TaskTracker was
	 * designed to track.
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	public boolean completed() {
		return partsCompleted.get() >= partsToComplete;
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
	 */
	@Override
	public String toString() {
		return name + "... " + ((int)(100*percentComplete())) + "%";
	}
	
}
