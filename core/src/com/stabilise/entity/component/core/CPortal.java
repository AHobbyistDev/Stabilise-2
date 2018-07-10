package com.stabilise.entity.component.core;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.CNearbyPortal;
import com.stabilise.entity.component.CSliceAnchorer;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.event.EPortalInRange;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;
import com.stabilise.world.multiverse.Multiverse;


/**
 * Very crappy work-in-progress portal implementation.
 * 
 * <p>A portal is a, well, portal, which if all things go well will provide a
 * seamless transition between two dimensions, or locations in a dimension.
 * 
 * @see CPhantom
 * @see CNearbyPortal
 */
public class CPortal extends CCore {
    
    /** If an entity comes within this squared distance of a portal, it is
     * notified via {@link Entity#nearbyPortal(EPortalInRange)}. */
    public static final float NEARBY_DIST_SQ = 8*8;
    /** If an entity is no longer within this squared distance of a portal,
     * it stops monitoring the portal. */
    public static final float NEARBY_MAX_DIST_SQ = 16*16;
    
    private static final int OPEN_ANIMATION_DURATION = 30;
    
    
    private static enum State {
        UNINITIALISED,
        WAITING_FOR_DIMENSION,
        OPEN,
        CLOSED,
    }
    
    /** 0.5 blocks wide, 3 blocks high. */
    private static final AABB AABB = new AABB(-0.25f, -1.5f, 0.5f, 3f);
    
    /** Name of the paired dimension, for getting via {@link
     * Multiverse#getDimension(String)}. This is {@code null} if this portal
     * is dimension-local. */
    private String pairedDimension;
    /** true if this is a portal to another dimension; false if this is the
     * same dimension. */
    private boolean interdimensional;
    
    private State state = State.UNINITIALISED;
    
    /** Anticlockwise rotation of the portal, in radians. An angle of 0 means
     * the portal is 'facing right', i.e. entities enter from the right. An
     * angle of pi means the portal is 'facing left', etc. */
    public float rotation = 0f;
    /** Points in the direction that this portal is facing (i.e. this is a
     * unit vector in the direction given by {@link #rotation}). This is
     * set to the correct value when the portal is added to the world. */
    public Vector2 direction = new Vector2(1f, 0f);
    /** The maximum height of the portal. This is <em>not</em> the portal's
     * current height; for this refer to {@link #halfHeight}. */
    public float height = 3f;
    /** Half the current height of the portal. This may not necessarily be half
     * of {@link #height} since we might be in an opening or closing animation. */
    public float halfHeight = 0f;
    /** true if the portal may be entered from either side. */
    public boolean doubleSided = false;
    
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
    
    /** Cache this portal's entity ID for convenience here. */
    public long id;
    /** The ID of the paired portal. */
    public long pairID;
    
    
    /** Event to send to entities which come in range. Cached to avoid
     * excessive object creation. */
    private EPortalInRange nearbyTestEvent, nearbyInformEvent;
    
    // Animation stuff
    private boolean animating = false;
    private int animationTicks = 0;
    
    
    
    public CPortal() {}
    
    
    /**
     * @param dimension The dimension to connect to. null to make this an
     * intradimensional (same dimension) portal.
     */
    public CPortal(String dimension) {
        this.pairedDimension = dimension;
        interdimensional = dimension != null;
    }
    
    private void onAddToWorld(World w, Entity e) {
        // cache the ID (can't do it in init() since it wouldn'tve been set yet)
        id = e.id(); 
        nearbyTestEvent = new EPortalInRange(id, true);
        nearbyInformEvent = new EPortalInRange(id, false);
        
        direction.rotateRad(rotation);
    }
    
