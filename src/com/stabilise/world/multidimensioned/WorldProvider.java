package com.stabilise.world.multidimensioned;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.concurrent.BoundedThreadPoolExecutor;
import com.stabilise.world.HostWorld;
import com.stabilise.world.WorldInfo;


public class WorldProvider {
	
	private final WorldInfo info;
	/** Maps dimension names -> dimensions. */
	private final Map<String, HostWorld> dimensions = new HashMap<>(2);
	
	/** The ExecutorService to use for delegating loader and generator threads. */
	public final ExecutorService executor;
	
	/** Profile any world's operation with this. */
	public final Profiler profiler;
	
	
	/**
	 * Creates a new WorldProvider.
	 * 
	 * @param info The world info.
	 * @param profiler The profiler to use to profile the world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public WorldProvider(WorldInfo info, Profiler profiler) {
		this.info = Preconditions.checkNotNull(info);
		this.profiler = Preconditions.checkNotNull(profiler);
		
		// Start up the executor
		
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
	
	public void update() {
		for(HostWorld dim : dimensions.values())
			dim.update();
	}
	
	/**
	 * @return The dimension, or {@code null} if the specified dimension is not
	 * loaded.
	 */
	public HostWorld getDimension(String name) {
		return dimensions.get(name);
	}
	
	/**
	 * Loads a dimension into memory.
	 * 
	 * @param name The name of the dimension.
	 * 
	 * @return The dimension, or {@code null} if no such dimension has been
	 * registered.
	 * @throws RuntimeException if something derped.
	 */
	public HostWorld loadDimension(String name) {
		HostWorld world = getDimension(name);
		if(world != null)
			return world;
		
		Dimension dim = Dimension.getDimension(name);
		if(dim == null)
			return null; // note: perhaps an exception might be more in order?
		
		// TODO: Load dimension data
		
		return dim.createHost(this, info);
	}
	
	public void close() {
		for(HostWorld dim : dimensions.values())
			dim.close();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
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
