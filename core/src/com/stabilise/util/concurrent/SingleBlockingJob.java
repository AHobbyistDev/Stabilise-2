package com.stabilise.util.concurrent;

import java.util.Objects;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides functionality for a job which can only be performed
 * once. This implementation is <em>work-stealing</em> which means that the
 * first thread to arrive at the scene performs the job, while all others after
 * it wait.
 */
@ThreadSafe
public class SingleBlockingJob {
	
	private final BoolCondition cond = new BoolCondition();
	private final Runnable job;
	
	
	/**
	 * @throws NullPointerException if job is null
	 */
	public SingleBlockingJob(Runnable job) {
		this.job = Objects.requireNonNull(job);
	}
	
	/**
	 * Runs the job if it has not yet been run; otherwise blocks until the job
	 * is done.
	 */
	public void run() {
		cond.doThenSetTrue(job);
	}
	
	/**
	 * Returns true if the job is done.
	 */
	public boolean isDone() {
		return cond.isTrue();
	}
	
}
