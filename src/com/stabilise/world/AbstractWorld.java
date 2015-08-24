package com.stabilise.world;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.core.Constants;
import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.GameObject;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticlePhysical;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.Array;
import com.stabilise.util.collect.ClearingLinkedList;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.LightLinkedList;
import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * An implementation of World which contains features common to all world
 * types.
 */
public abstract class AbstractWorld implements World {
	
	public final Multiverse<?> multiverse;
	/** This world's dimension. */
	protected final Dimension dimension;
	
	/** All players in the world. Maps IDs -> players' EntityMobs. */
	protected final Map<Integer, EntityMob> players = new HashMap<>(4);
	/** The map of loaded entities in the world. Maps IDs -> Entities.
	 * This is a LinkedHashMap as to allow for consistent iteration. */
	protected final Map<Integer, Entity> entities = new LinkedHashMap<>(64);
	/** The total number of entities which have existed during the lifetime of
	 * the world. When a new entity is created this is incremented and set as
	 * its ID. */
	protected int entityCount = 0;
	/** Entities queued to be added to the world at the end of the tick.
	 * <p>This is a ClearingQueue as entities may be added to a world from
	 * from another dimension, which can be hosted on another thread. */
	private final ClearingQueue<Entity> entitiesToAdd = ClearingQueue.create();
	/** Entities queued to be removed from the world at the end of the tick.
	 * <p>Implementation detail: Though it would be cleaner to invoke {@code
	 * destroy()} on entities and let them self-remove while being iterated
	 * over, this does not work when moving an entity to another dimension: if
	 * {@code destroy()} is invoked, this would also invalidate its position in
	 * the dimension it is being moved to. */
	private final ClearingLinkedList<Integer> entitiesToRemove =
			new ClearingLinkedList<>();
	/** The number of hostile mobs in the world. */
	protected int hostileMobCount = 0;
	
	/** Stores tile entities for iteration and updating. A loaded tile entity
	 * need not exist in this list if it does not require updates. */
	protected final LightLinkedList<TileEntity> tileEntities =
			new LightLinkedList<>();
	
	/** The list of hitboxes in the world. */
	protected final LightLinkedList<Hitbox> hitboxes =
			new LightLinkedList<>();
	/** The total number of hitboxes which have existed during the lifetime of
	 * the world. */
	protected int hitboxCount = 0;
	
	/** This world's particle manager. */
	public final ParticleManager particleManager = new ParticleManager(this);
	/** Stores all particles in the world. This should remain empty if this is
	 * a server world. */
	protected final LightLinkedList<Particle> particles =
			new LightLinkedList<>();
	/** The total number of particles which have existed during the lifetime of
	 * the world. */
	protected int particleCount = 0;
	
	/** The x/y-coordinates of the slice in which players initially spawn, in
	 * slice-lengths. */
	protected int spawnSliceX, spawnSliceY;
	
	private float timeDelta = 1f;
	private float timeIncrement = timeDelta / Constants.TICKS_PER_SECOND;
	private final float gravity = -3 * 9.8f; // arbitrary, but 9.8 feels too sluggish
	private float gravityIncrement = gravity * timeIncrement;
	private float gravity2ndOrder = gravity * timeIncrement * timeIncrement / 2;
	
	/** An easy-access utility RNG which should be used by any GameObject with
	 * a reference to this world in preference to constructing a new one.
	 * @see #getRnd() */
	public final Random rnd = new Random();
	
	/** Use this to profile the world's operation. */
	protected final Profiler profiler;
	protected final Log log;
	
	
	/**
	 * Creates a new AbstractWorld.
	 * 
	 * @param multiverse The multiverse.
	 * @param dimension The dimension of this world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public AbstractWorld(Multiverse<? extends AbstractWorld> multiverse,
			Dimension dimension) {
		this.multiverse = multiverse;
		this.dimension = dimension;
		
		profiler = multiverse.getProfiler();
		log = Log.getAgent("World_" + dimension.info.name);
	}
	
	/**
	 * Prepares the world by performing any necessary preemptive loading
	 * operations, such as preparing the spawn regions, etc. Polling {@link
	 * #isLoaded()} allows one to check the status of this operation.
	 * 
	 * @throws IllegalStateException if the world has already been prepared.
	 */
	public abstract void prepare();
	
	/**
	 * Polls the loaded status of the world.
	 * 
	 * @return {@code true} if the world is loaded; {@code false} otherwise.
	 */
	public abstract boolean isLoaded();
	
	/**
	 * Updates this world, and returns {@code true} if it has unloaded and
	 * should be disowned.
	 * 
	 * @return {@code true} if this World is unloaded and should be removed
	 * ASAP; {@code false} otherwise.
	 */
	public abstract boolean update();
	
