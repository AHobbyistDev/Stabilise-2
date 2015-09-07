package com.stabilise.entity;

import com.stabilise.core.Settings;
import com.stabilise.entity.collision.LinkedHitbox;
import com.stabilise.entity.effect.EffectFire;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Rectangle;
import com.stabilise.world.AbstractWorld.ParticleSource;
import com.stabilise.world.World;

/**
 * A flaming projectile which deals damage to mobs.
 */
public class EntityFireball extends EntityProjectile {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The fireball hitbox template. */
    private static final AABB FIREBALL_BOUNDING_BOX = new AABB(-0.05f, -0.05f, 0.1f, 0.1f);
    private static final Rectangle FIREBALL_HITBOX = new Rectangle(-0.25f, -0.25f, 0.5f, 0.5f);
    /** Default fireball damage. */
    private static final int DEFAULT_FIREBALL_DAMAGE = 10;
    
    /** The number of ticks after which a fireball despawns. */
    private static final int DESPAWN_TICKS = 300;
    
    private final ParticleSource particleSrc;
    
    
    /** TODO: temporary */
    public EntityFireball() {
        super(null, null, null);
        particleSrc = null;
    }
    
    /**
     * Creates a new fireball entity.
     * 
     * @param world The world in which the fireball will be placed.
     * @param owner The fireball's owner.
     */
    public EntityFireball(World world, Entity owner) {
        this(world, owner, DEFAULT_FIREBALL_DAMAGE);
    }
    
    /**
     * Creates a new fireball entity.
     * 
     * @param world The world in which the fireball will be placed.
     * @param owner The fireball's owner.
     * @param damage The fireball's damage.
     */
    public EntityFireball(World world, Entity owner, int damage) {
        super(world, owner, new LinkedHitbox(owner, FIREBALL_HITBOX, damage));
        ((LinkedHitbox)hitbox).linkedEntity = this;
        hitbox.force = 0.3f;
        hitbox.effect = new EffectFire(300);
        hitbox.hits = 1000000; // TODO: temporary for fun
        hitbox.persistenceTimer = -1;
        particleSrc = world.getParticleManager().getSource(new ParticleFlame());
    }
    
    @Override
    protected AABB getAABB() {
        return FIREBALL_BOUNDING_BOX;
    }
    
    @Override
    public void update(World world) {
        super.update(world);
        
        float div = Math.abs(dx) + Math.abs(dy);
        if(div != 0) {
            hitbox.fx = dx / div;
            hitbox.fy = dy / div;
        }
        
        if(Settings.settingParticlesAll())
            addFlightParticles(world, 8);
        else if(Settings.settingParticlesReduced())
            addFlightParticles(world, 4);
        
        if(age == DESPAWN_TICKS)
            destroy();
    }
    
    @Override
    protected void impact(World world, float dv, boolean tileCollision) {
        destroy();
        
        if(tileCollision) {        // Since it removes itself with an entity collision
            if(Settings.settingParticlesAll())
                addImpactParticles(world, 500);
            else if(Settings.settingParticlesReduced())
                addImpactParticles(world, 25);
        }
    }
    
    private void addFlightParticles(World world, int particles) {
        particleSrc.createBurst(particles, x, y, 0.02f, 0.05f, 0f, (float)Maths.TAU);
    }
    
    /**
     * Creates fire particles about the fireball's location of impact.
     * 
     * @param particles The number of particles to create.
     */
    private void addImpactParticles(World world, int particles) {
        particleSrc.createBurst(particles, x, y, 0.0001f, 0.11f, 0f, (float)Maths.TAU);
    }
    
    @Override
    public void onAdd() {
        float div = Math.abs(dx) + Math.abs(dy);
        if(div != 0) {
            hitbox.fx = dx / div;
            hitbox.fy = dy / div;
        }
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderFireball(this);
    }
    
}
