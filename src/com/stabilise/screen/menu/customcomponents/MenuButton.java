//package com.stabilise.screen.menu.customcomponents;
//
//import org.lwjgl.util.Point;
//import org.lwjgl.util.vector.Vector2f;
//
//import com.stabilise.opengl.Font;
//import com.stabilise.opengl.FontStyle;
//import com.stabilise.opengl.SpriteBatch;
//import com.stabilise.opengl.Texture;
//import com.stabilise.screen.menu.Menu;
//import com.stabilise.screen.menu.component.Button;
//import com.stabilise.util.Colour;
//import com.stabilise.util.shape.AxisAlignedBoundingBox;
//
///**
// * A generic menu button.
// */
//public class MenuButton extends Button {
//    
//    //--------------------==========--------------------
//    //-----=====Static Constants and Variables=====-----
//    //--------------------==========--------------------
//    
//    /** A MenuButton's template font style for the normal/idle state. */
//    private static final FontStyle STYLE_NORMAL_TEMPLATE = new FontStyle(16, Colour.BLACK, FontStyle.Alignment.CENTRE, 2, 0);
//    /** A MenuButton's template font style for the over state. */
//    private static final FontStyle STYLE_OVER_TEMPLATE = new FontStyle(16, Colour.WHITE, FontStyle.Alignment.CENTRE, 2, 0);
//    /** A MenuButton's template font style for the pressed state. */
//    private static final FontStyle STYLE_PRESSED_TEMPLATE = new FontStyle(16, Colour.LIGHT_GREY, FontStyle.Alignment.CENTRE, 2, 0);
//    /** A MenuButton's template font style for the disabled state. */
//    private static final FontStyle STYLE_DISABLED_TEMPLATE = new FontStyle(16, Colour.cloneColourWithAlpha(Colour.BLACK, 0.5f), FontStyle.Alignment.CENTRE, 2, 0);
//    
//    
//    /** The texture's reference dimensions. */
//    private static final MutablePoint SPRITE_DIMENSIONS = new MutablePoint(64, 16);
//    
//    /** The left cap's reference texture coordinate's location. */
//    private static final MutablePoint TC_NORMAL_CAP_LEFT_LOCATION = new MutablePoint(0, 0);
//    /** The left cap's reference texture coordinate's dimensions. */
//    private static final MutablePoint TC_NORMAL_CAP_LEFT_DIMENSIONS = new MutablePoint(3, 12);
//    /** The right cap's reference texture coordinate's location. */
//    private static final MutablePoint TC_NORMAL_CAP_RIGHT_LOCATION = new MutablePoint(3, 0);
//    /** The right cap's reference texture coordinate's dimensions. */
//    private static final MutablePoint TC_NORMAL_CAP_RIGHT_DIMENSIONS = new MutablePoint(3, 12);
//    /** The fill's reference texture coordinate's location. */
//    private static final MutablePoint TC_NORMAL_FILL_LOCATION = new MutablePoint(6, 0);
//    /** The fill's reference texture coordinate's dimensions. */
//    private static final MutablePoint TC_NORMAL_FILL_DIMENSIONS = new MutablePoint(1, 12);
//    
//    /** The amount by which all texcoords must be translated from the normal
//     * state to the over state. */
//    private static final MutablePoint TC_OVER_TRANSLATION = new MutablePoint(7, 0);
//    /** The amount by which all texcoords must be translated from the normal
//     * state to the disabled state. */
//    private static final MutablePoint TC_DISABLED_TRANSLATION = new MutablePoint(14, 0);
//    
//    //--------------------==========--------------------
//    //------------=====Member Variables=====------------
//    //--------------------==========--------------------
//    
//    /** The button's background sprite. */
//    private SpriteBatch sprite;
//    /** The button's font. */
//    private Font font;
//    /** The button's display text. */
//    private String text;
//    /** The text size. */
//    private int fontSize;
//    
//    /** Whether or not the button is centred horizontally. */
//    private boolean centredX;
//    /** Whether or not the button is centred vertically. */
//    private boolean centredY;
//    /** The effective (0,0) for the button's sprites. */
//    private MutablePoint origin;
//    
//    /** Whether or not the button has been initially updated. */
//    private boolean updated = false;
//    /** The last state of <tt>enabled</tt>. */
//    private boolean lastEnabled = true;
//    /** The last state of <tt>state</tt>.*/
//    private State lastState = State.OFF;
//    
//    /** The scale of the sprite relative to the reference texture dimensions. */
//    private float spriteScale;
//    /** The scale of the button relative to its reference texture dimensions. */
//    private float scale;
//    /** The width of each cap. */
//    private int capWidth;
//    /** The width of the main portion of the button. */
//    private int fillWidth;
//    
//    /** The texture coordinates for the left cap. */
//    private float[] capLeftTC;
//    /** The texture coordinates for the right cap. */
//    private float[] capRightTC;
//    /** The texture coordinates for the fill. */
//    private float[] fillTC;
//    
//    /** The texture coordinates translation from the normal state to the over
//     * state. */
//    private Vector2f overTranslation = new Vector2f();
//    /** The texture coordinates translation from the normal state to the
//     * disabled state. */
//    private Vector2f disabledTranslation = new Vector2f();
//    
//    
//    /**
//     * Creates a new MenuButton.
//     * 
//     * @param menu The menu containing the MenuButton.
//     * @param x The x-coordinate of the MenuButton, in pixels.
//     * @param y The y-coordinate of the MenuButton, in pixels.
//     * @param width The width of the MenuButton, in pixels.
//     * @param height The height of the MenuButton, in pixels.
//     * @param action The identifier for the action the Button's parent menu is
//     * to perform when the Button is clicked/pressed. A value of -1 should be
//     * used to indicate that the Button has no functionality.
//     * @param text The MenuButton's display text.
//     * @param fontSize The font size, in pixels.
//     * @param centredX Whether or not the button should be centred
//     * horizontally.
//     * @param centredY Whether or not the button should be centred vertically.
//     */
//    public MenuButton(Menu menu, int x, int y, int width, int height, int action, String text, int fontSize, boolean centredX, boolean centredY) {
//        super(menu, x, y, action, null);
//        
//        this.text = text;
//        this.centredX = centredX;
//        this.centredY = centredY;
//        
//        sprite = new SpriteBatch("button", this);
//        sprite.filter(Texture.NEAREST);
//        spriteScale = (float)sprite.getTextureWidth() / SPRITE_DIMENSIONS.getX();
//        
//        capLeftTC = SpriteBatch.getTextureData(TC_NORMAL_CAP_LEFT_LOCATION, TC_NORMAL_CAP_LEFT_DIMENSIONS, SPRITE_DIMENSIONS);
//        capRightTC = SpriteBatch.getTextureData(TC_NORMAL_CAP_RIGHT_LOCATION, TC_NORMAL_CAP_RIGHT_DIMENSIONS, SPRITE_DIMENSIONS);
//        fillTC = SpriteBatch.getTextureData(TC_NORMAL_FILL_LOCATION, TC_NORMAL_FILL_DIMENSIONS, SPRITE_DIMENSIONS);
//        overTranslation = new Vector2f(TC_OVER_TRANSLATION.getX() * spriteScale / SPRITE_DIMENSIONS.getX(), TC_OVER_TRANSLATION.getY() * spriteScale / SPRITE_DIMENSIONS.getY());
//        disabledTranslation = new Vector2f(TC_DISABLED_TRANSLATION.getX() * spriteScale / SPRITE_DIMENSIONS.getX(), TC_DISABLED_TRANSLATION.getY() * spriteScale / SPRITE_DIMENSIONS.getY());
//        
//        font = new Font("sheets/font1", this);
//        
//        // Technically refreshText() will get called twice due to these but... meh
//        setSize(width, height);
//        setFontSize(fontSize);
//    }
//    
//    /**
//     * Sets the size of the MenuButton.
//     * 
//     * @param width The new width, in pixels.
//     * @param height The new height, in pixels.
//     */
//    public void setSize(int width, int height) {
//        this.width = width;
//        this.height = height;
//        
//        sprite.setScaledHeight(height);
//        
//        origin = new MutablePoint(centredX ? -width/2 : 0, centredY ? -height/2 : 0);
//        scale = (float)height / TC_NORMAL_CAP_LEFT_DIMENSIONS.getY();
//        
//        boundingBox = new AxisAlignedBoundingBox(origin.getX(), origin.getY(), width, height);
//        
//        capWidth = (int) (TC_NORMAL_CAP_LEFT_DIMENSIONS.getX() * scale);
//        fillWidth = width - 2*capWidth;
//        
//        refreshText();
//    }
//    
//    /**
//     * Sets the text size.
//     * 
//     * @param fontSize The text height, in pixels.
//     */
//    public void setFontSize(int fontSize) {
//        this.fontSize = fontSize;
//        refreshText();
//    }
//    
//    /**
//     * Gets the origin point of the display text.
//     * 
//     * @return The origin point of the display text.
//     */
//    private MutablePoint getTextOrigin() {
//        return new MutablePoint(centredX ? 0 : width/2, (centredY ? 0 : height/2) - fontSize / 2);
//    }
//    
//    /**
//     * Refreshes the text placement.
//     */
//    private void refreshText() {
//        MutablePoint p = getTextOrigin();
//        
//        FontStyle styleNormal = new FontStyle(
//                fontSize,
//                STYLE_NORMAL_TEMPLATE.colour,
//                STYLE_NORMAL_TEMPLATE.alignment,
//                STYLE_NORMAL_TEMPLATE.kerning,
//                STYLE_NORMAL_TEMPLATE.verticalKerning
//        );
//        FontStyle styleOver = new FontStyle(
//                fontSize,
//                STYLE_OVER_TEMPLATE.colour,
//                STYLE_OVER_TEMPLATE.alignment,
//                STYLE_OVER_TEMPLATE.kerning,
//                STYLE_OVER_TEMPLATE.verticalKerning
//        );
//        FontStyle stylePressed = new FontStyle(
//                fontSize,
//                STYLE_PRESSED_TEMPLATE.colour,
//                STYLE_PRESSED_TEMPLATE.alignment,
//                STYLE_PRESSED_TEMPLATE.kerning,
//                STYLE_PRESSED_TEMPLATE.verticalKerning
//        );
//        
//        setDisplayText(font, text, styleNormal, p, text, styleOver, p, text, stylePressed, p);
//    }
//    
//    @Override
//    public void update() {
//        super.update();
//        
//        updateTexCoords();
//    }
//    
//    /**
//     * Updates the MenuButton's active texture coordinates in accordance with a
//     * possible change in state.
//     */
//    private void updateTexCoords() {
//        if(enabled) {
//            if(lastEnabled) {
//                if(lastState != state) {
//                    if(state == State.OVER) {
//                        translateTexCoords(overTranslation);
//                    } else {
//                        translateTexCoords(overTranslation.negate(new Vector2f()));
//                    }
//                } else {
//                    // nothing
//                }
//            } else {
//                if(state == State.OVER) {
//                    translateTexCoords(new Vector2f(overTranslation.x - disabledTranslation.x, overTranslation.y - disabledTranslation.y));
//                } else {
//                    translateTexCoords(disabledTranslation.negate(new Vector2f()));
//                }
//                // Set the font back to normal
//                refreshText();
//            }
//        } else {
//            if(lastEnabled) {
//                if(lastState == State.OVER) {
//                    translateTexCoords(new Vector2f(disabledTranslation.x - overTranslation.x, disabledTranslation.y - overTranslation.y));
//                } else {
//                    translateTexCoords(disabledTranslation);
//                }
//                // Set the font to the disabled state
//                MutablePoint p = getTextOrigin();
//                FontStyle styleDisabled = new FontStyle(
//                        fontSize,
//                        STYLE_DISABLED_TEMPLATE.colour,
//                        STYLE_DISABLED_TEMPLATE.alignment,
//                        STYLE_DISABLED_TEMPLATE.kerning,
//                        STYLE_DISABLED_TEMPLATE.verticalKerning
//                );
//                setDisplayText(font, text, styleDisabled, p, null, null, null, null, null, null);
//            } else {
//                // nothing
//            }
//        }
//        lastState = state;
//        lastEnabled = enabled;
//    }
//    
//    /**
//     * Translates each of the texture coordinates.
//     * 
//     * @param translation The translation.
//     */
//    private void translateTexCoords(Vector2f translation) {
//        SpriteBatch.translateTexCoords(capLeftTC, translation);
//        SpriteBatch.translateTexCoords(capRightTC, translation);
//        SpriteBatch.translateTexCoords(fillTC, translation);
//    }
//    
//    @Override
//    public void render() {
//        if(!updated) {
//            updateTexCoords();
//            updated = true;
//        }
//        
//        // Left cap
//        sprite.setTextureData(capLeftTC);
//        sprite.setScaledWidth(capWidth);
//        sprite.drawSprite(x + origin.getX(), y + origin.getY());
//        // Right cap
//        sprite.setTextureData(capRightTC);
//        sprite.drawSprite(x + origin.getX() + capWidth + fillWidth, y + origin.getY());
//        // Centre fill
//        sprite.setTextureData(fillTC);
//        sprite.setScaledWidth(fillWidth);
//        sprite.drawSprite(x + origin.getX() + capWidth, y + origin.getY());
//        
//        // Render the text using the super
//        super.render();
//    }
//    
//    @Override
//    public void destroy() {
//        font.destroy();
//        sprite.destroy();
//    }
//    
//}
