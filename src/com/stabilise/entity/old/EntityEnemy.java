//package com.stabilise.entity.old;
//
//import com.stabilise.entity.component.controller.EnemyController;
//import com.stabilise.item.Items;
//import com.stabilise.opengl.render.WorldRenderer;
//import com.stabilise.util.Direction;
//import com.stabilise.util.shape.AABB;
//import com.stabilise.world.World;
//
///**
// * A generic test enemy.
// */
//public class EntityEnemy extends EntityMob {
//    
//    /** The AABB for enemy entities. */
//    private static final AABB ENEMY_AABB = new AABB(-0.5f, 0, 1, 2);
//    
//    @Override
//    protected AABB getAABB() {
//        return ENEMY_AABB;
//    }
//    
//    @Override
//    protected void initProperties() {
//        // Temporary initial value setting
//        maxHealth = 20;
//        health = 20;
//        
//        jumpVelocity = 15f;
//        jumpCrouchDuration = 8;
//        //jumpVelocity = PhysicsUtil.jumpHeightToInitialJumpVelocity(4, gravity);
//        swimAcceleration = 0.08f;
//        acceleration = 1.3f;
//        airAcceleration = AIR_TRACTION;
//        maxDx = 13f;
//        
//        state = State.IDLE;
//        
//        setController(new EnemyController());
//    }
//    
//    @Override
//    public void onAdd() {
//        //world.hostileMobCount++;
//    }
//    
//    @Override
//    public void render(WorldRenderer renderer) {
//        renderer.renderEnemy(this);
//    }
//
//    @Override
//    public void kill(World world) {
//        super.kill(world);
//        
//        dropItem(world, Items.SWORD, 1, 0.75f);      // sword
//        dropItem(world, Items.APPLE, 1, 0.75f);      // apple
//        dropItem(world, Items.ARROW, 1, 0.75f);      // arrow
//    }
//    
//    @Override
//    public void destroy() {
//        super.destroy();
//        //world.hostileMobCount--;
//    }
//    
//    @Override
//    public void attack(Direction direction) {
//        
//    }
//    
//    @Override
//    public void specialAttack(Direction direction) {
//        
//    }
//    
//}
