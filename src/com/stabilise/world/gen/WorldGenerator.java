package com.stabilise.world.gen;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Slice.SLICE_SIZE;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.stabilise.util.Log;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.RegionCache;
import com.stabilise.world.Slice;
import com.stabilise.world.provider.WorldProvider;
import com.stabilise.world.structure.Structure;
import com.stabilise.world.tile.Tiles;

/**
 * The {@code WorldGenerator} class provides the mechanism for generating the
 * terrain of a world.
 * 
 * <h3>Standard Usage Guidelines - OUT OF DATE</h3>
 * 
 * <p>To obtain the {@code WorldGenerator} instance to use for a world, simply
 * invoke {@link #getGenerator(WorldData)}.
 * 
 * <p>To generate a region, invoke {@link #generate(Region)} on the {@code
 * WorldGenerator} instance. When the specified region's {@link
 * Region#generated generated} flag is set to {@code true}, generation of the
 * region has completed and it is safe to interact with the region.
 * 
 * <p>{@link #generateSynchronously(Region)} is provided as a convenience
 * alternative to {@link #generate(Region)} if it is known that the thread
 * invoking it will not suffer from the prolonged blocking that generation
 * entails (as a rule of thumb, it is acceptable to invoke {@code
 * generateSynchronously} on any thread but the main thread).
 * 
 * <p>To shut down the {@code WorldGenerator}, invoke {@link #shutdown()} and
 * then {@code shutdown()} on {@link WorldData#executor data.executor}, where
 * {@code data} is the {@code WorldData} object passed to {@code getGenerator}.
 * 
 * <h3>Implementation Details - OUT OF DATE</h3>
 * 
 * <p>Each invocation of {@code generate} either creates a new thread or reuses
 * an existing thread created for generation, loading, or saving, depending on
 * the {@code ExecutorService} contained in the given {@code WorldData} object.
 * This ensures maximum generation throughput in that every region should be
 * able to generate concurrently.
 * 
 * <p>The generation of a region may entail that any number of other regions
 * are cached by the generator to enable inter-region generation, specifically
 * in the case of certain 'structures', or {@link Structure schematics}, which
 * may be generated across region boundaries. These regions are obtained from
 * the world via invocation of {@link HostWorld#getRegionAt(int, int)
 * getRegionAt}, and have a slice marked as per {@link Region#anchorSlice()} to
 * indicate that they are being used. {@link Region#deAnchorSlice()} is invoked
 * when the region is no longer being used. If {@code getRegionAt} returns
 * {@code null}, the WorldGenerator will instantiate the region itself. Cached
 * regions may be accessed through {@link #getCachedRegion(int, int)}, and it
 * is advisable for the world to invoke this before instantiating regions to
 * prevent duplicate and inconsistent regions from being created.
 * 
 * <p>Cached regions may have schematics implanted via {@link
 * Region#queueSchematic(String, int, int, int, int, int, int) queueSchematic},
 * and these schematics will be added to the cached region immediately if it
 * has already been generated, or as the region is being normally generated if
 * it has not yet been generated.
 * 
 * <h3>Guidelines for Subclasses of WorldGenerator</h3>
 * 
 * <p>Generators are advised to keep the landform within the range
 * {@code -256 < y < 256} at and around {@code x = 0} for spawning purposes.
 */
@ThreadSafe
public abstract class WorldGenerator {
	
	/** The world provider. */
	private final WorldProvider<?> prov;
	/** The world for which the generator is generating. */
	private final HostWorld world;
	/** The seed to use for world generation. */
	protected final long seed;
	
	/** The executor which delegates threads. */
	private final Executor executor;
	/** Whether or not the generator has been shut down. This is volatile. */
	private volatile boolean isShutdown = false;
	
	private final RegionCache regionCache;
	
	/** The cache of schematics in use. TODO: this system is outdated */
	@SuppressWarnings("unused")
	private final Map<String, Structure> schematics = new ConcurrentHashMap<>(5);
	
