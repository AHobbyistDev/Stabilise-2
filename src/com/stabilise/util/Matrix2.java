package com.stabilise.util;

import com.badlogic.gdx.math.Vector2;

/**
 * A 2x2 row-major matrix. Such a matrix takes the form:
 * 
 * <pre>
 * | m00 m01 |
 * | m10 m11 |</pre>
 * 
 * <p>These entries are stored in an array of the form <tt>{m00, m01, m10,
 * m11}</tt>.
 */
public class Matrix2 {
	
	/** The top-left entry - use this to reference entry array indices. */
	public static final int M00 = 0;
	/** The top-right entry. */
	public static final int M01 = 1;
	/** The bottom-left entry. */
	public static final int M10 = 2;
	/** The bottom-right entry. */
	public static final int M11 = 3;
	
	
	/** The matrix's entries. */
	public float[] val;
	
	
	/**
	 * Creates an identity matrix.
	 */
	public Matrix2() {
		val = new float[] {
			1f, 0f,
			0f, 1f
		};
	}
	
	/**
	 * Creates a matrix with the specified entries.
	 */
	public Matrix2(float m00, float m01, float m10, float m11) {
		val = new float[] {
			m00, m01,
			m10, m11
		};
	}
	
	/**
	 * Sets the entries of this matrix.
	 * 
	 * @return This matrix, for chaining operations.
	 */
	public Matrix2 set(float m00, float m01, float m10, float m11) {
		val[M00] = m00;
		val[M01] = m01;
		val[M10] = m10;
		val[M11] = m11;
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
		System.arraycopy(m.val, 0, val, 0, val.length);
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
	 * Sets this matrix to a rotation matrix, which will rotate a vector
	 * anticlockwise about (0,0).
	 * 
	 * @param rad The angle, in radians.
	 * 
	 * @return This matrix, for chaining operations.
	 */
	public Matrix2 setToRotation(float rad) {
		float cos = (float)Math.cos(rad);
		float sin = (float)Math.sin(rad);
		return set(cos, -sin, sin, cos);
	}
	
	/**
	 * @return The determinant of this matrix.
	 */
	/*
	public float det() {
		return val[M00]*val[M11] - val[M01]*val[M10];
	}
	*/
	
	/**
	 * Transforms the specified vector (V) by this matrix (M) and returns the
	 * resulting vector. The supplied vector will not be modified.
	 * 
	 * @param vec The vector to multiply.
	 * 
	 * @return The resulting vector.
	 * @throws NullPointerException if either {@code vec} or {@code dest} are
	 * {@code null}.
	 */
	public Vector2 transform(Vector2 vec) {
		return new Vector2(
				val[M00]*vec.x + val[M01]*vec.y,
				val[M10]*vec.x + val[M11]*vec.y
		);
	}
	
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
	public Vector2 transform(Vector2 vec, Vector2 dest) {
		float x = val[M00]*vec.x + val[M01]*vec.y;
		float y = val[M10]*vec.x + val[M11]*vec.y;
		return dest.set(x, y);
	}
	
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
		float m00 = left.val[M00]*right.val[M00] + left.val[M01]*right.val[M10];
		float m01 = left.val[M00]*right.val[M01] + left.val[M01]*right.val[M11];
		float m10 = left.val[M10]*right.val[M00] + left.val[M11]*right.val[M10];
		float m11 = left.val[M10]*right.val[M01] + left.val[M11]*right.val[M11];
		
		return dest.set(m00, m01, m10, m11);
	}
	
}
