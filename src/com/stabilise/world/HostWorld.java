package com.stabilise.world;

import static com.stabilise.world.World.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.EntityPlayer;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.annotation.Unguarded;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.LightLinkedList;
import com.stabilise.util.maths.AbstractPoint;
import com.stabilise.util.maths.MutablePoint;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.loader.WorldLoader.DimensionLoader;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.multiverse.HostMultiverse.PlayerData;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world as viewed by its host (i.e. the client in singleplayer, or the
 * server (slash hosting client) in multiplayer).
 * 
 * <!--
 * TODO: Implementation details for everything. Details are very important when
 * it comes to documenting interactions between the world, the world loader,
 * and the world generator
 * -->
 */
public class HostWorld extends AbstractWorld {
	
	/** This world's region cache. */
	public final RegionCache regionCache;
	
	/** The world loader. */
	private final DimensionLoader loader;
	/** The world generator. */
	private final WorldGenerator generator;
	
	// VERY IMPORTANT IMPLEMENTATION DETAILS:
	// Empirical testing has revealed that the table size for region maps in
	// HostWorld is usually 16, but can grow to 32. This means the number of
	// valid hash bits is either 4 or 5 (all higher bits are masked out), so we
	// need to compress our data into those bits while minimising collisions.
	/** The map of all loaded regions. Maps region loc -> regions. */
	private final ConcurrentHashMap<AbstractPoint, Region> regions =
			new ConcurrentHashMap<>();
	/** Tracks the number of loaded regions, in preference to invoking
	 * regions.size(), which is not a constant-time operation. */
	private int numRegions = 0;
	/** Dummy key for {@link #regions} whose mutability may be abused for
	 * reusability ONLY on the main thread. */
	@Unguarded
	private final MutablePoint dummyLoc = Region.createMutableLoc();
	
	/** Holds all player slice maps. */
	private final List<SliceMap> sliceMaps = new LightLinkedList<>();
	
	/** Whether or not the world has been {@link #prepare() prepared}. */
	private boolean prepared = false;
	
	public final WorldStatistics stats = new WorldStatistics();
	
	
	/**
	 * Creates a new HostWorld.
	 * 
	 * @param multiverse The multiverse..
	 * @param dimension The dimension of this world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public HostWorld(Multiverse<?> multiverse, Dimension dimension) {
		super(multiverse, dimension);
		
		spawnSliceX = dimension.info.spawnSliceX;
		spawnSliceY = dimension.info.spawnSliceY;
		
		// We instatiate the loader, generator and cache, and then safely hand
		// them references to each other as required.
		loader = multiverse.loader.loaderFor(this);
		generator = dimension.generatorFor(multiverse, this);
		regionCache = new RegionCache(this);
		
		loader.prepare(generator);
		generator.prepare(loader, regionCache);
		regionCache.prepare(loader);
	}
	
	@NotThreadSafe
	@Override
	public void prepare() {
		if(prepared)
			throw new IllegalStateException("World has already been prepared!");
		
		// Load the spawn regions if this is the default dimension
		if(dimension.info.name.equals(Dimension.defaultDimensionName())) {
			// Ensure the 'spawn regions' are generated, and anchor them such that
			// they're always loaded
			// The spawn regions extend for -256 <= x,y <= 256 (this is arbitrary)
			for(int x = -1; x < 1; x++) {
				for(int y = -1; y < 1; y++) {
					// This will induce a permanent anchorage imbalance which
					// should never be rectified; the region will remain
					// perpetually loaded
					loadRegion(x, y).anchorSlice();
				}
			}
		}
		
		prepared = true;
	}
	
	/**
	 * Adds a player to the world.
	 * 
	 * @param data The data of the player to add.
	 * 
	 * @return The added player entity.
	 * @throws NullPointerException if {@code data} is {@code null}.
	 */
	public EntityMob addPlayer(PlayerData data) {
		EntityPlayer p = new EntityPlayer();
		if(data.newToWorld) {
			data.newToWorld = false;
			// TODO: For now I'm placing the character at (0,0) of the spawn
			// slice. In practice, we'll need to check to see whether or not
			// this location is valid, and keep searching until a valid
			// location is found.
			data.lastX = tileCoordFromSliceCoord(dimension.info.spawnSliceX);
			data.lastY = tileCoordFromSliceCoord(dimension.info.spawnSliceY);
		}
		addEntity(p, data.lastX, data.lastY);
		setPlayer(p);
		sliceMaps.add(new SliceMap(this, p));
		return p;
	}
	
