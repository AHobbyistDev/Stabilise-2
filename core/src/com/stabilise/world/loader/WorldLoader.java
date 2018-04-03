package com.stabilise.world.loader;

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
import com.stabilise.world.RegionState;
import com.stabilise.world.RegionStore;
import com.stabilise.world.RegionStore.RegionCallback;
import com.stabilise.world.WorldLoadTracker;


/**
 * A {@code WorldLoader} instance handles the loading and saving of regions for
 * a world. All save and load requests are made through a world's {@link
 * RegionStore}.
 * 
 * <p>The actual code for loading a region is provided by {@link
 * IRegionLoaders}, and these are provided upon construction by {@link
 * WorldFormat}.
 */
public class WorldLoader {
	
    /** A reference to the world that this WorldLoader handles the loading for. */
    private final HostWorld world;
    /** A reference to the Executor with which we send off all asynchronous
     * loading tasks. */
    private final Executor executor;
    
    private volatile boolean cancelLoadOperations = false;
    
    /** Tracker used for producing nice load bars while loading the world. */
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
     * Instructs this WorldLoader to asynchronously load a region. It is
     * assumed the caller has acquired a {@link RegionState#getLoadPermit()
     * permit} to load the region.
     * 
     * @param r The region to load.
     * @param generate true if the the region should also be generated, if it
     * has not already been generated.
     * @param callback The function to call once loading/generation is
     * completed.
     */
    @UserThread("Any")
    public void loadRegion(Region r, boolean generate, RegionCallback callback) {
        world.stats.load.requests.increment();
        tracker.startLoadOp();
        executor.execute(() -> doLoad(r, generate, callback));
    }
    
    private void doLoad(Region r, boolean generate, RegionCallback callback) {
    	world.stats.load.started.increment();
        
        if(cancelLoadOperations) {
            world.stats.load.aborted.increment();
            tracker.endLoadOp();
            callback.accept(r, false);
            return;
        }
        
        boolean success = true;
    	FileHandle file = r.getFile(world);
    	if(file.exists()) {
            try {
            	DataCompound c = IOUtil.read(file, Format.NBT, Compression.GZIP);
                boolean generated = c.optBool("generated").orElse(false);
                
                loaders.forEach(l -> l.load(r, c, generated));
                
                r.state.setLoaded(generated, r.hasQueuedStructures());
            	
                world.stats.load.completed.increment();
            } catch(Exception e) {
                log.postSevere("Loading " + r + " failed!", e);
                world.stats.load.failed.increment();
                success = false;
            }
    	} else {
    	    world.stats.load.completed.increment(); // we'll count this as completed
    	    r.state.setLoaded(false, false); // nothing to load == "loaded", but not generated
    	}
    	
    	tracker.endLoadOp();
    	callback.accept(r, success);
    }
    
    /**
     * Saves a region. It is assumed that the caller has acquired a {@link
     * RegionState#getSavePermit() permit} to save the region.
     * 
     * @param region The region to save.
     * @param useCurrentThread true to save on the current thread, false to
     * spawn a worker thread.
     * @param callback The function to call once saving is completed.
     */
    @UserThread("Any")
    public void saveRegion(Region region, boolean useCurrentThread, RegionCallback callback) {
        world.stats.save.requests.increment();
        if(useCurrentThread)
            doSave(region, callback);
        else
            executor.execute(() -> doSave(region, callback));
    }
    
    private void doSave(Region r, RegionCallback callback) {
    	world.stats.save.started.increment();
        boolean success;
    	
        do {
            DataCompound c = Format.NBT.newCompound();
            boolean generated = r.state.isGenerated();
            c.put("generated", generated);
            
            try {
                // Include the savers in the try-catch because it'd be foolish
                // to trust them.
                savers.forEach(s -> s.save(r, c, generated));
                
                IOUtil.writeSafe(r.getFile(world), c, Compression.GZIP);
                success = true;
                world.stats.save.completed.increment();
            } catch(Throwable t) {
                world.stats.save.failed.increment();
                log.postSevere("Saving " + r + " failed!", t);
                success = false;
                // Don't break from the do..while on a fail; if another save
                // was requested, we might get lucky and it may work the second
                // time.
            }
        } while(r.state.finishSaving()); // save again if another save was requested
        
        callback.accept(r, success);
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
