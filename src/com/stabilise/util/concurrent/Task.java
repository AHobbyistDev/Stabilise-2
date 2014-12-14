package com.stabilise.util.concurrent;

import java.util.concurrent.ExecutionException;

/**
 * A Task is a wrapper for a {@code Runnable} with additional facilities, such
 * as those of cancellation and progress tracking.
 * 
 * <p>Unlike a {@code Runnable}, subclasses should override {@link #execute()}
 * instead of {@link #run()} when implementing code. Furthermore, it is advised
 * that subclasses interact with the provided {@link #tracker TaskTracker} when
 * performing the task.
 * 
 * <p>Instances of this class are thread-safe - however, normal synchronisation
 * requirements apply for implemented code, as they would for any
 * {@code Runnable}.
 */
public abstract class Task implements Runnable {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
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
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The thread which is executing the task. */
	private Thread thread;
	
	/** The task's state. */
	private volatile TaskState state = TaskState.UNSTARTED;
	/** Whether or not the task has been cancelled. */
	private volatile boolean cancelled = false;
	/** The throwable thrown during execution of the task. A value of
	 * {@code null} indicates the task ran without encountering anything. */
	private volatile Throwable throwable = null;
	
	/** The task tracker. This is initially constructed as per
	 * {@link TaskTracker#TaskTracker(int) new TaskTracker(parts)} when the
	 * Task is constructed through {@link #Task(int) new Task(parts)}, or may
	 * be otherwise set by {@link Task#Task(TaskTracker)}. */
	protected final TaskTracker tracker;
	
	/** The lock used for waiting on the task for completion. */
	private final Object lock = new Object();
	
	
	/**
	 * Creates a new Task, as if by {@link #Task(int) new Task(0)}.
	 */
	public Task() {
		this(0);
	}
	
	/**
	 * Creates a new Task.
	 * 
	 * @param parts The number of parts in the task.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code parts < 0}.
	 */
	public Task(int parts) {
		this(new TaskTracker(parts));
	}
	
	/**
	 * Creates a new Task.
	 * 
	 * <p>This constructor should typically be used only when multiple tasks
	 * are intended to report to the same {@code TaskTracker}.
	 * 
	 * @param tracker The task's {@code TaskTracker}.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code tracker} is
	 * {@code null}.
	 */
	public Task(TaskTracker tracker) {
		if(tracker == null)
			throw new IllegalArgumentException("tracker is null");
		this.tracker = tracker;
	}
	
	/**
	 * Runs the task. If {@link #cancel()} was invoked before this method is
	 * invoked, the task will abort immediately.
	 * 
	 * @throws IllegalStateException Thrown if the task is already running.
	 */
	public final void run() {
		if(state.equals(TaskState.RUNNING))
			throw new IllegalStateException("Task is already running!");
		if(cancelled) {
			state = TaskState.STOPPED;
			return;
		}
		state = TaskState.RUNNING;
		thread = Thread.currentThread();
		try {
			execute();
		} catch(Throwable t) {
			setThrowable(t);
			state = TaskState.STOPPED;
			wakeWaitingThreads();
			return;
		}
		state = TaskState.COMPLETED;
		wakeWaitingThreads();
	}
	
	/**
	 * Executes the task. Implementations are encouraged to allow exceptions
	 * to propagate if they are severe enough to halt the task.
	 * 
	 * @throws Exception Thrown as per standard exceptional conditions,
	 * circumstantially defined based on the implementation. A
	 * {@code CancellationException} is thrown if the implementation invoked
	 * {@link #checkCancel()} and another thread had cancelled this task via
	 * {@link #cancel()}.
	 */
	protected abstract void execute() throws Exception;
	
	/**
	 * Wakes up any threads which are waiting for this task to complete, as per
	 * {@link #waitUntilStopped()} or {@link #waitUninterruptibly()}.
	 */
	private void wakeWaitingThreads() {
		synchronized(lock) {
			lock.notifyAll(); 
		}
	}
	
	/**
	 * Checks for whether or not the task has been cancelled. This method
	 * returns a boolean value in preference to throwing a
	 * {@code CancellationException} to allow for a the task implementation to
	 * perform any cleanup operations.
	 * 
	 * @return {@code true} if the task has been cancelled; {@code false}
	 * otherwise.
	 */
	protected final boolean wasCancelled() {
		return cancelled || Thread.interrupted();
	}
	
