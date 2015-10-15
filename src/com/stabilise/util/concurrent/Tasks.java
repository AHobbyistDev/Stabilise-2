package com.stabilise.util.concurrent;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.BooleanSupplier;

import com.stabilise.util.concurrent.task.Task;
import com.stabilise.util.concurrent.task.TaskBuilder;
import com.stabilise.util.concurrent.task.TaskRunnable;

/**
 * This class provides a number of utility methods relating to tasks.
 */
public class Tasks {
    
    private Tasks() { throw new AssertionError(); } // non-instantiable
    
    private static final Executor EXEC_CURRENT_THREAD = r -> r.run();
    private static final Executor EXEC_NEW_THREAD     = r -> new Thread(r).start();
    
    /**
     * Returns an executor which runs submitted tasks on the caller thread, as
     * if by:
     * 
     * <pre>return r -> r.run()</pre>
     */
    public static Executor currentThreadExecutor() {
        return EXEC_CURRENT_THREAD;
    }
    
    /**
     * Returns an executor which runs submitted tasks on a new thread, as if
     * by:
     * 
     * <pre>return r -> new Thread(r).start();</pre>
     */
    public static Executor newThreadExecutor() {
        return EXEC_NEW_THREAD;
    }
    
    /**
     * Waits on the specified object's monitor lock until the specified
     * condition returns {@code true}. If the current thread was interrupted
     * while waiting, the interrupt flag will be set when this method returns.
     * 
     * @param o The object to wait on.
     * @param endCondition The condition on which to wait.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static void waitUntil(Object o, BooleanSupplier endCondition) {
        boolean interrupted = false;
        synchronized(o) {
            while(!endCondition.getAsBoolean()) {
                try {
                    o.wait();
                } catch(InterruptedException retry) {
                    interrupted = true;
                }
            }
        }
        if(interrupted)
            Thread.currentThread().interrupt();
    }
    
    /**
     * Waits on the specified object's monitor lock until the specified
     * condition returns {@code true}.
     * 
     * @param o The object to wait on.
     * @param endCondition The condition on which to wait.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws InterruptedException if the current thread received an
     * interrupt while waiting.
     */
    public static void waitInterruptibly(Object o, BooleanSupplier endCondition)
            throws InterruptedException {
        synchronized(o) {
            while(!endCondition.getAsBoolean())
                o.wait();
        }
    }
    
    /**
     * Synchronises on {@code o}, then runs {@code task}, and then invokes
     * {@code notifyAll()} on {@code o} as such:
     * 
     * <pre>
     * synchronized(o) {
     *     if(task != null) task.run();
     *     o.notifyAll();
     * }</pre>
     * 
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public static void doThenNotify(Object o, Runnable task) {
        synchronized(o) {
            if(task != null) task.run();
            o.notifyAll();
        }
    }
    
    /**
     * If the specified condition returns {@code true}, invokes {@code
     * notifyAll()} on {@code o}.
     * 
     * @param syncCondition If true, the condition will be invoked while
     * synchronized on {@code o}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static void notifyIf(Object o, BooleanSupplier condition,
            boolean syncCondition) {
        if(syncCondition) {
            synchronized(o) {
                if(condition.getAsBoolean())
                    o.notifyAll();
            }
        } else {
            if(condition.getAsBoolean()) {
                synchronized(o) { o.notifyAll(); }
            }
        }
    }
    
    /**
     * Runs {@code r} while holding the lock on {@code l}. This method behaves
     * as if by:
     * 
     * <pre>
     * try {
     *     l.lock();
     *     r.run();
     * } finally {
     *     l.unlock();
     * }
     * </pre>
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static void withLock(Lock l, Runnable r) {
        try {
            l.lock();
            r.run();
        } finally {
            l.unlock();
        }
    }
    
    /**
     * Wraps a {@code Runnable} in a {@code TaskRunnable}.
     * 
     * @throws NullPointerException if {@code r} is {@code null}.
     */
    public static TaskRunnable wrap(Runnable r) {
        Objects.requireNonNull(r);
        return t -> r.run();
    }
    
    /**
     * Executes the given task through the given executor, with the specified
     * display name.
     * 
     * @param executor The executor through which to run the task.
     * @param name The name of the task.
     * @param task The task.
     * 
     * @return A Task object encapsulating the task.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static Task exec(Executor executor, String name, TaskRunnable task) {
        return new TaskBuilder(executor, name).andThen(task).build();
    }
    
    /**
     * Executes the given task through the given executor.
     * 
     * @param executor The executor through which to run the task.
     * @param task The task.
     * 
     * @return A Task object encapsulating the task.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static Task exec(Executor executor, Runnable task) {
        return exec(executor, "", wrap(task));
    }
    
    /**
     * Executes the given runnable on a new thread, and returns a Task object
     * encapsulating it.
     * 
     * @throws NullPointerException if {@code task} is {@code null}.
     */
    public static Task exec(Runnable task) {
        return exec(newThreadExecutor(), task);
    }
    
}
