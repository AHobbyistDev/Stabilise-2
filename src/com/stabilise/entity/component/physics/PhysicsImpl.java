package com.stabilise.entity.component.physics;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.ComponentEvent;
import com.stabilise.util.Direction;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;

/**
 * Extremely crappy physics implementation
 */
public class PhysicsImpl implements CPhysics {
    
    protected static final float AIR_FRICTION = 0.001f;
    
    
    public float dxi, dyi; // dx, dy integrals
    public boolean dxp, dyp; // dx/dy positive
    public boolean onGround;
    public int floorTile;
    
    @Override
    public void init(World w, Entity e) {}
    
    @Override
    public void update(World w, Entity e) {
        //if(dx != 0)
        //    dx *= (1-friction);
        
        // Thanks to calculus, we know that y at the next step (y_next) is
        // given by (where y' is dy at the end of the last step, and y'' is
        // gravitational accel).
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
            double px = e.x + xInc;        // projected x
            double py = e.y + yInc;        // projected y
            boolean xCollided = false;
            boolean yCollided = false;
            
            for(int i = 0; i < Math.ceil(divisor); i++) {
                if(!yCollided)
                    yCollided = verticalCollisions(w, e, px, py);
                if(!xCollided)
                    xCollided = horizontalCollisions(w, e, px, py);
                px += xInc;
                py += yInc;
            }
        } else {
            //double xp = x + dx + (dx > 0 ? boundingBox.p11.x : boundingBox.p00.x);        // projected x
            //double yp = y + dy + (dy > 0 ? boundingBox.p11.y : boundingBox.p00.y);        // projected y
            
            double px = e.x + dxi;        // projected x
            double py = e.y + dyi;        // projected y
            
            verticalCollisions(w, e, px, py);
            //collideHorizontal(xp, yp);
            
            // TODO: This is broken now that I use dyi instead of dy
            // The following is necessary because otherwise gravity will offset the vertical
            // wall being checked for sideways collisions slightly when on the ground.
            horizontalCollisions(w, e, px, e.y + dyi);
        }
        
        e.x += dxi;
        e.y += dyi;
        
        e.dy += w.getGravityIncrement(); // apply after updating y
        
