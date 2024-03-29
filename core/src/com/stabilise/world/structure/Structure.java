package com.stabilise.world.structure;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.world.Region;
import com.stabilise.world.RegionStore;

/**
 * A schematic is a structure blueprint.
 * 
 * <p>Currently schematics may be no larger than 32,767 tiles in width or
 * height, though it's highly unlikely one would find a use for schematics of
 * such dimensions anyway.
 */
@Incomplete
public class Structure {
    
    /**
     * Creates a new empty schematic.
     */
    public Structure() {
        
    }
    
    /**
     * 
     * @param regions source to use to get other regions
     * @param r the region to add this schematic to
     * @param x the x-coordinate at which this schematic is being placed,
     * relative to the region
     * @param y ditto
     * @param addToNeighbours true if this schematic should add itself to the
     * region's neighbours.
     */
    public void add(RegionStore regions, Region r,
            int x, int y, long seed, boolean addToNeighbours) {
        StructureBuilder builder = new StructureBuilder(
                this,
                regions, r,
                x, y,
                seed,
                addToNeighbours
        );
        //builder.start();
        build(builder);
        //builder.end();
    }
    
    protected void build(StructureBuilder builder) {
        
    }
    
    protected int originX() {
        return 0;
    }
    
    protected int originY() {
        return 0;
    }
    
    public int id() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "Schematic[" + /*name +*/ "]";
    }
    
    @SuppressWarnings("unused")
    protected static class StructureBuilder {
        
        private final Structure structure;
        private final RegionStore cache;
        private final Region r;
        private final int x, y;
        private final int ox, oy;
        private final long seed;
        private final boolean progenitor;
        
        private StructureBuilder(Structure structure, RegionStore cache,
                Region r, int x, int y, long seed, boolean progenitor) {
            this.structure = structure;
            this.cache = cache;
            this.r = r;
            this.x = x;
            this.y = y;
            this.seed = seed;
            this.progenitor = progenitor;
            
            ox = structure.originX();
            oy = structure.originY();
        }
        
        public void fillRect(int x, int y, int width, int height, int id) {
            
        }
        
        public void fill(int x, int y, int[][] template) {
            
        }
        
    }
    
}
