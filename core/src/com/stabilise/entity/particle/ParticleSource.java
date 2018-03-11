package com.stabilise.entity.particle;

import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.core.Settings;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.AbstractWorld;
import com.stabilise.world.World;


/**
 * A ParticleSource may be used to easily generate particles of a specified
 * type.
 * 
 * <p>A ParticleSource pools its particles using a {@link ParticlePool} as
 * a background optimisation.
 */
public class ParticleSource<T extends Particle> {
    
    private final AbstractWorld world;
    private final ParticlePool<T> pool;
    private final boolean physical; // true if T extends ParticlePhysical
    private final Random rnd;
    /** Dummy Position object that's used within this class. I've left this
     * public for convenience. */
    public final Position dummyPos = new Position(); // DON'T USE THIS ONE IN HERE
    private final Position dummyPos2 = new Position(); // Use this one!
    
    
    /**
     * Creates a new particle source.
     * 
     * @param world The world in which to generate particles.
     * @param clazz The class of the particle to generate.
     * 
     * @throws NullPointerException if {@code clazz} is null.
     * @throws IllegalArgumentException if particles of the given class have
     * not been registered.
     */
    ParticleSource(AbstractWorld world, Class<T> clazz) {
        this.world = world;
        this.pool = new ParticlePool<>(clazz);
        this.physical = ParticlePhysical.class.isAssignableFrom(clazz);
        this.rnd = world.rnd();
    }
    
    @SuppressWarnings("unchecked")
    public void reclaim(Particle p) {
        pool.reclaim((T)p);
    }
    
    /**
     * {@link ParticlePool#flush() Flushes} the pool.
     */
    void cleanup() {
        pool.flush();
    }
    
    /**
     * Modulates a given quantity of particles to generate based on the game's
     * {@link Settings#getSettingParticles() particles setting}.
     */
    private int count(int baseCount) {
        switch(Settings.getSettingParticles()) {
            case Settings.PARTICLES_ALL:
                return baseCount;
            case Settings.PARTICLES_REDUCED:
                return baseCount <= 4 ? 1 : baseCount / 4;
            case Settings.PARTICLES_NONE:
                return 0;
        }
        return 0;
    }
    
    /**
     * Creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle) addParticle(particle)}, and then returns
     * the particle.
     */
    public T create() {
        T p = pool.get();
        p.reset();
        world.addParticle(p);
        return p;
    }
    
    /**
     * Creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle) addParticle(particle)}, and then returns
     * the particle.
     */
    public T createAt(Position pos) {
        T p = pool.get();
        p.reset();
        p.pos.set(pos);
        world.addParticle(p);
        return p;
    }
    
