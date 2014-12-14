package com.stabilise.entity.model;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.MatrixUtil;
import com.stabilise.util.shape.Shape;

/**
 * todo
 */
public class Bone {
	
	/** The position of the bone. */
	protected final Position position;
	
	// Emulating the function of a RotatableShape, but maintaining the rotation
	// matrix for optimisation purposes.
	/** The bone's base hitbox.*/
	protected final Shape baseHitbox;
	/** The bone's effective hitbox. */
	protected Shape rotatedHitbox;
	/** The rotation matrix corresponding to the bone's rotation. */
	protected final Matrix2f rotationMatrix;
	
	
	/**
	 * Creates a new Bone.
	 * 
	 * @param hitbox
	 */
	public Bone(Shape hitbox) {
		baseHitbox = rotatedHitbox = hitbox;
		rotationMatrix = new Matrix2f(); // identity matrix
		
		position = new Position();
	}
	
	/**
	 * Gets this bone's hitbox.
	 * 
	 * @return The hitbox.
	 */
	public Shape getHitbox() {
		return rotatedHitbox;
	}
	
	/**
	 * Sets the bone's rotation.
	 * 
	 * @param rotation The angle by which to rotate the bone anticlockwise
	 * from its originally-defined position, in radians.
	 */
	public void setRotation(float rotation) {
		MatrixUtil.rotationMatrix2f(rotation, rotationMatrix);
		rotatedHitbox = baseHitbox.transform(rotationMatrix);
	}
	
	/**
	 * Flushes positional changes to this bone. If this is a child bone, it
	 * will be positioned appropriately relative to its parent.
	 */
	void flush() {
		// nothing to see here, move along
	}
	
	static class Position {
		
		/** The position itself. */
		Vector2f pos;
		/** The rotation, in radians. */
		float rotation;
		
		
		Position() {
			pos = new Vector2f();
			rotation = 0f;
		}
		
	}
	
}
