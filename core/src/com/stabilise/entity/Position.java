package com.stabilise.entity;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Exportable;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;


/**
 * Holds the position of an object in the world.
 * 
 * <p>An object's position is stored as four values: two ints (sliceX, sliceY),
 * and either two ints or two floats (localX, localY); see {@link
 * PositionFixed} and {@link PositionFree} respectively.
 * 
 * <p>This strategy for storing positions avoids a loss of floating-point
 * precision when an entity moves far away from the origin, as opposed to, say,
 * two doubles for (x,y).
 * 
 * <p>However, for either convenience or legacy reasons one may wish to
 * represent an entity's position as two doubles (x,y), and so this class
 * provides a way of converting between the two representations.
 * 
 * @see PositionFixed
 * @see PositionFree
 */
public abstract class Position implements Exportable, Cloneable {
    
    /** Slice coordinates, in slice-lengths. */
    public int sx, sy;
    
    
    
    /**
     * Creates a new Position at (0,0).
     */
    protected Position() {
        // fields are initialised to default values of all zeros, ty java
    }
    
    /**
     * Gets the x-coordinate of the region this Position is in.
     */
    public int rx() {
        return regionCoordFromSliceCoord(sx);
    }
    
    /**
     * Gets the x-coordinate of the region this Position is in.
     */
    public int ry() {
        return regionCoordFromSliceCoord(sy);
    }
    
    /**
     * Gets the x-coordinate of the slice this Position is in.
     * 
     * @see #sx
     */
    public int sx() {
        return sx;
    }
    
    /**
     * Gets the y-coordinate of the slice this Position is in.
     * 
     * @see #sy
     */
    public int sy() {
        return sy;
    }
    
    /**
     * Gets the x-coordinate of this Position, relative to the slice it is in.
     * Note that if this Position has not been {@link #align() aligned}, then
     * the returned value may not lie in the valid range of such local
     * coordinates.
     */
    public abstract float lx();
    
    /**
     * Gets the y-coordinate of this Position, relative to the slice it is in.
     * Note that if this Position has not been {@link #align() aligned}, then
     * the returned value may not lie in the valid range of such local
     * coordinates.
     */
    public abstract float ly();
    
    /**
     * Gets the clamped/tile-coordinate equivalent of {@link #lx()}.
     */
    public abstract int ltx();
    
    /**
     * Gets the clamped/local tile coordinate equivalent of {@link #ly}.
     */
    public abstract int lty();
    
    /**
     * Returns the x-coordinate of this Position, in tile-lengths, which is
     * equivalent (up to finite precision) to the position specified jointly by
     * {@link #sx} and {@link #tileX}.
     * 
     * @see #tileCoordFromLocalCoords(int, float)
     */
    public double gx() {
        return tileCoordFromLocalCoords(sx, lx());
    }
    
    /**
     * Returns the y-coordinate of this Position, in tile-lengths, which is
     * equivalent (up to finite precision) to the position specified jointly by
     * {@link #sy} and {@link #tileY}.
     * 
     * @see #tileCoordFromLocalCoords(int, float)
     */
    public double gy() {
        return tileCoordFromLocalCoords(sy, ly());
    }
    
    /**
     * Sets {@link #sx}
     */
    public void setSx(int sx) {
        this.sx = sx;
    }
    
    /**
     * Sets {@link #sy}
     */
    public void setSy(int sy) {
        this.sy = sy;
    }
    
    /**
     * Sets the local x-coordinate. If this is a fixed position, the fractional
     * information is lost. Note: this method does <em>not</em> align the
     * result!
     */
    public abstract void setLx(float lx);
    
    /**
     * Sets the local y-coordinate. If this is a fixed position, the fractional
     * information is lost. Note: this method does <em>not</em> align the
     * result!
     */
    public abstract void setLy(float ly);
    
    /**
     * Sets the local x-coordinate to an integer value. Note: this method does
     * <em>not</em> align the result!
     */
    public abstract void setLx(int lx);
    
    /**
     * Sets the local y-coordinate to an integer value. Note: this method does
     * <em>not</em> align the result!
     */
    public abstract void setLy(int ly);
    
    /**
     * Sets the coordinates of this Position, and then returns this Position
     * object.
     */
    public abstract Position set(int sliceX, int sliceY, float localX, float localY);
    