	/**
	 * Checks for whether or not the task has been cancelled. If so, this
	 * method will throw a {@code CancellationException}, which is handled
	 * automatically by the Task, thus aborting the current
	 * {@link #execute(boolean)} method. Any subclass of Task should not
	 * attempt to catch this.
	 * 
	 * @throws CancellationException Thrown if the task has been cancelled.
	 */
	protected final void checkCancel() throws CancellationException {
		if(state.equals(TaskState.RUNNING) && isCurrentThread() && (cancelled || Thread.interrupted()))
			throw new CancellationException();
	}
	
	/**
	 * Sets the task's throwable, as a means of indicating that a throwable
	 * was encountered during the execution of the task. The throwable will be
	 * returned on an invocation of {@link #getThrowable()}. Note that setting
	 * a throwable does not necessarily indicate that the task failed, but
	 * encountered exceptional conditions which may be worth reporting to the
	 * main thread.
	 * 
	 * <p>If {@code t} is a {@link CancellationException}, it will be ignored.
	 * 
	 * @param t The throwable.
	 */
	protected final void setThrowable(Throwable t) {
		if(!(t instanceof CancellationException))
			throwable = t;
	}
	
	/**
	 * Attempts to cancel the task; how quickly the task aborts is contingent
	 * upon how often the tasks checks for cancellation. Invoking this does
	 * nothing if the task has finished running.
	 */
	public final void cancel() {
		cancelled = true;
		if(state.equals(TaskState.RUNNING))
			thread.interrupt();
	}
	
	/**
	 * Checks for whether or not the task is currently stopped. Note that this
	 * will return {@code false} if the task has not been run yet.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * happen-before actions in the current thread if this method returns
	 * {@code true}.
	 * 
	 * @return {@code true} if the task is stopped; {@code false} if it is
	 * currently executing.
	 */
	public final boolean stopped() {
		return state.equals(TaskState.COMPLETED) || state.equals(TaskState.STOPPED);
	}
	
