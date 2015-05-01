package com.stabilise.util.concurrent;

/**
 * A Tracker provides a means of communication between threads as to the
 * progress towards completion of a task in a way that is meaningful to a
 * player.
 */
public interface Tracker {
	
	/**
	 * Gets the status of the task.
	 */
	String getStatus();
	
	/**
	 * @return The number of parts in the task, which is greater than 0.
	 */
	public int parts();
	
	/**
	 * @return The number of parts of the task which have been completed.
	 */
	public int partsCompleted();
	
	/**
	 * Gets the percentage of the task which has been completed thus far.
	 * 
	 * @return The percentage, which should be from {@code 0.0} to {@code 1.0}
	 * (inclusive).
	 */
	float percentComplete();
	
	/**
	 * Checks for whether or not the task is done. This result encapsulates the
	 * two possible end states: {@link #completed() completion} and {@link
	 * #failed() failure}.
	 * 
	 * {@code true} if the task is done; {@code false} otherwise.
	 */
	boolean stopped();
	
	/**
	 * Checks for whether or not the task has been completed. If this returns
	 * {@code true}, then {@link #stopped()} will return true too. Note that
	 * the returned value is <i>not</i> equivalent to the expression {@code
	 * partsCompleted == parts()}.
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	boolean completed();
	
	/**
	 * Checks for whether or not the task has failed. If this returns {@code
	 * true}, then {@link #stopped()} will return true too.
	 * 
	 * @return {@code true} if the task has failed; {@code false} otherwise.
	 */
	boolean failed();
	
	/**
	 * Blocks the current thread until the task is done, either due to
	 * completion or failure.
	 */
	void waitUntilDone() throws InterruptedException;
	
	/**
	 * Blocks the current thread until the task is done, either due to
	 * completion or failure.
	 * 
	 * <p>If the current thread received an interrupt while waiting, the
	 * interrupt flag will be set when this method returns.
	 */
	void waitUninterruptibly();
	
}
