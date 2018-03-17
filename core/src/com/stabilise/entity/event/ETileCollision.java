package com.stabilise.entity.event;


public class ETileCollision extends EntityEvent {
    
    public final float dv;
    
    private ETileCollision(Type type, float dv) {
        super(type);
        this.dv = dv;
    }
    
    //public static ETileCollision collision(float dv) {
    //    return new ETileCollision(Type.TILE_COLLISION, dv);
    //}
    
    public static ETileCollision collisionH(float dv) {
        return new ETileCollision(Type.TILE_COLLISION_H, dv);
    }
    
    public static ETileCollision collisionV(float dv) {
        return new ETileCollision(Type.TILE_COLLISION_V, dv);
    }
    
}
