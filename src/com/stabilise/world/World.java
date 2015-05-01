package com.stabilise.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.core.GameClient;
import com.stabilise.core.Resources;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.Checkable;
import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.concurrent.TaskTracker;
import com.stabilise.util.concurrent.TrackableFuture;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.AbstractWorld.ParticleManager;
import com.stabilise.world.provider.HostProvider;
import com.stabilise.world.provider.HostProvider.PlayerBundle;
import com.stabilise.world.provider.HostProvider.PlayerData;
import com.stabilise.world.provider.WorldProvider;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * Defines methods which summarise a world implementation.
 */
public interface World extends Checkable {
	
	/** The file name of the world info file. */
	public static final String FILE_INFO = "info";
	/** The name of the directory relative to the world dir in which dimension
	 * data is stored. */
	public static final String DIR_DIMENSIONS = "dimensions/";
	/** The name of the directory in which data about individual players is to
	 * be stored. */
	public static final String DIR_PLAYERS = "players/";
	/** The file extension for player data files. */
	public static final String EXT_PLAYERS = ".player";
	
	/** The maximum number of hostile mobs which may spawn.
	 * <p>TODO: Arbitrary, and probably temporary. */
	public static final int HOSTILE_MOB_CAP = 100;
	
	
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
	default void addEntity(Entity e, double x, double y) {
		e.x = x;
		e.y = y;
		addEntity(e);
	}
	
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
	 * Gets the entity with the specified ID.
	 * 
	 * @return The entity with the specified ID, or {@code null} if there is no
	 * such entity in the world.
	 */
	Entity getEntity(int id);
	
	/**
	 * Removes an entity from the world.
	 * 
	 * <p>The entity is not removed from the map of entities immediately;
	 * rather, it is removed at the end of the current tick.
	 * 
	 * <p>Note that it is normally preferable to invoke {@link Entity#destroy()
	 * destroy()} on an entity to remove it from the world.
	 * 
	 * @param e The entity.
	 * 
	 * @throws NullPointerException if {@code e} is {@code null}.
	 */
	default void removeEntity(Entity e) {
		removeEntity(e.id);
	}
	
	/**
	 * Removes an entity from the world.
	 * 
	 * <p>The entity is not removed from the map of entities immediately;
	 * rather, it is removed at the end of the current tick.
	 * 
	 * <p>Note that it is normally preferable to invoke {@link Entity#destroy()
	 * destroy()} on an entity to remove it from the world.
	 * 
	 * @param id The ID of the entity.
	 */
	void removeEntity(int id);
	
	/**
	 * Adds a hitbox to the world. The hitbox's ID is assigned automatically.
	 * 
	 * @param h The hitbox.
	 * @param x The x-coordinate at which to place the hitbox, in tile-lengths.
	 * @param y The y-coordinate at which to place the hitbox, in tile-lengths.
	 * 
	 * @throws NullPointerException if {@code h} is {@code null}.
	 */
	default void addHitbox(Hitbox h, double x, double y) {
		h.x = x;
		h.y = y;
		addHitbox(h);
	}
	
	/**
	 * Adds a hitbox to the world. The hitbox's ID is assigned automatically.
	 * 
	 * @param h The hitbox.
	 * 
	 * @throws NullPointerException if {@code h} is {@code null}.
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
	 * 
	 * @throws NullPointerException if {@code p} is {@code null}.
	 */
	default void addParticle(Particle p, double x, double y) {
		p.x = x;
		p.y = y;
		addParticle(p);
	}
	
	/**
	 * Adds a particle to the world.
	 * 
	 * @throws NullPointerException if {@code p} is {@code null}.
	 */
	void addParticle(Particle p);
	
	// ==========Collection getters==========
	
	/**
	 * @return The collection of all players in the world. Note that as a
	 * player is an entity, every element in the returned collection is also
	 * a member of the one returned by {@link #getEntities()}.
	 */
	Iterable<EntityMob> getPlayers();
	
	/**
	 * @return The collection of entities in the world.
	 */
	Iterable<Entity> getEntities();
	
