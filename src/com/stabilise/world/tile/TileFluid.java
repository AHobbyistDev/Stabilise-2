package com.stabilise.world.tile;

/**
 * A non-solid fluid tile.
 */
public class TileFluid extends Tile {
    
    /** The tile's viscosity. */
    private final float viscosity;
    
    
    /**
     * Creates a new fluid tile.
     */
    public TileFluid(TileBuilder b) {
        super(b);
        this.viscosity = b.viscosity;
    }
    
    /**
     * Gets the tile's viscosity.
     * 
     * @return The tile's viscosity.
     */
    public float getViscosity() {
        return viscosity;
    }

}
