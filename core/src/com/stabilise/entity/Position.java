package com.stabilise.entity;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Exportable;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;


/**
 * Holds the position of an entity (or more generally, a FreeGameObject) in the
 * world.
 * 
 * <p>An entity's position is stored as two ints and two floats (sliceX,
 * sliceY, localX, localY) representing the slice an entity is in, and the
 * entity's coordinates relative to that slice. This avoids a loss of
 * floating-point precision when an entity moves far away from the origin.
 * 
 * <p>However, for either convenience or legacy reasons one may wish to
 * represent an entity's position as two doubles (x,y), and so this class
 * provides a way of converting between the two representations.
 */
public class Position implements Exportable {
    
    // Keep the field names nice and short since they'll probably be used all
    // throughout the codebase, and it gets tedious writing e.g., sliceX and
    // localY everywhere.
    
    /** Slice coordinates, in slice-lengths. */
    public int sx, sy;
    /** Local coordinates, in tile-lengths, relative to the slice specified by
     * {@link #sx} and {@link #sy}. */
    public float lx, ly;
    
    
    /**
     * Creates a new Position at (0,0).
     */
    protected Position() {
        // fields are initialised to default values of all zeros, ty java
    }
    
    /**
     * Sets the coordinates of this Position, and then returns this Position
     * object.
     */
    public Position set(int sliceX, int sliceY, float localX, float localY) {
        this.sx = sliceX;
        this.sy = sliceY;
        this.lx = localX;
        this.ly = localY;
        return this;
    }
    
    /**
     * Sets the coordinates of this Position, and then returns this Position
     * object.
     */
    public Position set(double x, double y) {
        this.sx = sliceCoordFromTileCoord(x);
        this.sy = sliceCoordFromTileCoord(y);
        this.lx = (float)tileCoordRelativeToSliceFromTileCoordFree(x);
        this.ly = (float)tileCoordRelativeToSliceFromTileCoordFree(y);
        return this;
    }
    
    /**
     * Sets this Position to the same value as the given Position, and then
     * returns this Position object.
     * 
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public Position set(Position p) {
        sx = p.sx;
        sy = p.sy;
        lx = p.lx;
        ly = p.ly;
        return this;
    }
    
    /**
     * Sets this Position to the same value as the given Position, but with
     * lx and ly incremented by dx and dy respectively. This method does not
     * invoke {@link #align()}, so lx and ly may fall outside of the slice
     * bounds.
     * 
     * @return this Position.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public Position set(Position p, float dx, float dy) {
        sx = p.sx;
        sy = p.sy;
        lx = p.lx + dx;
        ly = p.ly + dy;
        return this;
    }
    
    /**
     * Sets the x-component values of this Position to the same as those of the
     * given position, but with dx added to lx. This method does not invoke
     * {@link #alignX()}, so lx may fall outside of the slice bounds.
     *  
     * @return this Position.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public Position setX(Position p, float dx) {
    	sx = p.sx;
    	lx = p.lx;
    	return this;
    }
    
    /**
     * Sets the y-component values of this Position to the same as those of the
     * given position, but with dy added to ly. This method does not invoke
     * {@link #alignY()}, so ly may fall outside of the slice bounds.
     *  
     * @return this Position.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public Position setY(Position p, float dy) {
    	sy = p.sy;
    	ly = p.ly;
    	return this;
    }
    
    /**
     * Sets this Position to the sum of the two given positions, (i.e. this =
     * p1 + p2). Warning: this method does not invoke {@link #align()}.
     * 
     * @return this Position.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public Position setSum(Position p1, Position p2) {
    	sx = p1.sx + p2.sx;
    	sy = p1.sy + p2.sy;
    	lx = p1.lx + p2.lx;
    	ly = p1.ly + p2.ly;
    	return this;
    }
    
    /**
     * Sets this Position to the difference of the two given positions, (i.e.
     * this = p1 - p2). Warning: this method does not invoke {@link #align()}.
     * 
     * @return this Position.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public Position setDiff(Position p1, Position p2) {
    	sx = p1.sx - p2.sx;
    	sy = p1.sy - p2.sy;
    	lx = p1.lx - p2.lx;
    	ly = p1.ly - p2.ly;
    	return this;
    }
    
    /**
     * Adds dx and dy to lx and ly respectively. This method does not invoke
     * {@link #align()}, so lx and ly may fall outside of the slice bounds.

     * @return this Position.
     */
    public Position add(float dx, float dy) {
        lx += dx;
        ly += dy;
        return this;
    }
    