	/**
	 * @return The collection of hitboxes in the world.
	 */
	Iterable<Hitbox> getHitboxes();
	
	/**
	 * @return The collection of tile entities in the world.
	 */
	Iterable<TileEntity> getTileEntities();
	
	/**
	 * @return The collection of particles in the world, or {@code null} if
	 * this view of the world is one which does not include particles (i.e.
	 * this would be the case if this is a server's world, as particles are
	 * purely aesthetic and a server doesn't concern itself with them).
	 */
	Iterable<Particle> getParticles();
	
	/**
	 * Gets this world's particle manager.
	 */
	ParticleManager getParticleManager();
	
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
	default Slice getSliceAtTile(int x, int y) {
		// This should be optimised for worlds which deal with regions
		return getSliceAt(
				sliceCoordFromTileCoord(x),
				sliceCoordFromTileCoord(y));
	}
	
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
	default Tile getTileAt(double x, double y) {
		return getTileAt(
				tileCoordFreeToTileCoordFixed(x),
				tileCoordFreeToTileCoordFixed(y));
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
	default Tile getTileAt(int x, int y) {
		Slice s = getSliceAtTile(x, y);
		if(s == null)
			return Tiles.BEDROCK_INVISIBLE;
		else
			return s.getTileAt(
					tileCoordRelativeToSliceFromTileCoord(x),
					tileCoordRelativeToSliceFromTileCoord(y)
			);
	}
	
	/**
	 * Sets the tile at the specified coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param tile The tile to set.
	 * 
	 * @throws NullPointerException if {@code tile} is {@code null}.
	 */
	default void setTileAt(int x, int y, Tile tile) {
		setTileAt(x, y, tile.getID());
	}
	
	/**
	 * Sets a tile at the specified coordinates.
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
	default TileEntity getTileEntityAt(int x, int y) {
		Slice s = getSliceAtTile(x, y);
		if(s == null)
			return null;
		else
			return s.getTileEntityAt(
					tileCoordRelativeToSliceFromTileCoord(x),
					tileCoordRelativeToSliceFromTileCoord(y)
			);
	}
	
	/**
	 * Sets a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param y The y-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param t The tile entity. Setting this to {@code null} will remove the
	 * tile entity at the specified location, if it exists.
	 */
	void setTileEntityAt(int x, int y, TileEntity t);
	
	/**
	 * Removes a tile entity at the given coordinates. Invoking this method is
	 * equivalent to invoking {@link #setTileEntityAt(int, int, TileEntity)}
	 * with a {@code null} parameter.
	 * 
	 * @param x The x-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 * @param y The y-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 */
	default void removeTileEntityAt(int x, int y) {
		setTileEntityAt(x, y, null);
	}
	
	/**
	 * Adds a tile entity to the "update list" of tile entities, so that it may
	 * be updated as per {@link TileEntity#updateAndCheck(World)} every tick.
	 * The supplied tile entity will only be added if {@link
	 * TileEntity#requiresUpdates()} returns {@code true}. To remove a tile
	 * entity from the update list, either {@link TileEntity#destroy() destroy}
	 * it, or invoke {@link #removeTileEntity(TileEntity)}.
	 * 
	 * <p>Note that if the supplied tile entity is already on the update list,
	 * it will be added again, and hence updated multiple times per tick!
	 * 
	 * @param t The tile entity.
	 * 
	 * @throws NullPointerException if {@code t} is {@code null}.
	 */
	void addTileEntity(TileEntity t);
	
	/**
	 * Removes a tile entity from the "update list" of tile entities. It will
	 * no longer be updated.
	 * 
	 * <p>If the tile entity is not present in the list of tile entities, this
	 * method does nothing asides from invoking {@link TileEntity#destroy()}.
	 * 
	 * <p>A technical point: {@code t} is not removed from the update list
	 * immediately; rather, it is removed by {@link Iterator#remove()} while
	 * iterating over the update list iff {@link
	 * TileEntity#updateAndCheck(World)} returns {@code true} (which it should,
	 * as this method invokes {@code t.destroy()}).
	 * 
	 * @throws NullPointerException if {@code t} is {@code null}.
	 */
	default void removeTileEntity(TileEntity t) {
		// Since it is expensive to directly remove an object from a
		// LinkedList, simply set its destroyed flag to true and have it remove
		// itself upon the next iteration.
		if(t.requiresUpdates()) // not actually a necessary check
			t.destroy();
	}
	
	/**
	 * Attempts to blow up a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param explosionPower The power of the explosion.
	 */
	void blowUpTile(int x, int y, float explosionPower);
	
	// ========== Dimensional stuff ==========
	
	/**
	 * Sends an entity to the specified dimension.
	 * 
	 * @param dimension The name of the dimension to which to send the entity.
	 * @param e The entity.
	 * @param x The x-coordinate at which to place the entity, in tile-lengths.
	 * @param y The y-coordinate at which to place the entity, in tile-lengths.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	void sendToDimension(String dimension, Entity e, double x, double y);
	
	// ========== Time delta stuff ==========
	
	/**
	 * Returns the gravity of the world, in ts<sup><font size=-1>-2</font>
	 * </sup> (tiles per second squared).
	 */
	float getGravity();
	
	/**
	 * Returns the gravity increment per update tick.
	 * 
	 * @return {@code gt}, where {@code g == }{@link #getGravity()} and {@code
	 * t == }{@link #getTimeIncrement()}.
	 */
	float getGravityIncrement();
	
	/**
	 * Returns the 2<sup><font size=-1>nd</font></sup>-order value for gravity
	 * with respect to time. This should be added to every non-grounded
	 * entity's y-coordinate each tick.
	 * 
	 * @return <tt>gt<sup><font size=-1>2</font></sup>/2</tt>, where {@code g
	 * == }{@link #getGravity()} and {@code t == }{@link #getTimeIncrement()}.
	 */
	float getGravity2ndOrder();
	
	/**
	 * Sets the world's time delta, where a value of {@code 1} is considered
	 * normal.
	 * 
	 * <p>For example, passing {@code 2} to this method will in general cause
	 * the world to update twice as quickly, and passing {@code 0.5} will cause
	 * everything to slow down to half as quickly.
	 * 
	 * @throws IllegalArgumentException if {@code delta <= 0}.
	 */
	void setTimeDelta(float delta);
	
	/**
	 * @return The world's time delta.
	 */
	float getTimeDelta();
	
	/**
	 * @return The time increment of each update tick, in seconds.
	 */
	float getTimeIncrement();
	
	/**
	 * @return The age of this world, in ticks.
	 */
	long getAge();
	
	// ========== Utility Methods ==========
	
	/**
	 * @return A {@code Random} instance held by this World.
	 */
	Random getRnd();
	
	/**
	 * @return {@code true} if this is a {@code HostWorld}; {@code false}
	 * otherwise.
	 */
	default boolean isHost() {
		return this instanceof HostWorld;
	}
	
	/**
	 * Returns {@code true} if this {@code World} holds a client view. This is
	 * {@code true} for all cases but for a server world without an integrated
	 * client.
	 */
	boolean isClient();
	
	/**
	 * Returns {@code true} if this world has particles; that is, if this is a
	 * client world.
	 */
	//boolean hasParticles();
	
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
		return c >> Region.REGION_SIZE_IN_TILES_SHIFT;
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
		return c >> Region.REGION_SIZE_SHIFT;
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
		return c >> Slice.SLICE_SIZE_SHIFT;
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
		return Maths.floor(c / Slice.SLICE_SIZE);
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
		//return Maths.wrappedRem(c, Region.REGION_SIZE);
		//return Maths.wrappedRem2(c, Region.REGION_SIZE);				// Way faster
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
		//return Maths.wrappedRem(c, Region.REGION_SIZE);
		//return Maths.wrappedRem2(c, Region.REGION_SIZE);		// Way faster
		return c & Region.REGION_SIZE_MINUS_ONE;				// One less instruction
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
		//return Maths.wrappedRem(c, Slice.SLICE_SIZE);
		//return Maths.wrappedRem2(c, Slice.SLICE_SIZE);		// Way faster
		return c & Slice.SLICE_SIZE_MINUS_ONE;					// One less instruction
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
		//return Maths.wrappedRem(c, Region.REGION_SIZE_IN_TILES);
		//return Maths.wrappedRem2(c, Region.REGION_SIZE_IN_TILES);		// Way faster
		return c & Region.REGION_SIZE_IN_TILES_MINUS_ONE;				// One less instruction
	}
	
