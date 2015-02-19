package com.stabilise.world.provider;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.stabilise.character.CharacterData;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.util.Checkable;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.concurrent.BoundedThreadPoolExecutor;
import com.stabilise.world.BaseWorld;
import com.stabilise.world.IWorld;
import com.stabilise.world.save.WorldLoader;

/**
 * A WorldProvider manages and 'provides' all the dimensions/worlds of a
 * world.<sup><font size=-1>1</font></sup>
 * 
 * <p>{@code 1.} The terminology is somewhat confusing here. From the user's
 * perspective, a <i>WorldProvider</i> is actually a <i>world</i>, and
 * different <i>Worlds</i> (e.g. {@code HostWorld}, etc.) are
 * <i>dimensions</i> of that world/WorldProvider. We largely refer to
 * 'dimensions' as 'worlds' in the code (e.g. GameObjects have a {@code world}
 * member through which they interact with the dimension they are in) for both
 * legacy and aesthetic purposes.
 */
public abstract class WorldProvider<W extends BaseWorld> {
	
	/*
	 * TYPES OF WORLDPROVIDER
	 * 
	 * 1. Singleplayer
	 *     Fairly straightforward
	 * 2. Singleplayer with integrated server
	 *     As with singleplayer, but also hosts the world such that multiple
	 *     players may play.
	 * 3. Multiplayer
	 *     Hosts the world such that multiple players may play. No integrated
	 *     client.
	 * 4. Client
	 *     Plays on a world provided by a server.
	 * 
	 * 1, 2 & 3 are variants of the 'host provider', which hosts each world.
	 * 4 is a 'client provider', which merely views a world but does not own
	 * it.
	 * 
	 * Furthermore, clients (all but 3.) are able to maintain a single client-
	 * only dimension, unique to each player character. This is to be an
	 * important gameplay feature.
	 *     
	 * DESIGN GOALS
	 * 
	 * - Achieve all of the above four types of provider with all desired
	 *   features.
	 * - Minimal repetitious code across all four types
	 * - Conversion between types while playing is not necessary, but do if
	 *   able.
	 */
	
	/** The ExecutorService to use for delegating loader and generator threads. */
	private final ExecutorService executor;
	/** The global WorldLoader to use for loading regions. */
	public final WorldLoader loader;
	
	/** Stores all dimensions. Maps dimension names -> dimensions. */
	protected final Map<String, W> dimensions = new HashMap<>(2);
	
	/** Profile any world's operation with this. */
	public final Profiler profiler;
	protected final Log log = Log.getAgent("WorldProvider");
	
	// Integrated player stuff
	/** {@code true} if we're providing for an integrated client. {@code false}
	 * by default. */
	protected boolean integratedClient = false;
	/** The integrated client's character data. {@code null} if there is no
	 * integrated client. */
	protected CharacterData integratedCharacter = null;
	/** The integrated client's player. {@code null} if there is no integrated
	 * client. */
	protected EntityMob integratedPlayer = null;
	
	
	/**
	 * Creates a new WorldProvider.
	 * 
	 * @param profiler The profiler to use to profile the world.
	 * 
	 * @throws NullPointerException if {@code profiler} is {@code null}.
	 */
	public WorldProvider(Profiler profiler) {
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
		executor = tpe;
		
		// Start up the world loader
		loader = WorldLoader.getLoader(this);
	}
	
	/**
	 * Updates all worlds.
	 */
	public void update() {
		Checkable.updateCheckables(dimensions.values());
	}
	
	/**
	 * @param name The name of the dimension.
	 * 
	 * @return The dimension, or {@code null} if the specified dimension is not
	 * loaded.
	 */
	public W getDimension(String name) {
		return dimensions.get(name);
	}
	
	/**
	 * Loads a dimension into memory.
	 * 
	 * @param name The name of the dimension.
	 * 
	 * @return The dimension.
	 * @throws IllegalArgumentException if {@code name} is not the name of a
	 * valid dimension.
	 * @throws RuntimeException if the dimension could not be prepared.
	 */
	public abstract W loadDimension(String name);
	
	// This is a WorldProvider method so we can catch client players being sent
	// and shift the worldview
	public void sendToDimension(BaseWorld oldDim, String dimension, Entity e, double x, double y) {
		oldDim.removeEntity(e);
		W dim = loadDimension(dimension);
		dim.addEntity(e, x, y);
	}
	
	/**
	 * Gets the executor with which to run concurrent tasks.
	 */
	public final Executor getExecutor() {
		return executor;
	}
	
	/**
	 * Gets the seed of the world encapsulated by this WorldProvider.
	 * 
	 * <p>If this is not a {@code HostProvider}, a dummy seed is returned.
	 */
	public abstract long getSeed();
	
	/**
	 * Saves the worlds.
	 * 
	 * @throws RuntimeException if an I/O error occurred while saving.
	 */
	public abstract void save();
	
	/**
	 * Closes this world provider down. This method will block the current
	 * thread until shutdown procedures have completed.
	 * 
	 * @throws RuntimeException if an I/O error occurred while saving.
	 */
	public void close() {
		loader.shutdown();
		
		for(W dim : dimensions.values())
			dim.close();
		
		closeExtra();
		
		executor.shutdown();
		
		try {
			if(!executor.awaitTermination(10, TimeUnit.SECONDS))
				log.postWarning("World executor took longer than 10 seconds to shutdown!");
		} catch(InterruptedException e) {
			log.postWarning("Interrupted while waiting for world executor to terminate!");
		}
	}
	
	/**
	 * Performs any closing procedures which may be done in-between {@link
	 * IWorld#close()} being invoked on every world, and the executor being
	 * shutdown.
	 */
	protected abstract void closeExtra();
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Thread factory implementation for world loader and world generator
	 * threads.
	 */
	private class WorldThreadFactory implements ThreadFactory {
		
		/** The number of threads created with this factory. */
		private final AtomicInteger threadNumber = new AtomicInteger(0);
		
		private final UncaughtExceptionHandler ripWorkerThread = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.postSevere("Worker thread \"" + t.toString() + "\" died!", e);
			}
		};
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "WorldThread" + threadNumber.incrementAndGet());
			if(t.isDaemon())
				t.setDaemon(false);
			if(t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			t.setUncaughtExceptionHandler(ripWorkerThread);
			return t;
		}
	}
	
}