	/**
	 * Checks for whether or not the spawn area about a player has been
	 * loaded.
	 * 
	 * @param player The player.
	 * 
	 * @return {@code true} if the area is loaded; {@code false} otherwise.
	 */
	/*
	public boolean spawnAreaLoaded(EntityMob player) {
		return true; // TODO
	}
	*/
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>In the HostWorld implementation, this returns {@code true} iff all
	 * regions have been loaded and generated.
	 */
	@Override
	public boolean isLoaded() {
		for(Region r : regions.values()) {
			if(!r.isActive()) {
				//log.postInfo("Not all regions loaded (" + r + ": "
				//		+ r.isLoaded() + ", " + r.isGenerated() + ")");
				return false;
			}
		}
		//log.postInfo("All regions loaded!");
		return prepared;
	}
	
	@Override
	public boolean update() {
		doUpdate();
		return numRegions == 0;
	}
	
	@Override
	protected void doUpdate() {
		super.doUpdate();
		
		profiler.start("sliceMap"); // root.update.game.world.sliceMap
		for(SliceMap m : sliceMaps)
			m.update();
		
		profiler.next("region"); // root.update.game.world.region
		IteratorUtils.forEach(regions.values(),
				r -> r.update(this) && unloadRegion(r));
		
		profiler.end(); // root.update.game.world
		
		// Uncache any regions which may have been cached during this tick.
		// TODO: Once a tick might be too often, since this can be expensive.
		regionCache.uncacheAll();
	}
	
	/**
	 * Gets a region at the given coordinates.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region, or {@code null} if no such region exists.
	 */
	@UserThread("MainThread")
	@NotThreadSafe
	public Region getRegionAt(int x, int y) {
		return regions.get(dummyLoc.set(x, y));
	}
	
	/**
	 * Gets the region at the specified location.
	 * 
	 * @param point The region's location, whose coordinates are in
	 * region-lengths.
	 * 
	 * @return The region, or {@code null} if no such region exists.
	 */
	@ThreadSafe
	public Region getRegionAt(AbstractPoint point) {
		return regions.get(point);
	}
	
	/**
	 * Loads a region into memory. If the region has already been loaded, it
	 * is returned.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region.
	 */
	@UserThread("MainThread")
	@NotThreadSafe
	private Region loadRegion(int x, int y) {
		// Get the region if it is already loaded
		Region r = regions.get(dummyLoc.set(x, y));
		if(r != null)
			return r;
		
		// If it is not loaded directly, try getting it from the cache.
		// Synchronised to make this atomic. See RegionCache.cacheRegion()
		synchronized(regionCache.getLock(x, y)) {
			r = regionCache.get(dummyLoc); // dummyLoc is already (x,y)
			if(r == null) // if it's not cached, create it
				r = new Region(x, y, getAge());
			regions.put(r.loc, r);
		}
		
		numRegions++;
		
		loader.loadRegion(r, true);
		
		return r;
	}
	
	private Region loadRegion(int x, int y, boolean primary) {
		// Get the region if it is already loaded
		Region r = regions.get(dummyLoc.set(x, y));
		if(r != null) {
			if(primary)
				setPrimary(r);
			return r;
		}
		
		// If it is not loaded directly, try getting it from the cache.
		// Synchronised to make this atomic. See RegionCache.cacheRegion()
		synchronized(regionCache.getLock(x, y)) {
			r = regionCache.get(dummyLoc); // dummyLoc is already (x,y)
			if(r == null) // if it's not cached, create it
				r = new Region(x, y, getAge());
			regions.put(r.loc, r);
		}
		
		numRegions++;
		
		loader.loadRegion(r, true);
		
		if(primary)
			setPrimary(r);
		
		return r;
	}
	
