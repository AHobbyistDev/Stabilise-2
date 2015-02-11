package com.stabilise.world.old;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.EntityPlayer;
import com.stabilise.entity.GameCamera;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.LightweightLinkedList;
import com.stabilise.util.maths.HashPoint;
import com.stabilise.world.HostWorld;
import com.stabilise.world.IWorld;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.SliceMap;
import com.stabilise.world.WorldData;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.save.WorldLoader;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The game world.
 * 
 * <!--
 * TODO: Implementation details for everything. Details are very important when
 * it comes to documenting interactions between the world, the world loader,
 * and the world generator
 * -->
 * 
 * @deprecated Due to reconstructing the World hierarchy
 */
public class GameWorld extends HostWorld {
	
	/** The world's information. */
	public final WorldInfo info;
	
	/** The world's configuration object. */
	private final WorldData config;
	/** The world loader. */
	public final WorldLoader loader;
	/** The world generator. */
	public final WorldGenerator generator;
	
	/** The map of all loaded regions. This is concurrent as to prevent
	 * problems when relevant methods are accessed by the world loader. */
	public final ConcurrentHashMap<HashPoint, Region> regions;
	/** The slice map, which manages 'loaded' slices. */
	private SliceMap sliceMap;
	
	/** The player's character. */
	private CharacterData playerChar;
	/** The player. */
	public EntityMob player;
	/** The game camera. */
	public GameCamera camera;
	
	/** */
	public final LightweightLinkedList<Particle> particles = new LightweightLinkedList<Particle>();
	/** The total number of particles in the world. */
	public int particleCount = 0;
	
	/** Whether or not the world has been prepared. */
	private boolean prepared = false;
	
	
	/**
	 * Creates a new GameWorld.
	 * 
	 * @param info The world's info.
	 */
	public GameWorld(WorldInfo info) {
		super(info, null, Log.getAgent("world"));
		
		this.info = null;
		
		spawnSliceX = info.spawnSliceX;
		spawnSliceY = info.spawnSliceY;
		
		regions = new ConcurrentHashMap<HashPoint, Region>();
		
		config = new WorldData(this, info);
		loader = WorldLoader.getLoader(config);
		generator = WorldGenerator.getGenerator(config);
	}
	
	/**
	 * Prepares the world by loading into memory any spawn regions, entities,
	 * etc.
	 * 
	 * @throws IllegalStateException Thrown if the world has already been
	 * prepared.
	 */
	public void prepare() {
		if(prepared)
			throw new IllegalStateException("World has already been prepared!");
		
		// Ensure the 'spawn regions' are generated, and anchor them such that
		// they're always loaded
		// For now, the spawn regions extend for -256 <= x,y <= 256
		for(int x = -1; x < 1; x++) {
			for(int y = -1; y < 1; y++) {
				// This will induce a permanent anchorage imbalance which
				// should never be rectified short of a bug; the region will
				// remain perpetually loaded
				loadRegion(x, y).anchorSlice();
			}
		}
		
		prepared = true;
	}
	
	/**
	 * Adds a player to the world.
	 * 
	 * <p>This can be performed on a loader thread if the world is not
	 * currently running - i.e. being set up initially (thus making this
	 * unsuitable for multiple characters in multiplayer (so beware of a direct
	 * port to WorldServer).
	 * 
	 * @param character The data of the player to add.
	 */
	public EntityMob addPlayer(CharacterData character) {
		this.playerChar = character;
		
		EntityPlayer p = new EntityPlayer(this);
		//----loadCharacterData(character);
		if(character.newToWorld) {
			// TODO: For now I'm placing the character at (0,0) of the spawn
			// slice. In practice, we'll need to check to see whether or not
			// this location is valid, and keep searching until a valid
			// location is found.
			character.lastX = tileCoordFromSliceCoord(info.spawnSliceX);
			character.lastY = tileCoordFromSliceCoord(info.spawnSliceY);
			character.newToWorld = false;
			//----saveCharacterData(character);
		}
		addEntity(p, character.lastX, character.lastY);
		setPlayer(p);
		
		//----camera = new GameCamera(this, p);
		sliceMap = new SliceMap(this, p);
		
		player = p;
		
		return p;
	}
	
