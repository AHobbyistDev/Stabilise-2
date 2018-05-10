package com.stabilise.entity.component;

import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;

import com.stabilise.core.Constants;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.HostWorld;
import com.stabilise.world.RegionState;
import com.stabilise.world.World;


/**
 * This component ensures that a box of slices around its attached entity
 * always remains loaded. This is typically placed on the player (to ensure the
 * world loads around them) and on portals (to ensure that the dimensions at
 * both ends remain loaded).
 */
public class CSliceAnchorer extends AbstractComponent {
    
    
    /** "Radius" of the square of slices to keep anchored. */
    private int radius;
    /** The entity's most recent slice coordinates, which serve the centre of
     * the coordinates of the slices to keep anchored. */
    private int centreX, centreY;
    /** Coordinates determining which slices are to be anchored. */
    private int minSliceX, maxSliceX, minSliceY, maxSliceY;
    
    /** true if we've done an initial anchoring of slices. */
    private boolean initialAnchor = false;
    
    /** true if this anchorer has been disabled, which means it should be
     * removed from the attached entity's components list */
    private boolean disabled = false;
    
    
    /**
     * Creates a slice anchorer component with radius {@link
     * Constants#LOADED_SLICE_RADIUS}.
     */
    public CSliceAnchorer() {
        this(Constants.LOADED_SLICE_RADIUS);
    }
    
    /**
     * Creates a slice anchorer component which anchors slices within the
     * specified radius.
     * 
     * @throws IllegalArgumentException if {@code radius < 1}.
     */
    public CSliceAnchorer(int radius) {
        this.radius = Checks.testMin(radius, 1);
    }
    
    @Override
    public void init(Entity e) {
        // do nothing on init
    }
    
    @Override
    public void update(World w, Entity e) {
        if(disabled)
            return;
        
        int sliceX = e.pos.sx();
        int sliceY = e.pos.sy();
        
        // If the entity hasn't moved slices, nothing needs to be changed
        if(centreX == sliceX && centreY == sliceY)
            return;
        
        // If the entity has moved very far, opt to refresh instead.
        if(Math.abs(sliceX - centreX) >= radius ||
           Math.abs(sliceY - centreY) >= radius) {
            refresh(w, e);
            return;
        }
        
        centreX = sliceX;
        centreY = sliceY;
        
        int oldMinX = minSliceX;
        int oldMaxX = maxSliceX;
        int oldMinY = minSliceY;
        int oldMaxY = maxSliceY;
        
        minSliceX = centreX - LOADED_SLICE_RADIUS;
        maxSliceX = centreX + LOADED_SLICE_RADIUS;
        minSliceY = centreY - LOADED_SLICE_RADIUS;
        maxSliceY = centreY + LOADED_SLICE_RADIUS;
        
        // Be careful to make sure we don't double-count; we blanket sweep
        // across x but tread carefully with our mins and maxes with y.
        int minX = Math.max(oldMinX, minSliceX);
        int maxX = Math.min(oldMaxX, maxSliceX);
        
        for(int x = minSliceX; x < oldMinX; x++) anchorCol(w, x, minSliceY, maxSliceY);
        for(int x = maxSliceX; x > oldMaxX; x--) anchorCol(w, x, minSliceY, maxSliceY);
        for(int y = minSliceY; y < oldMinY; y++) anchorRow(w, y, minX, maxX);
        for(int y = maxSliceY; y > oldMaxY; y--) anchorRow(w, y, minX, maxX);
        
        for(int x = oldMinX; x < minSliceX; x++) deanchorCol(w, x, oldMinY, oldMaxY);
        for(int x = oldMaxX; x > maxSliceX; x--) deanchorCol(w, x, oldMinY, oldMaxY);
        for(int y = oldMinY; y < minSliceY; y++) deanchorRow(w, y, minX, maxX);
        for(int y = oldMaxY; y > maxSliceY; y--) deanchorRow(w, y, minX, maxX);
    }
    
    /**
     * Anchors a column of slices. minY and maxY are inclusive.
     */
    private void anchorCol(World w, int x, int minY, int maxY) {
        for(int y = minY; y <= maxY; y++) w.anchorSlice(x, y);
    }
    
