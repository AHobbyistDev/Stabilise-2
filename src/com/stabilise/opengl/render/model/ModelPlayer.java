package com.stabilise.opengl.render.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.stabilise.core.Resources;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.EntityPerson;
import com.stabilise.util.maths.Point;

/**
 * The player model.
 * 
 * <p>The player model, for now at least, simply consists of a series of static
 * sprites.
 */
public class ModelPlayer extends Model {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of cols in the model's spritesheet. */
	private static final int NUM_COLS = 8;
	/** The number of rows in the model's spritesheet. */
	private static final int NUM_ROWS = 8;
	
	/** The dimensions of the model, in tile-lengths. */
	public static final Point DIMENSIONS = new Point(1, 2);
	
	/** The template dimensions for each sprite. */
	private static final Point TEMPLATE_DIMENSIONS = new Point(256,256);
	/** The number of pixels per tile, using the template dimensions. */
	@SuppressWarnings("unused")
	private static final int PIXELS_PER_TILE = 55;
	/** The origin point for each sprite. */
	private static final Point SPRITE_ORIGIN = new Point(125,56);		//58,23
	
	private static final Point SPRITE_IDLE = new Point(0, 0);
	private static final Point SPRITE_RUN_1 = new Point(1, 0);
	private static final Point SPRITE_RUN_2 = new Point(2, 0);
	private static final Point SPRITE_RUN_3 = new Point(3, 0);
	private static final Point SPRITE_RUN_4 = new Point(4, 0);
	private static final Point SPRITE_CROUCH = new Point(5, 0);
	private static final Point SPRITE_JUMP = new Point(6, 0);
	private static final Point SPRITE_FALL = new Point(7, 0);
	/*
	private static final Point SPRITE_BLOCK = new Point(0, 1);
	private static final Point SPRITE_AIRDODGE = new Point(1, 1);
	private static final Point SPRITE_HITSTUN = new Point(2, 1);
	private static final Point SPRITE_AIRHITSTUN = new Point(3, 1);
	private static final Point SPRITE_SIDESTEP = new Point(4, 1);
	*/
	private static final Point SPRITE_SLIDE_FORWARD = new Point(5, 1);
	private static final Point SPRITE_SLIDE_BACKWARD = new Point(6, 1);
	private static final Point SPRITE_ATTACK_GROUND_SIDE_1 = new Point(7, 1);
	private static final Point SPRITE_ATTACK_GROUND_SIDE_2 = new Point(0, 2);
	private static final Point SPRITE_ATTACK_GROUND_SIDE_3 = new Point(1, 2);
	private static final Point SPRITE_ATTACK_GROUND_UP_1 = new Point(2, 2);
	private static final Point SPRITE_ATTACK_GROUND_UP_2 = new Point(3, 2);
	private static final Point SPRITE_ATTACK_GROUND_DOWN_1 = new Point(4, 2);
	private static final Point SPRITE_SPECIAL_GROUND_SIDE_1 = new Point(5, 2);
	private static final Point SPRITE_SPECIAL_GROUND_SIDE_2 = new Point(6, 2);
	private static final Point SPRITE_SPECIAL_GROUND_UP_1 = new Point(7, 2);
	private static final Point SPRITE_SPECIAL_GROUND_UP_2 = new Point(0, 3);
	private static final Point SPRITE_SPECIAL_GROUND_DOWN_1 = new Point(1, 3);
	private static final Point SPRITE_SPECIAL_GROUND_DOWN_2 = new Point(2, 3);
	private static final Point SPRITE_ATTACK_AIR_SIDE_1 = new Point(3, 3);
	private static final Point SPRITE_ATTACK_AIR_SIDE_2 = new Point(4, 3);
	private static final Point SPRITE_ATTACK_AIR_SIDE_3 = new Point(5, 3);
	private static final Point SPRITE_ATTACK_AIR_UP_1 = new Point(6, 3);
	private static final Point SPRITE_ATTACK_AIR_UP_2 = new Point(7, 3);
	private static final Point SPRITE_ATTACK_AIR_UP_3 = new Point(0, 4);
	private static final Point SPRITE_ATTACK_AIR_DOWN_1 = new Point(1, 4);
	private static final Point SPRITE_ATTACK_AIR_DOWN_2 = new Point(2, 4);
	private static final Point SPRITE_ATTACK_AIR_DOWN_3 = new Point(3, 4);
	private static final Point SPRITE_SPECIAL_AIR_SIDE_1 = new Point(4, 4);
	private static final Point SPRITE_SPECIAL_AIR_SIDE_2 = new Point(5, 4);
	private static final Point SPRITE_SPECIAL_AIR_UP_1 = new Point(6, 4);
	private static final Point SPRITE_SPECIAL_AIR_UP_2 = new Point(7, 4);
	private static final Point SPRITE_SPECIAL_AIR_DOWN_1 = new Point(0, 5);
	private static final Point SPRITE_SPECIAL_AIR_DOWN_2 = new Point(1, 5);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The player model's spritesheet. */
	private final Texture texture;
	/** Current cell being displayed. */
	private final TextureRegion cell;
	