	/**
	 * Checks for whether or not the spawn area about a character has been
	 * loaded.
	 * 
	 * @param character The character.
	 * 
	 * @return {@code true} if the area is loaded; {@code false} otherwise.
	 */
	public boolean spawnAreaLoaded(CharacterData character) {
		return true;
	}
	
	/**
	 * Checks for whether or not all required regions have been loaded. This
	 * should only be called when the world is being set up to confirm that the
	 * world has fully loaded.
	 * 
	 * @return {@code true} if all required regions have been loaded;
	 * {@code false} otherwise.
	 */
	public boolean regionsLoaded() {
		for(Region r : regions.values()) {
			// If a region hasn't been generated and has anchored slices, it
			// has been cached by the world generator. Once it no longer has
			// any anchored slices, it will have been uncached.
			if(!r.loaded || (!r.generated && r.getAnchoredSlices() != 0))
				return false;
		}
		
		return true;
	}
	
	@Override
	public void update() {
		super.update();
		
		// Increment the world's age
		info.age++;
		
		profiler.start("particles");
		updateObjects(getParticles());
		
		profiler.next("camera");
		// Update the camera
		camera.update();
		
		profiler.next("sliceMap");
		// Update the slices and regions
		sliceMap.update();
		
		profiler.next("region");
		Iterator<Region> i = regions.values().iterator();
		while(i.hasNext()) {
			Region r = i.next();
			profiler.start("update");
			r.update();
			profiler.next("unload");
			if(r.unload) {
				unloadRegion(r);
				i.remove();
			}
			profiler.end();
		}
		profiler.end();
	}
	
	@Override
	public void addParticle(Particle p, double x, double y) {
		p.x = x;
		p.y = y;
		addParticle(p);
	}
	
	@Override
	public void addParticle(Particle p) {
		particles.add(p);
	}
	
	@Override
	public Collection<Particle> getParticles() {
		return particles;
	}
	
	/**
	 * Checks for whether or not a region is in memory.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return Whether or not the region is loaded in memory.
	 */
	public boolean hasRegion(int x, int y) {
		return regions.containsKey(Region.getKey(x, y));
	}
	
	/**
	 * Gets a region at the given coordinates.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region at the given coordinates, or {@code null} if no such
	 * region exists.
	 */
	@UserThread({"MainThread", "WorkerThread"})
	public Region getRegionAt(int x, int y) {
		return regions.get(Region.getKey(x, y));
	}
	
	/**
	 * Loads a region into memory as if by
	 * {@link #loadRegion(int, int, boolean) loadRegion(x, y, true)}. If the
	 * region has already been loaded, it is returned. This method should not
	 * be invoked regularly as it will induce a significant performance
	 * deficit; refer to {@link #getRegionAt(int, int)} instead.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region.
	 */
	public Region loadRegion(int x, int y) {
		return loadRegion(x, y, true);
	}
	
	/**
	 * Loads a region into memory. If the region has already been loaded, it is
	 * returned. This method should not be invoked regularly as it will induce
	 * a significant performance deficit; refer to
	 * {@link #getRegionAt(int, int)} instead.
	 * 
	 * <!-- TODO: For the the generate parameter is redundant as this method is
	 * always being invoked with it as true, but I'm leaving it in in
	 * anticipation of future uses where it is false. -->
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * @param generate Whether or not the region should be generated, if it was
	 * not already.
	 * 
	 * @return The region.
	 */
	@UserThread("MainThread")
	public Region loadRegion(int x, int y, boolean generate) {
		HashPoint loc = Region.getKey(x, y);
		
		// Get the region if it is already loaded
		Region r = regions.get(loc);
		if(r != null) {
			if(generate)
				generator.generate(r);
			return r;
		}
		
		// If it is not loaded directly, try getting it from the world
		// generator's cache.
		// Synchronised to make this atomic. See WorldGenerator.cacheRegion()
		synchronized(generator.getLock(loc)) {
			r = generator.getCachedRegion(loc);
			if(r == null) // if it's not cached, create it
				r = new Region(this, loc);
			regions.put(loc, r);
		}
		
		// Now, we load the region appropriately
		if(generate)
			loader.loadAndGenerateRegion(r);
		else
			loader.loadRegion(r);
		
		return r;
	}
	
