package com.stabilise.world;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.stabilise.core.Constants;
import com.stabilise.entity.Entity;
import com.stabilise.entity.GameCamera;
import com.stabilise.entity.GameObject;
import com.stabilise.entity.Position;
import com.stabilise.entity.hitbox.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticleManager;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.ForTestingPurposes;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.FragList;
import com.stabilise.util.collect.LongList;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.util.collect.FunctionalIterable;
import com.stabilise.util.collect.SimpleList;
import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * An implementation of World which contains features common to all world
 * types.
 */
public abstract class AbstractWorld implements World {
    
    /** The multiverse to which this world belongs. */
    protected final Multiverse<?> multiverse;
    /** This world's dimension. */
    protected final Dimension dimension;
    
    /** All players in the world. Maps IDs -> player Entities. */
    protected final Map<Long, Entity> players = new HashMap<>(4);
    private final FunctionalIterable<Entity> itrPlayers =
            FunctionalIterable.wrap(players.values(), players::size);
    /** The map of loaded entities in the world. Maps IDs -> Entities.
     * This is a LinkedHashMap as to allow for consistent iteration. */
    protected final Map<Long, Entity> entities = new LinkedHashMap<>(1024);
    private final FunctionalIterable<Entity> itrEntities =
            FunctionalIterable.wrap(entities.values(), entities::size);
    /** The total number of entities which have existed during the lifetime of
     * the world. When a new entity is created this is incremented and set as
     * its ID. */
    //@Deprecated
    //public long entityCount = 0; // Moved to Multiverse
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
    private final LongList entitiesToRemove = new LongList();
    /** The number of hostile mobs in the world. */
    protected int hostileMobCount = 0;
    
    /** Stores tile entities for iteration and updating. A loaded tile entity
     * need not exist in this list if it does not require updates. */
    protected final SimpleList<TileEntity> tileEntities = new UnorderedArrayList<>();
    
    /** The list of hitboxes in the world.
     * <p>Implementation note: This is an {@link UnorderedArrayList} as
     * internal hitbox ordering is unimportant. */
    protected final SimpleList<Hitbox> hitboxes = new UnorderedArrayList<>();
    /** The total number of hitboxes which have existed during the lifetime of
     * the world. */
    public int hitboxCount = 0;
    
    /** This world's particle manager. */
    public final ParticleManager particleManager = new ParticleManager(this);
    /** Stores all particles in the world. This should remain empty if this is
     * a server world.
     * <p>Implementation note: This is a FragList as we want to maintain local
     * render order between any two pairs of particles, but we don't really
     * care if new particles are spawned between any two. */
    protected final SimpleList<Particle> particles = new FragList<>(4096);
    /** The total number of particles which have existed during the lifetime of
     * the world. */
    public long particleCount = 0;
    
    /** The x/y-coordinates of the slice in which players initially spawn, in
     * slice-lengths. */
    protected int spawnSliceX, spawnSliceY;
    
    private float timeDelta = 1f;
    private float timeIncrement = timeDelta / Constants.TICKS_PER_SECOND;
    private final float gravity = -3 * 9.8f; // arbitrary, but 9.8 feels too sluggish
    private float gravityIncrement = gravity * timeIncrement;
    private float gravity2ndOrder = gravity * timeIncrement * timeIncrement / 2;
    
    public final GameCamera camera = new GameCamera();
    
    /** An easy-access utility RNG which should be used by any GameObject with
     * a reference to this world in preference to constructing a new one.
     * @see #rnd() */
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
    
    @Override
    public Multiverse<?> multiverse() {
        return this.multiverse;
    }
    
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
        getParticles().iterate(p -> p.updateAndCheck(this) && reclaimParticle(p));
        
        // Do a particle cleanup every 5 seconds
        if(getAge() % 300 == 0)
            particleManager.cleanup();
        
        // Now, add all queued entities
        profiler.next("entity"); // root.update.game.world.entity
        profiler.start("add"); // root.update.game.world.entity.add
        
        if(!entitiesToAdd.isEmpty()) {
            entitiesToAdd.consume(e -> {
                e.setID(multiverse().getNextEntityID());
                entities.put(e.id(), e);
                e.onAdd(this);
            });
        }
        
        profiler.next("remove"); // root.update.game.world.entity.remove
        
        entitiesToRemove.clear(id -> entities.remove(id));
        
