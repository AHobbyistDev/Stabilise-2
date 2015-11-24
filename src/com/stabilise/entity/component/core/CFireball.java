package com.stabilise.entity.component.core;

import com.stabilise.core.Settings;
import com.stabilise.entity.Entity;
import com.stabilise.entity.component.effect.EffectFire;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.hitbox.LinkedHitbox;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleSource;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.world.World;


public class CFireball extends BaseProjectile {
    
    /** The fireball hitbox template. */
    private static final AABB FIREBALL_BOUNDING_BOX = new AABB(-0.05f, -0.05f, 0.1f, 0.1f);
    private static final Polygon FIREBALL_HITBOX = Polygon.rectangle(-0.25f, -0.25f, 0.5f, 0.5f);
    /** Default fireball damage. */
    private static final int DEFAULT_FIREBALL_DAMAGE = 10;
    
    /** The number of ticks after which a fireball despawns. */
    private static final int DESPAWN_TICKS = 300;
    
    private ParticleSource<ParticleFlame> particleSrc;
    private int damage;
    
    public CFireball(long ownerID) {
        this(ownerID, DEFAULT_FIREBALL_DAMAGE);
    }
    
    public CFireball(long ownerID, int damage) {
        this.ownerID = ownerID;
        this.damage = damage;
    }
    
    @Override
    public void init(Entity e) {
        
    }
    
    @Override
    protected void onAdd(World w, Entity e) {
        super.onAdd(w, e);
        
        hitbox.force = 3f;
        hitbox.effects = tgt -> tgt.addComponent(new EffectFire(300));
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
    
    private void addFlightParticles(World w, Entity e, int particles) {
        particleSrc.createBurst(particles, e.x, e.y, 0.5f, 2.5f, 0f, Maths.TAUf);
    }
    
    /**
     * Creates fire particles about the fireball's location of impact.
     * 
     * @param particles The number of particles to create.
     */
    private void addImpactParticles(World w, Entity e, int particles) {
        particleSrc.createBurst(particles, e.x, e.y, 0.01f, 4.0f, 0f, (float)Maths.TAU);
    }
    
    @Override
    protected void onImpact(World w, Entity e) {
        e.destroy();
        
        addImpactParticles(w, e, 500);
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD)
            particleSrc = w.getParticleManager().getSource(ParticleFlame.class);
        return super.handle(w, e, ev);
    }
    
}
