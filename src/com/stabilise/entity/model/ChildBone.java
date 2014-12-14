package com.stabilise.entity.model;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.shape.Shape;

/**
 * todo
 */
public class ChildBone extends Bone {
	
	/** This bone's parent bone. */
	private final Bone parent;
	/** This bone's position relative to its unrotated parent. */
	private final Vector2f relativePos;
	
	
	public ChildBone(Shape hitbox, Bone parent, Vector2f relativePos) {
		super(hitbox);
		this.parent = parent;
		this.relativePos = relativePos;
	}
	
	@Override
	void flush() {
		Matrix2f.transform(parent.rotationMatrix, relativePos, position.pos);
		position.pos.x += parent.position.pos.x;
		position.pos.y += parent.position.pos.y;
	}
	
}
