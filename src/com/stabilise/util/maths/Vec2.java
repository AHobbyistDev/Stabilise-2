package com.stabilise.util.maths;

/**
 * A 2D vector.
 * 
 * @see #mutable(float, float)
 * @see #immutable(float, float)
 */
public abstract class Vec2 {
    
    /** Gets the x-component of this vector. */
    public abstract float x();
    
    /** Gets the y-component of this vector. */
    public abstract float y();
    
    /**
     * Sets the value of this vector.
     * 
     * @return This vector.
     * @throws UnsupportedOperationException if this is an immutable vector.
     */
    public abstract Vec2 set(float x, float y);
    
    /**
     * @return The dot product of this vector with another.
     * @throws NullPointerException if {@code v} is {@code null}.
     */
    public float dot(Vec2 v) {
        return x() * v.x() + y() * v.y();
    }
    
    /**
     * @return The dot product of this vector with the specified vector
     * components.
     */
    public float dot(float x, float y) {
        return this.x() * x + this.y() * y;
    }
    
    /**
     * Subtracts another vector from this vector and stores the result in
     * {@code dest}.
     * 
     * @return dest
     * @throws NullPointerException if either argument is {@code null}.
     * @throws UnsupportedOperationException if {@code dest} is immutable.
     */
    public Vec2 sub(Vec2 v, Vec2 dest) {
        return dest.set(x() - v.x(), y() - v.y());
    }
    
    /**
     * Subtracts another vector from this vector and returns the resultant
     * vector.
     * 
     * @return The resultant vector, which is mutable iff this vector is.
     * @throws NullPointerException if v is {@code null}.
     */
    public Vec2 sub(Vec2 v) {
        return isMutable() ? subM(v) : subI(v);
    }
    
    /**
     * Subtracts another vector from this vector and returns the resultant
     * mutable vector.
     * 
     * @return A mutable vector equivalent to this - v.
     * @throws NullPointerException if v is {@code null}.
     */
    public Vec2 subM(Vec2 v) {
        return mutable(x() - v.x(), y() - v.y());
    }
    
    /**
     * Subtracts another vector from this vector and returns the resultant
     * immutable vector.
     * 
     * @return An immutable vector equivalent to this - v.
     * @throws NullPointerException if v is {@code null}.
     */
    public Vec2 subI(Vec2 v) {
        return immutable(x() - v.x(), y() - v.y());
    }
    
    /**
     * Rotates this vector anticlockwise by the specified angle.
     * 
     * @param radians The angle, in radians.
     * 
     * @return The new rotated vector.
     */
    /*
    public Vec2 rotate(float radians) {
        //return rotate((float)Math.cos(radians), (float)Math.sin(radians));
        return rotate(MathUtils.cos(radians), MathUtils.sin(radians));
    }
    */
    
    /**
     * Rotates this vector and stores the result in {@code dest}.
     * 
     * @param cos The cosine of the angle by which to rotate this vector.
     * @param sin The sine of the angle by which to rotate this vector.
     * @param dest The destination vector.
     * 
     * @return dest
     * @throws NullPointerException if dest is {@code null}.
     * @throws UnsupportedOperationException if {@code dest} is immutable.
     */
    public Vec2 rotate(float cos, float sin, Vec2 dest) {
        return dest.set(
                x() * cos - y() * sin,
                x() * sin + y() * cos
        );
    }
    
    /**
     * Rotates this vector and returns the resultant vector.
     * 
     * @param cos The cosine of the angle by which to rotate this vector.
     * @param sin The sine of the angle by which to rotate this vector.
     * 
     * @return The resultant vector, which is mutable iff this vector is.
     */
    public Vec2 rotate(float cos, float sin) {
        return isMutable() ? rotateM(cos, sin) : rotateI(cos, sin);
    }
    
    /**
     * Rotates this vector and returns the resultant mutable vector.
     * 
     * @param cos The cosine of the angle by which to rotate this vector.
     * @param sin The sine of the angle by which to rotate this vector.
     * 
     * @return A mutable vector.
     */
    public Vec2 rotateM(float cos, float sin) {
        return mutable(
                x() * cos - y() * sin,
                x() * sin + y() * cos
        );
    }
    
    /**
     * Rotates this vector and returns the resultant immutable vector.
     * 
     * @param cos The cosine of the angle by which to rotate this vector.
     * @param sin The sine of the angle by which to rotate this vector.
     * 
     * @return An immutable vector.
     */
    public Vec2 rotateI(float cos, float sin) {
        return immutable(
                x() * cos - y() * sin,
                x() * sin + y() * cos
        );
    }
    
    /**
     * Rotates this vector 90 degrees anticlockwise and returns the result.
     */
    /*
    public Vec2 rotate90Degrees() {
        return new Vec2(-y(), x());
    }
    */
    
    /**
     * Returns {@code true} if this vector is mutable; {@code false} otherwise.
     */
    public boolean isMutable() {
        return this instanceof Mutable;
    }
    
    @Override
    public int hashCode() {
        return Float.floatToRawIntBits(x()) ^ Float.floatToRawIntBits(y());
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Vec2)) return false;
        Vec2 v = (Vec2)o;
        return x() == v.x() && y() == v.y();
    }
    
    @Override
    public String toString() {
        return "Vec2[" + x() + "," + y() + "]";
    }
    
    /**
     * Creates a new mutable vector with the specified components.
     */
    public static Vec2 mutable(float x, float y) {
        return new Mutable(x, y);
    }
    
    /**
     * Creates a new immutable vector with the specified components.
     */
    public static Vec2 immutable(float x, float y) {
        return new Immutable(x, y);
    }
    
    // IMPLEMENTATION CLASSES -------------------------------------------------
    
    private static class Mutable extends Vec2 {
        
        private float x, y;
        
        public Mutable(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public float x() {
            return x;
        }
        
        @Override
        public float y() {
            return y;
        }
        
        @Override
        public Vec2 set(float x, float y) {
            this.x = x;
            this.y = y;
            return this;
        }
        
    }
    
    private static class Immutable extends Vec2 {
        
        private final float x, y;
        
        public Immutable(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public float x() {
            return x;
        }
        
        @Override
        public float y() {
            return y;
        }
        
        @Override
        public Vec2 set(float x, float y) {
            throw new UnsupportedOperationException("This vector is immutable");
        }
        
    }
    
}
