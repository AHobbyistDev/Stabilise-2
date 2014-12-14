package com.stabilise.tests;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.core.Application;
import com.stabilise.core.state.State;
import com.stabilise.opengl.Sprite;
import com.stabilise.opengl.Texture;
import com.stabilise.util.MatrixUtil;
import com.stabilise.util.shape.DimensionedRectangle;
import com.stabilise.util.shape.RotatableShape;

/**
 * A test of creating models with joints and bones.
 */
public class BoneModelTest extends Application implements State {
	
	public static void main(String[] args) {
		new BoneModelTest();
	}
	
	// -------------------------------------
	
	private Sprite sprite;
	private Bone model;
	private ChildBone armLeft;
	
	public BoneModelTest() {
		super(60);
	}
	
	@Override
	protected State getInitialState() {
		return this;
	}
	
	@Override
	public void start() {
		sprite = new Sprite("shadedSquare");
		sprite.filter(Texture.NEAREST);
		sprite.x = 100;
		sprite.y = 100;
		sprite.setScaledDimensions(32, 32);
		
		model = new Bone(new DimensionedRectangle(-15, -15, 30, 100), new Vector2f(200, 200));
		armLeft = new ChildBone(new DimensionedRectangle(-5, -5, 10, 50));
		model.addChild(armLeft, new Vector2f(-15, 85));
		//armLeft.hitbox.setRotation(2.7f);
		
		model.refresh();
	}
	
	@Override
	public void dispose() {
		
	}
	
	@Override
	public void pause() {
		
	}
	
	@Override
	public void resume() {
		
	}
	
	@Override
	public void update() {
		model.hitbox.rotate(0.01f);
		armLeft.hitbox.rotate(0.02f);
		
		model.refresh();
	}
	
	@Override
	public void render(float delta) {
		model.render();
	}
	
	class Bone {
		
		/** The bone's coords */
		final Vector2f coords;
		/** Bones connected to this model. */
		final List<ChildBone> children = new LinkedList<ChildBone>();
		/** The bone's hitbox. */
		final RotatableShape<DimensionedRectangle> hitbox;
		
		/**
		 * Creates a new bone at (0,0)
		 * @param hitbox The bone's hitbox.
		 */
		Bone(DimensionedRectangle hitbox) {
			this(hitbox, new Vector2f(0,0));
		}
		
		/**
		 * Creates a new bone.
		 * @param hitbox The bone's hitbox.
		 * @param coords The bone's coords.
		 */
		Bone(DimensionedRectangle hitbox, Vector2f coords) {
			this.hitbox = new RotatableShape<DimensionedRectangle>(hitbox);
			this.coords = coords;
		}
		
		/**
		 * Adds a bone as a child of this one
		 * @param bone the child bone
		 * @param relativeCoords the coords of the bone relative to this one,
		 * when this one is not rotated.
		 */
		void addChild(ChildBone bone, Vector2f relativeCoords) {
			bone.relativeCoords = relativeCoords;
			children.add(bone);
		}
		
		void refresh() {
			Matrix2f rotationMatrix = MatrixUtil.rotationMatrix2f(hitbox.getRotation());
			for(ChildBone bone : children) {
				Matrix2f.transform(rotationMatrix, bone.relativeCoords, bone.coords);
				bone.coords.x += coords.x;
				bone.coords.y += coords.y;
				bone.refresh();
			}
		}
		
		void render() {
			DimensionedRectangle r = hitbox.get();
			sprite.setScaledDimensions((int)r.width, (int)r.height);
			sprite.setPivot((int)(r.v00.x / r.width), (int)(r.v00.y / r.height));
			sprite.rotation = (float)Math.toDegrees(hitbox.getRotation());
			sprite.drawSprite((int)(coords.x + r.v00.x), (int)(coords.y + r.v00.y));
			for(ChildBone bone : children)
				bone.render();
		}
		
	}
	
	class ChildBone extends Bone {
		
		/** Coords relative to its parent. */
		Vector2f relativeCoords;
		
		ChildBone(DimensionedRectangle hitbox) {
			super(hitbox);
		}
		
		@Override
		void refresh() {
			
		}
		
	}
	
	@Override
	public void resize(int width, int height) {
		
	}
	
}