	/**
	 * Saves a region at the specified coordinates, then unloads it. Entities
	 * within the region are removed from the world. The region will, however,
	 * not be removed from the map of regions in the world.
	 * 
	 * @param r The region.
	 */
	private void unloadRegion(Region r) {
		// TODO: What do we do if the region is currently loading or
		// generating?
		
		saveRegion(r);
		
		// Now unload entities in the region as well...
		int minX = r.loc.x * Region.REGION_SIZE_IN_TILES;
		int maxX = minX + Region.REGION_SIZE_IN_TILES;
		int minY = r.loc.y * Region.REGION_SIZE_IN_TILES;
		int maxY = minY + Region.REGION_SIZE_IN_TILES;
		
		for(Entity e : getEntities()) {
			if(e.x + e.boundingBox.getV11().x >= minX
					&& e.x + e.boundingBox.getV00().x <= maxX
					&& e.y + e.boundingBox.getV11().y >= minY
					&& e.y + e.boundingBox.getV00().y <= maxY)
				removeEntity(e);
		}
		
		//log.logMessage("Unloaded region " + r.x + "," + r.y);
	}
	
	/**
	 * Saves a region.
	 * 
	 * @param r The region to save.
	 */
	public void saveRegion(Region r) {
		//if(!r.unsavedChanges)
		//	return;
		if(r.generated)
			loader.saveRegion(r);
	}
	
	/**
	 * Marks a slice as loaded. This will attempt to load and generate the
	 * slice's parent region, as per {@link #loadRegion(int, int)}.
	 * 
	 * @param x The x-coordinate of the slice, in slice lengths.
	 * @param y The y-coordinate of the slice, in slice lengths.
	 */
	public void loadSlice(int x, int y) {
		loadRegion(regionCoordFromSliceCoord(x), regionCoordFromSliceCoord(y)).anchorSlice();
	}
	
	/**
	 * Unloads a slice.
	 * 
	 * @param x The x-coordinate of the slice, in slice lengths.
	 * @param y The y-coordinate of the slice, in slice lengths.
	 */
	public void unloadSlice(int x, int y) {
		Region r = getRegionAt(regionCoordFromSliceCoord(x), regionCoordFromSliceCoord(y));
		if(r != null)
			r.deAnchorSlice();
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		Region r = getRegionAt(regionCoordFromSliceCoord(x), regionCoordFromSliceCoord(y));
		return r == null ? null : r.getSliceAt(
				sliceCoordRelativeToRegionFromSliceCoord(x), sliceCoordRelativeToRegionFromSliceCoord(y));
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		Region r = getRegionAt(regionCoordFromTileCoord(x), regionCoordFromTileCoord(y));
		return r == null ? null : r.getSliceAt(
				sliceCoordRelativeToRegionFromTileCoord(x), sliceCoordRelativeToRegionFromTileCoord(y));
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		// TODO: This and getSliceAtTile(x,y) are pretty inefficient given the
		// sheer flux of calls this method is likely to receive during the
		// operation of the game. FIND A BETTER WAY TO DO THIS SOMEHOW, IF AT
		// ALL POSSIBLE (caching?).
		Slice slice = getSliceAtTile(x, y);
		if(slice != null) {
			return slice.getTileAt(
					tileCoordRelativeToSliceFromTileCoord(x),
					tileCoordRelativeToSliceFromTileCoord(y)
			);
		} else {
			return Tiles.BEDROCK_INVISIBLE;
		}
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		Slice slice = getSliceAtTile(x, y);
		
		if(slice != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			// TODO: remove this when I make sure one can't set a tile over another
			slice.getTileAt(tileX, tileY).handleRemove(this, x, y);
			
			slice.setTileAt(tileX, tileY, id);
			
			Tile.getTile(id).handlePlace(this, x, y);
		}
	}
	
	@Override
	public void breakTileAt(int x, int y) {
		Slice slice = getSliceAtTile(x, y);
		
		if(slice != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			slice.getTileAt(tileX, tileY).handleBreak(this, x, y);
			
			slice.setTileAt(tileX, tileY, 0);
		}
	}
	
	@Override
	public TileEntity getTileEntityAt(int x, int y) {
		Slice slice = getSliceAtTile(x, y);
		
		if(slice != null) {
			x = tileCoordRelativeToSliceFromTileCoord(x);
			y = tileCoordRelativeToSliceFromTileCoord(y);
			
			return slice.getTileEntityAt(x, y);
		}
		
		return null;
	}

