package com.stabilise.world.save;

import java.util.concurrent.ExecutorService;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.WorldData;

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
	
	/** The world for which the loader is loading. */
	protected final HostWorld world;
	
	/** The executor which delegates threads for loading. */
	private final ExecutorService executor;
	
	/** Whether or not loading operations should be cancelled due to the loader
	 * having been shut down. This is volatile. */
	private volatile boolean cancelLoadOperations = false;
	
	/** The world loader's log. */
	protected final Log log = Log.getAgent("WORLDLOADER");
	
	
	/**
	 * Creates a new WorldLoader.
	 * 
	 * @param data The world's data object.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code config} is
	 * {@code null}.
	 */
	public WorldLoader(WorldData data) {
		if(data == null)
			throw new IllegalArgumentException("config is null");
		
		world = data.world;
		executor = data.executor;
	}
	
	/**
	 * Instructs the WorldLoader to load a region.
	 * 
	 * <p>The region will be loaded asynchronously; the loader thread will set
	 * {@link Region#loaded loaded} to {@code true} once it has completed
	 * loading the region to indicate it is safe to access the region's
	 * members.
	 * 
	 * @param region The region to load.
	 */
	@UserThread("MainThread")
	public final void loadRegion(Region region) {
		if(region.loaded)
			return;
		
		if(!obtainRegionForLoad(region))
			return;
		
		executor.execute(new RegionLoader(region, false));
	}
	
	/**
	 * Instructs the WorldLoader to load a region on the current thread. This
	 * instruction is ignored, however, if the region has already loaded or is
	 * currently loading. Note that this will set {@link Region#loaded loaded}
	 * to {@code true} once the region has completed loading.
	 * 
	 * <p>This method exists for the use of the WorldGenerator.
	 * 
	 * @param region The region to load.
	 */
	public final void loadRegionSynchronously(Region region) {
		if(region.loaded)
			return;
		
		if(!obtainRegionForLoad(region))
			return;
		
		new RegionLoader(region, false).run();
	}
	
	/**
	 * Instructs the WorldLoader to load a region, then have the world
	 * generator generate the region (as per
	 * {@link WorldGenerator#generate(Region)}) if it has yet to be generated.
	 * 
	 * <p>The region will be loaded asynchronously; the loader thread will set
	 * {@link Region#loaded loaded} to {@code true} once it has completed
	 * loading the region to indicate it is safe to access the region's
	 * members.
	 * 
	 * @param region The region to load.
	 */
	@UserThread("MainThread")
	public final void loadAndGenerateRegion(Region region) {
		if(region.loaded)
			world.generator.generate(region);
		else if(obtainRegionForLoad(region))
				executor.execute(new RegionLoader(region, true));
	}
	
	/**
	 * Instructs the WorldLoader to save a region.
	 * 
	 * <p>The request will be ignored if the region has not been loaded.
	 * 
	 * @param region The region to save.
	 */
	@UserThread({"MainThread", "WorkerThread"})
	public final void saveRegion(Region region) {
		if(!region.loaded)
			return;
		
		region.pendingSave = true;
		executor.execute(new RegionSaver(region));
		region.unsavedChanges = false;
		region.lastSaved = world.info.age;
	}
	
	/**
	 * Instructs the WorldLoader to save a region on the current thread.
	 * 
	 * <p>This method exists for the use of the WorldGenerator.
	 * 
	 * @param region The region to save.
	 */
	@UserThread("WorkerThread")
	public final void saveRegionSynchronously(Region region) {
		region.pendingSave = true;
		new RegionSaver(region).run();
		region.unsavedChanges = false;
		region.lastSaved = world.info.age;
	}
	
	/**
	 * Loads a region.
	 * 
	 * @param r The region to load.
	 */
	@UserThread("WorkerThread")
	protected abstract void load(Region r);
	
	/**
	 * Saves a region.
	 * 
	 * @param r The region to save.
	 */
	@UserThread("WorkerThread")
	protected abstract void save(Region r);
	
	/**
	 * Attempts to obtain a permit to load the given region. If this method
	 * returns {@code true} then the current thread is permitted to load the
	 * region, or create a new thread to load it. Usage of this method ensures
	 * ensures only one thread may carry out the task of loading the region.
	 * 
	 * @param r The region.
	 * 
	 * @return {@code true} if the region may be loaded; {@code false}
	 * otherwise.
	 */
	private boolean obtainRegionForLoad(Region r) {
		// First perform a standard check to avoid synchronisation overhead +
		// the potential of prolonged exclusion blocking
		if(r.loading)
			return false;
		// Synchronised as to make this operation atomic
		synchronized(r) {
			if(r.loading)
				return false;
			r.loading = true;
		}
		return true;
	}
	
	/**
	 * Shuts down the WorldLoader; region loading operations will be cancelled
	 * but region saves will be permitted to complete.
	 */
	@UserThread("MainThread")
	public void shutdown() {
		cancelLoadOperations = true;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A {@code Runnable} which is passed into {@link #executor} for the
	 * purpose of loading or saving a region.
	 * 
	 * @see RegionLoader
	 * @see RegionSaver
	 */
	private abstract class RegionIO implements Runnable {
		
		/** The region. */
		protected final Region r;
		
		/**
		 * Creates a new RegionIO.
		 * 
		 * @param r The region.
		 */
		protected RegionIO(Region r) {
			this.r = r;
		}
		
	}
	
	/**
	 * A RegionLoader is a Runnable which is passed into the executor for the
	 * purpose of loading a region.
	 */
	private class RegionLoader extends RegionIO {
		
		/** True if the region should be generated, if it is not already. */
		private final boolean generate;
		
		/**
		 * Creates a new RegionLoader.
		 * 
		 * @param region The region to load.
		 * @param generate Whether or not the region should be generated after
		 * being loaded, if it is not already.
		 */
		public RegionLoader(Region region, boolean generate) {
			super(region);
			this.generate = generate;
		}
		
		@Override
		public void run() {
			if(cancelLoadOperations)
				return;
			
			if(r.fileExists())
				load(r);
			
			if(generate)
				world.generator.generateSynchronously(r);
			
			r.notifyOfLoaded();
			r.loading = false;
			
			//log.postFineDebug("Finished loading " + r);
		}
		
	}
	
	/**
	 * A RegionSaver is a Runnable which is passed into the executor for the
	 * purpose of saving a region.
	 */
	private class RegionSaver extends RegionIO {
		
		/**
		 * Creates a new RegionSaver.
		 * 
		 * @param region The region to save.
		 */
		public RegionSaver(Region region) {
			super(region);
		}
		
		@Override
		public void run() {
			// Abort immediately rather than hold the lock for sustained
			// periods as doing so can stall the world generator
			synchronized(r) {
				if(r.saving)
					return;
				r.saving = true;
			}
			r.pendingSave = false;
			save(r);
			r.saving = false;
		}
		
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the loader to use for world loading.
	 * 
	 * @param data The world's data object.
	 * 
	 * @return The loader to use for world loading.
	 */
	public static WorldLoader getLoader(WorldData data) {
		return new PreAlphaWorldLoader(data);
	}
	
}