	/**
	 * Checks for whether or not the task has been completed.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * happen-before actions in the current thread if this method returns
	 * {@code true}.
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	public final boolean completed() {
		return state.equals(TaskState.COMPLETED);
	}
	
	/**
	 * Checks for whether or not the task has been completed, and reattempts
	 * the task on the current thread if it failed to complete otherwise. This
	 * method executes as if by:
	 * 
	 * <pre>
	 * if(stopped()) {
	 *     if(!completed())
	 *         run();
	 *     return true;
	 * }
	 * return false;</pre>
	 * 
	 * <p>Note that this implies that this method may stall the current thread
	 * if the expression {@code stopped() && !completed()} evaluates to
	 * {@code true}.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * happen-before actions in the current thread if this method returns
	 * {@code true}.
	 * 
	 * @return {@code true} if the task has been completed; {@code false} if it
	 * has not.
	 */
	public final boolean completedWithReattempt() {
		if(stopped()) {
			if(!completed())
				run();
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the percentage of the task which has been completed thus far.
	 * 
	 * @return The percentage, from 0.0 to 1.0.
	 */
	public final float percentComplete() {
		return tracker.percentComplete();
	}
	
	/**
	 * Waits for the task to either complete or abort, if it is currently being
	 * executed asynchronously. The current thread will block until the task
	 * has stopped, or this thread is interrupted. Note that when this method
	 * returns the task may not necessarily have been completed successfully.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * happen-before actions in the current thread if this method returns
	 * without throwing an {@code InterruptedException}.
	 * 
	 * @throws InterruptedException Thrown if the current thread was
	 * interrupted while waiting for the task to stop.
	 * @throws ExecutionException Thrown if the task threw an exception or
	 * error while executing.
	 */
	public final void waitUntilStopped() throws InterruptedException, ExecutionException {
		if(canWait()) {
			if(state.equals(TaskState.RUNNING)) {
				synchronized(lock) {
					lock.wait(); // Awoken by wakeWaitingThreads()
				}
			}
			throwExcecutionException();
		}
	}
	
	/**
	 * Waits for the task to either complete or abort, if it is currently being
	 * executed. The current thread will block until the task has stopped
	 * (beware of potential deadlocks). Note that when this method returns the
	 * task may not necessarily have been completed successfully. However, all
	 * actions performed by the task will be synchronised when this method
	 * returns.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * happen-before actions in the current thread when this method returns.
	 * 
	 * @throws ExecutionException Thrown if the task threw an exception or
	 * error while executing.
	 */
	public final void waitUninterruptibly() throws ExecutionException {
		if(canWait()) {
			boolean interrupted = true;
			while(state.equals(TaskState.RUNNING) && interrupted) {
				interrupted = false;
				synchronized(lock) {
					try {
						lock.wait();
					} catch(InterruptedException ignored) {
						interrupted = true;
					}
				}
			}
			throwExcecutionException();
		}
	}
	
	/**
	 * Gets the Throwable most recently thrown by this task. Note that if a
	 * Throwable is returned, this does not necessarily indicate that the task
	 * failed; the executing code could simply have deemed it appropriate to
	 * report an exceptional condition.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * before setting the Throwable (either via
	 * {@link #setThrowable(Throwable)} or allowing one to propagate through
	 * {@link #execute()}) happen-before actions in the current thread when
	 * that Throwable is returned.
	 * 
	 * @return The Throwable most recently thrown by the task, or {@code null}
	 * if a Throwable has not been thrown.
	 */
	public final Throwable getThrowable() {
		return throwable;
	}
	
	/**
	 * Throws the Throwable most recently thrown by this task. If the Throwable
	 * which would be returned upon an invocation of {@link #getThrowable()} is
	 * {@code null}, this method will not throw anything.
	 * 
	 * <p>Memory consistency effects: actions by the thread executing this task
	 * before setting the Throwable (either via
	 * {@link #setThrowable(Throwable)} or allowing one to propagate through
	 * {@link #execute()}) happen-before actions in the current thread when
	 * that Throwable is thrown by this method.
	 * 
	 * @throws Throwable Thrown if a throwable was thrown by the task.
	 */
	public final void throwThrowable() throws Throwable {
		Throwable t = getThrowable();
		if(t != null)
			throw t;
	}
	
	/**
	 * Checks for whether or not the thread is in a state where it may be
	 * waited for.
	 * 
	 * @return {@code true} if task may be waited for; {@code false} otherwise.
	 */
	private boolean canWait() {
		return state.equals(TaskState.RUNNING) && !isCurrentThread();
	}
	
	/**
	 * Checks for whether or not the task is executing on the current thread.
	 * 
	 * @return {@code true} if the task is executing on the current thread;
	 * {@code false} otherwise.
	 */
	private boolean isCurrentThread() {
		return Thread.currentThread().equals(thread);
	}
	
	/**
	 * Throws an {@code ExecutionException} wrapping the most recently caught
	 * Throwable thrown by the task, provided a Throwable was thrown. This
	 * clears the currently stored Throwable.
	 * 
	 * @throws ExecutionException Thrown if the task threw a Throwable while
	 * executing.
	 */
	private void throwExcecutionException() throws ExecutionException {
		// There is no need to make this atomic as this will only be invoked
		// once the task has completed
		if(throwable != null) {
			Throwable t = throwable;
			throwable = null;
			if(!(t instanceof CancellationException))
				throw new ExecutionException(t);
		}
	}
	
	/**
	 * Gets the task's state.
	 */
	protected final TaskState getState() {
		return state;
	}
	
	/**
	 * Sets the identifying name of the task being executed, as returned by
	 * {@link #getName()}.
	 * 
	 * @param name The name of the current task being executed.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code name} is {@code null}.
	 */
	public final void setName(String name) {
		tracker.setName(name);
	}
	
	/**
	 * Gets the identifying name of the current task being executed.
	 * 
	 * <p>Memory consistency effects: actions by the thread which set the name
	 * happen-before actions in the current thread.
	 * 
	 * @return The task's identifying name.
	 */
	public final String getName() {
		return tracker.getName();
	}
	
	/**
	 * Gets a String representation of the Task, equivalent to
	 * {@link TaskTracker#toString()}.
	 */
	@Override
	public final String toString() {
		return tracker.toString();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A simple exception type used to indicate that the task has been
	 * cancelled.
	 */
	protected static class CancellationException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
}
