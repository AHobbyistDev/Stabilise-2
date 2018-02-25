package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import com.stabilise.util.concurrent.Waiter;

// This entire package looks like a huge clusterfuck and it's been so long
// since I touched it that I have no idea if it even works.

/**
 * A Task is essentially a {@code Runnable} with additional useful facilities.
 * 
 * <p>The main entry point for creating a Task is {@link #builder()}.
 */
@ThreadSafe
public interface Task extends TaskView {
    
    /**
     * Starts this task, unless it has been preemptively cancelled.
     * 
     * @return This task.
     * @throws IllegalStateException if this task has already been started.
     */
    Task start();
    
    /**
     * Gets the stack of currently running task units. This method may be
     * useful if you wish to display a dynamically growing stack of loading
     * bars for each unit in the stack. This method should be polled
     * regularly as the stack will evolve with time.
     */
    TaskView[] getStack();
    
    /**
     * Prints a graphical representation of the {@link #getStack() task stack}
     * to the console.
     */
    default void printStack() {
        TaskView[] view = getStack();
        StringBuilder sb = new StringBuilder();
        for(TaskView t : view)
            printProgressBar(sb, t);
        System.out.println(sb.toString());
    }
    
    static void printProgressBar(StringBuilder sb, TaskView view) {
        sb.append("<");
        String status = view.status();
        String progStatus = " (" + view.partsCompleted() + "/" + view.totalParts() + ")";
        if(status.length() + progStatus.length() > 80)
            status = status.substring(0, 80 - progStatus.length());
        int perc = view.percentCompleted();
        int statLen = status.length() + progStatus.length();
        int i;
        for(i = 1; i <= Math.min(perc, 50 - statLen/2); i++)
            sb.append('#');
        for(; i <= 50-statLen/2; i++)
            sb.append(' ');
        sb.append(status).append(progStatus);
        i += statLen;
        for(; i <= Math.min(100, perc); i++)
            sb.append('#');
        for(; i <= 100; i++)
            sb.append(' ');
        sb.append(">\n");
    }
    
    // End task stack operations
    
    /**
     * Cancels this task. The speed at which a Task actually stops following a
     * cancellation request depends on the responsiveness of an implementation,
     * but it is guaranteed that a new task unit will not begin following an
     * invocation of this method. A Task which stops due to cancellation is
     * considered to have {@link #failed() failed}.
     */
    void cancel();
    
    /**
     * Returns {@code true} if this task is stopped (i.e. it either hasn't been
     * started yet, has completed, or has failed); {@code false} otherwise.
     */
    boolean stopped();
    
    /**
     * Returns {@code true} if this task has been successfully completed;
     * {@code false} otherwise.
     */
    boolean completed();
    
    /**
     * Returns {@code true} if this task has failed or was cancelled; {@code
     * false} otherwise.
     * 
     * <p>An invocation of this is the atomic equivalent to {@code stopped()
     * && !completed()}.
     */
    boolean failed();
    
    /**
     * Blocks the current thread until either the task has finished executing,
     * or the current thread is interrupted.
     * 
     * @return {@code true} if the task successfully completed; {@code false}
     * if it either failed or was cancelled.
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     */
    boolean await() throws InterruptedException;
    
    /**
     * Blocks the current thread until the task has finished executing. If the
     * current thread was interrupted while waiting, the interrupt flag will be
     * set when this method returns.
     * 
     * @return {@code true} if the task successfully completed; {@code false}
     * if it either failed or was cancelled.
     */
    boolean awaitUninterruptibly();
    
    /**
     * Blocks the current thread until either the task has finished executing,
     * the current thread is interrupted, or the specified waiting time
     * elapses.
     * 
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @return {@code true} if the task {@link #stopped() stopped}; {@code
     * false} if the specified waiting time elapsed.
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     */
    boolean await(long time, TimeUnit unit) throws InterruptedException;
    
    /**
     * Creates a Waiter for this task.
     * 
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws NullPointerException if {@code unit} is {@code null}.
     */
    default Waiter waiter(long time, TimeUnit unit) {
        return new Waiter(this::stopped, time, unit);
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates a new TaskBuilderBuilder. This function is the main entry-point
     * for creating a Task.
     */
    public static TaskBuilderBuilder builder() {
        return new TaskBuilderBuilder();
    }
    
    /**
     * Creates a new TaskBuilderBuilder. Equivalent to {@code
     * builder().executor(executor)}.
     * 
     * @param executor The executor with which to run the task.
     * 
     * @throws NullPointerException if {@code executor} is {@code null}.
     */
    public static TaskBuilderBuilder builder(Executor executor) {
        return builder().executor(executor);
    }
    
}