	/**
	 * Updates the world by executing a single tick of game logic. In general,
	 * all GameObjects in the world will be updated (i.e. entities, hitboxes,
	 * tile entities, etc).
	 */
	@UserThread("MainThread")
	protected void doUpdate() {
		dimension.info.age++;
		
		profiler.start("entity"); // root.update.game.world.entity
		updateObjects(getEntities());
		profiler.next("hitbox"); // root.update.game.world.hitbox
		updateObjects(getHitboxes());
		profiler.next("tileEntity"); // root.update.game.world.tileEntity
		updateObjects(getTileEntities());
		profiler.next("particle"); // root.update.game.world.particle
		updateObjects(getParticles());
		
		// Do a particle cleanup every 5 seconds
		if(getAge() % 300 == 0)
			particleManager.cleanup();
		
		// Now, add all queued entities
		profiler.next("entity"); // root.update.game.world.entity
		profiler.start("add"); // root.update.game.world.entity.add
		
		if(!entitiesToAdd.isEmpty()) {
			entitiesToAdd.consume(e -> {
				e.id = ++entityCount;
				e.onAdd();
				entities.put(e.id, e);
			});
		}
		
		profiler.next("remove"); // root.update.game.world.entity.remove
		
		if(!entitiesToRemove.isEmpty())
			for(Integer id : entitiesToRemove)
				entities.remove(id);
		
		profiler.end(); // root.update.game.world.entity
		profiler.end(); // root.update.game.world
	}
	
	/**
	 * Iterates over the specified collection of GameObjects as per {@link
	 * GameObject#updateAndCheck(World)}. GameObjects are removed from the
	 * collection by the iterator if {@code updateAndCheck()} returns {@code
	 * true}.
	 */
	protected <E extends GameObject> void updateObjects(Iterable<E> objects) {
		IteratorUtils.forEach(objects, o -> o.updateAndCheck(this));
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
	
	@Override
	@UserThread("AnyDimensionThread")
	public void addEntity(Entity e) {
		entitiesToAdd.add(e);
	}
	
	@Override
	public Entity getEntity(int id) {
		return entities.get(id);
	}
	
	@Override
	public void removeEntity(int id) {
		entitiesToRemove.add(id);
	}
	
	@Override
	public void addHitbox(Hitbox h) {
		hitboxCount++;
		hitboxes.add(Objects.requireNonNull(h));
	}
	
	/**
	 * Adds a particle to the world.
	 * 
	 * @throws NullPointerException if {@code p} is {@code null}.
	 */
	protected void addParticle(Particle p) {
		particleCount++;
		particles.add(Objects.requireNonNull(p));
	}
	
	@Override
	public void addTileEntity(TileEntity t) {
		if(t.requiresUpdates())
			tileEntities.add(t);
	}
	
	@Override
	public void sendToDimension(String dimension, Entity e, double x, double y) {
		multiverse.sendToDimension(this, dimension, e, x, y);
	}
	
	// ==========Collection getters==========
	
	@Override
	public Iterable<EntityMob> getPlayers() {
		return players.values();
	}
	
	@Override
	public Iterable<Entity> getEntities() {
		return entities.values();
	}
	
	@Override
	public Iterable<Hitbox> getHitboxes() {
		return hitboxes;
	}
	
	@Override
	public Iterable<TileEntity> getTileEntities() {
		return tileEntities;
	}
	
	@Override
	public Iterable<Particle> getParticles() {
		return particles;
	}
	
	@Override
	public ParticleManager getParticleManager() {
		return particleManager;
	}
	
	// ========== Stuff ==========
	
	@Override
	public float getGravity() {
		return gravity;
	}
	
	@Override
	public float getGravityIncrement() {
		return gravityIncrement;
	}
	
	@Override
	public float getGravity2ndOrder() {
		return gravity2ndOrder;
	}
	
	@Override
	public void setTimeDelta(float delta) {
		if(delta <= 0f)
			throw new IllegalArgumentException("delta <= 0: " + delta);
		
		timeDelta = delta;
		
		timeIncrement = delta / Constants.TICKS_PER_SECOND;
		gravityIncrement = gravity * timeIncrement;
		gravity2ndOrder = gravity * timeIncrement * timeIncrement / 2;
	}
	
	@Override
	public float getTimeDelta() {
		return timeDelta;
	}
	
	@Override
	public float getTimeIncrement() {
		return timeIncrement;
	}
	
	// ========== Utils ==========
	
	@Override
	public long getAge() {
		return dimension.info.age;
	}
	
	@Override
	public Random getRnd() {
		return rnd;
	}
	
	/**
	 * The name of this world's dimension.
	 */
	public String getDimensionName() {
		return dimension.info.name;
	}
	
	@Override
	public boolean isClient() {
		return multiverse.hasClient();
	}
	
	// ========== Lifecycle Methods ==========
	
	/**
	 * Saves the world.
	 */
	public abstract void save();
	
	/**
	 * Closes the world. This method may block for a prolonged period while the
	 * the world is closed if this is a HostWorld.
	 */
	public abstract void close();
	
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
		
		int minX = Maths.floor(x + mob.boundingBox.v00.x());
		int maxX = Maths.ceil(x + mob.boundingBox.v11.x());
		int minY = Maths.floor(y + mob.boundingBox.v00.y());
		int maxY = Maths.ceil(y + mob.boundingBox.v11.y());
		
		// Check to see if the mob would be spawning in any tiles
		for(int tileX = minX; tileX < maxX; tileX++)
			for(int tileY = minY; tileY < maxY; tileY++)
				if(getTileAt(tileX, tileY).isSolid())
					return;
		addEntity(mob, x, y);
	}
	
