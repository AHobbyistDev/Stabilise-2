package com.stabilise.world;

import java.util.Collection;
import java.util.Iterator;

import com.stabilise.entity.Entity;
import com.stabilise.entity.GameObject;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * Provides implementations of methods in World which can be defined in terms
 * of other methods.
 */
public abstract class AbstractWorld implements World {
	
	/**
	 * Iterates over the specified collection of GameObjects as per {@link
	 * GameObject#updateAndCheck()}. GameObjects are removed from the
	 * collection by the iterator if {@code updateAndCheck()} returns {@code
	 * true}.
	 */
	protected <E extends GameObject> void updateObjects(Collection<E> collection) {
		Iterator<E> i = collection.iterator();
		while(i.hasNext())
			if(i.next().updateAndCheck())
				i.remove();
	}
	
	@Override
	public void addEntity(Entity e, double x, double y) {
		e.x = x;
		e.y = y;
		addEntity(e);
	}
	
	@Override
	public void addHitbox(Hitbox h, double x, double y) {
		h.x = x;
		h.y = y;
		addHitbox(h);
	}
	
	@Override
	public void addParticle(Particle p, double x, double y) {
		p.x = x;
		p.y = y;
		addParticle(p);
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		return getSliceAt(
				sliceCoordFromTileCoord(x),
				sliceCoordFromTileCoord(y)
		);
	}
	
	@Override
	public Tile getTileAt(double x, double y) {
		return getTileAt(MathsUtil.floor(x), MathsUtil.floor(y));
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		Slice s = getSliceAtTile(x, y);
		if(s == null)
			return Tiles.BEDROCK_INVISIBLE;
		else
			return s.getTileAt(
					tileCoordRelativeToSliceFromTileCoord(x),
					tileCoordRelativeToSliceFromTileCoord(y)
			);
	}
	