	@Override
	public void setTileEntityAt(int x, int y, TileEntity t) {
		Slice slice = getSliceAtTile(x, y);
			
		if(slice != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			TileEntity t2 = slice.getTileEntityAt(tileX, tileY);
			if(t2 != null) {
				t2.handleRemove(this, x, y);
				removeTileEntity(t2);
			}
			
			slice.setTileEntityAt(tileX, tileY, t);
			
			addTileEntity(t);
		}
	}
	
	@Override
	public void removeTileEntityAt(int x, int y) {
		Slice slice = getSliceAtTile(x, y);
		
		if(slice != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			TileEntity t2 = slice.getTileEntityAt(tileX, tileY);
			if(t2 != null) {
				t2.handleRemove(this, x, y);
				removeTileEntity(t2);
			}
			
			slice.setTileEntityAt(tileX, tileY, null);
		}
	}
	
	@Override
	public void blowUpTile(int x, int y, float explosionPower) {
		Slice slice = getSliceAtTile(x, y);
		
		if(slice != null) {
			int tileX = tileCoordRelativeToSliceFromTileCoord(x);
			int tileY = tileCoordRelativeToSliceFromTileCoord(y);
			
			if(slice.getTileAt(tileX, tileY).getHardness() < explosionPower) {
				slice.getTileAt(tileX, tileY).handleRemove(this, x, y);
				
				slice.setTileAt(tileX, tileY, 0);
				
				//Tile.air.handlePlace(this, x, y);
			}
		}
	}
	
	@Override
	public FileHandle getDir() {
		return IWorld.getWorldDir(info.fileSystemName);
	}
	
	/**
	 * Saves the world.
	 */
	public void save() {
		log.postInfo("Saving world...");
		
		try {
			info.save();
		} catch(IOException e) {
			log.postSevere("Could not save world info!");
		}
		
		savePlayers();
		
		for(Region r : regions.values())
			saveRegion(r);
	}
	
	/**
	 * Saves the player data.
	 */
	private void savePlayers() {
		// Simple implementation for just one player
		playerChar.lastX = player.x;
		playerChar.lastY = player.y;
		//----saveCharacterData(playerChar);
	}
	
	/**
	 * Closes the world by saving it as per an invocation of {@link #save()},
	 * and then shutting down the world loader and generator. The current
	 * thread will block until the world has been completely saved.
	 */
	public void close() {
		save();
		
		loader.shutdown();
		generator.shutdown();
		
		// This is done due to the fact that despite save() being invoked
		// before executor.shutdown(), due to the nature of the Executor being
		// used, tasks submitted before shutdown() is invoked may still be
		// rejected. This ensures that all regions manage to save.
		while(!regionSavesDone()) {
			try {
				Thread.sleep(0L);
			} catch(InterruptedException ignored) {}
		}
		
		config.executor.shutdown();
		try {
			if(!config.executor.awaitTermination(10, TimeUnit.SECONDS))
				log.postWarning("The world's worker threads took more than 10 seconds to shut down!");
		} catch(InterruptedException e) {
			log.postWarning("Interrupted while waiting for the worker threads to finish!", e);
		}
	}
	
	/**
	 * Checks for whether or not all region save operations have completed.
	 * 
	 * @return {@code true} if all region save operations have completed;
	 * {@code false} otherwise.
	 */
	private boolean regionSavesDone() {
		for(Region r : regions.values()) {
			if(r.pendingSave || r.saving)
				return false;
		}
		return true;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Creates a new GameWorld as per
	 * {@link #GameWorld(WorldInfo) new GameWorld(info)}, where {@code info} is
	 * the WorldInfo object returned as if by
	 * {@link WorldInfo#loadInfo(String) WorldInfo.loadInfo(worldName)}. If you
	 * already have access to a world's WorldInfo object, it is preferable to
	 * construct the GameWorld directly.
	 * 
	 * @param worldName The name of the world on the file system.
	 * 
	 * @return The GameWorld instance, or {@code null} if the world info could
	 * not be loaded.
	 */
	public static GameWorld loadWorld(String worldName) {
		WorldInfo info = WorldInfo.loadInfo(worldName);
		
		if(info != null)
			return new GameWorld(info);
		
		Log.get().postSevere("Could not load info file of world \"" + worldName + "\" during world loading!");
		return null;
	}

	@Override
	public boolean isLoaded() {
		// TODO Auto-generated method stub
		return false;
	}

}
