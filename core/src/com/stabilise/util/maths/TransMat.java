package com.stabilise.util.maths;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A full transformation (including translations) "matrix".
 * 
 * <p>Ordinarily one uses a 2x2 matrix to achieve the full set of affine
 * transformations of a 2D vector:
 * <pre>
 * | m00 m01 |
 * | m10 m11 |</pre>
 * However, spatial transformations can't work this way! The usual method of
 * getting around this is to instead use 3D vectors and 3x3 matrices:
 * <pre>
 * | m00 m01 t_x |
 * | m10 m11 t_y |
 * |  0   0   1  |</pre>
 * This achieves the desired properties, as long as we use vectors with
 * components <tt>[x,y,1]</tt> and ignore the z-component.
 * 
 * <p>This class implements the 3x3 matrix as a 2x3 matrix internally (i.e.,
 * we don't bother with variables for the bottom <tt>[0,0,1]</tt> row since
 * we know what it'll be; this also saves us from the need to use 3D vectors).
 */
@NotThreadSafe
public class TransMat {
    
    public float m00, m01, m10, m11, tx, ty;
    
    
    /**
     * Creates an identity matrix.
     */
    public TransMat() {
        m00 = m11 = 1;
        m01 = m10 = 0;
        tx = ty = 0;
    }
    
    /**
     * Creates a matrix with the specified entries.
     */
    public TransMat(float m00, float m01, float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }
    
    /**
     * Creates a matrix with the specified entries.
     */
    public TransMat(float m00, float m01, float m10, float m11, float tx, float ty) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.tx = tx;
        this.ty = ty;
    }
    
    /**
     * Creates a new matrix with the specified entries.
     * 
     * @param vals The entries.
     * @throws NullPointerException if {@code vals.length < 4}.
     */
    public TransMat(float... vals) {
        if(vals.length < 4)
            throw new IllegalArgumentException("vals.length < 4");
        m00 = vals[0];
        m01 = vals[1];
        m10 = vals[2];
        m11 = vals[3];
    }
    
    /**
     * Sets the entries of this matrix.
     * 
     * @return This matrix, for chaining operations.
     */
    public TransMat set(float m00, float m01, float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        return this;
    }
    
    /**
     * Sets the entries of this matrix.
     * 
     * @return This matrix, for chaining operations.
     */
    public TransMat set(float m00, float m01, float m10, float m11, float tx, float ty) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.tx = tx;
        this.ty = ty;
        return this;
    }
    
    /**
     * Copies the values of the provided matrix to this matrix.
     * 
     * @param m The matrix to copy.
     * 
     * @return This matrix, for chaining operations.
     */
    public TransMat set(TransMat m) {
        m00 = m.m00;
        m01 = m.m01;
        m10 = m.m10;
        m11 = m.m11;
        tx = m.tx;
        ty = m.ty;
        return this;
    }
    
    /**
     * Sets this matrix to the identity matrix.
     * 
     * @return This matrix, for chaining operations.
     */
    public TransMat identity() {
        return set(1f, 0f, 0f, 1f, 0f, 0f);
    }
    
    /**
     * Gets the determinant of this matrix.
     */
    public float det() {
        return m00 * m11 - m01 * m10;
    }
    
    /**
     * Gets the inverse of this matrix. Does not modify this matrix.
     * 
     * @throws ArithmeticException if this matrix does not have an inverse.
     */
    public TransMat inverse() {
        float det = det();
        if(det == 0)
            throw new ArithmeticException("Determinant is zero");
        return doInverse(1 / det);
    }
    
    private TransMat doInverse(float invDet) {
        return new TransMat(
                invDet * m11,  -invDet * m01,
                -invDet * m10, invDet * m00,
                invDet * (ty*m01 - tx*m11), invDet * (tx*m10 - ty*m00)
        );
    }
    
    /**
     * Postmultiplies this matrix (A) with the specified matrix (B) and stores
     * the result in this matrix. i.e. A = AB.
     * 
     * @return This matrix, for chaining operations.
     * @throws NullPointerException if {@code m} is {@code null}.
     */
    public TransMat mul(TransMat m) {
        return multiply(this, m, this);
    }
    
    /**
     * Postmultiplies this matrix (A) with the specified matrix (B) and stores
     * the result in dest (C). i.e. C = AB.
     * 
     * @return dest, for chaining operations.
     * @throws NullPointerException if {@code m} is {@code null}.
     */
    public TransMat mul(TransMat m, TransMat dest) {
        return multiply(this, m, dest);
    }
    
    /**
     * Premultiplies this matrix (A) with the specified matrix (B) and stores
     * the result in this matrix. i.e. A = BA.
     * 
     * @return This matrix, for chaining operations.
     * @throws NullPointerException if {@code m} is {@code null}.
     */
    public TransMat mulLeft(TransMat m) {
        return multiply(m, this, this);
    }
    
    /**
     * Transforms vec by this matrix and stores the result in {@code dest}.
     * 
     * @param vec The vector to multiply.
     * 
     * @return dest
     * @throws NullPointerException if either argument is {@code null}.
     * @throws UnsupportedOperationException if {@code dest} is immutable.
     */
    public Vec2 transform(Vec2 vec, Vec2 dest) {
        return dest.set(
                m00 * vec.x + m01 * vec.y + tx,
                m10 * vec.x + m11 * vec.y + ty
        );
    }
    
    /**
     * Transforms vec by this matrix and returns the resultant vector. Does not
     * modify vec.
     * 
     * @param vec The vector to multiply.
     * 
     * @return The resultant vector, which is mutable iff {@code vec} is.
     * @throws NullPointerException if {@code vec} is {@code null}.
     */
    public Vec2 transform(Vec2 vec) {
        return new Vec2(
                m00 * vec.x + m01 * vec.y + tx,
                m10 * vec.x + m11 * vec.y + ty
        );
    }
    
    /**
     * Sets this matrix to a rotation matrix, which will rotate a vector
     * anticlockwise about (0,0).
     * 
     * @param rad The angle, in radians.
     * 
     * @return This matrix, for chaining operations.
     */
    /*
    public TransMat setToRotation(float rad) {
        float cos = MathUtils.cos(rad); //(float)Math.cos(rad);
        float sin = MathUtils.sin(rad); //(float)Math.sin(rad);
        return set(cos, -sin, sin, cos);
    }
    */
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Multiplies the left matrix (A) by the right matrix (B) and stores the
     * result in the destination matrix (C). i.e. C = AB.
     * 
     * @return The destination matrix.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static TransMat multiply(TransMat left, TransMat right, TransMat dest) {
        return dest.set(
                left.m00*right.m00 + left.m01*right.m10,
                left.m00*right.m01 + left.m01*right.m11,
                left.m10*right.m00 + left.m11*right.m10,
                left.m10*right.m01 + left.m11*right.m11,
                left.m00*right.tx + left.m01*right.ty + left.tx,
                left.m10*right.tx + left.m11*right.ty + left.ty
        );
    }
    
}
