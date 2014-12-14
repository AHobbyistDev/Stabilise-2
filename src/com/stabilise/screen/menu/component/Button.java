package com.stabilise.screen.menu.component;

import org.lwjgl.util.Point;

import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.opengl.Graphic;
import com.stabilise.screen.menu.Menu;
import com.stabilise.util.shape.AxisAlignedBoundingBox;

/**
 * A button component which is capable of being clicked on or selected.
 */
public class Button extends MenuItem {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Possible button states. */
	public static enum State {
		OFF, OVER, PRESSED;
	};
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The button's current state. */
	protected State state = State.OFF;
	/** The identifier for the action the Button's parent menu is to perform
	 * when the button is clicked/pressed. */
	private int action;
	
	/** The button's bounding box. */
	protected AxisAlignedBoundingBox boundingBox;
	
	/** The background graphics for each of the button's states. */
	private Graphic graphicOff, graphicOver, graphicPressed;
	/** The origin points of the background graphics. */
	private Point graphicOriginOff, graphicOriginOver, graphicOriginPressed;
	/** The font used for display text. */
	private Font textFont;
	/** The display text for each of the button's states. */
	private String textOff, textOver, textPressed;
	/** The font styles used for the text for each of the button's states. */
	private FontStyle textStyleOff, textStyleOver, textStylePressed;
	/** The origin points of the display text. */
	private Point textOriginOff, textOriginOver, textOriginPressed;
	
	
	/**
	 * Creates a new Button.
	 * 
	 * @param menu The menu containing the Button.
	 * @param x The x-coordinate of the Button.
	 * @param y The y-coordinate of the Button.
	 * @param action The identifier for the action the Button's parent menu is
	 * to perform when the Button is clicked/pressed. A value of -1 should be
	 * used to indicate that the Button has no functionality.
	 * @param boundingBox The Button's bounding box.
	 */
	public Button(Menu menu, int x, int y, int action, AxisAlignedBoundingBox boundingBox) {
		super(menu, x, y);
		this.action = action;
		this.boundingBox = boundingBox;
	}
	
	/**
	 * Sets the display graphics of the button.
	 * 
	 * @param graphicOff The graphic to display during the off state.
	 * @param graphicOriginOff The origin point of the off state graphic.
	 * @param graphicOver The graphic to display during the over state.
	 * @param graphicOriginOver The origin point of the over state graphic.
	 * @param graphicPressed The graphic to display during the pressed state.
	 * @param graphicOriginPressed The origin point of the pressed state
	 * graphic.
	 * 
	 * @return The Button, for chain construction.
	 */
	public Button setDisplay(
			Graphic graphicOff, Point graphicOriginOff,
			Graphic graphicOver, Point graphicOriginOver,
			Graphic graphicPressed, Point graphicOriginPressed) {
		
		this.graphicOff = graphicOff;
		this.graphicOriginOff = graphicOriginOff;
		
		this.graphicOver = graphicOver;
		this.graphicOriginOver = graphicOriginOver;
		
		this.graphicPressed = graphicPressed;
		this.graphicOriginPressed = graphicOriginPressed;
		
		return this;
	}
	
	/**
	 * Sets the display text of the button.
	 * 
	 * @param font The display font.
	 * @param textOff The display text for the off state.
	 * @param textStyleOff The font style for the display text for the off
	 * state.
	 * @param textOriginOff The origin point of the text for the off state.
	 * @param textOver The display text for the over state.
	 * @param textStyleOver The font style for the display text for the over
	 * state.
	 * @param textOriginOver The origin point of the text for the over state.
	 * @param textPressed The display text for the pressed state.
	 * @param textStylePressed The font style for the display text for the
	 * pressed state.
	 * @param textOriginPressed The origin point of the text for the pressed
	 * state.
	 * 
	 * @return The Button, for chain construction.
	 */
	public Button setDisplayText(Font font,
			String textOff, FontStyle textStyleOff, Point textOriginOff,
			String textOver, FontStyle textStyleOver, Point textOriginOver,
			String textPressed, FontStyle textStylePressed, Point textOriginPressed) {
		
		this.textFont = font;
		
		this.textOff = textOff;
		this.textStyleOff = textStyleOff;
		this.textOriginOff = textOriginOff;
		
		this.textOver = textOver;
		this.textStyleOver = textStyleOver;
		this.textOriginOver = textOriginOver;
		
		this.textPressed = textPressed;
		this.textStylePressed = textStylePressed;
		this.textOriginPressed = textOriginPressed;
		
		return this;
	}
	
