package com.stabilise.world.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
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
import com.stabilise.world.loader.WorldLoader.RegionIO;
import com.stabilise.world.loader.WorldLoader.RegionLoader;
import com.stabilise.world.loader.WorldLoader.RegionSaver;


/**
 * A DimensionLoader is essentially a world/dimension-local handle to the
 * WorldLoader of a Multiverse.
 */
public class DimensionLoader {
	
	/** Reference to the parent WorldLoader. */
    final WorldLoader loader;
    final HostWorld world;
    WorldGenerator generator = null;
    volatile boolean cancelLoadOperations = false;
    final WorldLoadTracker tracker;
    
    final List<IRegionLoader> loaders = new ArrayList<>();
    final List<IRegionLoader> savers = new ArrayList<>();
    
    
    DimensionLoader(WorldLoader loader, HostWorld world) {
        this.loader = loader;
        this.world = world;
        this.tracker = world.loadTracker();
    }
    
    /**
     * Prepares this this loader by providing it with a reference to the
     * world generator.
     * 
     * @throws IllegalStateException if this loader has already been
     * prepared.
     * @throws NullPointerException if generator is null.
     */
    public void prepare(WorldGenerator generator) {
        if(this.generator != null)
            throw new IllegalStateException("Generator already set");
        this.generator = generator;
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
        //loader.loadRegion(this, region, andGenerate);
        
        world.stats.load.requests.increment();
        
        if(r.state.getLoadPermit()) {
            tracker.startLoadOp();
            loader.executor.execute(() -> doLoad(r, generate));
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
        //loader.saveRegion(this, region, cacheHandle);
    	
    	world.stats.save.requests.increment();
        loader.executor.execute(() -> doSave(region, cacheHandle));
        region.lastSaved = handle.world.getAge();
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
                loader.log.postSevere("Loading " + r + " failed!", e);
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
        
        if(r.getSavePermit()) {
            try {
                regionTag.put("generated", r.isGenerated());
                
                // ...............
                
                try {
                    IOUtil.writeSafe(file, regionTag, Compression.GZIP);
                } catch(IOException e) {
                    log.postSevere("Could not save " + r + "!", e);
                }
                r.finishSaving();
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
