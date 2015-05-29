//package com.stabilise.screen.menu.customcomponents;
//
//import org.lwjgl.util.Point;
//
//import com.stabilise.opengl.Font;
//import com.stabilise.opengl.FontStyle;
//import com.stabilise.opengl.SpriteBatch;
//import com.stabilise.screen.menu.Menu;
//import com.stabilise.screen.menu.component.Button;
//import com.stabilise.util.Colour;
//import com.stabilise.util.shape.AxisAlignedBoundingBox;
//import com.stabilise.world.WorldInfo;
//
///**
// * A WorldListButton is a button that appears in the list of worlds.
// */
//public class WorldListButton extends Button {
//	
//	//--------------------==========--------------------
//	//-----=====Static Constants and Variables=====-----
//	//--------------------==========--------------------
//	
//	/** The select action. */
//	public static final int ACTION_SELECT = 16367369;		// totally random value
//	
//	/** A world list button's bounding box template. */
//	private static final AxisAlignedBoundingBox BOUNDING_BOX = new AxisAlignedBoundingBox(0, 0, 56, 11);
//	
//	/** The button's padding in unscaled pixels, for graphical purposes. */
//	private static final int PADDING = 2;
//	
//	/** A world list button's template font for the world name. */
//	private static final FontStyle STYLE_WORLD_NAME = new FontStyle(2, Colour.WHITE, FontStyle.Alignment.LEFT, 1, 0);
//	
//	/** The text to display when a world hasn't been created. */
//	private static final String DEFAULT_TEXT = "Create new world";
//	
//	//--------------------==========--------------------
//	//------------=====Member Variables=====------------
//	//--------------------==========--------------------
//	
//	/** Whether or not the world has been created. */
//	private boolean created;
//	/** Whether or not the world is selected. */
//	public boolean selected = false;
//	/** The button's scale. */
//	private float scale;
//	
//	/** The font. */
//	private Font font;
//	/** The font style. */
//	private FontStyle fontStyle;
//	/** The x-coordinate at which to place the text. */
//	private int textX;
//	/** The y-coordinate at which to place the text. */
//	private int textY;
//	/** The allowable text width. */
//	private int textWidth;
//	/** The text to display. */
//	private String text;
//	/** The number of characters that can fit. */
//	private int numCharacters;
//	
//	/** The SpriteBatch source for the button's texture. */
//	private SpriteBatch sprite;
//	/** The base dimensions of the button. */
//	private MutablePoint baseDimensions;
//	/** The dimensions of the button. */
//	private MutablePoint dimensions = new MutablePoint();
//	/** The base location of the sprite. */
//	private MutablePoint baseSpriteLocation;
//	/** The location of the sprite. */
//	private MutablePoint spriteLocation = new MutablePoint();
//	/** The base dimensions of the sprite. */
//	private MutablePoint baseSpriteDimensions;
//	/** The dimensions of the sprite. */
//	private MutablePoint spriteDimensions = new MutablePoint();
//	/** The texture coordinates for the normal created world slot. */
//	private float[] filledNormTC;
//	/** The texture coordinates for the hovered-over created world slot. */
//	private float[] filledHoverTC;
//	/** The texture coordinates for the selected creates world slot. */
//	private float[] filledSelectedTC;
//	/** The texture coordinates for the normal empty world slot. */
//	private float[] emptyNormTC;
//	/** The texture coordinates for the hovered-over empty world slot. */
//	private float[] emptyHoverTC;
//	/** The texture coordinates for the selected empty world slot. */
//	private float[] emptySelectedTC;
//	
//	
//	/**
//	 * Creates a new WorldListButton.
//	 * 
//	 * @param menu The button's parent menu.
//	 * @param x The x-coordinate of the button, in pixels.
//	 * @param y The y-coordinate of the button, in pixels.
//	 * @param world The world info the button is to represent.
//	 * @param dimensions The button's dimensions.
//	 * @param sprite The SpriteBatch source for the button's texture.
//	 * @param spriteLocation The location of the sprite relative to the button.
//	 * @param spriteDimensions The sprite's dimensions.
//	 * @param scale The button's scale.
//	 * @param filledNormTC The texture coordinates for the normal created world
//	 * slot.
//	 * @param filledHoverTC The texture coordinates for the hovered-over
//	 * created world slot.
//	 * @param filledSelectedTC The texture coordinates for the hovered-over
//	 * created world slot.
//	 * @param emptyNormTC The texture coordinates for the normal empty world
//	 * slot.
//	 * @param emptyHoverTC The texture coordinates for the hovered-over empty
//	 * world slot.
//	 * @param emptySelectedTC The texture coordinates for the selected empty
//	 * world slot.
//	 */
//	public WorldListButton(Menu menu, int x, int y, WorldInfo world,
//			MutablePoint dimensions, SpriteBatch sprite, MutablePoint spriteLocation, MutablePoint spriteDimensions, float scale,
//			float[] filledNormTC, float[] filledHoverTC, float[] filledSelectedTC,
//			float[] emptyNormTC, float[] emptyHoverTC, float[] emptySelectedTC) {
//		super(menu, x, y, ACTION_SELECT, BOUNDING_BOX);
//		
//		//this.world = world;
//		//created = world != null;
//		setWorld(world);
//		
//		this.sprite = sprite;
//		this.baseDimensions = dimensions;
//		this.baseSpriteLocation = spriteLocation;
//		this.baseSpriteDimensions = spriteDimensions;
//		this.filledNormTC = filledNormTC;
//		this.filledHoverTC = filledHoverTC;
//		this.filledSelectedTC = filledSelectedTC;
//		this.emptyNormTC = emptyNormTC;
//		this.emptyHoverTC = emptyHoverTC;
//		this.emptySelectedTC = emptySelectedTC;
//		
//		font = new Font("sheets/font1", this);
//		
//		setScale(scale);
//	}
//	
//	@Override
//	public void render() {
//		sprite.setScaledDimensions(spriteDimensions.getX(), spriteDimensions.getY());
//		if(selected) {
//			if(created)
//				sprite.drawSprite(filledSelectedTC, x + spriteLocation.getX(), y + spriteLocation.getY());
//			else
//				sprite.drawSprite(emptySelectedTC, x + spriteLocation.getX(), y + spriteLocation.getY());
//		} else {
//			switch(state) {
//				case OFF:
//				case PRESSED:
//					if(created)
//						sprite.drawSprite(filledNormTC, x + spriteLocation.getX(), y + spriteLocation.getY());
//					else
//						sprite.drawSprite(emptyNormTC, x + spriteLocation.getX(), y + spriteLocation.getY());
//					break;
//				case OVER:
//					if(created)
//						sprite.drawSprite(filledHoverTC, x + spriteLocation.getX(), y + spriteLocation.getY());
//					else
//						sprite.drawSprite(emptyHoverTC, x + spriteLocation.getX(), y + spriteLocation.getY());
//					break;
//			}
//		}
//		font.drawLine(text, x + textX, y + textY, fontStyle);
//	}
//	
//	/**
//	 * Sets the scale of the button.
//	 * 
//	 * @param scale The scale.
//	 */
//	public void setScale(float scale) {
//		if(scale == this.scale)
//			return;
//		
//		this.scale = scale;
//		
//		boundingBox = new AxisAlignedBoundingBox(
//				BOUNDING_BOX.v00.x * scale,
//				BOUNDING_BOX.v00.y * scale,
//				BOUNDING_BOX.width * scale,
//				BOUNDING_BOX.height * scale
//		);
//		
//		fontStyle = new FontStyle(
//				(int) (STYLE_WORLD_NAME.size * scale),
//				STYLE_WORLD_NAME.colour,
//				STYLE_WORLD_NAME.alignment,
//				STYLE_WORLD_NAME.kerning,
//				STYLE_WORLD_NAME.verticalKerning
//		);
//		
//		dimensions.setLocation((int)(baseDimensions.getX() * scale), (int)(baseDimensions.getY() * scale));
//		spriteLocation.setLocation((int)(baseSpriteLocation.getX() * scale), (int)(baseSpriteLocation.getY() * scale));
//		spriteDimensions.setLocation((int)(baseSpriteDimensions.getX() * scale), (int)(baseSpriteDimensions.getY() * scale));
//		
//		textX = (int) ((PADDING + 1) * scale);
//		textY = (dimensions.getY() - fontStyle.size) / 2;
//		textWidth = (int) (boundingBox.width - (PADDING + 1) * 2 * scale);
//		numCharacters = font.getNumFittingCharacters(textWidth, fontStyle);
//		
//		//text = created ? world.name : DEFAULT_TEXT;
//		if(text.length() > numCharacters)
//			text = text.substring(0, numCharacters - 3) + "...";
//	}
//	
//	/**
//	 * Sets the button's associated world.
//	 * 
//	 * @param world The world.
//	 */
//	public void setWorld(WorldInfo world) {
//		//this.world = world;
//		created = world != null;
//		text = created ? world.name : DEFAULT_TEXT;
//	}
//	
//	@Override
//	public void destroy() {
//		font.destroy();
//	}
//	
//}
