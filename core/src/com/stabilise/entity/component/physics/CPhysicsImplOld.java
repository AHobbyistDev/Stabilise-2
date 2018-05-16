package com.stabilise.entity.component.physics;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.event.ETileCollision;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Checks;
import com.stabilise.util.Direction;
import com.stabilise.util.Log;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;

/**
 * Extremely crappy physics implementation
 */
public class CPhysicsImplOld extends CPhysics {
    
    protected static final float AIR_FRICTION = 0.001f;
    
    
    public float dxi, dyi; // dx, dy integrals
    public boolean dxp, dyp; // dx/dy positive
    public boolean onGround;
    public int floorTile;
    private final Position projPos = Position.create(); // for update()
    private final Position tmp = Position.create();  // for horizontalCollisions and verticalCollisions
    private final Position tmp2 = Position.create(); // for update() to give to horizontalCollisions and verticalCollisions
    private final Position tmp3 = Position.create(); // for rowValid and columnValid
    private final Position tmp4 = Position.create(); // for collideVertical
    
    
    @Override
    public void init(Entity e) {}
    
    @Override
    public void update(World w, Entity e) {
        //if(dx != 0)
        //    dx *= (1-friction);
        
        // We know that y at the next step is given by (where y' is dy at the
    	// end of the last step, and y'' is gravitational accel).
        // y_next = y + dy = y + y't + (1/2)y''t^2
        // In other words, in this step the net effect is the following
        // (assuming we don't run into a wall or something):
        // y_next = y_old + dy_old*t + 2ndOrderGravityValue
        // dy_next = dy_old + gravity*t
        
        dxi = e.dx * w.getTimeIncrement();
        dyi = e.dy * w.getTimeIncrement() + w.getGravity2ndOrder();
        
        dxp = dxi > 0;
        dyp = dyi > 0;
        
        onGround = false;
        
        if(dxi > 1.0f || dxi < -1.0f || dyi > 1.0f || dyi < -1.0f) {
            // TODO: vertical wall offsetting for higher velocities
            // That is, currently this is pretty screwy and glitches out
            
            float divisor = Math.abs(dxi) > Math.abs(dyi) ? Math.abs(dxi) : Math.abs(dyi);
            float xInc = dxi / divisor;        // x increments
            float yInc = dyi / divisor;        // y increments
            //double px = e.pos.getGlobalX() + xInc;        // projected x
            //double py = e.pos.getGlobalY() + yInc;        // projected y
            projPos.set(e.pos, xInc, yInc);
            boolean xCollided = false;
            boolean yCollided = false;
            
            for(int i = 0; i < Math.ceil(divisor); i++) {
                if(!yCollided)
                    yCollided = verticalCollisions(w, e, tmp2.set(projPos));
                if(!xCollided)
                    xCollided = horizontalCollisions(w, e, tmp2.set(projPos));
                projPos.add(xInc, yInc);
                //px += xInc;
                //py += yInc;
            }
        } else {
            //double xp = x + dx + (dx > 0 ? boundingBox.p11.x : boundingBox.p00.x);        // projected x
            //double yp = y + dy + (dy > 0 ? boundingBox.p11.y : boundingBox.p00.y);        // projected y
            
            //double px = e.pos.getGlobalX() + dxi;        // projected x
            //double py = e.pos.getGlobalY() + dyi;        // projected y
            projPos.set(e.pos, dxi, dyi);
            
            verticalCollisions(w, e, projPos);
            //collideHorizontal(xp, yp);
            
            // TODO: This is broken now that I use dyi instead of dy
            // TODO: what does the above comment even mean? it's been so long I don't remember
            // The following is necessary because otherwise gravity will offset the vertical
            // wall being checked for sideways collisions slightly when on the ground.
            horizontalCollisions(w, e, projPos.set(e.pos, dxi, dyi));
        }
        
        //e.x += dxi;
        //e.y += dyi;
        e.pos.add(dxi, dyi);
        
        e.dy += w.getGravityIncrement(); // apply after updating y
        
        e.dx *= getXFriction(w, e);
        e.dy *= getYFriction(w, e);
    }
    
