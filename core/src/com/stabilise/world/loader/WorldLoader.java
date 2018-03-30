package com.stabilise.world.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.WorldLoadTracker;
import com.stabilise.world.RegionStore.CachedRegion;
import com.stabilise.world.gen.WorldGenerator;


/**
 * A {@code WorldLoader} instance manages the loading and saving of regions for
 * a world.
 * 
 * <p>Internally, a WorldLoader uses an ExecutorService to perform its I/O
 * tasks; each individual load or save request for a region is delegated to a
 * separate thread.
 * 
 * <p>TODO: Synchronisation policy on saved regions. Since it is incredibly
 * inefficient and wasteful to make a defensive copy of a region and its
 * contents when it is being saved, it can be expected that concurrency
 * problems will arise from the fact that said region and contents will be
 * modified while it is in the process of being saved. This can be rectified
 * either by:
 * 
 * <ul>
 * <li>never saving regions mid-game (though this lends itself to potential
 *     loss of data if, say, the JVM crashes and as such the game can't
 *     properly shut down), or
 * <li>defining a synchronisation policy wherein at minimum no exceptions or
 *     errors will be thrown, and cases of deadlock, livelock and starvation
 *     are impossible (though there may be inconsistent state data as the world
 *     changes while it is being saved - though at least that would be
 *     preferable to losing data)
 * </ul>
 */
public class WorldLoader {
	
    /** A reference to the world that this WorldLoader handles the loading for. */
    private final HostWorld world;
    /** A reference to the world's generator. We need this so that we can hand
     * off regions to be generated if necessary after they've been loaded. */
    private WorldGenerator generator = null;
    /** A reference to the Executor with which we send off all asynchronous
     * loading tasks. */
    private final Executor executor;
    
    private volatile boolean cancelLoadOperations = false;
    private final WorldLoadTracker tracker;
    
    private final List<IRegionLoader> loaders = new ArrayList<>();
    private final List<IRegionLoader> savers = new ArrayList<>();
    
    private final Log log;
    
    
    /**
     * Creates a new WorldLoader for the given world.
     * 
     * @throws NullPointerException if world is null.
     */
    public WorldLoader(HostWorld world) {
        this.world = world;
        this.executor = world.multiverse().getExecutor();
        this.tracker = world.loadTracker();
        
        this.log = Log.getAgent("WORLDLOADER: " + world.getDimensionName());
        
        // Register all the base loaders in accordance with the world's save
        // format.
        WorldFormat.registerLoaders(this, world.multiverse().info);
        // Any additional dimension-specific loaders are added immediately
        // after this constructor in the HostWorld constructor.
    }
    
    /**
     * Passes this WorldLoader a reference to the world's generator.
     */
    public void passReferences(WorldGenerator generator) {
        this.generator = generator;
    }
    
    /**
     * Registers a loader. Loaders are run in the order they are registered.
     * 
     * <p>This method is not thread-safe and should only be invoked when
     * setting up this WorldLoader.
     */
    @ThreadUnsafeMethod
    void addLoader(IRegionLoader loader) {
        loaders.add(loader);
    }
    
    /**
     * Registers a saver. Savers are run in the order they are registered.
     * 
     * <p>This method is not thread-safe and should only be invoked when
     * setting up this WorldLoader.
     */
    @ThreadUnsafeMethod
    void addSaver(IRegionLoader saver) {
        savers.add(saver);
    }
    
    /**
     * Registers an IRegionLoader as both a loader and saver.
     * 
     * @see #addLoader(IRegionLoader)
     * @see #addSaver(IRegionLoader)
     */
    @ThreadUnsafeMethod
    void addLoaderAndSaver(IRegionLoader loader) {
        loaders.add(loader);
        savers.add(loader);
    }
    
    /**
     * Instructs the WorldLoader to asynchronously load a region. This
     * method does nothing if {@link RegionState#getLoadPermit()
     * r.state.getLoadPermit()} returns {@code false}.
     * 
     * TODO: not true, failing that it will attempt to generate
     * 
     * @param r The region to load.
     * @param generate Whether or not the region should also be generated,
     * if it is not already.
     * 
     * @throws NullPointerException if {@code region} is {@code null}.
     */
    @UserThread("Any")
    public void loadRegion(Region r, boolean generate) {
        world.stats.load.requests.increment();
        
        if(r.state.getLoadPermit()) {
            tracker.startLoadOp();
            executor.execute(() -> doLoad(r, generate));
        } else if(generate) {
            tracker.startLoadOp();
            world.stats.load.rejected.increment();
            generator.generate(r);
        }
    }
    
    /**
     * Instructs the WorldLoader to asynchronously save a region.
     * 
     * <p>The request will be ignored if the region does not grant its
     * {@link Region#getSavePermit() save permit}.
     * 
     * @param region The region to save.
     * @param cacheHandle The handle to this region's cache entry. {@code
     * null} is allowed.
     */
    @UserThread("Any")
    public void saveRegion(Region region, CachedRegion cacheHandle) {
    	world.stats.save.requests.increment();
        executor.execute(() -> doSave(region, cacheHandle));
        region.lastSaved = world.getAge();
    }
    
    private void doLoad(Region r, boolean generate) {
    	world.stats.load.started.increment();
        
        if(cancelLoadOperations) {
            tracker.endLoadOp();
            world.stats.load.aborted.increment();
            return;
        }
        
    	FileHandle file = r.getFile(world);
    	if(file.exists()) {
            try {
            	DataCompound c = IOUtil.read(file, Format.NBT, Compression.GZIP);
                boolean generated = c.optBool("generated").orElse(false);
                
                loaders.forEach(l -> l.load(r, c, generated));
                
                if(generated)
                	r.state.setGenerated();
            	
                world.stats.load.completed.increment();
            } catch(Exception e) {
                log.postSevere("Loading " + r + " failed!", e);
                world.stats.load.failed.increment();
            }
    	}
    	
        if(generate)
            generator.generateSynchronously(r);
        else
            tracker.endLoadOp();
    }
    
    private void doSave(Region r, CachedRegion cacheHandle) {
    	world.stats.save.started.increment();
        
        if(r.state.getSavePermit()) {
            DataCompound c = Format.NBT.newCompound();
            boolean generated = r.state.isGenerated();
            c.put("generated", generated);
            
            savers.forEach(s -> s.save(r, c, generated));
            
            try {
               try {
                    IOUtil.writeSafe(r.getFile(world), c, Compression.GZIP);
                } catch(IOException e) {
                    log.postSevere("Could not save " + r + "!", e);
                }
                r.state.finishSaving();
                world.stats.save.completed.increment();
            } catch(Throwable t) {
                world.stats.save.failed.increment();
                log.postSevere("Saving " + r + " failed!", t);
            }
        } else
            world.stats.save.aborted.increment();
        
        // Extremely important final step in the lifecycle of a region: try
        // to uncache a region after it has been saved.
        if(cacheHandle != null)
            cacheHandle.dispose();
    }
    
    /**
     * Shuts down the WorldLoader; region loading operations will be cancelled
     * but region saves will be permitted to complete.
     */
    @UserThread("MainThread")
    public void shutdown() {
        cancelLoadOperations = true;
    }
	
}
