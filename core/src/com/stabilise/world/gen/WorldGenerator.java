package com.stabilise.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.concurrent.ThreadSafe;

import com.stabilise.util.Log;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.RegionState;
import com.stabilise.world.RegionStore;
import com.stabilise.world.RegionStore.RegionCallback;
import com.stabilise.world.dimension.Dimension;

/**
 * A {@code WorldGenerator} instance handles the generation of regions for a
 * world. In broader terms, it's here that we generate the terrain of a world.
 * All generation requests are made through a world's {@link RegionStore}.
 * 
 * <p>This class simply manages the generation of a region; the generation
 * itself is performed by {@link IWorldGenerator}s which are registered by a
 * world's {@link Dimension}; see {@link
 * Dimension#addGenerators(WorldGenerator)}.
 * 
 * <p>Note that the generation of a region <em>always</em> comes after a load
 * of that region (whether anything was loaded or not), and thus every part of
 * the load process <i>happens-before</i> generation.
 */
@ThreadSafe
public final class WorldGenerator {
    
    private final HostWorld world;
    /** A copy of the world's seed. This is important since after all it
     * determines what's generated. */
    private final long seed;
    
    private final Executor executor;
    /** Whether or not the generator has been shut down. This is volatile. */
    private volatile boolean isShutdown = false;
    
    /** A reference to the region store, to cache/uncache regions. */
    private final RegionStore regionStore;
    
    /** These generators are what actually generate the terrain of each region. */
    private final List<Supplier<IWorldGenerator>> generators = new ArrayList<>(1);
    
    
    final Log log;
    
    
    /**
     * Creates a new WorldGenerator. Takes references to the world and the
     * world's region store.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public WorldGenerator(HostWorld world, RegionStore regionStore) {
        this.world = Objects.requireNonNull(world);
        this.executor = world.multiverse().getExecutor();
        this.regionStore = regionStore;
        
        seed = world.multiverse().getSeed();
        
        log = Log.getAgent("Generator_" + world.getDimensionName());
    }
    
    /**
     * Registers a generator. Generators are run in the order they are
     * registered.
     */
    @ThreadUnsafeMethod
    public void addGenerator(IWorldGenerator generator) {
        addGenerator(() -> generator);
    }
    
    /**
     * Registers a generator. Generators are run in the order they are
     * registered. Use this in preference to {@link
     * #addGenerator(IWorldGenerator)} if the desired generator is not
     * thread-safe, and it is better to simply construct one for each new
     * region generated.
     */
    @ThreadUnsafeMethod
    public void addGenerator(Supplier<IWorldGenerator> generator) {
        generators.add(generator);
    }
    
    /**
     * Instructs the WorldGenerator to generate the given region. This method
     * does nothing if it's unable to acquire a {@link
     * RegionState#getGenerationPermit() generation permit}.
     * 
     * @param r The region to generate.
     * @param useCurrentThread true to generate on the current thread, false to
     * spawn a worker thread.
     * @param callback The function to call once generation is completed.
     * 
     * @throws NullPointerException if {@code region} is {@code null}.
     */
    @UserThread("Any")
    public void generate(Region r, boolean useCurrentThread, RegionCallback callback) {
        world.stats.gen.requests.increment();
        if(useCurrentThread)
            genRegion(r, callback);
        else
            executor.execute(() -> genRegion(r, callback));
    }
    
    /**
     * Generates a region. Invoked (a)synchronously through {@link
     * #generate(Region, boolean, RegionCallback)}.
     */
    @UserThread("WorkerThread")
    private void genRegion(Region r, RegionCallback callback) {
        world.stats.gen.started.increment();
        
        if(isShutdown) {
            world.stats.gen.aborted.increment();
            callback.accept(r, false);
            return;
        }
        
        boolean success = false;
        
        TaskTimer timer = new TaskTimer("Generating " + r);
        timer.start();
        
        try {
            // Don't generate a region if we don't need to!
            boolean alreadyGenerated = r.state.isGenerated();
            if(!alreadyGenerated) {
                // Set up the region's slices
                r.initSlices();
                
                GenProvider prov = new GenProvider(world, r, seed);
                // Generate the region, as per the generators
                generators.forEach(g -> g.get().generate(r, prov, seed));
            }
            
            // After normal generation processes have been completed, add any
            // queued structures.
            r.implantStructures(regionStore);
            
            r.forEachSlice(s -> s.buildLight()); // TODO: temporary
            
            timer.stop();
            if(!alreadyGenerated)
                log.postDebug(timer.getResult(TimeUnit.MILLISECONDS));
            
            success = true;
            world.stats.gen.completed.increment();
            
            r.state.setGenerated();
            
            // Finally note that even if due to some other region being
            // generated concurrently we have some more structures to implant, 
            // we don't bother to do it anymore here -- we'll just have to do
            // it on the main thread during an update tick.
        } catch(Throwable t) {
            world.stats.gen.failed.increment();
            log.postSevere("Worldgen of " + r + " failed!", t);
        }
        
        // Clean up all regions cached during generation.
        regionStore.uncacheAll();
        
        callback.accept(r, success);
    }
    
    /**
     * Instructs the WorldGenerator to shut down.
     */
    @UserThread("MainThread")
    public final void shutdown() {
        isShutdown = true;
    }
    
}