    private void create(Position pos, float dx, float dy) {
        if(physical) {
            ParticlePhysical p = (ParticlePhysical)pool.get();
            p.reset();
            p.pos.set(pos);
            p.dx = dx;
            p.dy = dy;
            world.addParticle(p);
        } else {
            Particle p = pool.get();
            p.reset();
            p.pos.set(pos, dx, dy);
            world.addParticle(p);
        }
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
     * @param pos The position at which to place the particle. This method does
     * not modify {@code pos}.
     * @param minV The minimum velocity, in tiles per second.
     * @param maxV The maximum velocity, in tiles per second.
     * @param minAngle The minimum angle at which to direct the particles,
     * in radians.
     * @param maxAngle The maximum angle at which to direct the particles,
     * in radians.
     */
    public void createBurst(int numParticles, Position pos,
            float minV, float maxV, float minAngle, float maxAngle) {
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(pos, minV, maxV, minAngle, maxAngle);
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
     * @param minV The minimum velocity, in tiles per second.
     * @param maxV The maximum velocity, in tiles per second.
     * @param minAngle The minimum angle at which to direct the particles,
     * in radians.
     * @param maxAngle The maximum angle at which to direct the particles,
     * in radians.
     */
    @Deprecated
    public void createBurst(int numParticles,
            double minX, double maxX, double minY, double maxY,
            float minV, float maxV, float minAngle, float maxAngle) {
        /*
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(
                    minX + rnd.nextDouble() * (maxX-minX),
                    minY + rnd.nextDouble() * (maxY-minY),
                    minV, maxV, minAngle, maxAngle);
        */
    }
    
    /**
     * Creates a directed burst of particles. The particles will spawn randomly
     * in the box specified by {@code cornerPos}, {@code width} and {@code
     * height}.
     * 
     * If the particles created by this source are {@link ParticlePhysical
     * physical particles}, they will be created with a velocity of
     * magnitude between {@code minV} and {@code maxV} directed between the
     * specified angles; otherwise, the particles will simply be displaced
     * in that direction by that much.
     * 
     * @param numParticles The number of particles to create.
     * @param cornerPos The position of the corner of the box in which to spawn
     * the particles.
     * @param width The width of the box.
     * @param height The height of the box.
     * @param minV The minimum velocity, in tiles per second.
     * @param maxV The maximum velocity, in tiles per second.
     * @param minAngle The minimum angle at which to direct the particles,
     * in radians.
     * @param maxAngle The maximum angle at which to direct the particles,
     * in radians.
     */
    public void createBurst(int numParticles, Position cornerPos, float width,
            float height, float minV, float maxV, float minAngle, float maxAngle) {
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(
                    dummyPos2.set(cornerPos, rnd.nextFloat()*width, rnd.nextFloat()*height),
                    minV, maxV, minAngle, maxAngle);
    }
    
    /**
     * Same as {@link #createBurst(int, Position, float, float, float, float,
     * float, float)}, but the particles are placed at a random location on the
     * specified AABB (which is in turn considered to be defined relative to
     * the specified position).
     * 
     * @throws NullPointerException if {@code aabb} is {@code null}.
     */
    public void createBurst(int numParticles, Position pos,
            float minV, float maxV, float minAngle, float maxAngle,
            AABB aabb) {
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(
                    dummyPos2.set(pos,
                            aabb.minX() + rnd.nextFloat() * aabb.width(),
                            aabb.minY() + rnd.nextFloat() * aabb.height()
                    ),
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
        createBurst(numParticles, e.pos, minV, maxV, minAngle, maxAngle, e.aabb);
    }
    
    /**
     * Same as {@link
     * #createBurst(int, Position, float, float, float, float)}, but
     * the particles are placed somewhere on the tile specified by the
     * given tile coordinates.
     */
    public void createBurstOnTile(int numParticles, Position pos,
            float minV, float maxV, float minAngle, float maxAngle) {
        createBurst(numParticles, pos.copy().add(rnd.nextFloat(), rnd.nextFloat()),
                minV, maxV, minAngle, maxAngle);
    }
    
    private void createBurstParticle(Position pos,
            float minV, float maxV, float minAngle, float maxAngle) {
        float v = minV + rnd.nextFloat() * (maxV - minV);
        float angle = minAngle + rnd.nextFloat() * (maxAngle - minAngle);
        float dx = v * MathUtils.cos(angle);
        float dy = v * MathUtils.sin(angle);
        create(pos, dx, dy);
    }
    
    public void createOutwardsBurst(int numParticles, Position pos,
            boolean burstX, boolean burstY, float maxX, float maxY,
            AABB aabb) {
        float w = aabb.width();
        float h = aabb.height();
        for(int i = 0; i < count(numParticles); i++) {
            float xp = rnd.nextFloat();
            float yp = rnd.nextFloat();
            create(
                    dummyPos2.set(pos, w, h),
                    burstX ? (2*xp - 1) * maxX : 0f,
                    burstY ? (2*yp - 1) * maxY : 0f
            );
        }
    }
    
    public void createOutwardsBurst(int numParticles,
            boolean burstX, boolean burstY, float maxX, float maxY,
            Entity e) {
        createOutwardsBurst(numParticles, e.pos,
                burstX, burstY, maxX, maxY, e.aabb);
    }
    
    public void createCentredOutwardsBurst(Random rnd, int numParticles,
            float minV, float maxV, Entity e) {
        dummyPos2.set(e.pos, e.aabb.centreX(), e.aabb.centreY());
        for(int i = 0; i < count(numParticles); i++) {
            this.createBurstParticle(dummyPos2, minV, maxV, 0f, Maths.TAUf);
        }
    }
    
}