package com.stabilise.world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.stabilise.character.CharacterData;
import com.stabilise.core.Resources;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world.
 */
public interface IWorld {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The file name of the world info file. */
	public static final String FILE_INFO = "info";
	/** The name of the directory in which world regions are to be stored. */
	public static final String DIR_REGIONS = "regions/";
	/** The name of the directory in which data about individual players is to
	 * be stored. */
	public static final String DIR_PLAYERS = "players/";
	/** The file extension for player data files. */
	public static final String EXTENSION_PLAYERS = ".player";
	
	/** The maximum number of hostile mobs which may spawn. */
	public static final int HOSTILE_MOB_CAP = 100;
	
	/**
	 * Updates the world - that is, executes all game logic.
	 */
	@UserThread("MainThread")
	void update();
	
	/**
	 * Sets a mob as a player. The mob will be treated as if the player is
	 * controlling it thereafter.
	 */
	public void setPlayer(EntityMob m);
	
	/**
	 * Removes the status of player from a mob. The mob will no longer be
	 * treated as if controlled by a player thereafter.
	 */
	public void unsetPlayer(EntityMob m);
	
	/**
	 * Adds an entity to the world. The entity's ID is assigned automatically.
	 * 
	 * <p>The entity is not added to the map of entities immediately; rather,
	 * it is added at the end of the current tick. This is intended as to
	 * prevent a {@code ConcurrentModificationException} from being thrown if
	 * the entity is added while the map of entities is being iterated over.
	 * 
	 * @param e The entity.
	 * @param x The x-coordinate at which to place the entity, in tile-lengths.
	 * @param y The y-coordinate at which to place the entity, in tile-lengths.
	 */
	public void addEntity(Entity e, double x, double y);
	
	/**
	 * Adds an entity to the world. The entity's ID is assigned automatically.
	 * 
	 * <p>The entity is not added to the map of entities immediately; rather,
	 * it is added at the end of the current tick. This is intended as to
	 * prevent a {@code ConcurrentModificationException} from being thrown if
	 * the entity is added while the map of entities is being iterated over.
	 * 
	 * <p>Though the entity is not immediately added to the world, {@link
	 * Entity#onAdd() onAdd()} is invoked on {@code e}.
	 */
	public void addEntity(Entity e);
	
	/**
	 * Removes an entity from the world.
	 * 
	 * <p>The entity is not removed from the map of entities immediately;
	 * rather, it is removed at the end of the current tick. This is intended
	 * as to prevent a {@code ConcurrentModificationException} from being
	 * thrown if the entity is removed while the map of entities is being
	 * iterated over.
	 * 
	 * @param e The entity.
	 */
	public void removeEntity(Entity e);
	
	/**
	 * Removes an entity from the world.
	 * 
	 * <p>The entity is not removed from the map of entities immediately;
	 * rather, it is removed at the end of the current tick. This is intended
	 * as to prevent a {@code ConcurrentModificationException} from being
	 * thrown if the entity is removed while the map of entities is being
	 * iterated over.
	 * 
	 * @param id The ID of the entity.
	 */
	public void removeEntity(int id);
	
	/**
	 * Adds a hitbox to the world. The hitbox's ID is assigned automatically.
	 * 
	 * <p>The hitbox is not added to the map of hitboxes immediately; rather,
	 * it is added mid tick; after the entities have been updated, but before
	 * the hitboxes have been updated. This is intended as to prevent a
	 * {@code ConcurrentModificationException} from being thrown if the hitbox
	 * is added while the map of hitboxes is being iterated over.
	 * 
	 * @param h The hitbox.
	 * @param x The x-coordinate at which to place the hitbox, in tile-lengths.
	 * @param y The y-coordinate at which to place the hitbox, in tile-lengths.
	 */
	public void addHitbox(Hitbox h, double x, double y);
	
	/**
	 * Adds a hitbox to the world. The hitbox's ID is assigned automatically.
	 * 
	 * @param h The hitbox.
	 */
	public void addHitbox(Hitbox h);
	
	/**
	 * Adds a particle to the world. The particle's ID is assigned
	 * automatically.
	 * 
	 * @param p The particle.
	 * @param x The x-coordinate at which to place the particle, in
	 * tile-lengths.
	 * @param y The y-coordinate at which to place the particle, in
	 * tile-lengths.
	 */
	public void addParticle(Particle p, double x, double y);
	
	/**
	 * Adds a particle to the world. The particle's ID is assigned
	 * automatically.
	 * 
	 * @param p The particle.
	 */
	public void addParticle(Particle p);
	
	/**
	 * Removes a particle from the world.
	 * 
	 * @param p The particle.
	 */
	public void removeParticle(Particle p);
	
