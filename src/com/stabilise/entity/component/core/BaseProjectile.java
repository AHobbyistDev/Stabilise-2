package com.stabilise.entity.component.core;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.hitbox.LinkedHitbox;
import com.stabilise.util.shape.Shape;
import com.stabilise.world.World;


public abstract class BaseProjectile extends CCore {
    
    protected long ownerID;
    /** The projectile's hitbox. */
    public LinkedHitbox hitbox;
    
    private Shape baseShape;
    /** The rotation of the projectile. */
    public float rotation = 0;
    
    protected abstract LinkedHitbox getHitbox(Entity e, long ownerID);
    
    protected void onAdd(World w, Entity e) {
        hitbox = getHitbox(e, ownerID);
        baseShape = hitbox.boundingBox;
        hitbox.persistent = true;
        rotate(e);
        w.addHitbox(hitbox, e.x, e.y);
    }
    
    @Override
    public void update(World w, Entity e) {
        if(e.isDestroyed()) {
            hitbox.destroy();
            return;
        }
        
        rotate(e);
    }
    
    /**
     * Updates the projectile's rotation.
     */
    protected void rotate(Entity e) {
        rotation = MathUtils.atan2(e.dy, e.dx);
        
        if(hitbox != null)
            hitbox.boundingBox = baseShape.rotate(rotation);
        
        e.facingRight = e.dx > 0;
    }
    
    protected void onImpact(World w, Entity e) {
        e.destroy();
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD)
            onAdd(w, e);
        else if(ev.type() == EntityEvent.Type.TILE_COLLISION)
            onImpact(w, e);
        return false;
    }
    
}
