package com.stabilise.world;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.util.Log;
import com.stabilise.util.concurrent.BoundedThreadPoolExecutor;

/**
 * A WorldData object serves as a wrapper for any objects or methods related to
 * the world, for use by the WorldLoader and WorldGenerator.
 */
public class WorldData {
	
	/** The world. */
	public final HostWorld world;
	/** The world's info. */
	public final WorldInfo info;
	
	/** The ExecutorService to use for delegating loader and generator threads. */
	public final ExecutorService executor;
	
	
	/**
	 * Creates a new WorldData.
	 * 
	 * @param world The world.
	 * @param info The world info.
	 */
	public WorldData(HostWorld world, WorldInfo info) {
		this.world = world;
		this.info = info;
		
		final int coreThreads = 2; // region loading typically happens in pairs
		final int maxThreads = Math.max(coreThreads, Runtime.getRuntime().availableProcessors());
		
		BoundedThreadPoolExecutor tpe = new BoundedThreadPoolExecutor(
				coreThreads, maxThreads,
				30L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new WorldThreadFactory()
		);
		tpe.setRejectedExecutionHandler(new BoundedThreadPoolExecutor.CallerRunsPolicy());
		executor = tpe;
	}
	
	/**
	 * Thread factory implementation for world loader and world generator
	 * threads.
	 */
	private static class WorldThreadFactory implements ThreadFactory {
		
		/** The number of threads created with this factory. */
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		
		private final UncaughtExceptionHandler ripWorkerThread = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Log.get().postSevere("Worker thread \"" + t.toString() + "\" died!", e);
			}
		};
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "WorldThread" + threadNumber.getAndIncrement());
			if(t.isDaemon())
				t.setDaemon(false);
			if(t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			t.setUncaughtExceptionHandler(ripWorkerThread);
			return t;
		}
	}
	
}
