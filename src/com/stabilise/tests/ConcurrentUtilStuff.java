package com.stabilise.tests;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtilStuff {
	
	public ConcurrentUtilStuff() {
		
	}
	
	protected static void singleThreadExecutor() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Integer> task = new FutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				System.out.println("HI");
				return new Integer(5000 + 500 + 50 + 5);
			}
		});
		
		executor.execute(task);
		
		try {
			System.out.println(task.get());
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch(ExecutionException e) {
			e.printStackTrace();
		}
		executor.shutdown();
	}
	
	protected static void threadPoolExecutor() {
		ExecutorService executor = Executors.newCachedThreadPool();
		final int tasks = 100;
		for(int i = 0; i < tasks; i++) {
			final int taskNum = i;
			executor.execute(new FutureTask<Integer>(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					System.out.println("Executing task " + taskNum + " on thread " + Thread.currentThread().getName());
					return null;
				}
			}));
		}
		
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		executor.shutdown();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		threadPoolExecutor();
	}
	
}
