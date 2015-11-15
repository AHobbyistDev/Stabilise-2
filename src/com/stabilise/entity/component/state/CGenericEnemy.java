package com.stabilise.entity.component.state;

import com.stabilise.entity.Entity;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.shape.AABB;


public class CGenericEnemy extends CBaseMob {
    
    private static final AABB ENEMY_AABB = new AABB(-0.5f, 0, 1, 2);
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderEnemy(e, this);
    }
    
    @Override
    public AABB getAABB() {
        return ENEMY_AABB;
    }
    
    @Override
    public void attack(Direction direction) {
        
    }
    
    @Override
    public void specialAttack(Direction direction) {
        
    }
    
}
