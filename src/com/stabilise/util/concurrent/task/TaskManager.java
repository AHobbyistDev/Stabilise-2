package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Executor;


public class TaskManager {
	
	private final Executor executor;
	
	
	/**
	 * @throws NullPointerException if executor is null.
	 */
	private TaskManager(Executor executor) {
		this.executor = Objects.requireNonNull(executor);
	}
	
	
	/**
	 * Creates a new task manager.
	 * 
	 * @param executor The executor with which to run the tasks.
	 * 
	 * @throws NullPointerException if {@code executor} is {@code null}.
	 */
	public static TaskManager create(Executor executor) {
		return new TaskManager(executor);
	}
	
}
