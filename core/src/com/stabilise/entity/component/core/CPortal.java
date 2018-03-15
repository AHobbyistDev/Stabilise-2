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
        WAITING_FOR_REGION,
        OPEN,
        CLOSED
    }
    
    private static final AABB AABB = new AABB(-0.25f, 0f, 0.5f, 2f);
    private static final Position SEND_TO_POS = Position.create(0, 0, 8f, 8f);
    
    private String pairedDimension;
    @SuppressWarnings("unused")
    private long pairedPortalID = -1;
    private State state = State.WAITING_FOR_DIMENSION;
    
    public Vector2 direction = LEFT;
    
    public CPortal(String dimension) {
        this.pairedDimension = dimension;
    }
    
    @Override
    public void init(Entity e) {
        
    }
    
    @Override
    public void update(World w, Entity e) {
        HostWorld w2 = (HostWorld)w.multiverse().getDimension(pairedDimension);
        switch(state) {
            case WAITING_FOR_DIMENSION:
                if(w2.isLoaded()) {
                    state = State.WAITING_FOR_REGION;
                    // Fall through
                } else {
                    break;
                }
            case WAITING_FOR_REGION:
                if(w2.getRegionAt(0, 0).isPrepared()) {
                    Entity otherEnd = Entities.portal(((AbstractWorld)w).getDimensionName());
                    CPortal otherCore = (CPortal)otherEnd.core;
                    otherCore.direction = new Vector2(-direction.x, -direction.y);
                    otherCore.state = State.OPEN;
                    w2.addEntity(otherEnd, Position.create(0, 0, 8f, 8f));
                    
                    otherCore.pairedPortalID = e.id();
                    pairedPortalID = otherEnd.id();
                    
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
                    w.multiverse().sendToDimension(w2, pairedDimension, e2, SEND_TO_POS);
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
