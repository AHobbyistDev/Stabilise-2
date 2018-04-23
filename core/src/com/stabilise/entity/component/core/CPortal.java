package com.stabilise.entity.component.core;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.AbstractWorld;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;


/**
 * Extremely crappy pre-placeholder portal implementation. Absolutely nothing
 * like the (hopefully) intended final product.
 */
public class CPortal extends CCore {
    
    public static final Vector2 LEFT = new Vector2(-1, 0);
    public static final Vector2 RIGHT = new Vector2(1, 0);
    
    private static enum State {
        WAITING_FOR_DIMENSION,
        OPEN,
        CLOSED
    }
    
    /** 1 block wide, 3 blocks high. */
    private static final AABB AABB = new AABB(-0.5f, 0f, 1.0f, 3f);
    
    private String pairedDimension;
    private State state = State.WAITING_FOR_DIMENSION;
    
    public Vector2 direction = LEFT;
    
    
    /** The position of the other portal in its own dimension. */
    public final Position otherPortalPos = Position.create();
    /** The position offset that an entity undergoes when moving through this
     * portal. This is computed when this portal is added to the world. */
    public final Position offset = Position.create();
    
    
    public CPortal(String dimension) {
        this.pairedDimension = dimension;
    }
    
    @Override
    public void update(World w, Entity e) {
        HostWorld w2 = (HostWorld)w.multiverse().getDimension(pairedDimension);
        switch(state) {
            case WAITING_FOR_DIMENSION:
                if(w2.getRegionAt(0, 0).state.isActive()) {
                    Entity otherEnd = Entities.portal(((AbstractWorld)w).getDimensionName());
                    CPortal otherCore = (CPortal)otherEnd.core;
                    otherCore.direction = new Vector2(-direction.x, -direction.y);
                    otherCore.state = State.OPEN;
                    w2.addEntity(otherEnd, Position.create(0, 0, 8f, 8f));
                    
                    //otherCore.pairedPortalID = e.id();
                    //pairedPortalID = otherEnd.id();
                    
                    state = State.OPEN;
                }
                break;
            case OPEN:
                break;
            case CLOSED:
                break;
            default:
                break;
        }
        
        w.getEntities().forEach(e2 -> {
            if(!(e2.core instanceof CPortal)) {
                if(e2.core.getAABB().intersects(getAABB(), e.pos.diffX(e2.pos), e.pos.diffY(e2.pos))) {
                    w.multiverse().sendToDimension(w2, pairedDimension, e2, otherPortalPos);
                }
            }
        });
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderPortal(e, this);
    }
    
    @Override
    public AABB getAABB() {
        return AABB;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        switch(ev.type()) {
            case ADDED_TO_WORLD:
            	// Align and clamp our position, just to be safe
            	e.pos.align().clampToTile();
            	otherPortalPos.align().clampToTile();
            	
                w.multiverse().loadDimension(pairedDimension);
                break;
            default:
                break;
        }
        return false;
    }
    
    public boolean isOpen() {
        return state == State.OPEN;
    }
    
}