	/**
	 * Removes a particle from the world.
	 * 
	 * @param id The ID of the particle.
	 */
	public void removeParticle(int id);
	
	// ==========Collection getters==========
	
	/**
	 * @return The collection of all players in the world. Note that a player
	 * is also treated as an entity and as such every element in the returned
	 * collection is also a member of the one returned by {@link
	 * #getEntityIterator()}.
	 */
	public Collection<EntityMob> getPlayers();
	
	/**
	 * @return The collection of entities in the world.
	 */
	public Collection<Entity> getEntities();
	
	/**
	 * @return The collection of hitboxes in the world.
	 */
	public Collection<Hitbox> getHitboxes();
	
	/**
	 * @return The collection of tile entities in the world.
	 */
	public Collection<TileEntity> getTileEntities();
	
	/**
	 * @return The collection of particles in the world, or {@code null} if
	 * this view of the world is one which does not include particles (i.e.
	 * this would be the case if this is a server's world, as particles are
	 * purely aesthetic and a server doesn't concern itself with them).
	 */
	public abstract Collection<Particle> getParticles();
	
	/**
	 * Adds a player to the world.
	 * 
	 * @param player The data for the player.
	 */
	public abstract void addPlayer(CharacterData player);
	
	// ==========World component getters==========
	
	/**
	 * Gets the slice at the given coordinates.
	 * 
	 * @param x The slice's x-coordinate, in slice lengths.
	 * @param y The slice's y-coordinate, in slice lengths.
	 * 
	 * @return The slice at the given coordinates, or {@code null} if no such
	 * slice is loaded.
	 */
	public Slice getSliceAt(int x, int y);
	
	/**
	 * Gets the slice at the given coordinates.
	 * 
	 * @param x The slice's x-coordinate, in tile lengths.
	 * @param y The slice's y-coordinate, in tile lengths.
	 * 
	 * @return The slice at the given coordinates, or {@code null} if no such
	 * slice is loaded.
	 */
	public Slice getSliceAtTile(int x, int y);
	
	/**
	 * Gets a tile at the given coordinates. Fractional coordinates are rounded
	 * down.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The tile at the given coordinates, or the
	 * {@link com.stabilise.world.tile.Tile#invisibleBedrock invisibleBedrock}
	 * tile if no such tile is loaded.
	 */
	default public Tile getTileAt(double x, double y) {
		return getTileAt(MathsUtil.floor(x), MathsUtil.floor(y));
	}
	
	/**
	 * Gets a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The tile at the given coordinates, or the
	 * {@link com.stabilise.world.tile.Tiles#BEDROCK_INVISIBLE invisible
	 * bedrock} tile if no such tile is loaded.
	 */
	public Tile getTileAt(int x, int y);
	
	/**
	 * Sets a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param id The ID of the tile to set.
	 */
	public void setTileAt(int x, int y, int id);
	
	/**
	 * Breaks a tile.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void breakTileAt(int x, int y);
	
	/**
	 * Gets the tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The tile entity at the given coordinates, or {@code null} if no
	 * such tile entity is loaded.
	 */
	public TileEntity getTileEntityAt(int x, int y);
	
	/**
	 * Sets a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param y The y-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param t The tile entity.
	 */
	public void setTileEntityAt(int x, int y, TileEntity t);
	
	/**
	 * Removes a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 * @param y The y-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 */
	public void removeTileEntityAt(int x, int y);
	
	/**
	 * Attempts to blow up a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param explosionPower The power of the explosion.
	 */
	public void blowUpTile(int x, int y, float explosionPower);
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the coordinate of the region at the given tile coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the region occupying the given coordinate, in
	 * region-lengths.
	 */
	public static int regionCoordFromTileCoord(int c) {
		//return MathsUtil.fastFloor((float)c / Region.REGION_SIZE_IN_TILES)
		//return c < 0 ?								// Faster
		//		(c+1) / Region.REGION_SIZE_IN_TILES - 1 :
		//		c / Region.REGION_SIZE_IN_TILES;
		return c >> Region.REGION_SIZE_IN_TILES_SHIFT;	// Even faster
	}
	
	/**
	 * Gets the coordinate of the region at the given absolute slice
	 * coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in slice-lengths.
	 * 
	 * @return The coordinate of the region occupying the given coordinate, in
	 * region-lengths.
	 */
	public static int regionCoordFromSliceCoord(int c) {
		//return MathsUtil.fastFloor((float)c / Region.REGION_SIZE)
		//return c < 0 ?								// Faster
		//		(c+1) / Region.REGION_SIZE - 1 :
		//		c / Region.REGION_SIZE;
		return c >> Region.REGION_SIZE_SHIFT;			// Even faster
	}
	