    /**
     * Gets the x-coordinate of the region this Position is in.
     */
    public int getRegionX() {
        return regionCoordFromSliceCoord(sx);
    }
    
    /**
     * Gets the x-coordinate of the region this Position is in.
     */
    public int getRegionY() {
        return regionCoordFromSliceCoord(sy);
    }
    
    /**
     * @see #sx
     */
    public int getSliceX() {
        return sx;
    }
    
    /**
     * @see #sy
     */
    public int getSliceY() {
        return sy;
    }
    
    /**
     * @see #lx
     */
    public float getLocalX() {
        return lx;
    }
    
    /**
     * @see #ly
     */
    public float getLocalY() {
        return ly;
    }
    
    /**
     * Gets the local tile coordinate from {@link #lx}.
     */
    public int getLocalTileX() {
        return Position.tileCoordFreeToTileCoordFixed2(lx);
    }
    
    /**
     * Gets the local tile coordinate from {@link #ly}.
     */
    public int getLocalTileY() {
        return Position.tileCoordFreeToTileCoordFixed2(ly);
    }
    
    /**
     * Returns the x-coordinate of this Position, in tile-lengths, which is
     * equivalent (up to finite precision) to the position specified jointly by
     * {@link #sx} and {@link #tileX}.
     * 
     * @see #tileCoordFromLocalCoords(int, float)
     */
    public double getGlobalX() {
        return tileCoordFromLocalCoords(sx, lx);
    }
    
    /**
     * Returns the y-coordinate of this Position, in tile-lengths, which is
     * equivalent (up to finite precision) to the position specified jointly by
     * {@link #sy} and {@link #tileY}.
     * 
     * @see #tileCoordFromLocalCoords(int, float)
     */
    public double getGlobalY() {
        return tileCoordFromLocalCoords(sy, ly);
    }
    
    /**
     * Aligns this position. That is, if {@link #lx} or {@link #ly} have
     * overflowed into another slice, this method clamps them back and adjusts
     * {@link #sx} and {@link #sy} appropriately.
     * 
     * @return this Position.
     * 
     * @see #alignX()
     * @see #alignY()
     */
    public Position align() {
        alignX();
        alignY();
        return this;
    }
    
    /**
     * As with {@link #align()}, but only aligns the x-coordinates. Use this if
     * you know only {@link #lx} has been changed and want to save slightly on
     * computation time.
     * 
     * @see #align()
     */
    public void alignX() {
        sx += sliceCoordFromTileCoord2(lx);
        lx = tileCoordRelativeToSliceFromTileCoordFree2(lx);
    }
    
    /**
     * As with {@link #align()}, but only aligns the y-coordinates. Use this if
     * you know only {@link #ly} has been changed and want to save slightly on
     * computation time.
     * 
     * @see #align()
     */
    public void alignY() {
        sy += sliceCoordFromTileCoord2(ly);
        ly = tileCoordRelativeToSliceFromTileCoordFree2(ly);
    }
    
    /**
     * Clamps this position to that of an exact tile by removing any fractional
     * part.
     * 
     * @return this Position.
     */
    public Position clampToTile() {
        lx = tileCoordFreeToTileCoordFixed2(lx);
        ly = tileCoordFreeToTileCoordFixed2(ly);
        return this;
    }
    
    /**
     * Returns the relative distance along the x-axis between the given
     * position and this one.
     * 
     * @return other.x - x, in tile-lengths.
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public float diffX(Position other) {
        return (other.sx - sx) * Slice.SLICE_SIZE + other.lx - lx;
    }
    
    /**
     * Returns the relative distance along the x-axis between the given
     * position and this one.
     * 
     * @return other.x - x, in tile-lengths.
     */
    public float diffX(int otherSliceX, float otherTileX) {
        return (otherSliceX - sx) * Slice.SLICE_SIZE + otherTileX - lx;
    }
    
