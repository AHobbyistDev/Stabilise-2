package com.stabilise.util;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * This class provides utility matrix methods.
 */
public class MatrixUtil {
	
	// non-instantiable
	private MatrixUtil() {}
	
	/**
	 * Gets the 2x2 rotation matrix corresponding to the specified rotation.
	 * The returned matrix takes the form:
	 * 
	 * <pre>
	 * | cosθ  -sinθ |
	 * | sinθ  cosθ  |</pre>
	 * 
	 * where θ is specified by the {@code theta} parameter.
	 * 
	 * @param theta The rotation in the anticlockwise direction, in radians.
	 * 
	 * @return The rotation matrix.
	 */
	public static Matrix2f rotationMatrix2f(float theta) {
		Matrix2f m = new Matrix2f();
		m.m00 = (float)Math.cos(theta);
		m.m01 = (float)Math.sin(theta);
		m.m10 = -m.m01;
		m.m11 = m.m00;
		return m;
	}
	
	/**
	 * Sets the specified 2x2 matrix to the rotation matrix corresponding to
	 * the specified rotation. The rotation matrix takes the form:
	 * 
	 * <pre>
	 * | cosθ  -sinθ |
	 * | sinθ  cosθ  |</pre>
	 * 
	 * where θ is specified by the {@code theta} parameter.
	 * 
	 * @param theta The rotation in the anticlockwise direction, in radians.
	 * @param m The matrix to set to the rotation matrix.
	 */
	public static void rotationMatrix2f(float theta, Matrix2f m) {
		m.m00 = (float)Math.cos(theta);
		m.m01 = (float)Math.sin(theta);
		m.m10 = -m.m01;
		m.m11 = m.m00;
	}
	
	/**
	 * Gets the 3x3 rotation matrix corresponding to the specified rotation.
	 * The returned matrix takes the form:
	 * 
	 * <pre>
	 * | cosθ  -sinθ  0 |
	 * | sinθ  cosθ   0 |
	 * |  0      0    1 |</pre>
	 * 
	 * where θ is specified by the {@code theta} parameter.
	 * 
	 * @param theta The rotation in the anticlockwise direction, in radians.
	 * 
	 * @return The rotation matrix.
	 */
	public static Matrix3f rotationMatrix3f(float theta) {
		Matrix3f m = new Matrix3f(); // initialised to identity
		m.m00 = (float)Math.cos(theta);
		m.m01 = (float)Math.sin(theta);
		m.m10 = -m.m01;
		m.m11 = m.m00;
		return m;
	}
	
	/**
	 * Gets the 3x3 rotation and translation matrix corresponding to the
	 * specified rotation and translation. The returned matrix takes the form:
	 * 
	 * <pre>
	 * | cosθ  -sinθ  dx |
	 * | sinθ  cosθ   dy |
	 * |  0      0    1  |</pre>
	 * 
	 * where θ is specified by the {@code theta} parameter.
	 * 
	 * @param theta The rotation in the anticlockwise direction, in radians.
	 * @param dx The translation along the x-axis.
	 * @param dy The translation along the y-axis.
	 * 
	 * @return The rotation matrix.
	 */
	public static Matrix3f rotationTranslationMatrix3f(float theta, float dx, float dy) {
		Matrix3f m = rotationMatrix3f(theta);
		m.m20 = dx;
		m.m21 = dy;
		return m;
	}
	
	/**
	 * Expands the specified 2D vector to the third dimension, to allow for
	 * more advanced matrix transformations. After being transformed, the
	 * matrix is usually {@link #collapse collapsed} back into a 2D vector, or
	 * otherwise treated as one.
	 * 
	 * @param vec The vector.
	 * 
	 * @return The expanded vector.
	 */
	public static Vector3f expand(Vector2f vec) {
		return new Vector3f(vec.x, vec.y, 1);
	}
	
	/**
	 * Collapses the specified 3D vector into a 2D vector.
	 * 
	 * @param vec The vector.
	 * 
	 * @return The collapsed vector.
	 */
	public static Vector2f collapse(Vector2f vec) {
		return new Vector2f(vec.x, vec.y);
	}
	
}
