package com.stabilise.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.ClearOnIterateLinkedList;
import com.stabilise.util.collect.LightweightLinkedList;
import com.stabilise.util.maths.HashPoint;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * An implementation of IWorld which contains features common to all world
 * types.
 */
public abstract class BaseWorld extends AbstractWorld {
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** All players in the world. */
	protected Map<Integer, EntityMob> players = new HashMap<>(1);
	/** The map of loaded entities in the world. This is a LinkedHashMap as to
	 * allow for consistent iteration. */
	protected Map<Integer, Entity> entities = new LinkedHashMap<>(64);
	/** The total number of entities which have existed during the lifetime of
	 * the world. When a new entity is created this is incremented and set as
	 * its ID. */
	protected int entityCount = 0;
	/** Entities queued to be added to the world at the end of the tick. */
	private ClearOnIterateLinkedList<Entity> entitiesToAdd =
			new ClearOnIterateLinkedList<>();
	/** The IDs of entities queued to be removed from the world at the end of
	 * the tick.  */
	private ClearOnIterateLinkedList<Integer> entitiesToRemove =
			new ClearOnIterateLinkedList<>();
	/** The number of hostile mobs in the world. */
	protected int hostileMobCount = 0;
	
	/** Stores tile entities for iteration and updating. */
	protected LightweightLinkedList<TileEntity> tileEntities =
			new LightweightLinkedList<>();
	
	/** The list of hitboxes in the world. */
	protected LightweightLinkedList<Hitbox> hitboxes =
			new LightweightLinkedList<>();
	/** The total number of hitboxes which have existed during the lifetime of
	 * the world. */
	protected int hitboxCount = 0;
	
	/** The x-coordinate the slice in which players initially spawn, in
	 * slice-lengths. */
	protected int spawnSliceX;
	/** The x-coordinate the slice in which players initially spawn, in
	 * slice-lengths. */
	protected int spawnSliceY;
	
	/** An easy-access utility RNG which should be used by any GameObject with
	 * a reference to this world in preference to constructing a new one. */
	public final Random rng = new Random();
	
	/** Use this to profile the world's operation. */
	protected final Profiler profiler;
	protected final Log log;
	
	
	/**
	 * Creates a new AbstractWorld.
	 * 
	 * @param profiler The profiler to use for profiling the world.
	 * @param log The log to use for the world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public BaseWorld(Profiler profiler, Log log) {
		this.profiler = Preconditions.checkNotNull(profiler);
		this.log = Preconditions.checkNotNull(log);
	}
	
	@Override
	@UserThread("MainThread")
	public void update() {
		profiler.start("entity"); // "entity"
		updateObjects(getEntities());
		profiler.next("hitbox"); // "hitbox"
		updateObjects(getHitboxes());
		profiler.next("tileEntity"); // "tileEntity"
		updateObjects(getTileEntities());
		
		// Now, add and remove all queued entities
		profiler.next("entity"); // "entity"
		profiler.start("add"); // "entity.add"
		
		if(!entitiesToAdd.isEmpty()) {
			for(Entity e : entitiesToAdd)
				entities.put(e.id, e);
		}
		
		profiler.next("remove"); // "entity.remove"
		if(entitiesToRemove.size() != 0) {
			for(Integer id : entitiesToRemove)
				entities.remove(id);
		}
		profiler.end(); // "entity"
		
		profiler.end();
	}
	
	@Override
	public void setPlayer(EntityMob m) {
		players.put(m.id, m);
	}
	
	@Override
	public void unsetPlayer(EntityMob m) {
		players.remove(m.id);
	}
	
	@Override
	public void addEntity(Entity e) {
		e.id = ++entityCount;
		e.onAdd();
		entitiesToAdd.add(e);
	}
	
	@Override
	public void removeEntity(Entity e) {
		removeEntity(e.id);
	}
	
	@Override
	public void removeEntity(int id) {
		entitiesToRemove.add(id);
	}
	
	@Override
	public void addHitbox(Hitbox h) {
		hitboxes.add(h);
	}
	
	/**
	 * Adds a tile entity to the list of tile entities, so that it may be
	 * updated.
	 * 
	 * @param t The tile entity.
	 */
	protected void addTileEntity(TileEntity t) {
		tileEntities.add(t); // TODO
	}
	
	/**
	 * Removes a tile entity from the list of tile entities. It will no longer
	 * be updated.
	 */
	protected void removeTileEntity(TileEntity t) {
		tileEntities.remove(t); // TODO inefficient removing from a linkedlist
	}
	
	/**
	 * Removes a tile entity from the map of tile entities. This does not
	 * remove it from its owner slice.
	 * 
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	protected void removeTileEntity(int x, int y) {
		tileEntities.remove(new HashPoint(x, y)); // TODO this isn't a map anymore
	}
	
	// ==========Collection getters==========
	
	@Override
	public Collection<EntityMob> getPlayers() {
		return players.values();
	}
	
	@Override
	public Collection<Entity> getEntities() {
		return entities.values();
	}
	
	@Override
	public Collection<Hitbox> getHitboxes() {
		return hitboxes;
	}
	
	@Override
	public Collection<TileEntity> getTileEntities() {
		return tileEntities;
	}
	
	// ========== Utils ==========
	
	@Override
	public Random getRnd() {
		return rng;
	}
	
	// ========== Misc ==========
	
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
	
}