        e.dx *= getXFriction(w, e);
        e.dy *= getYFriction(w, e);
    }
    
    /**
     * Gets the frictive force acting on the entity.
     * 
     * @return The frictive force.
     */
    protected float getXFriction(World w, Entity e) {
        // TODO: soooooooooo temporary
        Tile groundTile = w.getTileAt(e.x, e.y - 0.01);
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
     * @param xp The entity's projected x-coordinate.
     * @param yp The entity's projected y-coordinate.
     * 
     * @return {@code true} if a collision is detected.
     */
    private boolean horizontalCollisions(World w, Entity e, double xp, double yp) {
        if(e.dx == 0) return false;
        
        float leadingEdge = dxp ? e.aabb.maxX() : e.aabb.minX();
        
        xp += leadingEdge;
        
        // If the vertical wall is the same wall as the one the entity is
        // currently occupying, don't bother checking
        if(dxp ? Math.ceil(xp) == Math.ceil(e.x + leadingEdge) : Math.floor(xp) == Math.floor(e.x + leadingEdge))
            return false;
        
        // Check the vertical wall of tiles to the left/right of the entity
        
        //double max = dyp ? Math.ceil(yp + boundingBox.p11.y) : Math.ceil(yp + boundingBox.p11.y);
        double max = Math.ceil(yp + e.aabb.maxY());
        
        // TODO: < vs <= - watch out for this, it may cause problems in the future
        for(double v = yp + e.aabb.minY(); v < max; v++) {
            if(w.getTileAt(xp, v).isSolid() && rowValid(w, e, xp, v)) {
                //x = dxp ? Math.floor(xp) - boundingBox.p11.x : Math.ceil(xp) - boundingBox.p00.x;
                // Alternatively... (doesn't really matter though)
                //x = dxp ? Math.floor(xp) - leadingEdge : Math.ceil(xp) - leadingEdge;
                //dx = 0;
                collideHorizontal(w, e, xp, dxp ? Direction.RIGHT : Direction.LEFT);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tests for all vertical collisions.
     * 
     * @param xp The entity's projected x-coordinate.
     * @param yp The entity's projected y-coordinate.
     * 
     * @return {@code true} if a collision is detected.
     */
    private boolean verticalCollisions(World w, Entity e, double xp, double yp) {
        if(dyi == 0.0f) return false;
        
        float leadingEdge = dyp ? e.aabb.maxY() : e.aabb.minY();
        
        yp += leadingEdge;
        
        // If the horizontal wall is the same as the one the entity is
        // currently occupying, don't bother checking.
        if(dyp ? Math.ceil(yp) == Math.ceil(e.y + leadingEdge) : Math.floor(yp) == Math.floor(e.y + leadingEdge))
            return false;
        
        // Check the horizontal wall of tiles at the top/bottom of the entity
        
        //double max = dxp ? Math.ceil(xp + boundingBox.p11.x) : Math.ceil(xp + boundingBox.p11.x);
        double max = Math.ceil(xp + e.aabb.maxX());
        
        // TODO: < vs <= - watch out for this, it may cause problems in the future
        for(double h = xp + e.aabb.minX(); h < max; h++) {
            if(w.getTileAt(h, yp).isSolid() && columnValid(w, e, h, yp)) {
                //y = dyp ? Math.floor(yp) - boundingBox.p11.y : Math.ceil(yp) - boundingBox.p00.y;
                //onGround = dy < 0;
                //dy = 0;
                collideVertical(w, e, yp, dyp ? Direction.UP : Direction.DOWN);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if a column of tiles above or below (depending on the
     * entity's vertical velocity) a given tile are valid tiles for the entity
     * to move into (that is, are non-solid).
     * 
     * @param x The x-coordinate of the tile to check about.
     * @param y The y-coordinate of the tile to check about.
     * 
     * @return {@code true} if and only if the entity is able to move into the
     * column.
     */
    private boolean columnValid(World w, Entity e, double x, double y) {
        // Only check as many tiles above or below the tile in question that
        // the height of the entity's bounding box would require.
        int max = Maths.ceil(e.aabb.height());
        for(int i = 1; i <= max; i++) {
            if(w.getTileAt(x, y + (dyp ? -i : i)).isSolid())
                return false;
        }
        return true;
    }
    
    /**
     * Returns true if a row of tiles to the left or right of (depending on the
     * entity's horizontal velocity) a given tile are valid tiles for the
     * entity to move into (that is, are non-solid).
     * 
     * @param x The x-coordinate of the tile to check about.
     * @param y The y-coordinate of the tile to check about.
     * 
     * @return {@code true} if and only if the entity is able to move into the
     * row.
     */
    private boolean rowValid(World w, Entity e, double x, double y) {
        // Only check as many tiles to the left or right of the tile in
        // question that the width of the entity's bounding box would require.
        int max = Maths.ceil(e.aabb.width());
        for(int i = 1; i <= max; i++) {
            if(w.getTileAt(x + (dxp ? -i : i), y).isSolid())
                return false;
        }
        return true;
    }
    
    /**
     * Causes the entity to horizontally collide with a tile.
     * 
     * @param xp The x position at which the collision is to be made.
     * @param direction The direction relative to the entity that the tile the
     * entity has collided with is located.
     */
    private void collideHorizontal(World w, Entity e, double xp, Direction direction) {
        ComponentEvent.COLLISION.post(w, e);
        ComponentEvent.COLLISION_HORIZONTAL.post(w, e);
        ComponentEvent.COLLISION_TILE.post(w, e);
        //impact(w, e, e.dx, true);
        
        e.dx = dxi = 0;
        
        if(direction == Direction.RIGHT) {
            e.x = Math.floor(xp) - e.aabb.maxX();
        } else {
            e.x = Math.ceil(xp) - e.aabb.minX();
        }
    }
    
    /**
     * Causes the entity to horizontally collide with a tile.
     * 
     * @param yp The x position at which the collision is to be made.
     * @param direction The direction relative to the entity that the tile the
     * entity has collided with is located.
     */
    private void collideVertical(World w, Entity e, double yp, Direction direction) {
        ComponentEvent.COLLISION.post(w, e);
        ComponentEvent.COLLISION_VERTICAL.post(w, e);
        ComponentEvent.COLLISION_TILE.post(w, e);
        //impact(w, e, e.dy, true);
        
        e.dy = dyi = 0;
        
        if(direction == Direction.UP) {
            e.y = Math.floor(yp) - e.aabb.maxY();
        } else {
            e.y = Math.ceil(yp) - e.aabb.minY();
            
            // TODO: Find a better way of doing this
            int tx = Maths.floor(e.x);
            int ty = Maths.floor(e.y - 0.001D);
            Tile t = w.getTileAt(tx, ty);
            t.handleStep(w, tx, ty, e);
            floorTile = t.getID();
            onGround = true;
        }
    }
    
    @Override public boolean onGround() { return onGround; }
    
    @Override
    public void handle(World w, Entity e, ComponentEvent ev) {
        
    }
    
}