	@Override
	public void render() {
		switch(state) {
			case OFF:
				if(graphicOff != null) {
					graphicOff.x = x + graphicOriginOff.getX();
					graphicOff.y = y + graphicOriginOff.getY();
					graphicOff.draw();
				}
				if(textOff != null)
					textFont.drawLine(textOff, x + textOriginOff.getX(), y + textOriginOff.getY(), textStyleOff);
				break;
			case OVER:
				if(graphicOver != null) {
					graphicOver.x = x + graphicOriginOver.getX();
					graphicOver.y = y + graphicOriginOver.getY();
					graphicOver.draw();
				}
				if(textOver != null)
					textFont.drawLine(textOver, x + textOriginOver.getX(), y + textOriginOver.getY(), textStyleOver);
				break;
			case PRESSED:
				if(graphicPressed != null) {
					graphicPressed.x = x + graphicOriginPressed.getX();
					graphicPressed.y = y + graphicOriginPressed.getY();
					graphicPressed.draw();
				}
				if(textPressed != null)
					textFont.drawLine(textPressed, x + textOriginPressed.getX(), y + textOriginPressed.getY(), textStylePressed);
				break;
		}
	}

	@Override
	public boolean isMouseInBounds(int mouseX, int mouseY) {
		return boundingBox.translate(x, y).containsPoint(mouseX, mouseY);
	}
	
	@Override
	public void focus() {
		setState(State.OVER);
	}
	
	@Override
	public void unfocus() {
		setState(State.OFF);
	}
	
	@Override
	public boolean canBeFocusedFromHover() {
		return true;
	}
	
	@Override
	public boolean canBeFocusedFromClick() {
		return true;
	}
	
	@Override
	public void destroy() {
		// nothing to see here
	}
	
	/**
	 * Sets the state of the button and invokes an action in its parent menu if
	 * appropriate.
	 * 
	 * @param state The state to transition the button into.
	 */
	public void setState(State state) {
		setState(state, true);
	}
	
	/**
	 * Sets the state of the button and invokes an action in its parent menu if
	 * appropriate.
	 * 
	 * @param state The state to transition the button into.
	 * @param doPressAction Whether or not the button should perform its 'press
	 * state' action, provided the state it's being set to is the over state.
	 * 
	 * @see Button.State
	 */
	public void setState(State state, boolean doPressAction) {
		if(!this.state.equals(state) && doPressAction && this.state.equals(State.PRESSED) && state.equals(State.OVER))
			//menu.performAction(action, parameter);
			menu.performAction(action, this);
		
		this.state = state;
	}
	
	/**
	 * Gets the button's state.
	 * 
	 * @return The button's current state.
	 */
	public final State getState() {
		return state;
	}
	
	/**
	 * Sets the button's display text.
	 * 
	 * @param offStateText The text to display normally.
	 * @param overStateText The text to display when the button is being
	 * hovered over.
	 * @param pressedStateText The text to display when the button is being
	 * pressed.
	 * 
	 * @return The Button, for chaining operations.
	 */
	public final Button setDisplayText(String offStateText, String overStateText, String pressedStateText) {
		textOff = offStateText;
		textOver = overStateText;
		textPressed = pressedStateText;
		return this;
	}
	
}
