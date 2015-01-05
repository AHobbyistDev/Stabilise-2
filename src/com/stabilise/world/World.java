package com.stabilise.world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.stabilise.character.CharacterData;
import com.stabilise.core.Application;
import com.stabilise.core.Resources;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.ClearOnIterateLinkedList;
import com.stabilise.util.maths.HashPoint;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The World class represents the game world.
 */
public abstract class World {
	
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
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** All players in the world. */
	protected Map<Integer, EntityMob> players = new HashMap<Integer, EntityMob>(1);
	/** The map of loaded entities in the world. This is a LinkedHashMap as to
	 * allow for consistent iteration. */
	protected Map<Integer, Entity> entities = new LinkedHashMap<Integer, Entity>(64);
	/** The total number of entities which have existed during the lifetime of
	 * the world. When a new entity is created its assigned ID is typically
	 * this value + 1, after which this is incremented.*/
	public int entityCount = 0;
	/** Entities queued to be added to the world at the end of the tick. This
	 * list clears automatically when it is iterated over. */
	private List<Entity> entitiesToAdd = new ClearOnIterateLinkedList<Entity>();
	/** The IDs of entities queued to be removed from the world at the end of
	 * the tick. This list clears automatically when it is iterated over.*/
	private List<Integer> entitiesToRemove = new ClearOnIterateLinkedList<Integer>();
	/** The number of hostile mobs in the world. */
	public int hostileMobCount = 0;
	
	/** The map of tile entities in the world. */
	protected Map<HashPoint, TileEntity> tileEntities = new HashMap<HashPoint, TileEntity>();
	
	/** All hitboxes. */
	protected LinkedHashMap<Integer, Hitbox> hitboxes = new LinkedHashMap<Integer, Hitbox>(20);
	/** The total number of hitboxes in the world. */
	public int hitboxCount = 0;
	
	/** Hitboxes queued to be added to the world. This list clears
	 * automatically when it is iterated over. */
	private List<Hitbox> hitboxesToAdd = new ClearOnIterateLinkedList<Hitbox>();
	/** Hitboxes queued to be removed from the world. */
	//private List<Integer> hitboxesToRemove = new LinkedList<Integer>();
	
	/** The x-coordinate the slice in which players initially spawn, in
	 * slice-lengths. */
	protected int spawnSliceX;
	/** The x-coordinate the slice in which players initially spawn, in
	 * slice-lengths. */
	protected int spawnSliceY;
	
	/** The gravity of the world. */
	public float gravity = -0.02f;
	
	/** An easy-access utility RNG which should be used by any GameObject with
	 * a reference to this world in preference to constructing a new one. */
	public Random rng = new Random();
	
	/** The profiler. */
	public Profiler profiler = Application.get().profiler;
	/** The world's logging agent. */
	public Log log;
	
	
	/**
	 * Creates a new World instance.
	 */
	public World() {
		// nothing to see here, move along
	}
	
	/**
	 * Updates the world - that is, executes all game logic.
	 */
	@UserThread("MainThread")
	public void update() {
		profiler.start("entity"); // "entity"
		{ // Update all entities
			Iterator<Entity> i = getEntities().iterator();
			while(i.hasNext())
				if(i.next().updateAndCheck())
					i.remove();
		}
		
		// Since it is expected that hitboxesToAdd, hitboxesToRemove, etc. will
		// be empty most of the time, it is faster to perform the size() != 0
		// check.
		
		profiler.next("hitbox"); // "hitbox"
		profiler.start("add"); // "hitbox.add"
		// Now, add and remove all queued hitboxes
		if(!hitboxesToAdd.isEmpty()) {
			for(Hitbox h : hitboxesToAdd)
				hitboxes.put(h.id, h);
		}
		
		profiler.next("update"); // "hitbox.update"
		
		{ // Now, update the hitboxes
			Iterator<Hitbox> i = getHitboxes().iterator();
			while(i.hasNext()) {
				Hitbox h = i.next();
				h.update();
				if(!h.persistent)
					i.remove();
			}
		}
		
		profiler.end(); // "hitbox"
		
		profiler.next("entity"); // "entity"
		profiler.start("add"); // "entity.add"
		// Now, add and remove all queued entities
		if(!entitiesToAdd.isEmpty()) {
			Iterator<Entity> i = entitiesToAdd.iterator();
			while(i.hasNext()) {
				Entity e = i.next();
				entities.put(e.id, e);
				i.remove(); // faster than clear() for a LinkedList
			}
		}
		
		profiler.next("remove"); // "entity.remove"
		if(entitiesToRemove.size() != 0) {
			for(Integer id : entitiesToRemove)
				entities.remove(id);
		}
		profiler.end(); // "entity"
		
		profiler.next("tileEntity"); // "tileEntity"
		profiler.start("update"); // "tileEntity.update"
		// Now, iterate over all tile entities
		for(TileEntity t : getTileEntities())
			t.update();
		profiler.end();
		
		profiler.end();
	}
	