	private void setPrimary(Region r) {
		if(r.active)
			return;
		r.active = true;
		
		// Load all 8 regions adjacent to r.
		for(int x = r.x() - 1; x <= r.x() + 1; x++) {
			for(int y = r.y() - 1; y <= r.y() + 1; y++) {
				if(x != r.x() && y != r.y())
					loadRegion(x, y, false).activeNeighbours++;
			}
		}
	}
	
	private void unsetPrimary(Region r) {
		if(!r.active)
			return;
		r.active = false;
		
		// Unload all 8 regions adjacent to r.
		for(int x = r.x() - 1; x <= r.x() + 1; x++) {
			for(int y = r.y() - 1; y <= r.y() + 1; y++) {
				if(x != r.x() && y != r.y()) {
					try {
						getRegionAt(x,y).activeNeighbours--;
					} catch(NullPointerException e) {
						// Theoretically the region should never ever be null,
						// but better safe than sorry.
						log.postWarning("Region at (" + x + "," + y + ") is "
								+ "null? src: unsetPrimary");
					}
				}
			}
		}
	}
	
	/**
	 * Ports the state of all entities located in a region to that region, and
	 * then moves the region to the cache so that it may be saved.
	 * 
	 * <p>The region should be removed from the map of regions immediately
	 * after this method returns.
	 * 
	 * @param r The region.
	 * 
	 * @return true always.
	 */
	private boolean unloadRegion(Region r) {
		// Now unload entities in the region as well...
		int minX = r.loc.x * Region.REGION_SIZE_IN_TILES;
		int maxX = minX + Region.REGION_SIZE_IN_TILES;
		int minY = r.loc.y * Region.REGION_SIZE_IN_TILES;
		int maxY = minY + Region.REGION_SIZE_IN_TILES;
		
		for(Entity e : getEntities()) {
			if(e.x + e.boundingBox.v11.x >= minX
					&& e.x + e.boundingBox.v00.x <= maxX
					&& e.y + e.boundingBox.v11.y >= minY
					&& e.y + e.boundingBox.v00.y <= maxY)
				e.destroy();
		}
		
		numRegions--;
		
		// Finally, we submit the region to the cache and let it manage saving
		// and then unloading.
		regionCache.saveRegion(r);
		
		return true;
	}
	
	/**
	 * Saves a region.
	 * 
	 * @param r The region to save.
	 */
	public void saveRegion(Region r) {
		loader.saveRegion(r, null);
	}
	
	/**
	 * Returns the region occupying the specified tile coord, or {@code null}
	 * if it is not loaded.
	 */
	private Region getRegionFromTileCoords(int x, int y) {
		return getRegionAt(regionCoordFromTileCoord(x),
				regionCoordFromTileCoord(y));
	}
	
	/**
	 * Returns the region occupying the specified slice coord, or {@code null}
	 * if it is not loaded.
	 */
	private Region getRegionFromSliceCoords(int x, int y) {
		return getRegionAt(regionCoordFromSliceCoord(x),
				regionCoordFromSliceCoord(y));
	}
	
	/**
	 * Returns the slice occupying the specified location in the region, or
	 * {@code null} if it has not been loaded yet.
	 * 
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	private Slice getSliceFromTileCoords(Region r, int x, int y) {
		return r.getSliceAt(sliceCoordRelativeToRegionFromTileCoord(x),
				sliceCoordRelativeToRegionFromTileCoord(y));
	}
	
	/**
	 * Marks a slice as loaded. This will attempt to load and generate the
	 * slice's parent region, as per {@link #loadRegion(int, int)}.
	 * 
	 * @param x The x-coordinate of the slice, in slice lengths.
	 * @param y The y-coordinate of the slice, in slice lengths.
	 */
	@UserThread("MainThread")
	@NotThreadSafe
	public void loadSlice(int x, int y) {
		loadRegion(
				regionCoordFromSliceCoord(x),
				regionCoordFromSliceCoord(y)
		).anchorSlice();
	}
	
