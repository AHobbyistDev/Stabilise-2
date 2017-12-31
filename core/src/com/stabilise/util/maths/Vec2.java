package com.stabilise.util.maths;

/**
 * A mutable 2D vector.
 */
public class Vec2 {
    
    public float x,y;
    
    
    /**
     * Creates a zero vector.
     */
    public Vec2() {
        this(0f,0f);
    }
    
    /**
     * Creates a Vec2 with the given components.
     */
    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Sets the value of this vector.
     * 
     * @return This vector.
     */
    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * @return The dot product of this vector with another.
     * @throws NullPointerException if {@code v} is {@code null}.
     */
    public float dot(Vec2 v) {
        return x * v.x + y * v.y;
    }
    
    /**
     * @return The dot product of this vector with the specified vector
     * components.
     */
    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }
    
    /**
     * Subtracts another vector from this vector and stores the result in
     * {@code dest}.
     * 
     * @return dest
     * @throws NullPointerException if either argument is {@code null}.
     */
    public Vec2 sub(Vec2 v, Vec2 dest) {
        return dest.set(x - v.x, y - v.y);
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
     */
    public Vec2 rotate(float cos, float sin, Vec2 dest) {
        return dest.set(
                x * cos - y * sin,
                x * sin + y * cos
        );
    }
    
    /**
     * Rotates this vector 90 degrees anticlockwise and returns the result.
     */
    /*
    public Vec2 rotate90Degrees() {
        return new Vec2(-y, x);
    }
    */
    
    @Override
    public int hashCode() {
        return Float.floatToRawIntBits(x) ^ Float.floatToRawIntBits(y);
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(!(o instanceof Vec2)) return false;
        Vec2 v = (Vec2)o;
        return x == v.x && y == v.y;
    }
    
    @Override
    public String toString() {
        return "Vec2[" + x + "," + y + "]";
    }
    
}
