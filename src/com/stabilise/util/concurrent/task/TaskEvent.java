package com.stabilise.util.concurrent.task;

import com.stabilise.core.app.Event;

public class TaskEvent extends Event {
	
	public static TaskEvent STOP = new TaskEvent("stop");
	public static TaskEvent CANCEL = new TaskEvent("cancel");
	public static TaskEvent COMPLETE = new TaskEvent("complete");
	
	private TaskEvent(String name) {
		super(name);
	}
	
}