	/**
	 * Gets the coordinate of the tile which occupies the specified coordinate.
	 * This method essentially provides a means to 'snap' an x or y to the
	 * coordinate grid of the world.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the tile, in tile-lengths.
	 */
	public static int tileCoordFreeToTileCoordFixed(double c) {
		return Maths.floor(c);
	}
	
	// WORLD MANAGEMENT STUFF -------------------------------------------------
	
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
		
		WorldInfo info = new WorldInfo(worldName);
		
		info.name = originalWorldName;
		info.age = 0;
		info.seed = worldSeed;
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
	 * @param worldName The world's filesystem name.
	 * 
	 * @return The file representing the world's directory.
	 * @throws NullPointerException if {@code worldName} is {@code null}.
	 * @throws IllegalArgumentException if {@code worldName} is empty.
	 */
	public static FileHandle getWorldDir(String worldName) {
		if(worldName.length() == 0)
			throw new IllegalArgumentException("The world name must not be empty!");
		return Resources.WORLDS_DIR.child(IOUtil.getLegalString(worldName) + "/");
	}
	
	/**
	 * Gets the list of created worlds.
	 * 
	 * @return An array of created worlds.
	 */
	public static WorldInfo[] getWorldsList() {
		FileHandle[] worldDirs = Resources.WORLDS_DIR.list();
		
		List<WorldInfo> worlds = new ArrayList<>(worldDirs.length);
		
		// Cycle over all the folders in the worlds directory and determine
		// their validity as worlds.
		for(int i = 0; i < worldDirs.length; i++) {
			try {
				WorldInfo info = new WorldInfo(worldDirs[i].name());
				info.load(); // throws IOE
				worlds.add(info);
			} catch(IOException e) {
				Log.get().postWarning("Could not load world info for world \""
						+ worldDirs[i].name() + "\"!" + ": "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
				continue;
			}
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
	 * @param worldName The world's filesystem name.
	 * 
	 * @throws NullPointerException if {@code worldName} is {@code null}.
	 * @throws IllegalArgumentException if {@code worldName} is empty.
	 */
	public static void deleteWorld(String worldName) {
		getWorldDir(worldName).deleteDirectory();
	}
	
	/**
	 * Creates and returns a new {@link WorldBuilder} to use to construct a
	 * world.
	 */
	public static WorldBuilder builder() {
		return new WorldBuilder();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A WorldBuilder is a builder used to construct and set up {@link
	 * WorldProvider} objects in an easy, concise, and consistent manner.
	 */
	public static class WorldBuilder {
		
		/** The info of the world. Null if client-only. */
		private WorldInfo worldInfo = null;
		/** The data of the integrated player. Null if server only. */
		private CharacterData integratedPlayer = null;
		/** The GameClient through which to communicate to the host server.
		 * Null unless multiplayer client. */
		private GameClient client = null;
		/** Profiler to use for the world. May be null. */
		private Profiler profiler = null;
		
		private boolean building = false;
		private Thread builderThread;
		private WorldFuture task;
		
		private WorldBuilder() {}
		
		private void checkState() {
			if(building)
				throw new IllegalStateException("Already building or built!");
		}
		
		/**
		 * Sets the world. This will throw an exception if the client has
		 * already been set via {@link #setClient(GameClient)}, as a client
		 * does not know the info of the world it is visiting.
		 * 
		 * @param worldName The world's filesystem name.
		 * 
		 * @return This WorldBuilder.
		 * @throws NullPointerException if {@code worldName} is {@code null}.
		 * @throws IllegalStateException if this builder has already built the
		 * world provider, or the world has already been set, or the client has
		 * already been set.
		 */
		public WorldBuilder setWorld(String worldName) {
			return setWorld(new WorldInfo(Objects.requireNonNull(worldName)));
		}
		
		/**
		 * Sets the world. This will throw an exception if the client has
		 * already been set via {@link #setClient(GameClient)}, as a client
		 * does not know the info of the world it is visiting.
		 * 
		 * @param worldInfo The world's info. This may or may not have already
		 * been loaded.
		 * 
		 * @return This WorldBuilder.
		 * @throws NullPointerException if {@code worldInfo} is {@code null}.
		 * @throws IllegalStateException if this builder has already built the
		 * world provider, or the world has already been set, or the client has
		 * already been set.
		 */
		public WorldBuilder setWorld(WorldInfo worldInfo) {
			checkState();
			if(this.worldInfo != null)
				throw new IllegalStateException("World already set!");
			if(client != null)
				throw new IllegalStateException("Cannot set both client and world");
			this.worldInfo = Objects.requireNonNull(worldInfo);
			return this;
		}
		
		/**
		 * Sets the integrated player.
		 * 
		 * @param characterName The name of the character.
		 * 
		 * @return This WorldBuilder.
		 * @throws NullPointerException if {@code characterName} is {@code
		 * null}.
		 * @throws IllegalStateException if this builder has already built the
		 * world provider, or the integrated player has already been set.
		 */
		public WorldBuilder setPlayer(String characterName) {
			return setPlayer(new CharacterData(Objects.requireNonNull(characterName)));
		}
		
		/**
		 * Sets the integrated player.
		 * 
		 * @param character The character's data. This may or may not be
		 * already loaded.
		 * 
		 * @return This WorldBuilder.
		 * @throws NullPointerException if {@code character} is {@code null}.
		 * @throws IllegalStateException if this builder has already built the
		 * world provider, or the integrated player has already been set.
		 */
		public WorldBuilder setPlayer(CharacterData character) {
			checkState();
			if(integratedPlayer != null)
				throw new IllegalStateException("Player already set!");
			integratedPlayer = Objects.requireNonNull(character);
			return this;
		}
		
		/**
		 * Sets the client with which to communicate with the server that is
		 * hosting the world. This will throw an exception if the world has
		 * already been set via either {@link #setWorld(String)} or {@link
		 * #setWorld(WorldInfo)}, as a client does not know the info of the
		 * world it is visiting.
		 * 
		 * @param client The client.
		 * 
		 * @return This WorldBuilder.
		 * @throws NullPointerException if {@code client} is {@code null}.
		 * @throws IllegalStateException if this builder has already built the
		 * world provider, or the client has already been set, or the world
		 * has been set.
		 */
		public WorldBuilder setClient(GameClient client) {
			checkState();
			if(this.client != null)
				throw new IllegalStateException("Client already set!");
			if(worldInfo != null)
				throw new IllegalStateException("Cannot set both client and world"
						+ " info!");
			this.client = Objects.requireNonNull(client);
			return this;
		}
		
		/**
		 * Sets the profiler to use to profile each world.
		 * 
		 * @return This WorldBuilder.
		 * @throws NullPointerException if {@code profiler} is {@code null}.
		 * @throws IllegalStateException if this builder has already built the
		 * world provider, or the profiler has already been set.
		 */
		public WorldBuilder setProfiler(Profiler profiler) {
			checkState();
			if(this.profiler != null)
				throw new IllegalStateException("Profiler already set!");
			this.profiler = Objects.requireNonNull(profiler);
			return this;
		}
		
		/**
		 * Builds a HostProvider, and optionally, if an integrated player has
		 * been set, that player's PlayerData, entity, and initial world.
		 * 
		 * @throws IllegalStateException if the world has already been built,
		 * or the world has not been set.
		 */
		public TrackableFuture<WorldBundle> buildHost() {
			return build(true);
		}
		
		/**
		 * Builds a ClientProvider, and the integrated player's entity and
		 * initial world.
		 * 
		 * @throws IllegalStateException if the world has already been built,
		 * or either the player or client have not been set.
		 */
		public TrackableFuture<WorldBundle> buildClient() {
			return build(false);
		}
		
		private TrackableFuture<WorldBundle> build(final boolean buildHost) {
			checkState();
			building = true;
			
			// Make sure we've set the right parameters for the requested
			// build before we begin building.
			if(buildHost) {
				if(worldInfo == null)
					throw new IllegalStateException("Cannot create a host "
							+ "world without setting the world!");
			} else {
				if(client == null)
					throw new IllegalStateException("Cannot create a client "
							+ "world without setting the game client!");
				if(integratedPlayer == null)
					throw new IllegalStateException("Cannot create a client "
							+ "world without setting the player!");
			}
			
			
			final TaskTracker tracker = new TaskTracker("Loading", buildHost ? 4 : 5);
			
			Callable<WorldBundle> callable = new Callable<WorldBundle>() {
				@Override
				public WorldBundle call() throws Exception {
					tracker.next("Loading player data");
					if(integratedPlayer != null) // host or client
						integratedPlayer.load();
					tracker.next("Constructing world");
					
					if(buildHost) {
						worldInfo.load();
						HostProvider provider = new HostProvider(worldInfo, profiler);
						PlayerBundle player = provider.addPlayer(integratedPlayer, true);
						HostWorld starterDim = player.world;
						tracker.next("Loading dimension " + starterDim.getDimensionName());
						while(!starterDim.isLoaded()) // 
							Thread.sleep(10L);
						tracker.next("All is done!");
						return new WorldBundle(provider, starterDim,
								player.playerEntity, player.playerData);
					} else {
						return new WorldBundle(null, null, null, null);
					}
				}
			};
			
			task = new WorldFuture(callable, tracker);
			builderThread = new Thread(task);
			builderThread.setName("WorldBuilderThread");
			builderThread.run();
			return task;
		}
		
	}
	
	/**
	 * Implementation combining FutureTask and TrackableFuture.
	 */
	static class WorldFuture extends FutureTask<WorldBundle>
			implements TrackableFuture<WorldBundle> {
		
		private final TaskTracker tracker;
		
		public WorldFuture(Callable<WorldBundle> callable, TaskTracker tracker) {
			super(callable);
			this.tracker = tracker;
		}
		
		@Override
		public String getStatus() {
			return tracker.getStatus();
		}
		
		@Override
		public int parts() {
			return tracker.parts();
		}
		
		@Override
		public int partsCompleted() {
			return tracker.partsCompleted();
		}
		
		@Override
		public float percentComplete() {
			return tracker.percentComplete();
		}
		
		@Override
		public boolean completed() {
			return tracker.completed();
		}
		
		@Override
		public String toString() {
			return tracker.toString();
		}
		
		@Override
		public boolean stopped() {
			return tracker.stopped();
		}
		
		@Override
		public boolean failed() {
			return tracker.failed();
		}
		
		@Override
		public void waitUntilDone() throws InterruptedException {
			tracker.waitUntilDone();
		}
		
		@Override
		public void waitUninterruptibly() {
			tracker.waitUninterruptibly();
		}
		
	}
	
	/**
	 * Encapsulates the items which may be built and returned by a {@link
	 * World#builder() WorldBuilder}.
	 */
	public static class WorldBundle {
		
		/** The world provider. This should be cast to a {@link HostProvider}
		 * if a host was built, or a {@link ClientProvider} if a client was
		 * built. */
		public final WorldProvider<?> provider;
		/** The world in which the integrated player has been placed. This
		 * should be cast to a {@link HostWorld} if a host was built, and
		 * to a {@link ClientWorld} if a client was built. This is {@code null}
		 * if the world was built without setting an integrated player (i.e. if
		 * a server was constructed). */
		public final AbstractWorld world;
		/** The player entity. This is {@code null} if no player was
		 * specified. */
		public final EntityMob playerEntity;
		/** The world-specific player data. This is non-null iff a host was
		 * built with an integrated player specified. */
		public final PlayerData playerData;
		
		protected WorldBundle(WorldProvider<?> provider, AbstractWorld world,
				EntityMob playerEntity, PlayerData playerData) {
			this.provider = provider;
			this.world = world;
			this.playerEntity = playerEntity;
			this.playerData = playerData;
		}
	}
	
}
