package com.stabilise.entity.component.core;

import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.CSliceAnchorer;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;


/**
 * Extremely crappy pre-placeholder portal implementation. Absolutely nothing
 * like the (hopefully) intended final product.
 */
public class CPortal extends CCore {
    
    private static enum State {
        WAITING_FOR_DIMENSION,
        OPEN,
        CLOSED
    }
    
    /** 1 block wide, 3 blocks high. */
    private static final AABB AABB = new AABB(-0.5f, 0f, 1.0f, 3f);
    
    private String pairedDimension;
    private State state = State.WAITING_FOR_DIMENSION;
    
    /** The position of the other portal in its own dimension. This shouldn't
     * be modified after either portal is added to the world. */
    public final Position otherPortalPos = Position.create();
    /** The position offset that an entity undergoes when moving through this
     * portal. This is computed when this portal is added to the world. This
     * is public for convenience and shouldn't be modified full-stop. */
    public final Position offset = Position.create();
    
    
    /** true if we are the original portal, false if we were created by the
     * original portal. */
    private boolean original = true;
    
    /** Cache the ID for convenience here. */
    private long id;
    
    
    public CPortal() {}
    
    public CPortal(String dimension) {
        this.pairedDimension = dimension;
    }
    
    private void onAddToWorld(World w, Entity e) {
        id = e.id(); // cache the ID
        
    	// Only do the setup if we're the original portal
        if(original) {
            // First clamp to the middle of a tile (since we have width 0.5 on
            // each side, this will centre the portal on a tile), then align.
            e.pos.clampToTile().add(0.5f, 0).align();
            otherPortalPos.clampToTile().add(0.5f, 0).align();
            
            // Subtract the direction vector since we enter from one edge of the
            // first portal and exit from the opposite edge of the other.
            offset.setDiff(otherPortalPos, e.pos).add(e.facingRight?1:-1, 0).align();
            
            // ope = "other portal entity", opc = "other portal core"
            Entity ope = Entities.portal(w.getDimensionName());
            CPortal opc = (CPortal) ope.core;
            
            ope.setID(id); // match our IDs
            ope.pos.set(otherPortalPos);
            ope.facingRight = !e.facingRight;
            
            opc.original = false;
            opc.otherPortalPos.set(e.pos);
            opc.offset.set(offset).reflect().align();
            opc.state = State.WAITING_FOR_DIMENSION;
            
            World w2 = w.multiverse().loadDimension(pairedDimension);
            w2.addEntityDontSetID(ope);
            ope.getComponent(CSliceAnchorer.class).anchorAll(w2, ope); // preanchor all slices
        }
    }
    
    /**
     * Closes the portal, if it is not already closed.
     */
    public void close(World w, Entity e) {
        if(state == State.CLOSED)
            return;
        state = State.CLOSED;
        
        World w2 = pairedWorld(w);
        Entity ope = w2.getEntity(id);
        CPortal opc = (CPortal) ope.core;
        
        opc.state = State.CLOSED;
        
        // Deanchor both ends, being careful which world to deanchor in
        e.getComponent(CSliceAnchorer.class).deanchorAll(w);
        ope.getComponent(CSliceAnchorer.class).deanchorAll(w2);
        
        // Destroy the portal that isn't the original, because why not
        if(original)
            ope.destroy();
        else
            e.destroy();
    }
    
    @Override
    public void update(World w, Entity e) {
        switch(state) {
            case WAITING_FOR_DIMENSION:
            	// Do all the work only if we are the original portal
            	if(!original)
            		return;
            	
            	HostWorld w2 = pairedWorld(w).asHost();
            	Entity ope = w2.getEntity(id);
            	
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
        if(ev.equals(EntityEvent.ADDED_TO_WORLD))
            onAddToWorld(w, e);
        else if(ev.equals(EntityEvent.REMOVED_FROM_WORLD))
            close(w, e);
        return false;
    }
    
    /**
     * Returns true if this portal is open.
     */
    public boolean isOpen() {
        return state == State.OPEN;
    }
    
    /**
     * Returns the world this portal is paired to.
     * 
     * @param w The world this portal is in (needed for accessing the
     * multiverse).
     */
    public World pairedWorld(World w) {
        return w.multiverse().getDimension(pairedDimension);
    }
    
    /**
     * Gets the portal in the given paired dimension which is paired to this
     * portal.
     */
    public Entity pairedPortal(World otherDimWorld) {
        return otherDimWorld.getEntity(id);
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
