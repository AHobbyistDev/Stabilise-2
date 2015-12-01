package com.stabilise.entity.particle;

import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.core.Settings;
import com.stabilise.entity.Entity;
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
    private final boolean physical;
    private final Random rnd;
    
    
    ParticleSource(AbstractWorld world, ParticlePool<T> pool, boolean physical) {
        this.world = world;
        this.pool = pool;
        this.physical = physical;
        this.rnd = world.getRnd();
    }
    
    @SuppressWarnings("unchecked")
    public void reclaim(Particle p) {
        pool.reclaim((T)p);
    }
    
    void cleanup() {
        pool.flush();
    }
    
    private int count(int baseCount) {
        switch(Settings.getSettingParticles()) {
            case Settings.PARTICLES_ALL:
                return baseCount;
            case Settings.PARTICLES_REDUCED:
                return baseCount == 1 ? 1 : baseCount / 2;
            case Settings.PARTICLES_NONE:
                return 0;
        }
        return 0;
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
    public T create() {
        T p = pool.getCasted();
        p.reset();
        world.addParticle(p);
        return p;
    }
    
    /**
     * Creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle, double, double)
     * addParticle(particle, x, y)}, and then returns the particle.
     */
    public T createAt(double x, double y) {
        T p = pool.getCasted();
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
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(x, y, minV, maxV, minAngle, maxAngle);
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
    public void createBurst(int numParticles,
            double minX, double maxX, double minY, double maxY,
            float minV, float maxV, float minAngle, float maxAngle) {
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(
                    minX + rnd.nextDouble() * (maxX-minX),
                    minY + rnd.nextDouble() * (maxY-minY),
                    minV, maxV, minAngle, maxAngle);
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
        for(int i = 0; i < count(numParticles); i++)
            createBurstParticle(
                    x + aabb.minX() + rnd.nextFloat() * aabb.width(),
                    y + aabb.minY() + rnd.nextFloat() * aabb.height(),
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
                e.aabb);
    }
    
    /**
     * Same as {@link
     * #createBurst(int, double, double, float, float, float, float)}, but
     * the particles are placed somewhere on the tile specified by the
     * given tile coordinates.
     */
    public void createBurstOnTile(int numParticles, int x, int y,
            float minV, float maxV, float minAngle, float maxAngle) {
        createBurst(numParticles, x + rnd.nextDouble(), y + rnd.nextDouble(),
                minV, maxV, minAngle, maxAngle);
    }
    
    private void createBurstParticle(double x, double y,
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
                maxX, maxY, e.aabb);
    }
    
    public void createCentredOutwardsBurst(Random rnd, int numParticles,
            float minV, float maxV, Entity e) {
        double midX = e.x + e.aabb.width()/2;
        double midY = e.y + e.aabb.height()/2;
        for(int i = 0; i < count(numParticles); i++) {
            this.createBurstParticle(midX, midY, minV, maxV, 0f, Maths.TAUf);
        }
    }
    
}