package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Executor;

import com.stabilise.core.app.Application;

public class TaskParent {
	
	private final Executor exec;
	private final TaskTracker tracker;
	
	public TaskParent() {
		this(Application.executor());
	}
	
	public TaskParent(Executor exec) {
		this.exec = Objects.requireNonNull(exec);
		this.tracker = new TaskTracker("", 0);
	}

}
