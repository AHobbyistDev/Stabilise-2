package com.stabilise.world.loader;

import java.util.Objects;
import java.util.concurrent.Executor;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.RegionCache.CachedRegion;
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
	private final Executor executor;
	
	/** The world loader's log. */
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
	private void loadRegion(DimensionLoader handle, Region r, boolean generate) {
		handle.world.stats.load.requests.increment();
		
		if(r.getLoadPermit())
			executor.execute(new RegionLoader(handle, r, generate));
		else if(generate) {
			handle.world.stats.load.rejected.increment();
			handle.generator.generate(r);
		}
	}
	
	@UserThread("Any")
	private void saveRegion(HostWorld world, Region region,
			CachedRegion cacheHandle) {
		world.stats.save.requests.increment();
		executor.execute(new RegionSaver(world, region, cacheHandle));
		region.lastSaved = world.getAge();
	}
	
	/**
	 * Loads a region.
	 * 
	 * @param r The region to load.
	 * @param file The region's file.
	 */
	@UserThread("WorkerThread")
	protected abstract void load(Region r, FileHandle file);
	
	/**
	 * Saves a region.
	 * 
	 * @param r The region to save.
	 * @param file The region's file.
	 */
	@UserThread("WorkerThread")
	protected abstract void save(Region r, FileHandle file);
	
	@UserThread("MainThread")
	@Deprecated
	private void shutdown() {
		// TODO remove?
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A DimensionLoader is essentially a world-local handle to the WorldLoader
	 * of a Multiverse.
	 */
	public static class DimensionLoader {
		
		private final WorldLoader loader;
		private final HostWorld world;
		private WorldGenerator generator = null;
		private volatile boolean cancelLoadOperations = false;
		
		
		private DimensionLoader(WorldLoader loader, HostWorld world) {
			this.loader = loader;
			this.world = world;
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
		 * method does nothing if {@link Region#getLoadPermit()
		 * region.getLoadPermit()} returns {@code false}.
		 * 
		 * @param region The region to load.
		 * @param generate Whether or not the region should also be generated,
		 * if it is not already.
		 * 
		 * @throws NullPointerException if {@code region} is {@code null}.
		 */
		@UserThread("Any")
		public void loadRegion(Region region, boolean generate) {
			loader.loadRegion(this, region, generate);
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
			loader.saveRegion(world, region, cacheHandle);
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
	
	/**
	 * A {@code Runnable} which is passed into {@link #executor} for the
	 * purpose of loading or saving a region.
	 * 
	 * @see RegionLoader
	 * @see RegionSaver
	 */
	private abstract class RegionIO implements Runnable {
		
		protected final HostWorld world;
		protected final Region r;
		
		/**
		 * @throws NPE if world is null
		 */
		protected RegionIO(HostWorld world, Region r) {
			this.world = Objects.requireNonNull(world);
			this.r = r;
		}
		
	}
	
	/**
	 * A RegionLoader is a Runnable which is passed into the executor for the
	 * purpose of loading a region.
	 */
	private class RegionLoader extends RegionIO {
		
		private final DimensionLoader loader;
		private final WorldGenerator generator;
		/** True if the region should be generated, if it is not already. */
		private final boolean generate;
		
		
		public RegionLoader(DimensionLoader loader, Region r, boolean generate) {
			super(loader.world, r);
			this.loader = loader;
			this.generate = generate;
			this.generator = loader.generator;
		}
		
		@Override
		public void run() {
			world.stats.load.started.increment();
			
			if(loader.cancelLoadOperations) {
				world.stats.load.aborted.increment();
				return;
			}
			
			try {
				if(r.fileExists(world))
					load(r, r.getFile(world));
				world.stats.load.completed.increment();
			} catch(Throwable t) {
				world.stats.load.failed.increment();
				log.postSevere("Loading " + r + " failed!", t);
			}
			
			if(generate)
				generator.generateSynchronously(r);
			
			//log.postFineDebug("Finished loading " + r);
		}
		
	}
	
	/**
	 * A RegionSaver is a Runnable which is passed into the executor for the
	 * purpose of saving a region.
	 */
	private class RegionSaver extends RegionIO {
		
		private final CachedRegion cacheHandle;
		
		/**
		 * Creates a new RegionSaver.
		 * 
		 * @param world The region's host world.
		 * @param r The region to save.
		 * @param cache The handle to the region's cache entry. null is
		 * allowed.
		 * 
		 * @throws NullPointerException if {@code world} is {@code null}.
		 */
		public RegionSaver(HostWorld world, Region r, CachedRegion cache) {
			super(world, r);
			this.cacheHandle = cache;
		}
		
		@Override
		public void run() {
			world.stats.save.started.increment();
			
			if(r.getSavePermit()) {
				try {
					save(r, r.getFile(world));
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
		
	}
	
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