	/** The generator's log. */
	protected final Log log = Log.getAgent("GENERATOR");
	
	
	/**
	 * Creates a new WorldGenerator.
	 * 
	 * @param worldProv The world provider. This should be a {@code
	 * HostProvider} for all but a {@code PrivateDimensionGenerator}.
	 * @param world The world.
	 * @param regionCache The region world's cache.
	 * 
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	protected WorldGenerator(WorldProvider<?> worldProv, HostWorld world,
			RegionCache regionCache) {
		this.prov = Objects.requireNonNull(worldProv);
		this.world = Objects.requireNonNull(world);
		this.regionCache = Objects.requireNonNull(regionCache);
		this.executor = prov.getExecutor();
		
		seed = prov.getSeed();
	}
	
	/**
	 * Instructs the WorldGenerator to generate the given region.
	 * 
	 * <p>Regions which are ineligible for generation will ignored. That is,
	 * the given region will not be generated if:
	 * 
	 * <ul>
	 * <li>it has not been loaded (i.e. {@link Region#loaded region.loaded}
	 *     is {@code false}).
	 * <li>it has already been generated (i.e. {@link
	 *     Region#isGenerated() region.isGenerated()} is {@code true}).
	 * <li>it is being generated concurrently.
	 * </ul>
	 * 
	 * @param region The region to generate.
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("MainThread")
	public final void generate(final Region region) {
		if(region.getGenerationPermit())
			executor.execute(() -> { genRegion(region); });
	}
	
	/**
	 * Instructs the WorldGenerator to generate the given region on the current
	 * thread. Unlike {@link #generate(Region)}, this method does not ignore
	 * the instruction if the region is currently generating (though it will if
	 * <tt>{@link Region#isGenerated() region.isGenerated()} == true</tt>);
	 * this is because it is expected for this method to be invoked only by the
	 * WorldLoader once it finishes loading a region, and hence it is
	 * guaranteed that {@code region} is not currently being generated.
	 * 
	 * @param region The region to generate.
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("WorkerThread")
	public final void generateSynchronously(Region region) {
		if(region.getGenerationPermit())
			genRegion(region);
	}
	
	/**
	 * Generates a region. The lock for the region must be obtained before this
	 * method is invoked.
	 * 
	 * @param r The region to generate.
	 * 
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	@UserThread("WorkerThread")
	private void genRegion(Region r) {
		if(isShutdown)
			return;
		
		//log.logMessage("Generating " + r);
		
		TaskTimer timer = new TaskTimer("Generating " + r);
		timer.start();
		
		try {
			// Don't generate a region if we don't need to!
			if(!r.isGenerated()) {
				// Set up the region's slices
				for(int y = 0; y < REGION_SIZE; y++) {
					for(int x = 0; x < REGION_SIZE; x++) {
						r.slices[y][x] = new Slice(
								x + r.loc.x * REGION_SIZE,
								y + r.loc.y * REGION_SIZE,
								// all values are 0 == Tiles.AIR
								new int[SLICE_SIZE][SLICE_SIZE]
						);
					}
				}
				
				// Generate the region, as implemented in subclasses
				generateRegion(r);
			}
			
			// After normal generation processes have been completed, add any
			// queued schematics.
			r.implantStructures(regionCache);
			
			/*
			for(Region.QueuedSchematic s : r.getSchematics(true)) {
				changes = true;
				addSchematicAt(
						r, s.schematicName,
						s.sliceX, s.sliceY,
						s.tileX, s.tileY,
						SchematicParams.scheduledGenParams(s.offsetX, s.offsetY)
				);
			}
			*/
			