    /**
     * Returns the relative distance along the y-axis between the given
     * position and this one.
     * 
     * @return other.y - y, in tile-lengths.
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public float diffY(Position other) {
        return (other.sy - sy) * Slice.SLICE_SIZE + other.ly - ly;
    }
    
    /**
     * Returns the relative distance along the y-axis between the given
     * position and this one.
     * 
     * @return other.y - y, in tile-lengths.
     */
    public float diffY(int otherSliceY, float otherTileY) {
        return (otherSliceY - sy) * Slice.SLICE_SIZE + otherTileY - ly;
    }
    
    /**
     * Returns the relative distance squared between the given position and
     * this one.
     * 
     * @return diffX()^2 + diffY()^2
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public float diffSq(Position other) {
        float dx = diffX(other);
        float dy = diffY(other);
        return dx*dx + dy*dy;
    }
    
    /**
     * Checks for whether or not another position is within a radius of {@code
     * dist} from this one.
     * 
     * @return {@code true} if {@code diffSq(other) <= dist*dist}.
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public boolean isWithinRange(Position other, float dist) {
    	return diffSq(other) <= dist*dist;
    }
    
    /**
     * Returns {@code true} if this Position is in the same slice as the given
     * Position. This method does not realign either positions, so if either
     * position is unaligned this method may return the erroneous naive result.
     * 
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public boolean inSameSlice(Position other) {
        return sx == other.sx && sy == other.sy;
    }
    
    /**
     * Returns {@code getSliceX() == sx && getSliceY() == sy}.
     */
    public boolean isInSlice(int sx, int sy) {
        return this.sx == sx && this.sy == sy;
    }
    
    /**
     * Clones this Position object.
     */
    public Position copy() {
        return new Position().set(this);
    }
    
    @Override
    public void importFromCompound(DataCompound o) {
        sx = o.getInt("sx");
        sy = o.getInt("sy");
        lx = o.getFloat("lx");
        ly = o.getFloat("ly");
    }
    
    @Override
    public void exportToCompound(DataCompound o) {
        o.put("sx", sx);
        o.put("sy", sy);
        o.put("lx", lx);
        o.put("ly", ly);
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(!(o instanceof Position)) return false;
        return equalsPos((Position) o);
    }
    
