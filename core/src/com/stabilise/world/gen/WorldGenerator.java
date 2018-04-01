package com.stabilise.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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
import com.stabilise.world.WorldLoadTracker;
import com.stabilise.world.gen.InstancedWorldgen.InstancedWorldgenSupplier;
import com.stabilise.world.loader.WorldLoader;
import com.stabilise.world.multiverse.Multiverse;

/**
 * The {@code WorldGenerator} class provides the mechanism for generating the
 * terrain of a world.
 * 
 * <h3>Usage Guidelines</h3>
 * 
 * <p>Firstly, a {@code WorldGenerator} must be prepared via {@link
 * #passReferences(WorldLoader, RegionStore) prepare()} before it can be used.
 * 
 * <p>A {@code WorldGenerator} should be used exclusively by the {@code
 * WorldLoader}, as region generation happens immediately after loading.
 * 
 * <p>To generate a region, invoke {@link #generateOld(Region)}. When {@link
 * Region#isPrepared()} returns {@code true}, generation of the region is
 * complete, and it is safe to interact with it. {@link
 * #generateSynchronously(Region)} is offered as a convenience alternative
 * for all threads but the main thread.
 * 
 * <p>To shut down the generator, invoke {@link #shutdown()}.
 */
@ThreadSafe
public final class WorldGenerator {
    
    /** The world for which the generator is generating. */
    private final HostWorld world;
    private final long seed;
    
    /** The executor which delegates threads. */
    private final Executor executor;
    /** Whether or not the generator has been shut down. This is volatile. */
    private volatile boolean isShutdown = false;
    
    private RegionStore regionStore;
    
    private final List<IWorldGenerator> generators = new ArrayList<>(1);
    
    private final WorldLoadTracker loadTracker;
    
    private final Log log;
    
    
    /**
     * Creates a new WorldGenerator.
     * 
     * @param multiverse The multiverse.
     * @param world The world.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public WorldGenerator(Multiverse<?> multiverse, HostWorld world) {
        this.world = Objects.requireNonNull(world);
        this.executor = multiverse.getExecutor();
        
        seed = multiverse.getSeed();
        
        loadTracker = world.loadTracker();
        
        log = Log.getAgent("Generator_" + world.getDimensionName());
    }
    
    /**
     * Registers a generator. Generators are run in the order they are
     * registered.
     * 
     * <p>This method is not thread-safe and should only be invoked when
     * setting up the generator.
     */
    @ThreadUnsafeMethod
    private void addGenerator(IWorldGenerator generator) {
        generators.add(generator);
    }
    
    /**
     * Passes this WorldGenerator a reference to the region store.
     */
    public void passReferences(RegionStore regions) {
        this.regionStore = regions;
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
        if(isShutdown) { // || !r.state.getGenerationPermit()) {
            world.stats.gen.rejected.increment();
            // If we're using the current thread it means we're following
            // directly off a load operation, so we do the courtesy of ending
            if(useCurrentThread)
                loadTracker.endLoadOp();
            callback.accept(r, false);
            return;
        }
        
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
            loadTracker.endLoadOp();
            world.stats.gen.aborted.increment();
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
                
                GenProvider prov = new GenProvider(world, r);
                // Generate the region, as per the generators
                generators.forEach(g -> g.generate(r, prov, seed));
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
            
            // Even if due to some other region being generated concurrently
            // we have some more structures to implant, we don't bother to do
            // it anymore here -- we'll just have to do it on the main thread
            // during an update tick. Thus we hardcode a false here.
            r.state.setGenerated(false);
        } catch(Throwable t) {
            // TODO: What do we do if worldgen fails? Do we retry?
            world.stats.gen.failed.increment();
            log.postSevere("Worldgen of " + r + " failed!", t);
        }
        
        //loadTracker.endLoadOp(); // TODO
        
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
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * This class delegates the registering of {@code IWorldGenerators} to the
     * main {@code WorldGenerator}.
     */
    public static class GeneratorRegistrant {
        
        private final WorldGenerator gen;
        
        public GeneratorRegistrant(WorldGenerator gen) {
            this.gen = gen;
        }
        
        /**
         * Registers a generator. Generators are run in the order they are
         * registered.
         */
        @ThreadUnsafeMethod
        public void add(IWorldGenerator generator) {
            if(generator instanceof InstancedWorldgenSupplier)
                gen.log.postWarning("Registering a constructed instance of \"" +
                        generator.getClass().getSimpleName() + "\" even though"+
                        "it is a subclass of InstancedWorldgen. Mistake?");
            gen.addGenerator(generator);
        }
        
        /**
         * Registers a generator. Generators are run in the order they are
         * registered.
         */
        @ThreadUnsafeMethod
        public void add(InstancedWorldgenSupplier generator) {
            gen.addGenerator((r,w,s) -> generator.get(w, s).generate(r));
        }
        
    }
    
}