    /**
     * Anchors a row of slices. minX and maxX are inclusive
     */
    private void anchorRow(World w, int y, int minX, int maxX) {
        for(int x = minX; x <= maxX; x++) w.anchorSlice(x, y);
    }
    
    /**
     * Deanchors a column of slices. minY and maxY are inclusive.
     */
    private void deanchorCol(World w, int x, int minY, int maxY) {
        for(int y = minY; y <= maxY; y++) w.deanchorSlice(x, y);
    }
    
    /**
     * Deanchors a row of slices. minX and maxX are inclusive.
     */
    private void deanchorRow(World w, int y, int minX, int maxX) {
        for(int x = minX; x <= maxX; x++) w.deanchorSlice(x, y);
    }
    
    /**
     * Refreshes all the anchors. This is useful to call if, say, the entity is
     * teleported and an entirely new batch of slices will need to be anchored.
     */
    public void refresh(World w, Entity e) {
        int oldMinX = minSliceX;
        int oldMaxX = maxSliceX;
        int oldMinY = minSliceY;
        int oldMaxY = maxSliceY;
        
        // Chuck down our new anchors before removing the old ones. This
        // prevents the possibility of some slices being momentarily deanchored
        // and executing any unnecessary de-anchor logic.
        anchorAll(w, e, false);
        
        for(int x = oldMinX; x <= oldMaxX; x++)
            deanchorCol(w, x, oldMinY, oldMaxY);
    }
    
    /**
     * Performs an initial anchorage of all slices around the entity. Does
     * nothing if this initial anchorage has already been done.
     */
    public void anchorAll(World w, Entity e) {
    	anchorAll(w, e, true);
    }
    
    /**
     * Anchor the slices which should be loaded about the entity. Does nothing
     * if this method has already been invoked once.
     */
    private void anchorAll(World w, Entity e, boolean check) {
    	if((check && initialAnchor) || disabled)
    		return;
    	
    	initialAnchor = true;
    	
        centreX = e.pos.sx();
        centreY = e.pos.sy();
        minSliceX = centreX - LOADED_SLICE_RADIUS;
        maxSliceX = centreX + LOADED_SLICE_RADIUS;
        minSliceY = centreY - LOADED_SLICE_RADIUS;
        maxSliceY = centreY + LOADED_SLICE_RADIUS;
        
        for(int x = minSliceX; x <= maxSliceX; x++)
            anchorCol(w, x, minSliceY, maxSliceY);
    }
    
    /**
     * Deanchors all anchored slices. This permanently disables this component,
     * and it will be removed from its attached entity soon.
     */
    public void deanchorAll(World w) {
        if(disabled)
            return;
        disabled = true;
        
        for(int x = minSliceX; x <= maxSliceX; x++)
            deanchorCol(w, x, minSliceY, maxSliceY);
    }
    
    /**
     * Checks for whether all anchored slices are active.
     * 
     * @see RegionState#isActive()
     */
    public boolean allSlicesActive(World w) {
    	// Just check regions
    	HostWorld hw = w.asHost();
    	int minRegionX = Position.regionCoordFromSliceCoord(minSliceX);
    	int maxRegionX = Position.regionCoordFromSliceCoord(maxSliceX);
    	int minRegionY = Position.regionCoordFromSliceCoord(minSliceY);
    	int maxRegionY = Position.regionCoordFromSliceCoord(maxSliceY);
    	
    	for(int x = minRegionX; x <= maxRegionX; x++) {
    		for(int y = minRegionY; y <= maxRegionY; y++) {
    			if(!hw.getRegionAt(x, y).state.isActive())
    				return false;
    		}
    	}
    	
    	return true;
    }
    
    @Override
    public boolean shouldRemove() {
        return disabled;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.equals(EntityEvent.ADDED_TO_WORLD))
            anchorAll(w, e, true);
        else if(ev.equals(EntityEvent.REMOVED_FROM_WORLD))
        	deanchorAll(w);
        return false;
    }
    
    @Override
    public int getWeight() {
        // Pretty far down in the list; we apply slice updates after all
        // positional changes have taken place.
        return 1000;
    }
    
    @Override
    public Action resolve(Component c) {
        throw new IllegalStateException("Two slices anchorers on the same entity?");
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        radius = c.getI32("radius");
        disabled = c.getBool("disabled");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        c.put("radius", radius);
        c.put("disabled", disabled);
    }
    
}