	@Override
	public TileEntity getTileEntityAt(int x, int y) {
		Slice s = getSliceAtTile(x, y);
		if(s == null)
			return null;
		else
			return s.getTileEntityAt(
					tileCoordRelativeToSliceFromTileCoord(x),
					tileCoordRelativeToSliceFromTileCoord(y)
			);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the coordinate of the region at the given tile coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the region occupying the given coordinate, in
	 * region-lengths.
	 */
	public static int regionCoordFromTileCoord(int c) {
		//return MathsUtil.fastFloor((float)c / Region.REGION_SIZE_IN_TILES)
		//return c < 0 ?								// Faster
		//		(c+1) / Region.REGION_SIZE_IN_TILES - 1 :
		//		c / Region.REGION_SIZE_IN_TILES;
		return c >> Region.REGION_SIZE_IN_TILES_SHIFT;	// Even faster
	}
	
	/**
	 * Gets the coordinate of the region at the given absolute slice
	 * coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in slice-lengths.
	 * 
	 * @return The coordinate of the region occupying the given coordinate, in
	 * region-lengths.
	 */
	public static int regionCoordFromSliceCoord(int c) {
		//return MathsUtil.fastFloor((float)c / Region.REGION_SIZE)
		//return c < 0 ?								// Faster
		//		(c+1) / Region.REGION_SIZE - 1 :
		//		c / Region.REGION_SIZE;
		return c >> Region.REGION_SIZE_SHIFT;			// Even faster
	}
	
	/**
	 * Gets the coordinate of the slice at the given coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the slice occupying the given coordinate, in
	 * slice-lengths.
	 */
	public static int sliceCoordFromTileCoord(int c) {
		//return MathsUtil.fastFloor((float)c / Slice.SLICE_SIZE)
		//return c < 0 ?							// Faster
		//		(c+1) / Slice.SLICE_SIZE - 1 :
		//		c / Slice.SLICE_SIZE;
		return c >> Slice.SLICE_SIZE_SHIFT;			// Even faster
	}
	
	/**
	 * Gets the coordinate of the slice at the given coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the slice occupying the given coordinate, in
	 * slice-lengths.
	 */
	public static int sliceCoordFromTileCoord(double c) {
		return MathsUtil.floor(c / Slice.SLICE_SIZE);
	}
	
	/**
	 * Gets the coordinate of the slice at the start of a region at the given
	 * coordinate, in slice-lengths.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in region-lengths.
	 * 
	 * @return The coordinate of the slice at the start of the region, in
	 * slice-lengths.
	 */
	public static int sliceCoordFromRegionCoord(int c) {
		return c * Region.REGION_SIZE;
	}
	
	/**
	 * Gets the coordinate of the slice, relative to its parent region, at the
	 * given coordinate.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the slice occupying the given coordinate, in
	 * slice-lengths, relative to its parent region.
	 */
	public static int sliceCoordRelativeToRegionFromTileCoord(int c) {
		//c = sliceCoordFromTileCoord(c);
		//return MathsUtil.wrappedRem(c, Region.REGION_SIZE);
		//return MathsUtil.wrappedRem2(c, Region.REGION_SIZE);				// Way faster
		
		// i.e. (c >> Slice.SLICE_SIZE_SHIFT) & Region.REGION_SIZE_MINUS_ONE
		return sliceCoordFromTileCoord(c) & Region.REGION_SIZE_MINUS_ONE;	// One less instruction
	}
	
	/**
	 * Gets the coordinate of the slice, relative to its parent region, at the
	 * given coordinate. That is, converts the given slice coordinate to local
	 * region space.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in slice-lengths.
	 * 
	 * @return The coordinate of the slice, in slice-lengths, relative to its
	 * parent region.
	 */
	public static int sliceCoordRelativeToRegionFromSliceCoord(int c) {
		//return MathsUtil.wrappedRem(c, Region.REGION_SIZE);
		//return MathsUtil.wrappedRem2(c, Region.REGION_SIZE);		// Way faster
		return c & Region.REGION_SIZE_MINUS_ONE;					// One less instruction
	}
	
	/**
	 * Gets the coordinate of the start of a slice at the given coordinate, in
	 * tile-lengths.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * <p>Also note that this method also returns the starting tile of a slice
	 * relative to a region, provided the {@code c} parameter given is that of
	 * the slice's coordinate relative to the region.
	 * 
	 * @param c The coordinate, in slice-lengths.
	 * 
	 * @return The coordinate of the start of the slice, in tile-lengths.
	 */
	public static int tileCoordFromSliceCoord(int c) {
		return c * Slice.SLICE_SIZE;
	}
	
	/**
	 * Gets the coordinate of the start of a region at the given coordinate,in
	 * tile-lengths.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in region-lengths.
	 * 
	 * @return The coordinate of the start of the region, in tile-lengths.
	 */
	public static int tileCoordFromRegionCoord(int c) {
		return c * Region.REGION_SIZE_IN_TILES;
	}
	
	/**
	 * Gets the coordinate of the tile, relative to its parent slice, at the
	 * given coordinate. That is, converts the given tile coordinate to local
	 * slice space.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the tile, in tile-lengths, relative to its
	 * parent slice.
	 */
	public static int tileCoordRelativeToSliceFromTileCoord(int c) {
		//return MathsUtil.wrappedRem(c, Slice.SLICE_SIZE);
		//return MathsUtil.wrappedRem2(c, Slice.SLICE_SIZE);		// Way faster
		return c & Slice.SLICE_SIZE_MINUS_ONE;						// One less instruction
	}
	
	/**
	 * Gets the coordinate of the tile, relative to its parent region, at the
	 * given coordinate. That is, converts the given tile coordinate to local
	 * region space.
	 * 
	 * <p>Note that the given coordinate may be one along any axis.
	 * 
	 * @param c The coordinate, in tile-lengths.
	 * 
	 * @return The coordinate of the tile, in tile-lengths, relative to its
	 * parent region.
	 */
	public static int tileCoordRelativeToRegionFromTileCoord(int c) {
		//return MathsUtil.wrappedRem(c, Region.REGION_SIZE_IN_TILES);
		//return MathsUtil.wrappedRem2(c, Region.REGION_SIZE_IN_TILES);		// Way faster
		return c & Region.REGION_SIZE_IN_TILES_MINUS_ONE;					// One less instruction
	}
	
}