	private final int cellWidth, cellHeight;
	
	/** The amount by which to offset each draw of a sprite */
	private final Point offset;
	/** The effective transformational origin/pivot. */
	private final Point pivot;
	
	/** Whether or not the model is flipped. */
	private boolean flipped = false;
	
	
	/**
	 * Creates a new player model.
	 */
	public ModelPlayer() {
		texture = Resources.texture("sheets/player");
		cell = new TextureRegion(texture);
		
		cellWidth = texture.getWidth() / NUM_COLS;
		cellHeight = texture.getHeight() / NUM_ROWS;
		
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		float scale = texture.getWidth() / TEMPLATE_DIMENSIONS.x;
		offset = new Point(
				(int)(SPRITE_ORIGIN.x * scale),
				(int)(SPRITE_ORIGIN.x * scale)
		);
		pivot = new Point(offset);
	}
	
	@Override
	public void rescale(float height) {
		//----sprites.setScale(height / PIXELS_PER_TILE);
	}
	
	/**
	 * Sets the horizontal flip of the model.
	 * 
	 * @param flipped Whether or not the model should be flipped.
	 */
	public void setFlipped(boolean flipped) {
		if(this.flipped == flipped)
			return;
		
		this.flipped = flipped;
		
		if(flipped)
			pivot.set(TEMPLATE_DIMENSIONS.x - offset.x, offset.y);
		else
			pivot.set(offset);
	}
	
