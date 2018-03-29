package com.stabilise.world.loader;

import java.util.Objects;
import java.util.concurrent.Executor;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.RegionStore.CachedRegion;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multiverse.Multiverse;

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
public abstract class WorldLoader {
    
    /** The multiverse for which the loader is loading. */
    protected final Multiverse<?> multiverse;
    final Executor executor;
    
    protected final Log log = Log.getAgent("WORLDLOADER");
    
    
    /**
     * Creates a new WorldLoader.
     * 
     * @param multiverse The multiverse.
     * 
     * @throws NullPointerException if {@code multiverse} is {@code null}.
     */
    public WorldLoader(Multiverse<?> multiverse) {
        this.multiverse = Objects.requireNonNull(multiverse);
        executor = multiverse.getExecutor();
    }
    
    /**
     * Gets the handle to this WorldLoader to use for the specified world.
     * 
     * @param world The world to get the loader for.
     * 
     * @throws NullPointerException if world is null.
     */
    public DimensionLoader loaderFor(HostWorld world) {
        return new DimensionLoader(this, Objects.requireNonNull(world));
    }
    
    @UserThread("Any")
    void loadRegion(DimensionLoader handle, Region r, boolean generate) {
        handle.world.stats.load.requests.increment();
        
        if(r.getLoadPermit()) {
            handle.tracker.startLoadOp();
            executor.execute(new RegionLoader(handle, r, generate));
        } else if(generate) {
            handle.tracker.startLoadOp();
            handle.world.stats.load.rejected.increment();
            handle.generator.generate(r);
        }
    }
    
    @UserThread("Any")
    void saveRegion(DimensionLoader handle, Region region,
            CachedRegion cacheHandle) {
        handle.world.stats.save.requests.increment();
        executor.execute(new RegionSaver(handle, region, cacheHandle));
        region.lastSaved = handle.world.getAge();
    }
    
    /**
     * Loads a region.
     * 
     * @param r The region to load.
     * @param file The region's file.
     */
    @UserThread("WorkerThread")
    @Deprecated
    protected abstract void load(Region r, FileHandle file);
    
    /**
     * Saves a region.
     * 
     * @param r The region to save.
     * @param file The region's file.
     */
    @UserThread("WorkerThread")
    @Deprecated
    protected abstract void save(Region r, FileHandle file);
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Gets the loader to use for world loading.
     * 
     * @param multiverse The multiverse.
     * 
     * @return The loader to use for world loading.
     * @throws NullPointerException if multiverse is null.
     */
    public static WorldLoader getLoader(Multiverse<?> multiverse) {
        return new PreAlphaWorldLoader(multiverse);
    }
    
}