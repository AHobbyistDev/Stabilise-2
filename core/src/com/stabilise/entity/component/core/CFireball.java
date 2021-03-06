package com.stabilise.entity.component.core;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.effect.CEffectFire;
import com.stabilise.entity.event.ELinkedHitboxCollision;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.hitbox.Hitbox;
import com.stabilise.entity.hitbox.LinkedHitbox;
import com.stabilise.entity.particle.ParticleExplosion;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.manager.ParticleEmitter;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.world.World;


public class CFireball extends CBaseProjectile {
    
    /** The fireball hitbox template. */
    private static final AABB FIREBALL_BOUNDING_BOX = new AABB(-0.0625f, -0.0625f, 0.125f, 0.125f);
    private static final Polygon FIREBALL_HITBOX = Polygon.rectangle(-0.25f, -0.25f, 0.5f, 0.5f);
    /** Default fireball damage. */
    private static final int DEFAULT_FIREBALL_DAMAGE = 10;
    
    private static final Polygon SPLASH_HITBOX = Polygon.circle(0f, 0f, 3f, 8);
    
    /** The number of ticks after which a fireball despawns. */
    private static final int DESPAWN_TICKS = 300;
    
    private ParticleEmitter<ParticleFlame> particleSrc;
    private ParticleEmitter<ParticleExplosion> explosionSrc;
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
    public void init(Entity e) {
        
    }
    
    @Override
    protected void onAdd(World w, Entity e) {
        super.onAdd(w, e);
        
        hitbox.force = 3f;
        hitbox.effects = tgt -> tgt.addComponent(new CEffectFire(60*7, 2));
        hitbox.hits = 3;
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
    public void update(World w, Entity e, float dt) {
        super.update(w, e, dt);
        
        float div = Math.abs(e.dx) + Math.abs(e.dy);
        if(div != 0) {
            hitbox.fx = e.dx / div;
            hitbox.fy = e.dy / div;
        }
        
        addFlightParticles(w, e, 8);
        
        if(e.age == DESPAWN_TICKS)
            e.destroy();
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderFireball(e, this);
    }
    
    private void addFlightParticles(World w, Entity e, int particles) {
        particleSrc.createBurst(w, particles, e.pos, 0.5f, 2.5f, 0f, Maths.TAUf);
    }
    
    /**
     * Creates fire particles about the fireball's location of impact.
     */
    private void addImpactParticles(World w, Entity e, int particles) {
        particleSrc.createBurst(w, particles, e.pos, 0.01f, 4.0f, 0f, Maths.TAUf);
    }
    
    @Override
    protected void onImpact(World w, Entity e) {
        if(e.isDestroyed())
            return;
        e.destroy();
        
        /*
        // Destroy a bunch of nearby blocks
        final float range = 5.75f;
        int min = Maths.floor(-range);
        int max = Maths.floor(range);
        Position tmp = Position.createFixed().set(e.pos, -range, -range);
        
        for(int y = min; y <= max; y++) {
        	for(int x = min; x <= max; x++) {
        		if(tmp.isWithinRange(e.pos, range) && w.getTileAt(tmp.align()).getHardness() <= 5f)
        			w.breakTileAt(tmp);
        		tmp.add(1f, 0f);
        	}
        	tmp.add(max - min + 1, 1f);
        }
        //*/
        
        Hitbox h = new Hitbox(ownerID, SPLASH_HITBOX, 2*damage, hitbox.entitiesHit);
        h.persistent = true;
        h.persistenceTimer = 3;
        h.stickToOwner = false;
        h.hits = -1;
        h.effects = tgt -> tgt.addComponent(new CEffectFire(60*5, 1));
        w.addHitbox(h, e.pos);
        
        explosionSrc.createAlwaysAt(w, e.pos);
        addImpactParticles(w, e, 500);
        
        // TODO: shake again
        //w.getCamera().shake(0.1f, 30);
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD) {
            particleSrc = w.particleEmitter(ParticleFlame.class);
            explosionSrc = w.particleEmitter(ParticleExplosion.class);
        } else if(ev.type() == EntityEvent.Type.HITBOX_COLLISION) {
            if(((ELinkedHitboxCollision)ev).hitsRemaining == 0)
                onImpact(w, e);
        }
        return super.handle(w, e, ev);
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
}
