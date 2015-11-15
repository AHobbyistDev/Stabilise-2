package com.stabilise.entity.component.core;

import com.stabilise.core.Settings;
import com.stabilise.entity.Entity;
import com.stabilise.entity.effect.EffectFire;
import com.stabilise.entity.hitbox.LinkedHitbox;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.world.World;
import com.stabilise.world.AbstractWorld.ParticleSource;


public class CFireball extends BaseProjectile {
    
    /** The fireball hitbox template. */
    private static final AABB FIREBALL_BOUNDING_BOX = new AABB(-0.05f, -0.05f, 0.1f, 0.1f);
    private static final Polygon FIREBALL_HITBOX = Polygon.rectangle(-0.25f, -0.25f, 0.5f, 0.5f);
    /** Default fireball damage. */
    private static final int DEFAULT_FIREBALL_DAMAGE = 10;
    
    /** The number of ticks after which a fireball despawns. */
    private static final int DESPAWN_TICKS = 300;
    
    private ParticleSource particleSrc;
    private int damage;
    
    public CFireball() {}
    
    public CFireball(long ownerID) {
        this(ownerID, DEFAULT_FIREBALL_DAMAGE);
    }
    
    public CFireball(long ownerID, int damage) {
        this.ownerID = ownerID;
        this.damage = damage;
    }
    
    @Override
    public void init(World w, Entity e) {
        super.init(w, e);
        
        ownerID = e.id();
        
        particleSrc = w.getParticleManager().getSource(new ParticleFlame());
        hitbox.force = 0.3f;
        hitbox.effect = new EffectFire(300);
        hitbox.hits = 1000000; // TODO: temporary for fun
        hitbox.persistenceTimer = -1;
        
        float div = Math.abs(e.dx) + Math.abs(e.dy);
        if(div != 0) {
            hitbox.fx = e.dx / div;
            hitbox.fy = e.dy / div;
        }
    }
    
    @Override
    public AABB getAABB() {
        return FIREBALL_BOUNDING_BOX;
    }
    
    @Override
    protected LinkedHitbox getHitbox(Entity e, long ownerID) {
        return new LinkedHitbox(ownerID, FIREBALL_HITBOX, damage, e.id());
    }
    
    @Override
    public void update(World w, Entity e) {
        super.update(w, e);
        
        float div = Math.abs(e.dx) + Math.abs(e.dy);
        if(div != 0) {
            hitbox.fx = e.dx / div;
            hitbox.fy = e.dy / div;
        }
        
        if(Settings.settingParticlesAll())
            addFlightParticles(w, e, 8);
        else if(Settings.settingParticlesReduced())
            addFlightParticles(w, e, 4);
        
        if(e.age == DESPAWN_TICKS)
            e.destroy();
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderFireball(e, this);
    }
    
    protected void impact(World w, Entity e, float dv, boolean tileCollision) {

    }
    
    private void addFlightParticles(World w, Entity e, int particles) {
        particleSrc.createBurst(particles, e.x, e.y, 0.02f, 0.05f, 0f, (float)Maths.TAU);
    }
    
    /**
     * Creates fire particles about the fireball's location of impact.
     * 
     * @param particles The number of particles to create.
     */
    private void addImpactParticles(World w, Entity e, int particles) {
        particleSrc.createBurst(particles, e.x, e.y, 0.0001f, 0.11f, 0f, (float)Maths.TAU);
    }
    
    @Override
    protected void onImpact(World w, Entity e) {
        e.destroy();
        
        if(Settings.settingParticlesAll())
            addImpactParticles(w, e, 500);
        else if(Settings.settingParticlesReduced())
            addImpactParticles(w, e, 25);
    }
    
}