    /**
     * Sets the coordinates of this Position, and then returns this Position
     * object.
     */
    public abstract Position set(int sliceX, int sliceY, int localX, int localY);
    
    /**
     * Sets the coordinates of this Position, and then returns this Position
     * object.
     */
    public abstract Position set(double x, double y);
    
    /**
     * Sets this Position to the same value as the given Position, and then
     * returns this Position object.
     * 
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public abstract Position set(Position p);
    
    /**
     * Sets this Position to the same value as the given Position, but with
     * lx and ly incremented by dx and dy respectively. This method does not
     * invoke {@link #align()}, so lx and ly may fall outside of the slice
     * bounds.
     * 
     * @return this Position.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public abstract Position set(Position p, float dx, float dy);
    
    /**
     * Sets the x-component values of this Position to the same as those of the
     * given position, but with dx added to lx. This method does not invoke
     * {@link #alignX()}, so lx may fall outside of the slice bounds.
     *  
     * @return this Position.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public abstract Position setX(Position p, float dx);
    
    /**
     * Sets the y-component values of this Position to the same as those of the
     * given position, but with dy added to ly. This method does not invoke
     * {@link #alignY()}, so ly may fall outside of the slice bounds.
     *  
     * @return this Position.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public abstract Position setY(Position p, float dy);
    
    /**
     * Sets this Position to the sum of the two given positions, (i.e. this =
     * p1 + p2). Warning: this method does not invoke {@link #align()}.
     * 
     * @return this Position.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public abstract Position setSum(Position p1, Position p2);
    
    /**
     * Sets this Position to the difference of the two given positions, (i.e.
     * this = p1 - p2). Warning: this method does not invoke {@link #align()}.
     * 
     * @return this Position.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public abstract Position setDiff(Position p1, Position p2);
    
    /**
     * Adds dx and dy to lx and ly respectively. This method does not invoke
     * {@link #align()}, so lx and ly may fall outside of the slice bounds.
     * 
     * @return this Position.
     */
    public Position add(float dx, float dy) {
        addX(dx);
        addY(dy);
        return this;
    }
    
    /**
     * Adds dx to lx. This method does not invoke {@link #alignX()}, so lx may
     * fall outside of the slice bounds.
     * 
     * @return this Position.
     */
    public abstract Position addX(float dx);
    
    /**
     * Adds dy to ly. This method does not invoke {@link #alignY()}, so ly may
     * fall outside of the slice bounds.
     * 
     * @return this Position.
     */
    public abstract Position addY(float dy);
    
    /**
     * Adds dx and dy to lx and ly respectively. This method does not invoke
     * {@link #align()}, so lx and ly may fall outside of the slice bounds.
     * 
     * @return this Position.
     */
    public Position add(int dx, int dy) {
        addX(dx);
        addY(dy);
        return this;
    }
    
    /**
     * Adds dx to lx. This method does not invoke {@link #alignX()}, so lx may
     * fall outside of the slice bounds.
     * 
     * @return this Position.
     */
    public abstract Position addX(int dx);
    
    /**
     * Adds dy to ly. This method does not invoke {@link #alignY()}, so ly may
     * fall outside of the slice bounds.
     * 
     * @return this Position.
     */
    public abstract Position addY(int dy);
    
    
    /**
     * Reflects this Position (i.e., negates everything). This method does not
     * invoke {@link #align()}, so lx and ly will almost certainly fall outside
     * slice bounds.
     * 
     * @return this Position.
     */
    public abstract Position reflect();
    
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
     * @return this Position.
     * 
     * @see #align()
     */
    public abstract Position alignX();
    
    /**
     * As with {@link #align()}, but only aligns the y-coordinates. Use this if
     * you know only {@link #ly} has been changed and want to save slightly on
     * computation time.
     * 
     * @return this Position.
     * 
     * @see #align()
     */
    public abstract Position alignY();
    
    /**
     * Turns this into a "global position", by setting {@link #sx} and {@link
     * #sy} to zero and changing {@code #lx} and {@link #ly} in accordance.
     * This method is essentially the opposite of {@link #align()}.
     * 
     * @return this Position.
     */
    public abstract Position globalify();
    
    /**
     * Returns the relative distance along the x-axis between the given
     * position and this one.
     * 
     * @return other.x - x, in tile-lengths.
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public float diffX(Position other) {
        return (other.sx - sx) * Slice.SLICE_SIZE + other.lx() - lx();
    }
    
    /**
     * Returns the relative distance along the x-axis between the given
     * position and this one.
     * 
     * @return other.x - x, in tile-lengths.
     */
    public float diffX(int otherSliceX, float otherTileX) {
        return (otherSliceX - sx) * Slice.SLICE_SIZE + otherTileX - lx();
    }
    