        profiler.end(); // root.update.game.world.entity
        profiler.end(); // root.update.game.world
    }
    
    /**
     * Iterates over the specified collection of GameObjects as per {@link
     * GameObject#updateAndCheck(World)}. GameObjects are removed from the
     * collection by the iterable if {@code updateAndCheck()} returns {@code
     * true}.
     */
    protected void updateObjects(FunctionalIterable<? extends GameObject> objects) {
        objects.iterate(o -> o.updateAndCheck(this));
    }
    
    /**
     * Sets a mob as a player. The mob will be treated as if the player is
     * controlling it thereafter.
     */
    public void setPlayer(Entity e) {
        players.put(e.id(), e);
        camera.setFocus(e);
    }
    
    /**
     * Removes the status of player from a mob. The mob will no longer be
     * treated as if controlled by a player thereafter.
     */
    public void unsetPlayer(Entity e) {
        players.remove(e.id());
    }
    
    @Override
    @UserThread("AnyDimensionThread")
    public void addEntity(Entity e) {
        entitiesToAdd.add(e);
    }
    
    @Override
    public Entity getEntity(long id) {
        return entities.get(id);
    }
    
    @Override
    public void removeEntity(long id) {
        entitiesToRemove.add(id);
    }
    
    @Override
    public void addHitbox(Hitbox h) {
        hitboxCount++;
        hitboxes.append(Objects.requireNonNull(h));
    }
    
    /**
     * Adds a particle to the world. This is for {@link ParticleSource} use
     * only!
     * 
     * <p>It is implicitly trusted that {@code p} is non-null.
     */
    public void addParticle(Particle p) {
        particleCount++;
        particles.append(p);
    }
    
    private boolean reclaimParticle(Particle p) {
        getParticleManager().getSource(p.getClass()).reclaim(p);
        return true;
    }
    
    @Override
    public void addTileEntity(TileEntity t) {
        if(t.requiresUpdates())
            tileEntities.append(t);
    }
    
    @Override
    public void sendToDimension(String dimension, Entity e, Position pos) {
        multiverse.sendToDimension(this, dimension, e, pos);
    }
    
    // ==========Collection getters==========
    
    @Override
    public FunctionalIterable<Entity> getPlayers() {
        return itrPlayers;
    }
    
    @Override
    public FunctionalIterable<Entity> getEntities() {
        return itrEntities;
    }
    
    @Override
    public FunctionalIterable<Hitbox> getHitboxes() {
        return hitboxes;
    }
    
    @Override
    public FunctionalIterable<TileEntity> getTileEntities() {
        return tileEntities;
    }
    
    @Override
    public FunctionalIterable<Particle> getParticles() {
        return particles;
    }
    
    @Override
    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    @Override
    public WorldCamera getCamera() {
        return camera;
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
    public Random rnd() {
        return rnd;
    }
    
    /**
     * Returns this world's Dimension.
     */
    public Dimension getDimension() {
        return dimension;
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
    
    @Override
    public Profiler profiler() {
        return profiler;
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
     * @param pos The position at which to place the mob.
     */
    public void spawnMob(Entity mob, Position pos) {
    	this.log.postWarning("Attempted to call spawnMob (is NYI for now out of laziness)");
    	/*
        if(hostileMobCount >= HOSTILE_MOB_CAP)
            return;
        
        // For now, mobs must spawn in a radius from a player in the
        // bounds of 32 <= r <= 128
        boolean inRange = false;        // In range of at least 1 player
        for(Entity p : players.values()) {
            float dist2 = pos.diffSq(p.pos);
            
            if(dist2 <= 1024f)//32*32
                return;
            else if(dist2 <= 16384f)//128*128
                inRange = true;
        }
        if(!inRange)
            return;
        
        // TODO: inelegant using globals
        int minX = Maths.floor(pos.getGlobalX() + mob.aabb.minX());
        int maxX = Maths.ceil(pos.getGlobalX() + mob.aabb.maxX());
        int minY = Maths.floor(pos.getGlobalY() + mob.aabb.minY());
        int maxY = Maths.ceil(pos.getGlobalY() + mob.aabb.maxY());
        
        // Check to see if the mob would be spawning in any tiles
        for(int tileX = minX; tileX < maxX; tileX++)
            for(int tileY = minY; tileY < maxY; tileY++)
                if(getTileAt(tileX, tileY).isSolid())
                    return;
        mob.pos.set(pos);
        addEntity(mob);
        */
    }
    
    /**
     * Blocks the current thread until the world has closed.
     */
    public abstract void blockUntilClosed();
    
    /**
     * Destroys all non-player entities in the world.
     */
    @ForTestingPurposes
    public void destroyEntities() {
        for(Entity e : entities.values()) {
            if(!e.isPlayerControlled())
                e.destroy();
        }
    }
    
}
