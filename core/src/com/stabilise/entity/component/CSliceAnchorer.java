package com.stabilise.entity.component;

import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;

import com.stabilise.core.Constants;
import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Checks;
import com.stabilise.world.World;


/**
 * This component ensures that a box of slices around its attached entity
 * always remains loaded. This is typically placed on the player (to ensure the
 * world loads around them) and on portals (to ensure that the dimensions at
 * both ends remain loaded).
 */
public class CSliceAnchorer extends AbstractComponent {
    
    
    /** "Radius" of the square of slices to keep anchored. */
    private final int radius;
    /** The entity's most recent slice coordinates, which serve the centre of
     * the coordinates of the slices to keep anchored. */
    private int centreX, centreY;
    /** Coordinates determining which slices are to be anchored. */
    protected int minSliceX, maxSliceX, minSliceY, maxSliceY;
    
    
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
        int sliceX = e.pos.getSliceX();
        int sliceY = e.pos.getSliceY();
        
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
        
        for(int x = minSliceX; x < oldMinX; x++) loadCol(w, x, minSliceY, maxSliceY);
        for(int x = maxSliceX; x > oldMaxX; x--) loadCol(w, x, minSliceY, maxSliceY);
        for(int y = minSliceY; y < oldMinY; y++) loadRow(w, y, minX, maxX);
        for(int y = maxSliceY; y > oldMaxY; y--) loadRow(w, y, minX, maxX);
        
        for(int x = oldMinX; x < minSliceX; x++) unloadCol(w, x, oldMinY, oldMaxY);
        for(int x = oldMaxX; x > maxSliceX; x--) unloadCol(w, x, oldMinY, oldMaxY);
        for(int y = oldMinY; y < minSliceY; y++) unloadRow(w, y, minX, maxX);
        for(int y = oldMaxY; y > maxSliceY; y--) unloadRow(w, y, minX, maxX);
    }
    
    /**
     * Loads a column of slices. minY and maxY are inclusive.
     */
    private void loadCol(World w, int x, int minY, int maxY) {
        for(int y = minY; y <= maxY; y++) w.anchorSlice(x, y);
    }
    
    /**
     * Loads a row of slices. minX and maxX are inclusive
     */
    private void loadRow(World w, int y, int minX, int maxX) {
        for(int x = minX; x <= maxX; x++) w.anchorSlice(x, y);
    }
    
    /**
     * Unloads a column of slices. minY and maxY are inclusive.
     */
    protected void unloadCol(World w, int x, int minY, int maxY) {
        for(int y = minY; y <= maxY; y++) w.deanchorSlice(x, y);
    }
    
    /**
     * Unloads a row of slices. minX and maxX are inclusive.
     */
    protected void unloadRow(World w, int y, int minX, int maxX) {
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
        loadAll(w, e);
        
        for(int x = oldMinX; x <= oldMaxX; x++)
            unloadCol(w, x, oldMinY, oldMaxY);
    }
    
    /**
     * Loads the slices which should be loaded about the entity.
     */
    private void loadAll(World w, Entity e) {
        centreX = e.pos.getSliceX();
        centreY = e.pos.getSliceY();
        minSliceX = centreX - LOADED_SLICE_RADIUS;
        maxSliceX = centreX + LOADED_SLICE_RADIUS;
        minSliceY = centreY - LOADED_SLICE_RADIUS;
        maxSliceY = centreY + LOADED_SLICE_RADIUS;
        
        for(int x = minSliceX; x <= maxSliceX; x++)
            loadCol(w, x, minSliceY, maxSliceY);
    }
    
    /**
     * Deanchors all anchored slices.
     */
    public void unloadAll(World w) {
        for(int x = minSliceX; x <= maxSliceX; x++)
            unloadCol(w, x, minSliceY, maxSliceY);
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type().equals(EntityEvent.ADDED_TO_WORLD))
            loadAll(w, e);
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
    
}
