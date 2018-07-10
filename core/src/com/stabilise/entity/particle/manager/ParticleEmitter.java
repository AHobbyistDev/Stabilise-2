package com.stabilise.entity.particle.manager;

import javax.annotation.Nullable;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticlePhysical;
import com.stabilise.util.box.I32Box;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


/**
 * A ParticleEmitter may be used to easily generate particles of a specified
 * type.
 * 
 * <p>A ParticleEmitter is essentially just a wrapper for a ParticleSource
 * which provides various helper methods and a per-instance tracker of the
 * number of particles generates as to appropriately reduce them based on the
 * particle reduction setting.
 */
public class ParticleEmitter<T extends Particle> {
    
    private final ParticlePool<T> pool;
    
    /** Dummy Position object provided for convenience. */
    public final Position dummyPos; // DON'T USE THIS ONE IN HERE
    private final Position dumPos2; // Internal - use this one!
    
    /** Reference to the ParticleManager's reductionFactor. */
    private final I32Box reductionFactor;
    private int count;
    
    
    /**
     * Creates a new particle source.
     * 
     * @param clazz The class of the particle to generate.
     * 
     * @throws NullPointerException if {@code clazz} is null.
     * @throws IllegalArgumentException if particles of the given class have
     * not been registered.
     */
    ParticleEmitter(ParticlePool<T> pool, I32Box reductionFactor) {
        this.pool = pool;
        
        this.dummyPos = pool.dummyPos1;
        this.dumPos2 = pool.dummyPos2;
        
        this.reductionFactor = reductionFactor;
        count = reductionFactor.get() - 1; // so first attempt is successful
    }
    
    private boolean canMake() {
        if(++count >= reductionFactor.get()) {
            count = 0;
            return true;
        } else
            return false;
    }
    
    private int adjustCount(int baseCount) {
        int rf = reductionFactor.get();
        
        int num = baseCount / rf;
        count += baseCount % rf;
        if(count >= rf) { // may have caused an overflow
            num++;
            count -= rf;
        }
        return num;
    }
    
    /**
     * Possibly creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle) addParticle(particle)}, and then returns
     * the particle. If the game's particle setting is reduced at all, then
     * this may sometimes return null!
     * 
     * @return the particle, or null if particle creation was refused due to
     * the game's particle setting being lowered
     */
    @Nullable
    public T create(World w) {
        return canMake() ? createAlways(w) : null;
    }
    
    /**
     * Possibly creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle) addParticle(particle)}, and then returns
     * the particle. If the game's particle setting is reduced at all, then
     * this may sometimes return null!
     * 
     * @return the particle, or null if particle creation was refused due to
     * the game's particle setting being lowered
     */
    @Nullable
    public T createAt(World w, Position pos) {
        T p = create(w);
        if(p != null)
            p.pos.set(pos);
        return p;
    }
    
    /**
     * Creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle) addParticle(particle)}, and then returns
     * the particle.
     */
    public T createAlways(World w) {
        T p = pool.get();
        w.addParticle(p);
        return p;
    }
    
    /**
     * Creates a particle and adds it to the world as if by {@link
     * World#addParticle(Particle) addParticle(particle)}, and then returns
     * the particle.
     */
    public T createAlwaysAt(World w, Position pos) {
        T p = pool.get();
        p.pos.set(pos);
        w.addParticle(p);
        return p;
    }
    
    
    
    @SuppressWarnings("unused")
    private void create(World w, Position pos, float dx, float dy) {
        if(canMake())
            createAlways(w, pos, dx, dy);
    }
    
    private void createAlways(World w, Position pos, float dx, float dy) {
        if(pool.physical) {
            ParticlePhysical p = (ParticlePhysical)pool.get();
            p.pos.set(pos);
            p.dx = dx;
            p.dy = dy;
            w.addParticle(p);
        } else {
            Particle p = pool.get();
            p.pos.set(pos, dx, dy);
            w.addParticle(p);
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
    public void createBurst(World w, int numParticles, Position pos,
            float minV, float maxV, float minAngle, float maxAngle) {
        for(int i = 0; i < adjustCount(numParticles); i++)
            createBurstParticle(w, pos, minV, maxV, minAngle, maxAngle);
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
    public void createBurst(World w, int numParticles, Position cornerPos, float width,
            float height, float minV, float maxV, float minAngle, float maxAngle) {
        for(int i = 0; i < adjustCount(numParticles); i++)
            createBurstParticle(w,
                    dumPos2.set(cornerPos, w.rnd().nextFloat()*width, w.rnd().nextFloat()*height),
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
    public void createBurst(World w, int numParticles, Position pos,
            float minV, float maxV, float minAngle, float maxAngle,
            AABB aabb) {
        for(int i = 0; i < adjustCount(numParticles); i++)
            createBurstParticle(w,
                    dumPos2.set(pos,
                            aabb.minX() + w.rnd().nextFloat() * aabb.width(),
                            aabb.minY() + w.rnd().nextFloat() * aabb.height()
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
    public void createBurst(World w, int numParticles,
            float minV, float maxV, float minAngle, float maxAngle,
            Entity e) {
        createBurst(w, numParticles, e.pos, minV, maxV, minAngle, maxAngle, e.aabb);
    }
    
    /**
     * Same as {@link
     * #createBurst(int, Position, float, float, float, float)}, but
     * the particles are placed somewhere on the tile specified by the
     * given tile coordinates.
     */
    public void createBurstOnTile(World w, int numParticles, Position pos,
            float minV, float maxV, float minAngle, float maxAngle) {
        createBurst(w, numParticles, pos.clone().add(w.rnd().nextFloat(), w.rnd().nextFloat()),
                minV, maxV, minAngle, maxAngle);
    }
    
    private void createBurstParticle(World w, Position pos,
            float minV, float maxV, float minAngle, float maxAngle) {
        float v = minV + w.rnd().nextFloat() * (maxV - minV);
        float angle = minAngle + w.rnd().nextFloat() * (maxAngle - minAngle);
        float dx = v * MathUtils.cos(angle);
        float dy = v * MathUtils.sin(angle);
        createAlways(w, pos, dx, dy);
    }
    
    public void createOutwardsBurst(World w, int numParticles, Position pos,
            boolean burstX, boolean burstY, float maxX, float maxY,
            AABB aabb) {
        float dx = aabb.width();
        float dy = aabb.height();
        for(int i = 0; i < adjustCount(numParticles); i++) {
            float xp = w.rnd().nextFloat();
            float yp = w.rnd().nextFloat();
            createAlways(w,
                    dumPos2.set(pos, dx, dy),
                    burstX ? (2*xp - 1) * maxX : 0f,
                    burstY ? (2*yp - 1) * maxY : 0f
            );
        }
    }
    
    public void createOutwardsBurst(World w, int numParticles,
            boolean burstX, boolean burstY, float maxX, float maxY,
            Entity e) {
        createOutwardsBurst(w, numParticles, e.pos,
                burstX, burstY, maxX, maxY, e.aabb);
    }
    
    public void createCentredOutwardsBurst(World w, int numParticles,
            float minV, float maxV, Entity e) {
        dumPos2.set(e.pos, e.aabb.centreX(), e.aabb.centreY());
        for(int i = 0; i < adjustCount(numParticles); i++)
            createBurstParticle(w, dumPos2, minV, maxV, 0f, Maths.TAUf);
    }
    
}