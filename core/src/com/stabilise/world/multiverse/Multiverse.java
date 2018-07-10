package com.stabilise.world.multiverse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.entity.particle.manager.ParticleManager;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.world.AbstractWorld;
import com.stabilise.world.World;
import com.stabilise.world.WorldInfo;

/**
 * A Multiverse manages and 'provides' all the dimensions/worlds of a
 * world.<sup><font size=-1>1</font></sup>
 * 
 * <p>{@code 1.} The terminology is somewhat confusing here. From the user's
 * perspective, a <i>Multiverse</i> is actually a <i>world</i>, and
 * different <i>Worlds</i> (e.g. {@code HostWorld}, etc.) are
 * <i>dimensions</i> of that world/Multiverse. We largely refer to
 * 'dimensions' as 'worlds' in the code (e.g. GameObjects have a {@code world}
 * member through which they interact with the dimension they are in, and we
 * have AbstractWorld, HostWorld etc. instead of AbstractDimension,
 * HostDimension) for both legacy and aesthetic purposes.
 */
public abstract class Multiverse<W extends AbstractWorld> {
    
    /** The ExecutorService to use for delegating loader and generator threads. */
    protected final ExecutorService executor;
    
    /** Stores all dimensions. Maps dimension names -> dimensions. */
    protected final Map<String, W> dimensions = new HashMap<>(2);
    
    /** The WorldInfo. Dimensions should treat this as read-only. */
    public final WorldInfo info;
    
    /** The global particle manager, shared between worlds. */
    public final ParticleManager particleManager = new ParticleManager();
    
    /** Profile any world's operation with this. Never {@code null}. */
    protected Profiler profiler;
    protected final Log log = Log.getAgent("Multiverse");
    
    
    /**
     * Creates a new Multiverse.
     * 
     * @param info The world info.
     * @param profiler The profiler to use to profile this multiverse and its
     * worlds. If {@code null}, a default disabled profiler is instead set.
     * 
     * @throws NullPointerException if {@code info} is {@code null}.
     */
    public Multiverse(WorldInfo info, Profiler profiler) {
    	this.info = Objects.requireNonNull(info);
    	
        this.profiler = profiler != null
                ? profiler
                : new Profiler(false, "root", false);
        
        // Start up the executor
        
        // processors-1 because the main thread already exists.
        final int coreThreads = Runtime.getRuntime().availableProcessors() - 1;
        executor = new ThreadPoolExecutor(
        		coreThreads,
        		Integer.MAX_VALUE, // doesn't matter since queue is unbounded
        		30L, TimeUnit.SECONDS, // idle thread timeout
        		new LinkedBlockingQueue<>(),
        		new WorldThreadFactory()
        );
        
        log.postDebug("Started thread pool with " + coreThreads + " threads.");
    }
    
    /**
     * Updates all worlds.
     */
    public void update() {
        dimensions.values().removeIf(AbstractWorld::update);
        
        info.age++;
        // Do a particle cleanup every 10-ish seconds
        if(info.age % 600 == 0)
            particleManager.cleanup();
        
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
     * Loads a dimension into memory. If the dimension is already loaded, it
     * will be returned as per {@link #getDimension(String)}. Note that the
     * returned world may not yet be loaded nor ready (as I/O or other
     * preparative operations may be being performed asynchronously).
     * 
     * @param name The name of the dimension.
     * 
     * @return The dimension.
     * @throws IllegalArgumentException if {@code name} is not the name of a
     * valid dimension.
     */
    public abstract W loadDimension(String name);
    
    
    /**
     * Generates and returns a unique entity ID.
     */
    public abstract long getNextEntityID();
    
    /**
     * Returns the total number of entities which have existed in all worlds of
     * this multiverse.
     */
    public abstract long getTotalEntityCount();
    
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
     * Returns this WorldProvider's profiler. Use this to profile a world.
     */
    public Profiler getProfiler() {
        return profiler;
    }
    
    /**
     * Checks for whether or not this WorldProvider has an integrated client
     * associated with it. This returns {@code true} in all cases but for a
     * server with no integrated client.
     */
    public boolean hasClient() {
        return true; // TODO
    }
    
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
        particleManager.shutdown();
        
        for(AbstractWorld dim : dimensions.values())
            dim.close();
        
        closeExtra();
        
        for(AbstractWorld dim : dimensions.values())
            dim.blockUntilClosed();
        
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
     * World#close()} being invoked on every world, and the executor being
     * shutdown.
     * 
     * <p>The default implementation does nothing.
     */
    protected void closeExtra() {}
    
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
        
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "WorldThread" + threadNumber.incrementAndGet());
            t.setUncaughtExceptionHandler((th, e) -> 
                log.postSevere("Worker thread \"" + th.getName() + "\" died!", e)
            );
            return t;
        }
    }
    
}