    /**
     * Gets the frictive force acting on the entity.
     */
    protected float getXFriction(World w, Entity e) {
        Tile groundTile = w.getTileAt(tmp.set(e.pos, 0f, -0.01f).align());
        return 1 - groundTile.getFriction();
    }
    
    /**
     * Gets the vertical frictive force acting on the entity.
     * 
     * @return The frictive force.
     */
    protected float getYFriction(World w, Entity e) {
        return 1f;
    }
    
    /**
     * Gets the air friction.
     * 
     * @return The air friction acting on the entity.
     */
    protected final float getAirFriction() {
        // TODO: Possibly a temporary method
        return AIR_FRICTION;
    }
    
    /**
     * Gets the tile friction.
     * 
     * @return The tile friction acting on the entity.
     */
    protected float getTileFriction() {
        // TODO: Possibly a temporary method
        if(onGround)
            return Tile.getTile(floorTile).getFriction();
        else
            return 0;
    }
    
    /**
     * Tests for all horizontal collisions.
     * 
     * @param proj Projected position.
     * 
     * @return {@code true} if a collision is detected.
     */
    private boolean horizontalCollisions(World w, Entity e, Position proj) {
        if(dxi == 0) return false;
        
        float leadingEdge = dxp ? e.aabb.maxX() : e.aabb.minX();
        
        proj.addX(leadingEdge);
        
        // If the vertical wall is the same wall as the one the entity is
        // currently occupying, don't bother checking
        if(dxp ? Math.ceil(proj.lx()) == Math.ceil(e.pos.lx() + leadingEdge) : Math.floor(proj.lx()) == Math.floor(e.pos.lx() + leadingEdge))
            return false;
        
        // Check the vertical wall of tiles to the left/right of the entity
        
        //double max = dyp ? Math.ceil(yp + boundingBox.p11.y) : Math.ceil(yp + boundingBox.p11.y);
        float max = Maths.ceil(proj.ly() + e.aabb.maxY());
        
        // TODO: < vs <= - watch out for this, it may cause problems in the future
        for(float v = proj.ly() + e.aabb.minY(); v < max; v++) {
        	tmp.set(proj.sx, proj.sy, proj.lx(), v).align();
            if(w.getTileAt(tmp).isSolid() && rowValid(w, e, tmp)) {
                //x = dxp ? Math.floor(xp) - boundingBox.p11.x : Math.ceil(xp) - boundingBox.p00.x;
                // Alternatively... (doesn't really matter though)
                //x = dxp ? Math.floor(xp) - leadingEdge : Math.ceil(xp) - leadingEdge;
                //dx = 0;
                collideHorizontal(w, e, tmp, dxp ? Direction.RIGHT : Direction.LEFT);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tests for all vertical collisions.
     * 
     * @param proj The entity's projected position.
     * 
     * @return {@code true} if a collision is detected.
     */
    private boolean verticalCollisions(World w, Entity e, Position proj) {
        if(dyi == 0.0f) return false;
        
        float leadingEdge = dyp ? e.aabb.maxY() : e.aabb.minY();
        
        proj.addY(leadingEdge);
        
        // If the horizontal wall is the same as the one the entity is
        // currently occupying, don't bother checking.
        if(dyp ? Math.ceil(proj.ly()) == Math.ceil(e.pos.ly() + leadingEdge) : Math.floor(proj.ly()) == Math.floor(e.pos.ly() + leadingEdge))
            return false;
        
        // Check the horizontal wall of tiles at the top/bottom of the entity
        
        //double max = dxp ? Math.ceil(xp + boundingBox.p11.x) : Math.ceil(xp + boundingBox.p11.x);
        float max = Maths.ceil(proj.lx() + e.aabb.maxX());
        
        // TODO: < vs <= - watch out for this, it may cause problems in the future
        for(float h = proj.lx() + e.aabb.minX(); h < max; h++) {
        	tmp.set(proj.sx, proj.sy, h, proj.ly()).align();
        	try {
                if(w.getTileAt(tmp).isSolid() && columnValid(w, e, tmp)) {
                    //y = dyp ? Math.floor(yp) - boundingBox.p11.y : Math.ceil(yp) - boundingBox.p00.y;
                    //onGround = dy < 0;
                    //dy = 0;
                    collideVertical(w, e, tmp, dyp ? Direction.UP : Direction.DOWN);
                    return true;
                }
        	} catch(ArrayIndexOutOfBoundsException ex) {
        	    Log.getAgent("ASDFGHJKL").postSevere("It hath strucketh", ex);
        	}
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
     * @return {@code true} if and only if the entity is able to move into the
     * column.
     */
    private boolean columnValid(World w, Entity e, Position pos) {
        // Only check as many tiles above or below the tile in question that
        // the height of the entity's bounding box would require.
        int max = Maths.ceil(e.aabb.height());
        for(int i = 1; i <= max; i++) {
            if(w.getTileAt(tmp3.set(pos).add(0f, dyp ? -i : i).align()).isSolid())
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
     * @return {@code true} if and only if the entity is able to move into the
     * row.
     */
    private boolean rowValid(World w, Entity e, Position pos) {
        // Only check as many tiles to the left or right of the tile in
        // question that the width of the entity's bounding box would require.
        int max = Maths.ceil(e.aabb.width());
        for(int i = 1; i <= max; i++) {
            if(w.getTileAt(tmp3.set(pos).add(dxp ? -i : i, 0f).align()).isSolid())
                return false;
        }
        return true;
    }
    
    /**
     * Causes the entity to horizontally collide with a tile.
     * 
     * @param collisionPos The position at which the collision is to be made.
     * Only the x-coord matters here.
     * @param direction The direction relative to the entity that the tile the
     * entity has collided with is located.
     */
    private void collideHorizontal(World w, Entity e, Position collisionPos, Direction direction) {
        //e.post(w, ETileCollision.collision(dxi));
        e.post(w, ETileCollision.collisionH(dxi));
        
        e.dx = dxi = 0;
        
        e.pos.sx = collisionPos.sx;
        if(direction == Direction.RIGHT) {
        	e.pos.setLx(Maths.floor(collisionPos.lx()) - e.aabb.maxX());
            //e.x = Math.floor(xp) - e.aabb.maxX();
        } else {
        	e.pos.setLx(Maths.ceil(collisionPos.lx()) - e.aabb.minX());
            //e.x = Math.ceil(xp) - e.aabb.minX();
        }
    }
    
    /**
     * Causes the entity to horizontally collide with a tile.
     * 
     * @param collisionPos The position at which the collision is to be made.
     * Only the y-coord matters here.
     * @param direction The direction relative to the entity that the tile the
     * entity has collided with is located.
     */
    private void collideVertical(World w, Entity e, Position collisionPos, Direction direction) {
        //e.post(w, ETileCollision.collision(dyi));
        e.post(w, ETileCollision.collisionV(dyi));
        
        e.dy = dyi = 0;
        
        e.pos.sy = collisionPos.sy;
        if(direction == Direction.UP) {
        	e.pos.setLy(Maths.floor(collisionPos.ly()) - e.aabb.maxY());
            //e.y = Math.floor(yp) - e.aabb.maxY();
        } else {
        	e.pos.setLy(Maths.ceil(collisionPos.ly()) - e.aabb.minY());
            //e.y = Math.ceil(yp) - e.aabb.minY();
            
            // TODO: Find a better way of doing this
        	tmp4.set(e.pos).add(0f, -0.001f).clampToTile().align();
        	Tile t = w.getTileAt(tmp4);
        	t.handleStep(w, tmp4, e);
        	floorTile = t.getID();
        	onGround = true;
            //int tx = Maths.floor(e.x);
            //int ty = Maths.floor(e.y - 0.001D);
            //Tile t = w.getTileAt(tx, ty);
            //t.handleStep(w, tx, ty, e);
            //floorTile = t.getID();
            //onGround = true;
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