	/**
	 * Sets the state of the player model.
	 * 
	 * @param state The state of the player.
	 * @param stateDuration The duration for which the player has remained in
	 * the state.
	 */
	public void setState(EntityMob.State state, int stateDuration) {
		switch(state) {
			case IDLE:
				setSprite(SPRITE_IDLE);
				break;
			case RUN:
				stateDuration %= 50;
				if(stateDuration < 8)
					setSprite(SPRITE_RUN_1);
				else if(stateDuration < 25)
					setSprite(SPRITE_RUN_2);
				else if(stateDuration < 33)
					setSprite(SPRITE_RUN_3);
				else
					setSprite(SPRITE_RUN_4);
				break;
			case SLIDE_FORWARD:
				setSprite(SPRITE_SLIDE_FORWARD);
				break;
			case SLIDE_BACK:
				setSprite(SPRITE_SLIDE_BACKWARD);
				break;
			case CROUCH:
			case JUMP_CROUCH:
			case LAND_CROUCH:
				setSprite(SPRITE_CROUCH);
				break;
			case JUMP:
				setSprite(SPRITE_JUMP);
				break;
			case FALL:
				setSprite(SPRITE_FALL);
				break;
			case BLOCK:
				
				break;
			case DODGE_AIR:
				
				break;
			case HITSTUN_GROUND:
				
				break;
			case HITSTUN_AIR:
				
				break;
			case ATTACK_SIDE_GROUND:
				if(stateDuration < EntityPerson.ATTACK_SIDE_GROUND_FRAME_3_BEGIN) {
					if(stateDuration < EntityPerson.ATTACK_SIDE_GROUND_FRAME_2_BEGIN)
						setSprite(SPRITE_ATTACK_GROUND_SIDE_1);
					else
						setSprite(SPRITE_ATTACK_GROUND_SIDE_2);
				} else {
					setSprite(SPRITE_ATTACK_GROUND_SIDE_3);
				}
				break;
			case ATTACK_UP_GROUND:
				if(stateDuration < EntityPerson.ATTACK_UP_GROUND_FRAME_2_BEGIN)
					setSprite(SPRITE_ATTACK_GROUND_UP_1);
				else
					setSprite(SPRITE_ATTACK_GROUND_UP_2);
				break;
			case ATTACK_DOWN_GROUND:
				setSprite(SPRITE_ATTACK_GROUND_DOWN_1);
				break;
			case ATTACK_SIDE_AIR:
				if(stateDuration < EntityPerson.ATTACK_SIDE_AIR_FRAME_3_BEGIN) {
					if(stateDuration < EntityPerson.ATTACK_SIDE_AIR_FRAME_2_BEGIN)
						setSprite(SPRITE_ATTACK_AIR_SIDE_1);
					else
						setSprite(SPRITE_ATTACK_AIR_SIDE_2);
				} else
					setSprite(SPRITE_ATTACK_AIR_SIDE_3);
				break;
			case ATTACK_UP_AIR:
				if(stateDuration < EntityPerson.ATTACK_UP_AIR_FRAME_3_BEGIN) {
					if(stateDuration < EntityPerson.ATTACK_UP_AIR_FRAME_2_BEGIN)
						setSprite(SPRITE_ATTACK_AIR_UP_1);
					else
						setSprite(SPRITE_ATTACK_AIR_UP_2);
				} else
					setSprite(SPRITE_ATTACK_AIR_UP_3);
				break;
			case ATTACK_DOWN_AIR:
				if(stateDuration < EntityPerson.ATTACK_DOWN_AIR_FRAME_3_BEGIN) {
					if(stateDuration < EntityPerson.ATTACK_DOWN_AIR_FRAME_2_BEGIN)
						setSprite(SPRITE_ATTACK_AIR_DOWN_1);
					else
						setSprite(SPRITE_ATTACK_AIR_DOWN_2);
				} else
					setSprite(SPRITE_ATTACK_AIR_DOWN_3);
				break;
			case SPECIAL_SIDE_GROUND:
				if(stateDuration < EntityPerson.SPECIAL_SIDE_GROUND_FRAME_2_BEGIN)
					setSprite(SPRITE_SPECIAL_GROUND_SIDE_1);
				else
					setSprite(SPRITE_SPECIAL_GROUND_SIDE_2);
				break;
			case SPECIAL_UP_GROUND:
				if(stateDuration < EntityPerson.SPECIAL_UP_GROUND_FRAME_2_BEGIN)
					setSprite(SPRITE_SPECIAL_GROUND_UP_1);
				else
					setSprite(SPRITE_SPECIAL_GROUND_UP_2);
				break;
			case SPECIAL_DOWN_GROUND:
				if(stateDuration < EntityPerson.SPECIAL_DOWN_GROUND_FRAME_2_BEGIN)
					setSprite(SPRITE_SPECIAL_GROUND_DOWN_1);
				else
					setSprite(SPRITE_SPECIAL_GROUND_DOWN_2);
				break;
			case SPECIAL_SIDE_AIR:
				if(stateDuration < EntityPerson.SPECIAL_SIDE_AIR_FRAME_2_BEGIN)
					setSprite(SPRITE_SPECIAL_AIR_SIDE_1);
				else
					setSprite(SPRITE_SPECIAL_AIR_SIDE_2);
				break;
			case SPECIAL_UP_AIR:
				if(stateDuration < EntityPerson.SPECIAL_UP_AIR_FRAME_2_BEGIN)
					setSprite(SPRITE_SPECIAL_AIR_UP_1);
				else
					setSprite(SPRITE_SPECIAL_AIR_UP_2);
				break;
			case SPECIAL_DOWN_AIR:
				if(stateDuration < EntityPerson.SPECIAL_DOWN_AIR_FRAME_2_BEGIN)
					setSprite(SPRITE_SPECIAL_AIR_DOWN_1);
				else
					setSprite(SPRITE_SPECIAL_AIR_DOWN_2);
				break;
			default:
				// to please the compiler warnings
				break;
		}
	}
	
	/**
	 * Sets the model's sprite.
	 * 
	 * @param p The sprite's position within the spritesheet.
	 */
	private void setSprite(Point p) {
		int x = p.x * cellWidth;
		int y = p.y * cellHeight;
		cell.setRegion(x, y, x + cellWidth, y + cellHeight);
	}
	
	@Override
	public void render(SpriteBatch batch, int x, int y) {
		if(flipped)
			batch.draw(cell, x + offset.x, y + offset.y, -cellWidth, cellHeight);
		else
			batch.draw(cell, x + offset.x, y + offset.y, cellWidth, cellHeight);
	}
	
	@Override
	public void dispose() {
		texture.dispose();
	}
	
}
