package com.stabilise.util.maths;

/**
 * An object encapsulating integer x and y coordinates. Use {@link
 * #mutable(int, int)} to create a mutable point, or {@link
 * #immutable(int, int)} to create an immutable point. Mutable and immutable
 * points with equal components are considered equal.
 */
public abstract class Point {
    
    protected int x, y;
    
    protected Point() {}
    
    /**
     * Returns the x component of this point.
     */
    public final int x() { return x; }
    
    /**
     * Returns the y component of this point.
     */
    public final int y() { return y; }
    
    /**
     * Sets the components of this point, and returns this point.
     * 
     * @throws UnsupportedOperationException if this point is immutable.
     */
    public abstract Point set(int x, int y);
    
    /**
     * Sets the components of this point to those of the specified point, and
     * returns this point.
     * 
     * @throws NullPointerException if {@code p} is {@code null}.
     * @throws UnsupportedOperationException if this point is immutable.
     */
    public Point set(Point p) {
        return set(p.x(), p.y());
    }
    
    /**
     * Generates and returns this point's hash code.
     */
    protected int genHash() {
        // This has too many close collisions for my liking, but is acceptable
        // as a default hash which incorporates all of the state.
        return x() ^ y();
        
        // Collisions are nicely distributed this way (though there's collision
        // clumping nearby (0,0) as there's more or less mirroring about (0,0))
        //return x ^ (y << 16) ^ (y >>> 16); // Cyclicly shift y by 16 bits
        
        // This eliminates higher-order bits, and as such is susceptible to
        // collisions between two points (x0, y0) and (x1, y1) when:
        // Maths.wrappedRem(x0, 65536) == Maths.wrappedRem(x1, 65536) &&
        // Maths.wrappedRem(y0, 65536) == Maths.wrappedRem(y1, 65536)
        // I feel this is the best option for collision distribution since
        // nearby points shouldn't have hash collisions at all.
        // However, since this disobeys the general contract of hashCode() to
        // utilise all of an object's state, we won't use it.
        //return (getX() << 16) | (getY() & 0xFFFF);
    }
    
    @Override
    public int hashCode() {
        return genHash();
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Point)) return false;
        Point p = (Point)o;
        return x() == p.x() && y() == p.y();
    }
    
    /**
     * @return {@code true} if this point holds the specified coordinates;
     * {@code false} otherwise.
     */
    public boolean equals(int x, int y) {
        return x() == x && y() == y;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + x() + "," + y() + "]";
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Gets a new mutable point with components (0,0).
     */
    public static Point mutable() {
        return mutable(0, 0);
    }
    
    /**
     * Gets a new mutable point with the specified components.
     */
    public static Point mutable(int x, int y) {
        return new MutablePoint(x, y);
    }
    
    /**
     * Gets a new immutable point with components (0,0).
     */
    public static Point immutable() {
        return immutable(0, 0);
    }
    
    /**
     * Gets a new immutable point with the specified components.
     */
    public static Point immutable(int x, int y) {
        return new ImmutablePoint(x, y);
    }
    
    // Nested classes ---------------------------------------------------------
    
    static class ImmutablePoint extends Point {
        
        private final int hash;
        
        ImmutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
            this.hash = genHash();
        }
        
        @Override
        public Point set(int x, int y) {
            throw new UnsupportedOperationException("This point is immutable");
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
    }
    
    static class MutablePoint extends Point {
        
        MutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public Point set(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }
        
    }
    
}
