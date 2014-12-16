package com.stabilise.entity.model;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.shape.Shape;

/**
 * todo
 */
public class ChildBone extends Bone {
	
	/** This bone's parent bone. */
	private final Bone parent;
	/** This bone's position relative to its unrotated parent. */
	private final Vector2 relativePos;
	
	
	public ChildBone(Shape hitbox, Bone parent, Vector2 relativePos) {
		super(hitbox);
		this.parent = parent;
		this.relativePos = relativePos;
	}
	
	@Override
	void flush() {
		parent.rotationMatrix.transform(relativePos, position.pos);
		position.pos.x += parent.position.pos.x;
		position.pos.y += parent.position.pos.y;
	}
	
}