			timer.stop();
			log.postDebug(timer.getResult(TimeUnit.MILLISECONDS));
		} catch(Throwable t) {
			log.postSevere("Worldgen of " + r + " failed!", t);
		}
		
		r.setGenerated();
		
		// Save the region once it's done generating
		prov.loader.saveRegion(world, r, null);
		
		// Finally we clean up all the regions cached during generation.
		regionCache.uncacheAll();
	}
	
	/**
	 * Generates a region.
	 * 
	 * <p>The general contract of this method is that it may modify the
	 * contents of any slice in the given region - namely, it may set tiles and
	 * tile entities, and add schematics. Leaving this method blank is
	 * equivalent to setting every tile in the region to {@link Tiles#AIR}.
	 * 
	 * <p>Subclasses of WorldGenerator should note that this method may be
	 * invoked by various worker threads concurrently, and it is hence the
	 * responsibility of implementors to ensure correct thread safety
	 * techniques are observed.
	 * 
	 * @param r The region to generate.
	 */
	@UserThread("WorkerThread")
	protected abstract void generateRegion(Region r);
	
	/**
	 * Adds a schematic to the world.
	 * 
	 * @param r The region to add the schematic to.
	 * @param name The name of the schematic to add.
	 * @param sliceX The x-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param sliceY The y-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param tileX The x-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param tileY The y-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param params The parameters to use for adding the schematic.
	 */
	/*
	@UserThread("WorkerThread")
	protected final void addSchematicAt(Region r, String name, int sliceX, int sliceY, int tileX, int tileY, SchematicParams params) {
		addSchematicAt(r, getSchematic(name), sliceX, sliceY, tileX, tileY, params);
	}
	*/
	
	/**
	 * Adds a schematic to the world.
	 * 
	 * @param r The region to add the schematic to.
	 * @param sc The name of the schematic to add.
	 * @param sliceX The x-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param sliceY The y-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param tileX The x-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param tileY The y-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param params The parameters to use for adding the schematic.
	 * 
	 * @throws IllegalArgumentException Thrown if the schematic's origin has
	 * negative coordinates.
	 */
	/*
	@UserThread("WorkerThread")
	protected final void addSchematicAt(Region r, Structure sc, int sliceX, int sliceY, int tileX, int tileY, SchematicParams params) {
		if(sc == null)
			return;
		
		//log.logMessage("Adding " + sc + " to " + r);
		
		// Don't allow negative pivots; they'll mess up our algorithm
		if(sc.x < 0 || sc.y < 0)
			throw new IllegalArgumentException(sc + " has a pivot in an invalid location! (" + sc.x + "," + sc.y + ")");
		
		//int offsetX = ((tileX + sliceX * SLICE_SIZE - sc.x) % (REGION_SIZE_IN_TILES));
		
		// Inform neighbouring regions of the schematic if the schematic overlaps into them
		if(params.informRegions) {
			for(int regionY = r.loc.y + params.offsetY + ((tileY + sliceY * SLICE_SIZE - sc.y) / (REGION_SIZE_IN_TILES));
					regionY <= r.loc.y + params.offsetY + ((tileY + sliceY * SLICE_SIZE - sc.y + sc.height) / (REGION_SIZE_IN_TILES));
					regionY++) {
				for(int regionX = r.loc.x + params.offsetX + ((tileX + sliceX * SLICE_SIZE - sc.x) / (REGION_SIZE_IN_TILES));
						regionX <= r.loc.x + params.offsetX + ((tileX + sliceX * SLICE_SIZE - sc.x + sc.width) / (REGION_SIZE_IN_TILES));
						regionX++) {
					if(regionY != r.loc.y || regionX != r.loc.x) {
						Region cachedRegion = regionCache.cacheRegion(regionX, regionY);
						cachedRegion.addStructure(
								new Region.QueuedStructure(
										sc.name, sliceX, sliceY, tileX, tileY, r.loc.x - regionX, r.loc.y - regionY
								)
						);
					}
				}
			}
		}
		
		// Get the bottom-left corner tile of the region from which the schematic will originate
		int initialX = sliceX * SLICE_SIZE + tileX - sc.x + params.offsetX * REGION_SIZE_IN_TILES;
		int initialY = sliceY * SLICE_SIZE + tileY - sc.y + params.offsetY * REGION_SIZE_IN_TILES;
		
		int x, y;
		
		// If the bottom-left corner tile is within the region, start from there
		// If not, we'll start generating from the bottom and/or left boundary of the region, and
		// skip to the first part of the schematic that is within the region
		if(initialX >= 0) {
			tileX = (initialX % REGION_SIZE_IN_TILES) % SLICE_SIZE;
			sliceX = Maths.floor(((initialX / SLICE_SIZE)) % REGION_SIZE);
			x = 0;
		} else {
			tileX = 0;
			sliceX = 0;
			x = -initialX;
		}
		
		if(initialY >= 0) {
			tileY = (initialY % REGION_SIZE_IN_TILES) % SLICE_SIZE;
			sliceY = Maths.floor(((initialY / SLICE_SIZE)) % REGION_SIZE);
			y = 0;
		} else {
			tileY = 0;
			sliceY = 0;
			y = -initialY;
		}
		
		// tileX, sliceX and x need resetting every time it loops to the next row
		int defaultTileX = tileX;
		int defaultSliceX = sliceX;
		initialX = x;
		
		Slice s = r.getSliceAt(sliceX, sliceY);
		
		for(; y < sc.height; y++) {
			for(; x < sc.width; x++) {
				
				int tile = sc.getTileAt(x, y);
				if(tile != -1 && (params.copyAir || tile != 0))
					s.setTileAt(tileX, tileY, tile);
				
				tileX++;
				
				if(tileX == SLICE_SIZE) {
					sliceX++;
					tileX = 0;
					if(sliceX == REGION_SIZE)
						break;
					s = r.getSliceAt(sliceX, sliceY);
				}
			}
			
			tileX = defaultTileX;
			sliceX = defaultSliceX;
			x = initialX;
			
			tileY++;
			
			if(tileY == SLICE_SIZE) {
				sliceY++;
				tileY = 0;
				if(sliceY == REGION_SIZE)
					break;
			}
			
			// Reset the slice
			s = r.getSliceAt(sliceX, sliceY);
		}
	}
	*/
	
	/**
	 * Gets a schematic.
	 * 
	 * @param schematic The schematic's name.
	 * 
	 * @return The schematic, or {@code null} if a schematic with the given
	 * name could not be found.
	 */
	/*
	@UserThread("WorkerThread")
	private Structure getSchematic(String schematic) {
		// Weakly synchronised through use of a ConcurrentHashMap - this is
		// sufficient for now (it doesn't really matter if a map entry is
		// overwritten by an equivalent schematic)
		if(schematics.containsKey(schematic)) {
			return schematics.get(schematic);
		} else {
			Structure s = new Structure(schematic);
			try {
				s.load();
			} catch(IOException e) {
				log.postWarning("Could not load schematic \"" + schematic + "\"!", e);
				return null;
			}
			schematics.put(schematic, s);
			return s;
		}
	}
	*/
	

	
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
	 * A class containing parameters for placing schematics in the world.
	 */
	protected static class SchematicParams {
		
		/** Whether or not to copy air tiles from the schematic. */
		protected final boolean copyAir;
		/** Whether or not to inform neighbouring regions of the schematic
		 * needing to be generated on their sides of a region boundary. */
		protected final boolean informRegions;
		/** The x-offset of the schematic's origin, in region-lengths. */
		protected final int offsetX;
		/** The y-offset of the schematic's origin, in region-lengths. */
		protected final int offsetY;
		
		
		/**
		 * Creates a new object containing parameters for schematic placement.
		 * 
		 * @param copyAir Whether or not to copy air tiles.
		 * @param informRegions Whether or not to carry generation over to
		 * neighbouring regions.
		 */
		protected SchematicParams(boolean copyAir, boolean informRegions, int offsetX, int offsetY) {
			this.copyAir = copyAir;
			this.informRegions = informRegions;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
		// Static factories
		
		/**
		 * Gets a SchematicParams object suitable for default cases.
		 */
		protected static SchematicParams defaultParams() {
			return new SchematicParams(true, true, 0, 0);
		}
		
		/**
		 * Gets a SchematicParams object suitable for scheduled generation.
		 * 
		 * @param offsetX The x-offset of the schematic's origin, in
		 * region-lengths.
		 * @param offsetY The y-offset of the schematic's origin, in
		 * region-lengths.
		 */
		protected static SchematicParams scheduledGenParams(int offsetX, int offsetY) {
			return new SchematicParams(true, false, offsetX, offsetY);
		}
		
	}
	
}
