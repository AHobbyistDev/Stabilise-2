package com.stabilise.entity.component.physics;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;

/**
 * Extremely crappy physics implementation
 */
public class CPhysicsImpl extends CPhysics {
    
    private static final float eps = 0.00001f;
    
    public boolean dxp, dyp; // "dx/dy positive?"
    public boolean onGround;
    public int floorTile;
    private final Position newPos = Position.create();
    
    private final Position tmp1 = Position.createFixed(); // for horizontal/verticalCollisions
    private final Position tmp2 = Position.createFixed(); // for row/columnValid
    
    
    @Override
    public void init(Entity e) {}
    
    @Override
    public void update(World w, Entity e) {
        float dxi = e.dx * w.getTimeIncrement();
        float dyi = e.dy * w.getTimeIncrement() + w.getGravity2ndOrder();
        
        dxp = dxi > 0;
        dyp = dyi > 0;
        
        onGround = false;
        
        if(dxi > 1.0f || dxi < -1.0f || dyi > 1.0f || dyi < -1.0f) {
            int divisor = Maths.ceil(Maths.max(Math.abs(dxi), Math.abs(dyi)));
            float xInc = dxi / divisor;   // x increments
            float yInc = dyi / divisor;   // y increments
            newPos.set(e.pos);
            boolean xCollided = false;
            boolean yCollided = false;
            
            for(int i = 0; i < divisor; i++) {
                newPos.add(xInc, yInc); // no need to align
                if(!yCollided && dyi != 0.0f)
                    yCollided = verticalCollisions(w, e);
                if(!xCollided && dxi != 0.0f)
                    xCollided = horizontalCollisions(w, e);
            }
        } else {
            // No need to align
            newPos.set(e.pos, dxi, dyi);
            
            if(dyi != 0.0f)
                verticalCollisions(w, e);
            if(dxi != 0.0f)
                horizontalCollisions(w, e);
        }
        
        // Even if unaligned, entity.update() will do it for us
        // nvm, need it aligned for getXFriction
        e.pos.set(newPos).align();
        
        e.dy += w.getGravityIncrement(); // apply after updating y
        
        e.dx *= getXFriction(w, e);
        e.dy *= getYFriction(w, e);
    }
    
    /**
     * Gets the horizontal friction factor
     */
    protected float getXFriction(World w, Entity e) {
        Tile groundTile = w.getTileAt(tmp1.set(e.pos).addY(-1).alignY());
        return 1 - groundTile.getFriction();
    }
    
    /**
     * Gets the vertical friction factor.
     */
    protected float getYFriction(World w, Entity e) {
        return 1f;
    }
    
    /**
     * Tests for horizontal collisions.
     * 
     * @return {@code true} if a collision is detected.
     */
    private boolean horizontalCollisions(World w, Entity e) {
        float leadingEdge = dxp ? e.aabb.maxX() : e.aabb.minX();
        
        // If the vertical wall is the same wall as the one the entity is
        // currently occupying, don't bother checking
        if(dxp ? Maths.ceil(newPos.lx()+leadingEdge) == Maths.ceil(e.pos.lx()+leadingEdge-eps)
               : Maths.floor(newPos.lx()+leadingEdge) == Maths.floor(e.pos.lx()+leadingEdge+eps))
            return false;
        
        // Check the vertical wall of tiles to the left/right of the entity
        int min = Maths.floor(newPos.ly() + e.aabb.minY());
        int max = Maths.floor(newPos.ly() + e.aabb.maxY());
        
        tmp1.set(newPos.sx, newPos.sy, newPos.lx()+leadingEdge, min).align();
        for(int y = min; y <= max; y++) {
            if(w.getTileAt(tmp1).isSolid() && rowValid(w, e, tmp1)) {
                collideHorizontal(w, e, tmp1);
                return true;
            }
            tmp1.addY(1).alignY();
        }
        return false;
    }
    