	/**
	 * Gets the coordinate of the slice at the given coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the slice occupying the given coordinate, in
	 * slice-lengths.
	 */
	public static int sliceCoordFromTileCoord(int c) {
		//return MathsUtil.fastFloor((float)c / Slice.SLICE_SIZE)
		//return c < 0 ?							// Faster
		//		(c+1) / Slice.SLICE_SIZE - 1 :
		//		c / Slice.SLICE_SIZE;
		return c >> Slice.SLICE_SIZE_SHIFT;			// Even faster
	}
	
	/**
	 * Gets the coordinate of the slice at the given coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the slice occupying the given coordinate, in
	 * slice-lengths.
	 */
	public static int sliceCoordFromTileCoord(double c) {
		// TODO: Is there a way to make a faster alternative for floating-point
		// input?
		return MathsUtil.floor(c / Slice.SLICE_SIZE);
	}
	
	/**
	 * Gets the coordinate of the slice at the start of a region at the given
	 * coordinate, in slice-lengths.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in region-lengths.
	 * 
	 * @return The coordinate of the slice at the start of the region, in
	 * slice-lengths.
	 */
	public static int sliceCoordFromRegionCoord(int c) {
		return c * Region.REGION_SIZE;
	}
	
	/**
	 * Gets the coordinate of the slice, relative to its parent region, at the
	 * given coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the slice occupying the given coordinate, in
	 * slice-lengths, relative to its parent region.
	 */
	public static int sliceCoordRelativeToRegionFromTileCoord(int c) {
		//c = sliceCoordFromTileCoord(c);
		//return MathsUtil.wrappedRem(c, Region.REGION_SIZE);
		//return MathsUtil.wrappedRem2(c, Region.REGION_SIZE);				// Way faster
		
		// i.e. (c >> Slice.SLICE_SIZE_SHIFT) & Region.REGION_SIZE_MINUS_ONE
		return sliceCoordFromTileCoord(c) & Region.REGION_SIZE_MINUS_ONE;	// One less instruction
	}
	
	/**
	 * Gets the coordinate of the slice, relative to its parent region, at the
	 * given coordinate. That is, converts the given slice coordinate to local
	 * region space.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in slice-lengths.
	 * 
	 * @return The coordinate of the slice, in slice-lengths, relative to its
	 * parent region.
	 */
	public static int sliceCoordRelativeToRegionFromSliceCoord(int c) {
		//return MathsUtil.wrappedRem(c, Region.REGION_SIZE);
		//return MathsUtil.wrappedRem2(c, Region.REGION_SIZE);		// Way faster
		return c & Region.REGION_SIZE_MINUS_ONE;					// One less instruction
	}
	
	/**
	 * Gets the coordinate of the start of a slice at the given coordinate, in
	 * tile-lengths.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * <p>Also note that this method also returns the starting tile of a slice
	 * relative to a region, provided the {@code c} parameter given is that of
	 * the slice's coordinate relative to the region.
	 * 
	 * @param c The coordinate, in slice-lengths.
	 * 
	 * @return The coordinate of the start of the slice, in tile-lengths.
	 */
	public static int tileCoordFromSliceCoord(int c) {
		return c * Slice.SLICE_SIZE;
	}
	
	/**
	 * Gets the coordinate of the start of a region at the given coordinate,in
	 * tile-lengths.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in region-lengths.
	 * 
	 * @return The coordinate of the start of the region, in tile-lengths.
	 */
	public static int tileCoordFromRegionCoord(int c) {
		return c * Region.REGION_SIZE_IN_TILES;
	}
	
	/**
	 * Gets the coordinate of the tile, relative to its parent slice, at the
	 * given coordinate. That is, converts the given tile coordinate to local
	 * slice space.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the tile, in tile-lengths, relative to its
	 * parent slice.
	 */
	public static int tileCoordRelativeToSliceFromTileCoord(int c) {
		//return MathsUtil.wrappedRem(c, Slice.SLICE_SIZE);
		//return MathsUtil.wrappedRem2(c, Slice.SLICE_SIZE);		// Way faster
		return c & Slice.SLICE_SIZE_MINUS_ONE;						// One less instruction
	}
	
	/**
	 * Gets the coordinate of the tile, relative to its parent region, at the
	 * given coordinate. That is, converts the given tile coordinate to local
	 * region space.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the tile, in tile-lengths, relative to its
	 * parent region.
	 */
	public static int tileCoordRelativeToRegionFromTileCoord(int c) {
		//return MathsUtil.wrappedRem(c, Region.REGION_SIZE_IN_TILES);
		//return MathsUtil.wrappedRem2(c, Region.REGION_SIZE_IN_TILES);		// Way faster
		return c & Region.REGION_SIZE_IN_TILES_MINUS_ONE;					// One less instruction
	}
	
