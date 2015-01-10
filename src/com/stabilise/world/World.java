package com.stabilise.world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.stabilise.core.Resources;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world.
 */
public interface World {
	
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
	 * Updates the world by executing a single tick of game logic. In general,
	 * all GameObjects in the world will be updated (i.e. entities, hitboxes,
	 * tile entities, etc).
	 */
	@UserThread("MainThread")
	void update();
	
	/**
	 * Sets a mob as a player. The mob will be treated as if the player is
	 * controlling it thereafter.
	 */
	void setPlayer(EntityMob m);
	
	/**
	 * Removes the status of player from a mob. The mob will no longer be
	 * treated as if controlled by a player thereafter.
	 */
	void unsetPlayer(EntityMob m);
	
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
	void addEntity(Entity e, double x, double y);
	
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
	void addEntity(Entity e);
	
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
	void removeEntity(Entity e);
	
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
	void removeEntity(int id);
	
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
	void addHitbox(Hitbox h, double x, double y);
	
	/**
	 * Adds a hitbox to the world. The hitbox's ID is assigned automatically.
	 * 
	 * @param h The hitbox.
	 */
	void addHitbox(Hitbox h);
	
	/**
	 * Adds a particle to the world.
	 * 
	 * @param p The particle.
	 * @param x The x-coordinate at which to place the particle, in
	 * tile-lengths.
	 * @param y The y-coordinate at which to place the particle, in
	 * tile-lengths.
	 */
	void addParticle(Particle p, double x, double y);
	
	/**
	 * Adds a particle to the world.
	 */
	void addParticle(Particle p);
	
	/**
	 * Removes a particle from the world.
	 */
	void removeParticle(Particle p);
	
	// ==========Collection getters==========
	
	/**
	 * @return The collection of all players in the world. Note that a player
	 * is also treated as an entity and as such every element in the returned
	 * collection is also a member of the one returned by {@link
	 * #getEntityIterator()}.
	 */
	Collection<EntityMob> getPlayers();
	
	/**
	 * @return The collection of entities in the world.
	 */
	Collection<Entity> getEntities();
	
	/**
	 * @return The collection of hitboxes in the world.
	 */
	Collection<Hitbox> getHitboxes();
	
	/**
	 * @return The collection of tile entities in the world.
	 */
	Collection<TileEntity> getTileEntities();
	
	/**
	 * @return The collection of particles in the world, or {@code null} if
	 * this view of the world is one which does not include particles (i.e.
	 * this would be the case if this is a server's world, as particles are
	 * purely aesthetic and a server doesn't concern itself with them).
	 */
	abstract Collection<Particle> getParticles();
	
	// ==========World component getters and setters==========
	
	/**
	 * Gets the slice at the given coordinates.
	 * 
	 * @param x The slice's x-coordinate, in slice lengths.
	 * @param y The slice's y-coordinate, in slice lengths.
	 * 
	 * @return The slice at the given coordinates, or {@code null} if no such
	 * slice is loaded.
	 */
	Slice getSliceAt(int x, int y);
	
	/**
	 * Gets the slice at the given coordinates.
	 * 
	 * @param x The slice's x-coordinate, in tile lengths.
	 * @param y The slice's y-coordinate, in tile lengths.
	 * 
	 * @return The slice at the given coordinates, or {@code null} if no such
	 * slice is loaded.
	 */
	Slice getSliceAtTile(int x, int y);
	
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
	Tile getTileAt(double x, double y);
	
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
	Tile getTileAt(int x, int y);
	
	/**
	 * Sets a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param id The ID of the tile to set.
	 */
	void setTileAt(int x, int y, int id);
	
	/**
	 * Breaks a tile.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	void breakTileAt(int x, int y);
	
	/**
	 * Gets the tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The tile entity at the given coordinates, or {@code null} if no
	 * such tile entity is loaded.
	 */
	TileEntity getTileEntityAt(int x, int y);
	
	/**
	 * Sets a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param y The y-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param t The tile entity.
	 */
	void setTileEntityAt(int x, int y, TileEntity t);
	
	/**
	 * Removes a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 * @param y The y-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 */
	void removeTileEntityAt(int x, int y);
	
	/**
	 * Attempts to blow up a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param explosionPower The power of the explosion.
	 */
	void blowUpTile(int x, int y, float explosionPower);
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
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
