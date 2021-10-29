package com.stabilise.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This class holds a single boolean value, and provides a number of utility
 * methods akin to {@link Condition} to wait on the value being set to true.
 */
@ThreadSafe
public class BoolCondition {
	
	private volatile boolean value = false;
	private final Lock lock = new ReentrantLock();
	private final Condition cond = lock.newCondition();
	
	
	/**
	 * Returns the value.
	 */
	public boolean isTrue() {
		return value;
	}
	
	/**
	 * Sets the value to true and notifies all waiting threads. 
	 */
	public void setTrue() {
		if(value) return; // is already true, nothing to do
		
		lock.lock();
		try {
			if(value)
				return; // double-check
			
			value = true;
			cond.signalAll();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * If the value is false, runs the given Runnable then sets the value to
	 * true and notifies all waiting threads.
	 * 
	 * @return true if the runnable was run and the value was set; false if it
	 * was already set.
	 */
	public boolean doThenSetTrue(Runnable runnable) {
		if(value) return false; // avoid locking if possible
		
		lock.lock();
		try {
			if(value) return false; // double-check
			
			runnable.run();
			value = true;
			cond.signalAll();
			return true;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * See {@link Condition#await()}. This method is essentially the same, but
	 * only returns when the value is true (asides from interrupts).
	 */
	public void await() throws InterruptedException {
		if(value) return; // avoid locking if possible
		lock.lock();
		try {
			while(!value)
				cond.await();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * See {@link Condition#awaitUninterruptibly()}. This method is essentially
	 * the same, but only returns when the value is set to true.
	 */
	public void awaitUninterruptibly() {
		if(value) return; // avoid locking if possible
		lock.lock();
		try {
			while(!value)
				cond.awaitUninterruptibly();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * See {@link Condition#await(long, TimeUnit)}. This method is essentially
	 * the same, but only returns either when the value is set to true, or the
	 * time has elapsed.
	 * 
	 * @throws IllegalArgumentException if {@code time <= 0}.
	 */
	public boolean await(long time, TimeUnit unit) throws InterruptedException {
		if(time <= 0L)
			throw new IllegalArgumentException("nanosTimeout <= 0");
		time = unit.toNanos(time);
		
		if(value) return true; // avoid locking if possible
		lock.lock();
		try {
			while(!value) {
				if(time <= 0L)
					return false; // timeout elapsed, ripperino
				time = cond.awaitNanos(time);
			}
			return true;
		} finally {
			lock.unlock();
		}
	}
	
    /**
     * Creates a Waiter for this condition.
     * 
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws IllegalArgumentException if {@code time <= 0}.
     */
	public Waiter waiter(long time, TimeUnit unit) {
		return new Waiter(this::isTrue, time, unit);
	}
	
}