	/**
	 * Sets a mob as a player. The mob will be treated as if the player is
	 * controlling it thereafter.
	 */
	public void setPlayer(EntityMob m) {
		players.put(m.id, m);
	}
	
	/**
	 * Removes the status of player from a mob. The mob will no longer be
	 * treated as if controlled by a player thereafter.
	 */
	public void unsetPlayer(EntityMob m) {
		players.remove(m.id);
	}
	
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
	public void addEntity(Entity e, double x, double y) {
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
	public void addEntity(Entity e) {
		e.id = ++entityCount;
		e.onAdd();
		entitiesToAdd.add(e);
	}
	
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
	public void removeEntity(Entity e) {
		removeEntity(e.id);
	}
	
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
	public void removeEntity(int id) {
		entitiesToRemove.add(id);
	}
	
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
	public void addHitbox(Hitbox h, double x, double y) {
		h.x = x;
		h.y = y;
		addHitbox(h);
	}
	
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
	 */
	public void addHitbox(Hitbox h) {
		h.id = ++hitboxCount;
		hitboxesToAdd.add(h);
	}
	
	/**
	 * Removes a hitbox from the world.
	 * 
	 * <!--
	 * <p>The hitbox is not removed from the map of hitboxes immediately;
	 * rather, it is removed mid tick; after the entities have been updated,
	 * but before the hitboxes have been updated. This is intended as to
	 * prevent a {@code ConcurrentModificationException} from being thrown if
	 * the hitbox is removed while the map of hitbox is being iterated over.
	 * </p>
	 * -->
	 * 
	 * @param h The hitbox.
	 */
	public void removeHitbox(Hitbox h) {
		removeHitbox(h.id);
	}
	
	/**
	 * Removes a hitbox from the world.
	 * 
	 * <!--
	 * <p>The hitbox is not removed from the map of hitboxes immediately;
	 * rather, it is removed mid tick; after the entities have been updated,
	 * but before the hitboxes have been updated. This is intended as to
	 * prevent a {@code ConcurrentModificationException} from being thrown if
	 * the hitbox is removed while the map of hitbox is being iterated over.
	 * -->
	 * 
	 * @param id The ID of the hitbox.
	 */
	public void removeHitbox(int id) {
		hitboxes.remove(id);
	}
	
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
	public void addParticle(Particle p, double x, double y) {
		// nothing here; implemented in GameWorld
	}
	
	/**
	 * Adds a particle to the world. The particle's ID is assigned
	 * automatically.
	 * 
	 * @param p The particle.
	 */
	public void addParticle(Particle p) {
		// nothing here; implemented in GameWorld
	}
	
	/**
	 * Removes a particle from the world.
	 * 
	 * @param p The particle.
	 */
	public void removeParticle(Particle p) {
		// nothing here; implemented in GameWorld
	}
	
	/**
	 * Removes a particle from the world.
	 * 
	 * @param id The ID of the particle.
	 */
	public void removeParticle(int id) {
		// nothing here; implemented in GameWorld
	}
	
	/**
	 * Adds a tile entity to the map of tile entities, so that it may be
	 * updated. This does not add it to its owner slice.
	 * 
	 * @param t The tile entity.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	protected void addTileEntity(TileEntity t, int x, int y) {
		tileEntities.put(new HashPoint(x, y), t);
	}
	
	/**
	 * Removes a tile entity from the map of tile entities. This does not
	 * remove it from its owner slice.
	 */
	protected void removeTileEntity(TileEntity t) {
		removeTileEntity(t.x, t.y);
	}
	
	/**
	 * Removes a tile entity from the map of tile entities. This does not
	 * remove it from its owner slice.
	 * 
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	protected void removeTileEntity(int x, int y) {
		tileEntities.remove(new HashPoint(x, y));
	}
	
	// ==========Collection getters==========
	
	/**
	 * @return The collection of all players in the world. Note that a player
	 * is also treated as an entity and as such every element in the returned
	 * collection is also a member of the one returned by {@link
	 * #getEntityIterator()}.
	 */
	public Collection<EntityMob> getPlayers() {
		return players.values();
	}
	
	/**
	 * @return The collection of entities in the world.
	 */
	public Collection<Entity> getEntities() {
		return entities.values();
	}
	
	/**
	 * @return The collection of hitboxes in the world.
	 */
	public Collection<Hitbox> getHitboxes() {
		return hitboxes.values();
	}
	
	/**
	 * @return The collection of tile entities in the world.
	 */
	public Collection<TileEntity> getTileEntities() {
		return tileEntities.values();
	}
	
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
	
	// ==========Misc mob stuff==========
	
	/**
	 * Attempts to spawn a mob at a given location.
	 * 
	 * @param mob The mob.
	 * @param x The x-coordinate at which to attempt to spawn the mob, in
	 * tile-lengths.
	 * @param y The y-coordinate at which to attempt to spawn the mob, in
	 * tile-lengths.
	 */
	public void spawnMob(EntityMob mob, double x, double y) {
		if(hostileMobCount >= HOSTILE_MOB_CAP)
			return;
		
		// For now, mobs must spawn in a radius from a player in the
		// bounds of 32 <= r <= 128
		boolean inRange = false;		// In range of at least 1 player
		double dx, dy, dist2;
		for(EntityMob p : players.values()) {
			dx = p.x - x;
			dy = p.y - y;
			dist2 = dx*dx + dy*dy;
			
			if(dist2 <= 1024D)//32*32
				return;
			else if(dist2 <= 16384D)//128*128
				inRange = true;
		}
		if(!inRange)
			return;
		
		int minX = MathsUtil.floor(x + mob.boundingBox.getV00().x);
		int maxX = MathsUtil.ceil(minX + mob.boundingBox.width);
		int minY = MathsUtil.floor(y + mob.boundingBox.getV00().y);
		int maxY = MathsUtil.ceil(minY + mob.boundingBox.height);
		
		// Check to see if the mob would be spawning in any tiles
		for(int tileX = minX; tileX < maxX; tileX++) {
			for(int tileY = minY; tileY < maxY; tileY++) {
				if(getTileAt(tileX, tileY).isSolid())
					return;
			}
		}
		addEntity(mob, x, y);
	}
	
	/**
	 * Destroys all non-player-controlled mobs in the world.
	 */
	public void exterminateMobs() {
		for(Entity e : entities.values()) {
			if(e instanceof EntityMob && !((EntityMob)e).isPlayerControlled())
				e.destroy();
		}
	}
	
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
	public abstract Slice getSliceAt(int x, int y);
	
	/**
	 * Gets the slice at the given coordinates.
	 * 
	 * @param x The slice's x-coordinate, in tile lengths.
	 * @param y The slice's y-coordinate, in tile lengths.
	 * 
	 * @return The slice at the given coordinates, or {@code null} if no such
	 * slice is loaded.
	 */
	public abstract Slice getSliceAtTile(int x, int y);
	
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
	public final Tile getTileAt(double x, double y) {
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
	public abstract Tile getTileAt(int x, int y);
	
	/**
	 * Sets a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param id The ID of the tile to set.
	 */
	public abstract void setTileAt(int x, int y, int id);
	
	/**
	 * Breaks a tile.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public abstract void breakTileAt(int x, int y);
	
	/**
	 * Gets the tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The tile at the given coordinates, or the
	 * {@link com.stabilise.world.tile.Tiles#BEDROCK_INVISIBLE invisible
	 * bedrock} tile if no such tile is loaded.
	 */
	public abstract TileEntity getTileEntityAt(int x, int y);
	
	/**
	 * Sets a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param y The y-coordinate of the tile at which to place the tile entity,
	 * in tile-lengths.
	 * @param t The tile entity.
	 */
	public abstract void setTileEntityAt(int x, int y, TileEntity t);
	
	/**
	 * Removes a tile entity at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 * @param y The y-coordinate of the tile at which the tile entity to remove
	 * is placed.
	 */
	public abstract void removeTileEntityAt(int x, int y);
	
	/**
	 * Attempts to blow up a tile at the given coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param explosionPower The power of the explosion.
	 */
	public abstract void blowUpTile(int x, int y, float explosionPower);
	
	/**
	 * Gets the world's directory.
	 * 
	 * @return The File representing the world's directory.
	 */
	public abstract File getDir();
	
	/**
	 * Loads the character's world-specific data, (i.e. their coordinates,
	 * current health, etc.)
	 * 
	 * @param character The character data for which to load the info.
	 * 
	 * @throws NullPointerException Thrown if {@code character} is
	 * {@code null}.
	 */
	protected void loadCharacterData(CharacterData character) {
		new PlayerDataFile(character);
		character.dataFile.load();
	}
	
	/**
	 * Saves the character's world-specific data.
	 * 
	 * @param character The character data for which to save the info.
	 * 
	 * @throws NullPointerException Thrown if {@code character} or
	 * {@code character.dataFile} is {@code null}.
	 */
	protected void saveCharacterData(CharacterData character) {
		character.dataFile.save();
	}
	
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
			Log.critical("Could not save world info during creation process!", e);
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
				Log.critical("Could not load world info for world \"" + worldDirs[i].getName() + "\"!");
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
			Log.critical("Could not delete world \"" + worldName + "\"!", e);
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A way of easily working with a world's data file for each
	 * player/character.
	 */
	public class PlayerDataFile {
		
		/** The file. */
		private File file;
		/** Whether or not the file has been initially loaded in. */
		private boolean loaded;
		/** The root compound tag of the player's data file. */
		private NBTTagCompound nbt;
		/** The compound representing the character's tag compound, with a name
		 * which is that of the character's hash. */
		private NBTTagCompound tag;
		/** Whether or not the character's tag exists within the file and was
		 * loaded. */
		private boolean tagLoaded;
		/** The character data. */
		private CharacterData character;
		
		
		/**
		 * Creates a new player data file.
		 * 
		 * @param character The player data upon which to base the data file.
		 */
		private PlayerDataFile(CharacterData character) {
			this.character = character;
			character.dataFile = this;
			
			file = getFile();
			nbt = null;
			loaded = !file.exists();
			tagLoaded = false;
		}
		
		/**
		 * Loads the file's contents into the character data.
		 */
		private void load() {
			loadNBT();
			
			if(tagLoaded) {
				try {
					character.lastX = tag.getDoubleUnsafe("x");
					character.lastY = tag.getDoubleUnsafe("y");
					character.newToWorld = false;
					return;
				} catch(IOException ignored) {}
			}
			
			character.newToWorld = true;
		}
		
		/**
		 * Loads the NBT file.
		 */
		private void loadNBT() {
			if(file.exists()) {
				try {
					nbt = NBTIO.readCompressed(file);
					tag = nbt.getCompound(character.hash);
					if(tag.isEmpty())
						nbt.addCompound(tag.getName(), tag);
					else
						tagLoaded = true;
					loaded = true;
				} catch(IOException e) {
					log.logCritical("Could not load character data file for character " + character.name, e);
				}
			} else {
				nbt = new NBTTagCompound("");
				tag = new NBTTagCompound(character.hash);
				nbt.addCompound(tag.getName(), tag);
				loaded = true;
			}
		}
		
		/**
		 * Saves the character's data into the file.
		 */
		private void save() {
			// In case there are other characters with the same name but a
			// different hash, we don't want to completely overwrite their data
			// in the file, so load in the file's content if possible
			if(!loaded)
				loadNBT();
			
			tag.addDouble("x", character.lastX);
			tag.addDouble("y", character.lastY);
			
			try {
				NBTIO.writeCompressed(file, nbt);
			} catch(IOException e) {
				log.logCritical("Could not save character data file for character " + character.name, e);
			}
		}
		
		/**
		 * Gets the data file's file reference.
		 * 
		 * @return The world's local character file.
		 */
		private File getFile() {
			return new File(getDir(), DIR_PLAYERS + character.name + EXTENSION_PLAYERS);
		}
	}
	
}
