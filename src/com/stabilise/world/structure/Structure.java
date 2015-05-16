package com.stabilise.world.structure;

import java.util.Objects;

import com.stabilise.world.Region;
import com.stabilise.world.RegionCache;

/**
 * A schematic is a structure blueprint.
 * 
 * <p>Currently schematics may be no larger than 32,767 tiles in width or
 * height, though it's highly unlikely one would find a use for schematics of
 * such dimensions anyway.
 */
public class Structure {
	
	/** The schematic's name. */
	public String name = "";
	
	
	/**
	 * Creates a new empty schematic.
	 */
	public Structure() {
		// le nothing
	}
	
	/**
	 * Creates a new schematic.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public Structure(String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	/**
	 * 
	 * @param regions source to use to get other regions
	 * @param r the region to add this schematic to
	 * @param offsetX the x-coordinate at which this schematic is being placed,
	 * relative to the region
	 * @param offsetY ditto
	 * @param addToNeighbours true if this schematic should add itself to the
	 * region's neighbours.
	 */
	public void add(RegionCache regions, Region r,
			int offsetX, int offsetY, boolean addToNeighbours) {
		
		
	}
	
	@Override
	public String toString() {
		return "Schematic[" + name + "]";
	}
	
}