	/**
	 * Blocks the current thread until the world has closed.
	 */
	public abstract void blockUntilClosed();
	
	/**
	 * Destroys all non-player-controlled mobs in the world.
	 */
	public void exterminateMobs() {
		for(Entity e : entities.values()) {
			if(e instanceof EntityMob && !((EntityMob)e).isPlayerControlled())
				e.destroy();
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Manages particles for a world by performing the following tasks:
	 * 
	 * <ul>
	 * <li>Decides whether or not to add particles to the world based on the
	 *     game settings.
	 * <li>Acts as a particle generator.
	 * <li>Handles particle pooling to avoid excessive object creation.
	 * </ul>
	 */
	@NotThreadSafe
	public static class ParticleManager {
		
		private final AbstractWorld world;
		/** Caches all the particle pools used and shared by each
		 * ParticleSource. */
		private final Map<Class<? extends Particle>, ParticleSource> sources =
				new IdentityHashMap<>();
		
		
		private ParticleManager(AbstractWorld world) {
			this.world = world;
		}
		
		/*
		public void addParticle(Particle p) {
			if(world.isClient()) {
				world.addParticle(p);
			}
		}
		*/
		
		/**
		 * Returns a generator, or <i>source</i>, of particles of the same type
		 * as the specified particle.
		 * 
		 * @param templateParticle The particle to use as the template for all
		 * particles created by the returned {@code ParticleSource}.
		 * 
		 * @throws NullPointerException if {@code templateParticle} is {@code
		 * null}.
		 */
		public ParticleSource getSource(Particle templateParticle) {
			ParticleSource source = sources.get(templateParticle.getClass());
			if(source == null) {
				source = new ParticleSource(
						world,
						new ParticlePool(templateParticle),
						templateParticle instanceof ParticlePhysical
				);
				sources.put(templateParticle.getClass(), source);
			} else {
				// We don't want the particle to go to waste so we may as well
				// put it in the pool.
				templateParticle.pool = source.pool;
				source.pool.put(templateParticle);
			}
			return source;
		}
		
		/**
		 * Tries to release any apparently unused pooled particles if possible,
		 * in order to free up memory.
		 */
		void cleanup() {
			for(ParticleSource src : sources.values())
				src.pool.flush();
		}
		
	}
	
	/**
	 * Provides a pool of particles of the same type to avoid unnecessary
	 * object instantiation and to reduce the strain on the GC.
	 */
	@NotThreadSafe
	public static class ParticlePool {
		
		/** Functions as the initial and the minimum capacity. */
		private static final int CAPACITY_INITIAL = 16;
		/** Maximum pool capacity. */
		private static final int CAPACITY_MAX = 1024; // 6 expansions
		/** Number of active particles must be this many times the size of
		 * the pool to force a resize. */
		private static final int LOAD_FACTOR = 3;
		/** The amount by which the pool's length is multiplied when it is
		 * resized. */
		private static final int EXPANSION = 2;
		/** Maximum number of pooled particles retained when the pool is
		 * flushed. This must be less than {@link #CAPACITY_INITIAL}. */
		private static final int RETENTION_ON_FLUSH = 8;
		
		
		/** The template particle for this pool. We use {@link
		 * Particle#duplicate()} to create new particles when the pool is
		 * empty. */
		final Particle template;
		private final Array<Particle> pool = new Array<>(CAPACITY_INITIAL);
		/** Number of particles in the pool. Always < pool.length(). */
		private int poolSize = 0;
		/** Number of particles currently in the world which are linked to this
		 * pool. */
		private int activeParticles = 0;
		/** If activeParticles exceeds the expansion load, we increase the size
		 * of the pool. */
		private int expansionLoad = CAPACITY_INITIAL * LOAD_FACTOR;
		
		
		/**
		 * Creates a new pool with the specified template particle. The
		 * particle will henceforth be used as a component of this pool, and
		 * should no longer be used for anything else.
		 */
		private ParticlePool(Particle template) {
			this.template = template;
			template.pool = this;
			pool.set(0, template);
		}
		
		/**
		 * Gets a particle from this pool, instantiating a new one if
		 * necessary.
		 */
		Particle get() {
			activeParticles++;
			if(poolSize == 0) {
				Particle p = template.duplicate();
				p.pool = this;
				return p;
			}
			return pool.get(--poolSize);
		}
		
		/**
		 * Puts a particle in this pool. If this pool is full, this method does
		 * nothing. If it is added to this pool, {@code p} is {@link
		 * Particle#reset() reset}.
		 */
		void put(Particle p) {
			// If the pool is full, let the particle get GC'd
			if(poolSize == pool.length())
				return;
			
			p.reset();
			pool.set(poolSize++, p);
		}
		
		/**
		 * Reclaims a particle into this pool. This should only be invoked by
		 * a particle when it is registered as destroyed (i.e. from within
		 * {@link Particle#updateAndCheck(World)}).
		 * <!--
		 * Reclaims a particle into this pool as per {@link #put(Particle)},
		 * expanding this pool if able and necessary. This method should be
		 * invoked to return any particle obtained through {@link #get()}.
		 * -->
		 */
		public void reclaim(Particle p) {
			if(activeParticles-- > expansionLoad && pool.length() < CAPACITY_MAX) {
				//System.out.println("Resizing the pool from " + pool.length()
				//		+ " to " + (pool.length() * EXPANSION));
				pool.resize(EXPANSION * pool.length());
				expansionLoad = pool.length() * LOAD_FACTOR;
			}
			
			put(p);
		}
		
		/**
		 * Flushes this pool by garbage-collecting all but a few pooled
		 * particles, and shrinking the internal size if necessary. This
		 * shouldn't be invoked too frequently as this can be an expensive
		 * operation.
		 */
		void flush() {
			// Dump all but RETENTION_ON_FLUSH-many pooled particles.
			// TODO: We might want to retain a larger amount if the pool has
			// sufficiently expanded.
			pool.setBetween(null, RETENTION_ON_FLUSH, poolSize);
			// If the pool has been expanded over its initial capacity and the
			// most recent expansion's worth of space is unused, we'll shrink
			// the pool.
			if(pool.length() > CAPACITY_INITIAL &&
					activeParticles < pool.length() / LOAD_FACTOR) {
				//System.out.println("Shrinking the pool from " + pool.length()
				//		+ " to " + (pool.length() / EXPANSION));
				pool.resize(pool.length() / EXPANSION);
				expansionLoad = pool.length() * LOAD_FACTOR;
			}
			if(poolSize > RETENTION_ON_FLUSH)
				poolSize = RETENTION_ON_FLUSH;
		}
		
	}
	
	/**
	 * A ParticleSource may be used to easily generate particles of a specified
	 * type.
	 * 
	 * <p>A ParticleSource pools its particles using a {@link ParticlePool} as
	 * a background optimisation.
	 */
	@NotThreadSafe
	public static class ParticleSource {
		
		private final AbstractWorld world;
		private final ParticlePool pool;
		private final boolean physical;
		
		ParticleSource(AbstractWorld world, ParticlePool pool, boolean physical) {
			this.world = world;
			this.pool = pool;
			this.physical = physical;
		}
		
		private int count(int baseCount) {
			return baseCount;
		}
		
		private void addParticle(Particle p, double x, double y) {
			p.x = x;
			p.y = y;
			world.addParticle(p);
		}
		
		private void create(double x, double y, float dx, float dy) {
			if(physical) {
				ParticlePhysical p = (ParticlePhysical)pool.get();
				p.dx = dx;
				p.dy = dy;
				addParticle(p, x, y);
			} else {
				addParticle(pool.get(), x + dx, y + dy);
			}
		}
		
		/**
		 * Creates a particle and adds it to the world as if by {@link
		 * World#addParticle(Particle) addParticle(particle)}, and then returns
		 * the particle.
		 */
		public Particle create() {
			Particle p = pool.get();
			world.addParticle(p);
			return p;
		}
		
		/**
		 * Creates a particle and adds it to the world as if by {@link
		 * World#addParticle(Particle, double, double)
		 * addParticle(particle, x, y)}, and then returns the particle.
		 */
		public Particle createAt(double x, double y) {
			Particle p = pool.get();
			addParticle(p, x, y);
			return p;
		}
		
		/**
		 * Creates a directed burst of particles at the specified coordinates.
		 * If the particles created by this source are {@link ParticlePhysical
		 * physical particles}, they will be created with a velocity of
		 * magnitude between {@code minV} and {@code maxV} directed between the
		 * specified angles; otherwise, the particles will simply be displaced
		 * in that direction by that much.
		 * 
		 * @param numParticles The number of particles to create.
		 * @param x The x-coordinate at which to place the particles, in
		 * tile-lengths.
		 * @param y The y-coordinate at which to place the particles, in
		 * tile-lengths.
		 * @param minV The minimum velocity, in tiles per second.
		 * @param maxV The maximum velocity, in tiles per second.
		 * @param minAngle The minimum angle at which to direct the particles,
		 * in radians.
		 * @param maxAngle The maximum angle at which to direct the particles,
		 * in radians.
		 */
		public void createBurst(int numParticles, double x, double y,
				float minV, float maxV, float minAngle, float maxAngle) {
			Random rnd = world.getRnd();
			for(int i = 0; i < count(numParticles); i++)
				createBurstParticle(rnd, x, y, minV, maxV, minAngle, maxAngle);
		}
		
		/**
		 * Same as {@link
		 * #createBurst(int, double, double, float, float, float, float)}, but
		 * the particles are placed at a random location on the specified AABB
		 * (which is in turn considered to be defined relative to the specified
		 * x and y).
		 * 
		 * @param aabb
		 * 
		 * @throws NullPointerException if {@code aabb} is {@code null}.
		 */
		public void createBurst(int numParticles, double x, double y,
				float minV, float maxV, float minAngle, float maxAngle,
				AABB aabb) {
			Random rnd = world.getRnd();
			for(int i = 0; i < count(numParticles); i++)
				createBurstParticle(rnd,
						x + aabb.v00.x() + rnd.nextFloat() * aabb.width(),
						y + aabb.v00.y() + rnd.nextFloat() * aabb.height(),
						minV, maxV, minAngle, maxAngle);
		}
		
		/**
		 * Same was {@link
		 * #createBurst(int, double, double, float, float, float, float)}, but
		 * the particles are placed somewhere on the specified entity.
		 * 
		 * @throws NullPointerException if {@code e} is {@code null}.
		 */
		public void createBurst(int numParticles,
				float minV, float maxV, float minAngle, float maxAngle,
				Entity e) {
			createBurst(numParticles, e.x, e.y, minV, maxV, minAngle, maxAngle,
					e.boundingBox);
		}
		
		/**
		 * Same as {@link
		 * #createBurst(int, double, double, float, float, float, float)}, but
		 * the particles are placed somewhere on the tile specified by the
		 * given tile coordinates.
		 */
		public void createBurstOnTile(int numParticles, int x, int y,
				float minV, float maxV, float minAngle, float maxAngle) {
			Random rnd = world.getRnd();
			createBurst(numParticles, x + rnd.nextDouble(), y + rnd.nextDouble(),
					minV, maxV, minAngle, maxAngle);
		}
		
		private void createBurstParticle(Random rnd, double x, double y,
				float minV, float maxV, float minAngle, float maxAngle) {
			float v = minV + rnd.nextFloat() * (maxV - minV);
			float angle = minAngle + rnd.nextFloat() * (maxAngle - minAngle);
			float dx = v * MathUtils.cos(angle);
			float dy = v * MathUtils.sin(angle);
			create(x, y, dx, dy);
		}
		
		public void createOutwardsBurst(int numParticles, double x, double y,
				boolean burstX, boolean burstY, float maxX, float maxY,
				AABB aabb) {
			Random rnd = world.getRnd();
			float w = aabb.width();
			float h = aabb.height();
			for(int i = 0; i < count(numParticles); i++) {
				float xp = rnd.nextFloat();
				float yp = rnd.nextFloat();
				create(
						x + w*xp,
						y + h*yp,
						burstX ? (2*xp - 1) * maxX : 0f,
						burstY ? (2*yp - 1) * maxY : 0f
				);
			}
		}
		
		public void createOutwardsBurst(int numParticles,
				boolean burstX, boolean burstY, float maxX, float maxY,
				Entity e) {
			createOutwardsBurst(numParticles, e.x, e.y, burstX, burstY,
					maxX, maxY, e.boundingBox);
		}
		
	}
	
}