    /**
     * Returns true if the given position is equal to this one; false
     * otherwise.
     */
    public boolean equalsPos(Position p) {
        return sx == p.sx && sy == p.sy && lx == p.lx && ly == p.ly;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return "Pos[(sx,sy); (lx,ly)]"
     */
    @Override
    public String toString() {
        return "Pos[(" + sx + "," + sy + "); (" + lx + "," + ly + ")]";
    }
    
    /**
     * Returns a string representation of this Position in terms of the global
     * coordinates.
     * 
     * @return "(x,y)"
     */
    public String toGlobalString() {
        return "(" + getGlobalX() + "," + getGlobalY() + ")";
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates a new Position at (0,0).
     */
    public static Position create() {
        return new Position();
    }
    
    /**
     * Creates a new Position from the given slice coordinates and local
     * coordinates.
     */
    public static Position create(int sliceX, int sliceY, float localX, float localY) {
        return new Position().set(sliceX, sliceY, localX, localY);
    }
    
    /**
     * Creates a position from the given global coordinates. x and y are
     * decomposed into slice and local coordinates.
     */
    public static Position create(double x, double y) {
        return new Position().set(x, y);
    }
    
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
        return c >> Region.REGION_SIZE_IN_TILES_SHIFT;
    }
    
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
    public static int regionCoordFromTileCoord(double c) {
        return tileCoordFreeToTileCoordFixed(c) >> Region.REGION_SIZE_IN_TILES_SHIFT;
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
        return c >> Region.REGION_SIZE_SHIFT;
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
        return c >> Slice.SLICE_SIZE_SHIFT;
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
        //return Maths.floor(c / Slice.SLICE_SIZE);
        // Shift is faster than division
        return tileCoordFreeToTileCoordFixed(c) >> Slice.SLICE_SIZE_SHIFT;
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
    public static int sliceCoordFromTileCoord2(float c) {
        return tileCoordFreeToTileCoordFixed2(c) >> Slice.SLICE_SIZE_SHIFT;
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
        //return Maths.remainder2(c, Region.REGION_SIZE);
        
        // One less instruction:
        return sliceCoordFromTileCoord(c) & Region.REGION_SIZE_MINUS_ONE;
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
        //return Maths.remainder2(c, Region.REGION_SIZE);
        
        // One less instruction:
        return c & Region.REGION_SIZE_MINUS_ONE;
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
        //return Maths.remainder2(c, Slice.SLICE_SIZE);
        
        // One less instruction:
        return c & Slice.SLICE_SIZE_MINUS_ONE;
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
    public static double tileCoordRelativeToSliceFromTileCoordFree(double c) {
        double rem = Maths.remainder(c, Slice.SLICE_SIZE);
        // See comment in function below this for why we do this check.
        if(rem == 16.0d) // TODO: hardcoded 16 is bad
            rem = Slice.SLICE_SIZE_MINUS_EPSd;
        return rem;
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
     * parent slice. The value returned will be greater than or equal to 0, and
     * strictly less than {@link Slice#SLICE_SIZE}.
     */
    public static float tileCoordRelativeToSliceFromTileCoordFree2(float c) {
        float rem = Maths.remainder(c, Slice.SLICE_SIZE);
        // This is a very important adjustment that needs to be made in order
        // to ensure a correct result. To see this, let's peek at the code of
        // Maths.remainder():
        //     num %= div;
        //     return num >= 0 ? num : num + div;
        // The problem lies in "num >= 0 ? num : num + div". If num (in our
        // case, c) is negative but with sufficiently small absolute value,
        // then that precision may be lost when we add div (here, SLICE_SIZE)
        // to it. That is,
        //     num + div == div.
        // Thus, e.g. -0.0000001 may become 16.0. We do not want this. This
        // function should never return 16, only numbers strictly below it.
        // Hence if rem comes out as 16.0f, we return a value as close as
        // possible to -- but still less than -- 16 within machine precision.
        // 
        // Strictly speaking, the error lies in Maths.remainder() and the fix
        // belongs in there, but it's too much work and I won't bother.
        if(rem == Slice.SLICE_SIZEf)
            rem = Slice.SLICE_SIZE_MINUS_EPSf;
        return rem;
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
        //return Maths.remainder2(c, Region.REGION_SIZE_IN_TILES);
        
        // One less instruction:
        return c & Region.REGION_SIZE_IN_TILES_MINUS_ONE;
    }
    
    /**
     * Gets the coordinate of the tile which occupies the specified coordinate.
     * This method essentially provides a means to 'snap' an x or y to the
     * coordinate grid of the world.
     * 
     * @param c The coordinate, in tile-lengths.
     * 
     * @return The coordinate of the tile, in tile-lengths.
     */
    public static int tileCoordFreeToTileCoordFixed(double c) {
        return Maths.floor(c);
    }
    
    /**
     * Gets the coordinate of the tile which occupies the specified coordinate.
     * This method essentially provides a means to 'snap' an x or y to the
     * coordinate grid of the world.
     * 
     * @param c The coordinate, in tile-lengths.
     * 
     * @return The coordinate of the tile, in tile-lengths.
     */
    public static int tileCoordFreeToTileCoordFixed2(float c) {
        return Maths.floor(c);
    }
    
    /**
     * Converts a fixed - or integer - coordinate to a free - or floating point
     * - coordinate.
     * 
     * @param c The coordinate, in tile-lengths.
     * 
     * @return The coordinate, in tile-lengths.
     */
    public static double tileCoordFixedToTileCoordFree(int c) {
        return (double)c;
    }
    
    /**
     * Converts from local coordinates to global coordinate. That is, given a
     * slice coordinate (in slice-lengths) and a tile coordinate relative to
     * that slice (in tile-lengths), returns the equivalent global tile
     * coordinate.
     * 
     * @param slice The slice coordinate, in slice-lengths.
     * @param tile The local tile coordinate, in tile-lengths.
     * 
     * @return The global coordinate, in tile-lengths.
     */
    public static double tileCoordFromLocalCoords(int slice, float tile) {
        return (((double) slice) * Slice.SLICE_SIZE) + tile;
    }
    
}
