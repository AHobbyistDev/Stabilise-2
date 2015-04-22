package com.stabilise.util.concurrent;

/**
 * A Tracker provides a means of communication between threads as to the
 * progress towards completion of a task in a way that is meaningful to the end
 * user.
 */
public interface Tracker {
	
	/**
	 * Gets the status of the task.
	 */
	String getStatus();
	
	/**
	 * @return The number of parts in the task.
	 */
	public int parts();
	
	/**
	 * @return The number of parts of the task which have been completed.
	 */
	public int partsCompleted();
	
	/**
	 * Gets the percentage of the task which has been completed thus far.
	 * 
	 * @return The percentage, which should be from {@code 0.0} (inclusive) to
	 * {@code 1.0} (inclusive).
	 */
	default float percentComplete() {
		return parts() == 0f
				? 1f
				: (float)partsCompleted() / parts();
	}
	
	/**
	 * Checks for whether or not the task has been completed based on the parts
	 * count; that is, returns a value equivalent to the expression:
	 * 
	 * <pre>partsCompleted() >= parts();</pre>
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	default boolean completed() {
		return partsCompleted() >= parts();
	}
	
}
