package com.stabilise.opengl.render.model;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.stabilise.core.Resources;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.EntityPerson;
import com.stabilise.opengl.TextureSheet;
import com.stabilise.util.maths.MutablePoint;

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
	
	/** The number of cols and rows in the model's spritesheet. */
	private static final int NUM_COLS = 8, NUM_ROWS = 8;
	
	/** The dimensions of the model, in tile-lengths. */
	public static final MutablePoint DIMENSIONS = new MutablePoint(1, 2);
	
	/** The template dimensions for each sprite. */
	private static final MutablePoint TEMPLATE_DIMENSIONS = new MutablePoint(256,256);
	
	/** The number of pixels per tile, using the template dimensions. */
	private static final int PIXELS_PER_TILE = 55;
	
	/** Cell dimensions, in tile-lengths. */
	private static final Vector2 CELL_DIMENSIONS_TILES = new Vector2(
			TEMPLATE_DIMENSIONS.x / PIXELS_PER_TILE,
			TEMPLATE_DIMENSIONS.y / PIXELS_PER_TILE
	);
	
	/** The origin point for each sprite. */
	private static final Vector2 ORIGIN = new Vector2( //58,23
			105f / PIXELS_PER_TILE, // 125
			49f / PIXELS_PER_TILE // 56
	);		
	
	private static final MutablePoint
		SPRITE_IDLE = new MutablePoint(0, 0),
		SPRITE_RUN_1 = new MutablePoint(1, 0),
		SPRITE_RUN_2 = new MutablePoint(2, 0),
		SPRITE_RUN_3 = new MutablePoint(3, 0),
		SPRITE_RUN_4 = new MutablePoint(4, 0),
		SPRITE_CROUCH = new MutablePoint(5, 0),
		SPRITE_JUMP = new MutablePoint(6, 0),
		SPRITE_FALL = new MutablePoint(7, 0),
	/*
		SPRITE_BLOCK = new MutablePoint(0, 1),
		SPRITE_AIRDODGE = new MutablePoint(1, 1),
		SPRITE_HITSTUN = new MutablePoint(2, 1),
		SPRITE_AIRHITSTUN = new MutablePoint(3, 1),
		SPRITE_SIDESTEP = new MutablePoint(4, 1),
	*/
		SPRITE_SLIDE_FORWARD = new MutablePoint(5, 1),
		SPRITE_SLIDE_BACKWARD = new MutablePoint(6, 1),
		SPRITE_ATTACK_GROUND_SIDE_1 = new MutablePoint(7, 1),
		SPRITE_ATTACK_GROUND_SIDE_2 = new MutablePoint(0, 2),
		SPRITE_ATTACK_GROUND_SIDE_3 = new MutablePoint(1, 2),
		SPRITE_ATTACK_GROUND_UP_1 = new MutablePoint(2, 2),
		SPRITE_ATTACK_GROUND_UP_2 = new MutablePoint(3, 2),
		SPRITE_ATTACK_GROUND_DOWN_1 = new MutablePoint(4, 2),
		SPRITE_SPECIAL_GROUND_SIDE_1 = new MutablePoint(5, 2),
		SPRITE_SPECIAL_GROUND_SIDE_2 = new MutablePoint(6, 2),
		SPRITE_SPECIAL_GROUND_UP_1 = new MutablePoint(7, 2),
		SPRITE_SPECIAL_GROUND_UP_2 = new MutablePoint(0, 3),
		SPRITE_SPECIAL_GROUND_DOWN_1 = new MutablePoint(1, 3),
		SPRITE_SPECIAL_GROUND_DOWN_2 = new MutablePoint(2, 3),
		SPRITE_ATTACK_AIR_SIDE_1 = new MutablePoint(3, 3),
		SPRITE_ATTACK_AIR_SIDE_2 = new MutablePoint(4, 3),
		SPRITE_ATTACK_AIR_SIDE_3 = new MutablePoint(5, 3),
		SPRITE_ATTACK_AIR_UP_1 = new MutablePoint(6, 3),
		SPRITE_ATTACK_AIR_UP_2 = new MutablePoint(7, 3),
		SPRITE_ATTACK_AIR_UP_3 = new MutablePoint(0, 4),
		SPRITE_ATTACK_AIR_DOWN_1 = new MutablePoint(1, 4),
		SPRITE_ATTACK_AIR_DOWN_2 = new MutablePoint(2, 4),
		SPRITE_ATTACK_AIR_DOWN_3 = new MutablePoint(3, 4),
		SPRITE_SPECIAL_AIR_SIDE_1 = new MutablePoint(4, 4),
		SPRITE_SPECIAL_AIR_SIDE_2 = new MutablePoint(5, 4),
		SPRITE_SPECIAL_AIR_UP_1 = new MutablePoint(6, 4),
		SPRITE_SPECIAL_AIR_UP_2 = new MutablePoint(7, 4),
		SPRITE_SPECIAL_AIR_DOWN_1 = new MutablePoint(0, 5),
		SPRITE_SPECIAL_AIR_DOWN_2 = new MutablePoint(1, 5);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	private final TextureSheet texSheet;
	private TextureRegion cell;
	
	private boolean flipped = false;
	
	
	/**
	 * Creates a new player model.
	 */
	public ModelPlayer() {
		texSheet = new TextureSheet(Resources.texture("sheets/player"), NUM_COLS, NUM_ROWS);
		texSheet.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		cell = texSheet.getRegion(0, 0); // default cell
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
		
		/*
		if(flipped)
			pivot.set(TEMPLATE_DIMENSIONS.x - offset.x, offset.y);
		else
			pivot.set(offset);
		*/
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
	private void setSprite(MutablePoint p) {
		cell = texSheet.getRegion(p.x, p.y);
	}
	
	@Override
	public void render(SpriteBatch batch, float x, float y) {
		if(flipped)
			batch.draw(cell, x + ORIGIN.x, y - ORIGIN.y, -CELL_DIMENSIONS_TILES.x, CELL_DIMENSIONS_TILES.y);
		else
			batch.draw(cell, x - ORIGIN.x, y - ORIGIN.y, CELL_DIMENSIONS_TILES.x, CELL_DIMENSIONS_TILES.y);
	}
	
	@Override
	public void dispose() {
		texSheet.dispose();
	}
	
}
