package com.stabilise.util.maths;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A 2x2 row-major matrix. Such a matrix takes the form:
 * 
 * <pre>
 * | m00 m01 |
 * | m10 m11 |</pre>
 */
@NotThreadSafe
public class Matrix2 {
    
    public float m00, m01, m10, m11;
    
    
    /**
     * Creates an identity matrix.
     */
    public Matrix2() {
        m00 = m11 = 1;
        m01 = m10 = 0;
    }
    
    /**
     * Creates a matrix with the specified entries.
     */
    public Matrix2(float m00, float m01, float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }
    
    /**
     * Creates a new matrix with the specified entries.
     * 
     * @param vals The entries.
     * @throws NullPointerException if {@code vals.length < 4}.
     */
    public Matrix2(float... vals) {
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
    public Matrix2 set(float m00, float m01, float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        return this;
    }
    
    /**
     * Copies the values of the provided matrix to this matrix.
     * 
     * @param m The matrix to copy.
     * 
     * @return This matrix, for chaining operations.
     */
    public Matrix2 set(Matrix2 m) {
        m00 = m.m00;
        m01 = m.m01;
        m10 = m.m10;
        m11 = m.m11;
        return this;
    }
    
    /**
     * Sets this matrix to the identity matrix.
     * 
     * @return This matrix, for chaining operations.
     */
    public Matrix2 identity() {
        return set(1f, 0f, 0f, 1f);
    }
    
    /**
     * Gets the transpose of this matrix. This matrix will remain unmodified.
     * 
     * @return The transpose of this matrix.
     */
    public Matrix2 transpose() {
        return new Matrix2(
                m00, m10,
                m01, m11
        );
    }
    
    /**
     * Gets the determinant of this matrix.
     */
    public float det() {
        return m00 * m11 - m01 * m10;
    }
    
    /**
     * Gets the inverse of this matrix.
     * 
     * @throws ArithmeticException if this matrix does not have an inverse.
     */
    public Matrix2 inverse() {
        float det = det();
        if(det == 0)
            throw new ArithmeticException("Determinant is zero");
        return doInverse(1 / det);
    }
    
    private Matrix2 doInverse(float invDet) {
        return new Matrix2(
                invDet * m11,  -invDet * m01,
                -invDet * m10, invDet * m00
        );
    }
    
    /**
     * Postmultiplies this matrix (A) with the specified matrix (B) and stores
     * the result in this matrix. i.e. A = AB.
     * 
     * @return This matrix, for chaining operations.
     * @throws NullPointerException if {@code m} is {@code null}.
     */
    public Matrix2 mul(Matrix2 m) {
        return multiply(this, m, this);
    }
    
    /**
     * Premultiplies this matrix (A) with the specified matrix (B) and stores
     * the result in this matrix. i.e. A = BA.
     * 
     * @return This matrix, for chaining operations.
     * @throws NullPointerException if {@code m} is {@code null}.
     */
    public Matrix2 mulLeft(Matrix2 m) {
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
                m00 * vec.x() + m01 * vec.y(),
                m10 * vec.x() + m11 * vec.y()
        );
    }
    
    /**
     * Transforms vec by this matrix and returns the resultant vector.
     * 
     * @param vec The vector to multiply.
     * 
     * @return The resultant vector, which is mutable iff {@code vec} is.
     * @throws NullPointerException if {@code vec} is {@code null}.
     */
    public Vec2 transform(Vec2 vec) {
        return vec.isMutable() ? transformM(vec) : transformI(vec);
    }
    
    /**
     * Transforms vec by this matrix and returns the resultant mutable vector.
     * 
     * @param vec The vector to multiply.
     * 
     * @return The resultant mutable vector.
     * @throws NullPointerException if {@code vec} is {@code null}.
     */
    public Vec2 transformM(Vec2 vec) {
        return Vec2.mutable(
                m00 * vec.x() + m01 * vec.y(),
                m10 * vec.x() + m11 * vec.y()
        );
    }
    
    /**
     * Transforms vec by this matrix and returns the resultant immutable
     * vector.
     * 
     * @param vec The vector to multiply.
     * 
     * @return The resultant immutable vector.
     * @throws NullPointerException if {@code vec} is {@code null}.
     */
    public Vec2 transformI(Vec2 vec) {
        return Vec2.immutable(
                m00 * vec.x() + m01 * vec.y(),
                m10 * vec.x() + m11 * vec.y()
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
    public Matrix2 setToRotation(float rad) {
        float cos = MathUtils.cos(rad); //(float)Math.cos(rad);
        float sin = MathUtils.sin(rad); //(float)Math.sin(rad);
        return set(cos, -sin, sin, cos);
    }
    */
    
    /**
     * Transforms the specified vector (V) by this matrix (M) and stores it in
     * the specified destination vector (D). i.e. D = MV.
     * 
     * @param vec The vector to multiply.
     * @param dest The destination vector.
     * 
     * @return The destination vector.
     * @throws NullPointerException if either {@code vec} or {@code dest} are
     * {@code null}.
     */
    /*
    public Vec2 transform(Vec2 vec, Vec2 dest) {
        float x = m00*vec.x + m01*vec.y;
        float y = m10*vec.x + m11*vec.y;
        return dest.set(x, y);
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
    public static Matrix2 multiply(Matrix2 left, Matrix2 right, Matrix2 dest) {
        return dest.set(
                left.m00*right.m00 + left.m01*right.m10,
                left.m00*right.m01 + left.m01*right.m11,
                left.m10*right.m00 + left.m11*right.m10,
                left.m10*right.m01 + left.m11*right.m11
        );
    }
    
}