    /**
     * Returns the relative distance along the y-axis between the given
     * position and this one.
     * 
     * @return other.y - y, in tile-lengths.
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public float diffY(Position other) {
        return (other.sy - sy) * Slice.SLICE_SIZE + other.ly() - ly();
    }
    
    /**
     * Returns the relative distance along the y-axis between the given
     * position and this one.
     * 
     * @return other.y - y, in tile-lengths.
     */
    public float diffY(int otherSliceY, float otherTileY) {
        return (otherSliceY - sy) * Slice.SLICE_SIZE + otherTileY - ly();
    }
    
    /**
     * Returns the relative distance squared between the given position and
     * this one.
     * 
     * @return diffX()^2 + diffY()^2
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public float distSq(Position other) {
        float dx = diffX(other);
        float dy = diffY(other);
        return dx*dx + dy*dy;
    }
    
    /**
     * Returns the square of the distance from the origin to this position.
     */
    public double distFromOriginSq() {
        double x = gx();
        double y = gy();
        return x*x + y*y;
    }
    
    /**
     * Returns the distance from the origin to this position.
     */
    public double distFromOrigin() {
        return Math.sqrt(distFromOriginSq());
    }
    
    /**
     * Checks for whether or not another position is within a radius of {@code
     * dist} from this one.
     * 
     * @return {@code true} if {@code diffSq(other) <= dist*dist}.
     * @throws NullPointerException if {@code other} is {@code null}.
     */
    public boolean isWithinRange(Position other, float dist) {
    	return distSq(other) <= dist*dist;
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
     * Clamps this position to that of an exact tile by removing any fractional
     * part.
     * 
     * @return this Position.
     * 
     * @see #fixed()
     */
    public abstract Position clampToTile();
    
    /**
     * Converts this Position to a PositionFree. If this Position is already
     * free, then itself is returned.
     */
    public PositionFree free() {
        return new PositionFree().set(this);
    }
    
    /**
     * Converts this Position to a PositionFixed. If this Position is already
     * fixed, then itself is returned.
     * 
     * @see #clampToTile()
     */
    public PositionFixed fixed() {
        return new PositionFixed().set(this);
    }
    
    /**
     * Clones this Position object.
     */
    @Override
    public abstract Position clone();
    
    @Override
    public void importFromCompound(DataCompound c) {
        sx = c.getI32("sx");
        sy = c.getI32("sy");
        
        // Subclasses must override
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        c.put("sx", sx);
        c.put("sy", sy);
        
        // Subclasses must override
    }
    
    @Override
    public abstract int hashCode();
    
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
    public abstract boolean equalsPos(Position p);
    
    /**
     * {@inheritDoc}
     * 
     * @return "Pos[(sx,sy); (lx,ly)]"
     */
    @Override
    public abstract String toString();
    
    /**
     * Returns a string representation of this Position in terms of the global
     * coordinates.
     * 
     * @return "(x,y)"
     */
    public String toGlobalString() {
        return "(" + gx() + "," + gy() + ")";
    }
    
    
    
    
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates a new free position at (0,0).
     */
    public static PositionFree create() {
        return new PositionFree();
    }
    
    /**
     * Creates a new free position from the given slice coordinates and local
     * coordinates.
     */
    public static PositionFree create(int sliceX, int sliceY, float localX, float localY) {
        return new PositionFree().set(sliceX, sliceY, localX, localY);
    }
    
    /**
     * Creates a position from the given global coordinates. x and y are
     * decomposed into slice and local coordinates.
     */
    public static PositionFree create(double x, double y) {
        return new PositionFree().set(x, y);
    }
    
    /**
     * Creates a new fixed position at (0,0).
     */
    public static PositionFixed createFixed() {
        return new PositionFixed();
    }
    
    /**
     * Creates a new fixed position from the given slice coordinates and local
     * coordinates.
     */
    public static PositionFixed create(int sliceX, int sliceY, int localX, int localY) {
        return new PositionFixed().set(sliceX, sliceY, localX, localY);
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
    public static float tileCoordFixedToTileCoordFree(int c) {
        return (float)c;
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