	/**
	 * Creates a new world with a random seed.
	 * 
	 * <p>Note that this does NOT check for whether or not a world by the same
	 * name already exists. Such a check should be performed earlier.
	 * 
	 * @param worldName The world's name.
	 * 
	 * @return The WorldInfo object for the created world, or {@code null} if
	 * the world could not be created.
	 */
	public static WorldInfo createWorld(String worldName) {
		return createWorld(worldName, new Random().nextLong());
	}
	
	/**
	 * Creates a new world.
	 * 
	 * <p>Note that this does NOT check for whether or not a world by the same
	 * name already exists. Such a check should be performed earlier.
	 * 
	 * @param worldName The world's name.
	 * @param worldSeed The world's seed.
	 * 
	 * @return The WorldInfo object for the created world, or {@code null} if
	 * the world could not be created.
	 */
	public static WorldInfo createWorld(String worldName, long worldSeed) {
		// Handles the delegation of duplicate world names
		String originalWorldName = worldName;
		int iteration = 0;
		while(getWorldDir(worldName).exists()) {
			iteration++;
			worldName = originalWorldName + " - " + iteration;
		}
		
		IOUtil.createDirQuietly(Resources.WORLDS_DIR);
		
		WorldInfo info = new WorldInfo(worldName);
		
		info.name = originalWorldName;
		info.age = 0;
		info.seed = worldSeed;
		info.spawnSliceX = 0;					// TODO: temporary value
		info.spawnSliceY = 0;					// TODO: temporary value
		info.flatland = info.seed < 0;
		info.worldFormatVersion = -1;			// TODO: temporary value
		info.sliceFormatVersion = -1;			// TODO: temporary value
		info.creationDate = System.currentTimeMillis();//new Date().getTime();
		info.lastPlayedDate = info.creationDate;
		
		// Set the player spawn. TODO: Possibly temporary
		//WorldGenerator generator = WorldGenerator.getGenerator(null, info);
		//generator.setPlayerSpawn(info);
		
		try {
			info.save();
		} catch(IOException e) {
			Log.get().postSevere("Could not save world info during creation process!", e);
			return null;
		}
		
		return info;
	}
	
	/**
	 * Gets a world's directory, given its name.
	 * 
	 * @param worldName The world's name, on disk.
	 * 
	 * @return The file representing the world's folder.
	 * @throws IllegalArgumentException Thrown if the given string is empty or
	 * {@code null}.
	 */
	public static File getWorldDir(String worldName) {
		if(worldName == null || worldName == "")
			throw new IllegalArgumentException("The world name must not be null or empty!");
		return new File(Resources.WORLDS_DIR, IOUtil.getLegalString(worldName) + "/");
	}
	
	/**
	 * Gets the list of created worlds.
	 * 
	 * @return An array of created worlds.
	 */
	public static WorldInfo[] getWorldsList() {
		IOUtil.createDirQuietly(Resources.WORLDS_DIR);
		File[] worldDirs = Resources.WORLDS_DIR.listFiles();
		
		// Initially store as an ArrayList because of its dynamic length
		List<WorldInfo> worlds = new ArrayList<WorldInfo>();
		
		int validWorlds = 0;		// The number of valid worlds (all worlds in the worldDirs might not be valid)
		
		// Cycle over all the folders in the worlds directory and determine their
		// validity as worlds.
		for(int i = 0; i < worldDirs.length; i++) {
			worlds.add(validWorlds, new WorldInfo(worldDirs[i].getName()));
			try {
				worlds.get(validWorlds).load();
			} catch(IOException e) {
				Log.get().postWarning("Could not load world info for world \"" + worldDirs[i].getName() + "\"!", e);
				worlds.remove(validWorlds);
				continue;
			}
			validWorlds++;
		}
		
		// Now, we convert the ArrayList to a conventional array
		WorldInfo[] worldArr = worlds.toArray(new WorldInfo[0]);
		
		// Sort the worlds - uses Java's Comparable interface
		Arrays.sort(worldArr);
		
		return worldArr;
	}
	
	/**
	 * Deletes a world. All world files will be removed permanently from the
	 * file system.
	 * 
	 * @param worldName The name of the world, on the file system.
	 */
	public static void deleteWorld(String worldName) {
		if(worldName == null || worldName == "")
			throw new IllegalArgumentException("The world name must not be null or empty!");
		
		File world = new File(Resources.WORLDS_DIR, IOUtil.getLegalString(worldName) + "/");
		
		try {
			FileUtils.deleteDirectory(world);
		} catch(IOException e) {
			Log.get().postSevere("Could not delete world \"" + worldName + "\"!", e);
		}
	}
	
}