    /**
     * Closes the portal, if it is not already closed.
     */
    public void close(World w, Entity e) {
        if(state == State.CLOSED)
            return;
        state = State.CLOSED;
        
        World w2 = pairedWorld(w);
        Entity ope = w2.getEntity(pairID);
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
    public void update(World w, Entity e, float dt) {
        switch(state) {
            case UNINITIALISED:
                updateUninitialised(w, e);
                break;
            case WAITING_FOR_DIMENSION:
            	updateWaiting(w, e);
                break;
            case OPEN:
                updateOpen(w, e);
                break;
            case CLOSED:
                updateClosed(w, e);
                break;
        }
    }
    
    private void updateUninitialised(World w, Entity e) {
        // Only do the setup if we're the original portal
        if(original) {
            // Subtract the direction vector since we enter from one edge of the
            // first portal and exit from the opposite edge of the other.
            offset.setDiff(otherPortalPos, e.pos).align();
            
            // ope = "other portal entity", opc = "other portal core"
            Entity ope = Entities.portal(w.getDimensionName());
            CPortal opc = (CPortal) ope.core;
            
            ope.pos.set(otherPortalPos);
            
            opc.original = false;
            opc.pairedDimension = w.getDimensionName();
            opc.interdimensional = interdimensional;
            opc.pairID = id;
            opc.otherPortalPos.set(e.pos);
            opc.offset.set(offset).reflect().align();
            opc.rotation = (rotation + Maths.PIf) % Maths.TAUf;
            //opc.direction.set(direction).scl(-1); // no need; set by onAddToWorld()
            opc.doubleSided = doubleSided;
            opc.height = height;
            opc.state = State.WAITING_FOR_DIMENSION;
            
            World w2 = interdimensional
                    ? w.multiverse().loadDimension(pairedDimension)
                    : w;
            w2.addEntity(ope);
            ope.getComponent(CSliceAnchorer.class).anchorAll(w2, ope); // preanchor all slices
            
            pairID = ope.id();
        }
        
        state = State.WAITING_FOR_DIMENSION;
    }
    
    private void updateWaiting(World w, Entity e) {
        // Do all the work only if we are the original portal
        if(!original)
            return;
        
        World w2 = pairedWorld(w);
        Entity ope = pairedPortal(w2);
        
        // Might be null for a single tick if the other portal entity
        // is still queued to be added to the other dimension.
        if(ope == null)
            return;
        
        if(ope.getComponent(CSliceAnchorer.class).allSlicesActive(w2)) {
            state = State.OPEN;
            animating = true;
            animationTicks = 0;
            
            CPortal opc = (CPortal) ope.core;
            opc.state = State.OPEN;
            opc.animating = true;
            opc.animationTicks = 0;
        }
    }
    
    private void updateOpen(World w, Entity e) {
        if(animating) {
            if(++animationTicks == OPEN_ANIMATION_DURATION) {
                animating = false;
                halfHeight = height/2;
            } else {
                float x = (float)animationTicks / OPEN_ANIMATION_DURATION;
                halfHeight = Interpolation.CUBIC.easeOut(0f, height/2, x);
            }
        }
        
        w.getEntitiesNearby(e.pos).forEach(en -> {
            // Ignore phantoms and portals (including ourselves!) for now
            if(en.isPhantom() || en.isPortal())
                return;
            
            if(e.pos.distSq(en.pos) < NEARBY_DIST_SQ)
                informNearbyEntity(w, e, en);
        });
    }
    
    private void informNearbyEntity(World w, Entity portal, Entity en) {
        // If the entity already knows about is, one of its CNearbyPortal
        // components will reject us here.
        
        // Iterating backwards is admittedly a microoptimisation over using
        // en.post().
        if(en.components.anyBackwards(c -> c.handle(w, en, nearbyTestEvent),
                Component.WEIGHT_NEARBY_PORTAL))
            return;
        
        // Entity doesn't already know about this portal, so let's send the
        // actual inform event and add in the component.
        en.post(w, nearbyInformEvent);
        
        CNearbyPortal cnp = new CNearbyPortal(id);
        en.addComponent(cnp);
        
        // If this portal is interdimensional, we create a phantom on the other
        // side and let the entity know.
        if(interdimensional) {
            // If phantom is non-null, then it is already a phantom of another
            // portal linked to the same dimension. This is fine; we let cnp
            // track it too.
            Entity phantom = pairedWorld(w).getEntity(en.id());
            if(phantom == null) {
                phantom = Entities.phantom(en);
                cnp.phantom = phantom;
                cnp.updatePhantomPos(en, this); // set before adding to world
                pairedWorld(w).addEntityDontSetID(phantom);
            } else {
                ((CPhantom)phantom.core).anchors++;
                cnp.phantom = phantom;
            }
        }
    }
    
    private void updateClosed(World w, Entity e) {
        e.destroy();
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
        return interdimensional
                ? w.multiverse().getDimension(pairedDimension)
                : w;
    }
    
    /**
     * Gets the portal in the given paired dimension which is paired to this
     * portal.
     */
    public Entity pairedPortal(World otherDimWorld) {
        return otherDimWorld.getEntity(pairID);
    }
    
    /**
     * Returns true if this is a portal to another dimension; false otherwise.
     */
    public boolean interdimensional() {
        return interdimensional;
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