    /**
     * Tests for vertical collisions.
     * 
     * @return {@code true} if a collision is detected.
     */
    private boolean verticalCollisions(World w, Entity e) {
        float leadingEdge = dyp ? e.aabb.maxY() : e.aabb.minY();
        
        // If the horizontal wall is the same as the one the entity is
        // currently occupying, don't bother checking.
        if(dyp ? Maths.ceil(newPos.ly()+leadingEdge) == Maths.ceil(e.pos.ly()+leadingEdge-eps)
               : Maths.floor(newPos.ly()+leadingEdge) == Maths.floor(e.pos.ly()+leadingEdge+eps))
            return false;
        
        // Check the horizontal wall of tiles at the top/bottom of the entity
        int min = Maths.floor(newPos.lx() + e.aabb.minX());
        int max = Maths.ceil(newPos.lx() + e.aabb.maxX());
        
        tmp1.set(newPos.sx, newPos.sy, min, newPos.ly()+leadingEdge).align();
        for(int x = min; x < max; x++) {
            if(w.getTileAt(tmp1).isSolid() && columnValid(w, e, tmp1)) {
                collideVertical(w, e, tmp1);
                return true;
            }
            tmp1.addX(1).alignX();
        }
        return false;
    }
    
    /**
     * Returns true if a column of tiles above or below (depending on the
     * entity's vertical velocity) a given tile are valid tiles for the entity
     * to move into (that is, are non-solid).
     * 
     * @param pos The position of the tile to check about
     * 
     * @return {@code true} if the entity is able to move into the column.
     */
    private boolean columnValid(World w, Entity e, Position pos) {
        // Only check as many tiles above or below the tile in question that
        // the height of the entity's bounding box would require.
        int max = Maths.ceil(e.aabb.height());
        tmp2.set(pos);
        for(int i = 1; i <= max; i++) {
            if(w.getTileAt(tmp2.addY(dyp ? -1 : 1).alignY()).isSolid())
                return false;
        }
        return true;
    }
    
    /**
     * Returns true if a row of tiles to the left or right of (depending on the
     * entity's horizontal velocity) a given tile are valid tiles for the
     * entity to move into (that is, are non-solid).
     * 
     * @param pos The position of the tile to check about.
     * 
     * @return {@code true} if the entity is able to move into the row.
     */
    private boolean rowValid(World w, Entity e, Position pos) {
        // Only check as many tiles to the left or right of the tile in
        // question that the width of the entity's bounding box would require.
        int max = Maths.ceil(e.aabb.width());
        tmp2.set(pos);
        for(int i = 1; i <= max; i++) {
            if(w.getTileAt(tmp2.addX(dxp ? -1 : 1).alignX()).isSolid())
                return false;
        }
        return true;
    }
    
    /**
     * Causes the entity to horizontally collide with a tile.
     * 
     * @param collisionPos The position at which the collision is to be made.
     * Only the x-coord matters here.
     */
    private void collideHorizontal(World w, Entity e, Position collisionPos) {
        // Not currently used by anything so I'm not going to bother
        //e.post(w, ETileCollision.collisionH(e.dx));
        
        e.dx = 0;
        
        newPos.sx = collisionPos.sx;
        if(dxp)
        	newPos.setLx(collisionPos.lx() - e.aabb.maxX());
        else
        	newPos.setLx(collisionPos.lx() - e.aabb.minX() + 1);
    }
    
    /**
     * Causes the entity to vertically collide with a tile.
     * 
     * @param collisionPos The position at which the collision is to be made.
     */
    private void collideVertical(World w, Entity e, Position collisionPos) {
        // Not currently used by anything so I'm not going to bother
        //e.post(w, ETileCollision.collisionV(e.dy));
        
        e.dy = 0;
        
        newPos.sy = collisionPos.sy;
        if(dyp)
        	newPos.setLy(collisionPos.ly() - e.aabb.maxY());
        else {
        	newPos.setLy(collisionPos.ly() - e.aabb.minY() + 1);
            
        	//tmp3.set(newPos).addY(-1).alignY();
        	Tile t = w.getTileAt(collisionPos);
        	t.handleStep(w, collisionPos, e);
        	floorTile = t.getID();
        	onGround = true;
        }
    }
    
    @Override public boolean onGround() { return onGround; }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
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
