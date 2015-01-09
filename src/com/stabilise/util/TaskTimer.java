package com.stabilise.util;

import java.util.concurrent.TimeUnit;

/**
 * A TaskTimer allows the execution time of a task to be measured and compared
 * to the execution time of another task for the sake of determining
 * efficiency. Alternatively, a TaskTimer may simply be used to measure the
 * the time taken to complete a single task.
 * 
 * <p>An instance of this class does not use any resources, threaded or
 * otherwise, once {@code start} is invoked; it merely compares the system
 * times when {@code start} and {@code stop} are invoked.
 * 
 * <p>For comparison purposes, a SpeedComparator may be used as such:
 * 
 * <pre>
 * TaskTimer t1 = new TaskTimer("Task 1");
 * TaskTimer t2 = new TaskTimer("Task 2");
 * 
 * t1.start();
 * doSomeLongTask();
 * t1.stop();
 * t2.start();
 * doSomeOtherLongTask();
 * t2.stop();
 * 
 * t1.printResult();
 * t2.printResult();
 * t1.printComparison(c2);
 * </pre>
 * 
 * <p>A TaskTimer is designed for use by a single thread, and an instance is
 * not thread-safe.
 */
public class TaskTimer {
	
	/** The name of the task being timed. */
	private final String name;
	/** The last time recorded. */
	private long init = 0L;
	/** The duration of the task. */
	private long duration = 0L;
	
	
	/**
	 * Creates a new TaskTimer.
	 * 
	 * @param name The name of the task this TaskTimer is to time.
	 */
	public TaskTimer(String name) {
		this.name = name;
	}
	
	/**
	 * Starts the timer.
	 */
	public void start() {
		duration = 0L;
		init = System.nanoTime();
	}
	
	/**
	 * Stops the timer.
	 * 
	 * @throws IllegalStateException if the timer has not yet been started.
	 */
	public void stop() {
		if(init == 0L)
			throw new IllegalStateException("Timer has not been started!");
		duration = System.nanoTime() - init;
		init = 0L;
	}
	
	/**
	 * Gets the duration of the task in the specified units - that is, the time
	 * between the most recent consecutive invocations of {@link #start()} and
	 * {@link #stop()}.
	 * 
	 * @param unit The unit in which form to return the duration.
	 * 
	 * @return The duration in the specified units.
	 */
	public long getDuration(TimeUnit unit) {
		return unit.convert(duration, TimeUnit.NANOSECONDS);
	}
	
	/**
	 * @return The result of the timing operation, in nanoseconds.
	 */
	public String getResult() {
		return getResult(TimeUnit.NANOSECONDS);
	}
	
	/**
	 * @param unit The unit in which form to return the result.
	 * 
	 * @return The result of the timing operation, in the specified units.
	 */
	public String getResult(TimeUnit unit) {
		return name + " took " + getDuration(unit) + " " + unitName(unit) + ".";
	}
	
	/**
	 * Prints the result of the timing operation to the console.
	 */
	public void printResult() {
		System.out.println(getResult());
	}
	
	/**
	 * Prints the result of the timing operation to the console.
	 * 
	 * @param unit The unit in which form to print the result.
	 */
	public void printResult(TimeUnit unit) {
		System.out.println(getResult(unit));
	}
	
	/**
	 * Prints the result of the timing operation to the log.
	 * 
	 * @see com.stabilise.util.Log
	 */
	public void logResult() {
		logResult(Log.get());
	}
	
	/**
	 * Prints the result of the timing operation to the log.
	 * 
	 * @param unit The unit in which form to print the result.
	 * 
	 * @see com.stabilise.util.Log
	 */
	public void logResult(TimeUnit unit) {
		logResult(Log.get(), unit);
	}
	
	/**
	 * Prints the result of the timing operation to the given logging agent.
	 * 
	 * @param log The logging agent.
	 * 
	 * @see com.stabilise.util.Log
	 */
	public void logResult(Log log) {
		log.postInfo(getResult());
	}
	
	/**
	 * Prints the result of the timing operation to the given logging agent.
	 * 
	 * @param log The logging agent.
	 * @param unit The unit in which form to print the result.
	 * 
	 * @see com.stabilise.util.Log
	 */
	public void logResult(Log log, TimeUnit unit) {
		log.postInfo(getResult(unit));
	}
	
	/**
	 * Gets a comparison string for the task performed by this timer and the
	 * task performed by another timer.
	 * 
	 * @param other The other task's timer.
	 * 
	 * @return The comparison string.
	 * @throws IllegalStateException if either timer has not been run.
	 */
	public String getComparison(TaskTimer other) {
		if(duration == 0L || other.duration == 0L)
			throw new IllegalStateException("Both timers must have been run!");
		if(duration == other.duration)
			return name + " was equally as fast as " + other.name + "!";
		else if(duration > other.duration)
			return other.name + " was " + percentage(other, this) + "% faster than " + name + "!";
		else
			return name + " was " + percentage(this, other) + "% faster than " + other.name + "!";
	}
	
	/**
	 * Prints a comparison string for the task performed by this timer and the
	 * task performed by another timer to the console.
	 * 
	 * @param other The other task's timer.
	 * 
	 * @throws IllegalStateException if either timer has not been run.
	 */
	public void printComparison(TaskTimer other) {
		System.out.println(getComparison(other));
	}
	
	/**
	 * Prints a comparison string for the task performed by this timer and the
	 * task performed by another timer to the log.
	 * 
	 * @param other The other task's timer.
	 * 
	 * @throws IllegalStateException if either timer has not been run.
	 * @see com.stabilise.util.Log
	 */
	public void logComparison(TaskTimer other) {
		logComparison(other, Log.get());
	}
	
	/**
	 * Prints a comparison string for the task performed by this timer and the
	 * task performed by another comparator to the given logging agent.
	 * 
	 * @param other The other task's timer.
	 * @param log The logging agent.
	 * 
	 * @throws IllegalStateException if either timer has not been run.
	 * @see com.stabilise.util.Log
	 */
	public void logComparison(TaskTimer other, Log log) {
		log.postInfo(getComparison(other));
	}
	
	// ----------Static Methods----------
	
	/**
	 * Calculates and returns a string representation (to 2 d.p) of the
	 * percentage by which the faster timer completed than the slower one.
	 * 
	 * @param faster The faster timer.
	 * @param slower The slower timer.
	 * 
	 * @return The string representation of the percentage.
	 */
	private static String percentage(TaskTimer faster, TaskTimer slower) {
		return StringUtil.cullFP(100 * (((double)slower.duration / (double)faster.duration) - 1), 2);
	}
	
	/**
	 * @return The name of the specified unit.
	 */
	private static String unitName(TimeUnit unit) {
		switch(unit) {
			case DAYS:
				return "days";
			case HOURS:
				return "hours";
			case MICROSECONDS:
				return "microseconds";
			case MILLISECONDS:
				return "milliseconds";
			case MINUTES:
				return "minutes";
			case NANOSECONDS:
				return "nanoseconds";
			case SECONDS:
				return "seconds";
			default:
				return "";
		}
	}
	
}
