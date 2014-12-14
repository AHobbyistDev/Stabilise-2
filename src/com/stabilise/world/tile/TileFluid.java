package com.stabilise.world.tile;

/**
 * A non-solid fluid tile.
 */
public class TileFluid extends Tile {
	
	/** The tile's viscosity. */
	private float viscosity;
	
	
	/**
	 * Creates a new fluid tile.
	 */
	public TileFluid() {
		super();
		solid = false;
	}
	
	/**
	 * Sets the tile's viscosity.
	 * 
	 * @param viscosity The viscosity.
	 * 
	 * @return The tile, for chain construction.
	 */
	Tile setViscosity(float viscosity) {
		this.viscosity = viscosity;
		return this;
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
