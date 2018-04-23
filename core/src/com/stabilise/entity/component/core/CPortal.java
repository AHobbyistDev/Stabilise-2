package com.stabilise.entity.component.core;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.CSliceAnchorer;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
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
    
    /** The direction this portal is facing. Default: {@link #direction */
    public Vector2 direction = LEFT;
    
    
    /** The position of the other portal in its own dimension. This shouldn't
     * be modified after either portal is added to the world. */
    public final Position otherPortalPos = Position.create();
    /** The position offset that an entity undergoes when moving through this
     * portal. This is computed when this portal is added to the world. This
     * is public for convenience and shouldn't be modified full-stop. */
    public final Position offset = Position.create();
    
    
    /** true if we are the original portal, false if we were created by the
     * original portal. */
    public boolean original = true;
    
    
    public CPortal(String dimension) {
        this.pairedDimension = dimension;
    }
    
    private void onAddToWorld(World w, Entity e) {
    	// Only do the setup if we're the original portal
        if(original) {
            // Align and clamp our position, just to be safe
            e.pos.align().clampToTile();
            otherPortalPos.align().clampToTile();
            
            offset.setDiff(otherPortalPos, e.pos).align();
            
            // ope = "other portal entity", opc = "other portal core"
            Entity ope = Entities.portal(w.getDimensionName());
            CPortal opc = (CPortal) ope.core;
            
            ope.setID(e.id()); // match our IDs
            ope.pos.set(otherPortalPos);
            
            opc.original = false;
            opc.otherPortalPos.set(e.pos);
            opc.offset.setDiff(e.pos, ope.pos).align();
            opc.direction.set(direction).scl(-1); // faces opposite direction
            opc.state = State.WAITING_FOR_DIMENSION;
            
            World w2 = w.multiverse().loadDimension(pairedDimension);
            w2.addEntityDontSetID(ope);
            ope.getComponent(CSliceAnchorer.class).anchorAll(w2, ope); // preanchor all slices
        }
    }
    
    @Override
    public void update(World w, Entity e) {
        switch(state) {
            case WAITING_FOR_DIMENSION:
            	// Do all the work only if we are the original portal
            	if(!original)
            		return;
            	
            	HostWorld w2 = w.multiverse().getDimension(pairedDimension).asHost();
            	Entity ope = w2.getEntity(e.id());
            	
            	// Might be null for a single tick if the other portal entity
            	// is still queued to be added to the other dimension.
            	if(ope == null)
            		return;
            	
            	if(ope.getComponent(CSliceAnchorer.class).allSlicesActive(w2)) {
            		state = State.OPEN;
            		
            		CPortal opc = (CPortal) ope.core;
            		opc.state = State.OPEN;
            	}
                break;
            case OPEN:
                // nothing to do, really
                break;
            case CLOSED:
                // do we remove ourselves?
                break;
        }
        
        /*
        w.getEntities().forEach(e2 -> {
            if(!(e2.core instanceof CPortal)) {
                if(e2.core.getAABB().intersects(getAABB(), e.pos.diffX(e2.pos), e.pos.diffY(e2.pos))) {
                    w.multiverse().sendToDimension(w2, pairedDimension, e2, otherPortalPos);
                }
            }
        });
        */
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
            	onAddToWorld(w, e);
                break;
            default:
                break;
        }
        return false;
    }
    
    public boolean isOpen() {
        return state == State.OPEN;
    }
    
    /**
     * Closes the portal.
     */
    public void close() {
        // TODO
    }
    
}