	/**
	 * Unloads a slice.
	 * 
	 * @param x The x-coordinate of the slice, in slice lengths.
	 * @param y The y-coordinate of the slice, in slice lengths.
	 */
	public void unloadSlice(int x, int y) {
		Region r = getRegionFromSliceCoords(x, y);
		if(r != null)
			r.deAnchorSlice();
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		Region r = getRegionFromSliceCoords(x, y);
		return r == null ? null : r.getSliceAt(
				sliceCoordRelativeToRegionFromSliceCoord(x),
				sliceCoordRelativeToRegionFromSliceCoord(y));
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		Region r = getRegionFromTileCoords(x, y);
		return r == null ? null : getSliceFromTileCoords(r, x, y);
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		Slice s = getSliceAtTile(x, y);
		
		if(s != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			// TODO: remove this when I make sure one can't set a tile over another
			s.getTileAt(tileX, tileY).handleRemove(this, x, y);
			
			s.setTileAt(tileX, tileY, id);
			
			Tile.getTile(id).handlePlace(this, x, y);
		}
	}
	
	@Override
	public void breakTileAt(int x, int y) {
		Slice s = getSliceAtTile(x, y);
		
		if(s != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			s.getTileAt(tileX, tileY).handleBreak(this, x, y);
			
			s.setTileAt(tileX, tileY, Tiles.AIR);
		}
	}
	
	@Override
	public void setTileEntityAt(int x, int y, TileEntity t) {
		Slice s = getSliceAtTile(x, y);
			
		if(s != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			TileEntity t2 = s.getTileEntityAt(tileX, tileY);
			if(t2 != null) {
				t2.handleRemove(this, x, y);
				removeTileEntity(t2);
			}
			
			s.setTileEntityAt(tileX, tileY, t);
			
			if(t != null)
				addTileEntity(t);
		}
	}
	
	@Override
	public void blowUpTile(int x, int y, float explosionPower) {
		Slice s = getSliceAtTile(x, y);
		
		if(s != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			if(s.getTileAt(tileX, tileY).getHardness() < explosionPower) {
				s.getTileAt(tileX, tileY).handleRemove(this, x, y);
				
				s.setTileAt(tileX, tileY, Tiles.AIR);
				
				//Tiles.AIR.handlePlace(this, x, y);
			}
		}
	}
	
	/**
	 * Gets this world's filesystem directory.
	 */
	public FileHandle getWorldDir() {
		return dimension.info.getDimensionDir();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws RuntimeException if an I/O error occurred while saving.
	 */
	@Override
	public void save() {
		save(false);
	}
	
	/**
	 * Saves this world.
	 * 
	 * @param unload Whether or not every region should be unloaded as well as
	 * saved.
	 * 
	 * @throws RuntimeException if an I/O error occurred while saving.
	 */
	private void save(boolean unload) {
		log.postInfo("Saving dimension...");
		
		if(unload) // just in case
			regionCache.uncacheAll();
		
		for(Region r : regions.values()) {
			if(unload) regionCache.saveRegion(r);
			else saveRegion(r);
		}
		
		try {
			dimension.saveData();
		} catch(IOException e) {
			throw new RuntimeException("Could not save dimension info!", e);
		}
		
		//savePlayers();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws RuntimeException if an I/O error occurred while saving.
	 */
	@Override
	public void close() {
		loader.shutdown();
		save(true);
		generator.shutdown();
	}
	
	@Override
	public void blockUntilClosed() {
		regionCache.waitUntilDone();
		
		log.postDebug(stats.toString());
	}
	
	@Override
	public int hashCode() {
		return dimension.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return o == this;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Creates a new HostWorld as per
	 * {@link #HostWorld(WorldInfo) new HostWorld(info)}, where {@code info} is
	 * the WorldInfo object returned as if by
	 * {@link WorldInfo#loadInfo(String) WorldInfo.loadInfo(worldName)}. If you
	 * already have access to a world's WorldInfo object, it is preferable to
	 * construct the GameWorld directly.
	 * 
	 * @param worldName The name of the world on the file system.
	 * 
	 * @return The HostWorld instance, or {@code null} if the world info could
	 * not be loaded.
	 */
	/*
	public static HostWorld loadWorld(String worldName) {
		WorldInfo info = WorldInfo.loadInfo(worldName);
		
		if(info != null)
			return new HostWorld(info);
		
		Log.get().postSevere("Could not load info file of world \"" + worldName + "\" during world loading!");
		return null;
	}
	*/
	
}
