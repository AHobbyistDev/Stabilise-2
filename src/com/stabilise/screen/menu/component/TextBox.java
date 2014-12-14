package com.stabilise.screen.menu.component;

import org.lwjgl.input.Keyboard;

import com.stabilise.core.Constants;
import com.stabilise.input.InputManager;
import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.screen.menu.Menu;

/**
 * This class represents a text box into which the user may enter characters.
 * Note that it currently only supports single lines of text.
 */
public class TextBox extends MenuItem {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The interval in ticks between when the cursor flashes off and on. */
	private static int CURSOR_DURATION = Constants.TICKS_PER_SECOND / 2;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The text box's display text. */
	public String text;
	/** The text the textbox should display when empty and unfocused, if any. */
	private String defaultText;
	
	/** The font to use for the display text. */
	private Font font;
	/** The font style. */
	private FontStyle style;
	
	/** True if the textbox should be displayed in multiple lines.
	 * TODO: Unimplemented. */
	//public boolean multiline;
	/** The character limit of the textbox. */
	private int charLimit;
	
	/** The cursor character index - to determine at what point characters are
	 * inserted. */
	private int index;
	/** By how many characters the textbox is scrolled horizontally. Only used
	 * if multiline is false. */
	private int horizontalScroll;
	
	/** True if the TextBox is being focused on. */
	private boolean focus = false;
	
	/** True if the '|' character is being displayed. */
	private boolean cursorState = false;
	/** The number of ticks before the pipe changes state. */
	private int cursorCounter = 0;
	
	/** The string of characters currently visible within the textbox. */
	private String visibleText = "";
	/** Whether or not the contents of the TextBox were modified since the last
	 * tick. */
	public boolean updated = true;
	
	
	/**
	 * Creates a new TextBox.
	 * 
	 * @param menu The menu containing the TextBox.
	 * @param x The x-coordinate of the TextBox.
	 * @param y The y-coordinate of the TextBox.
	 * @param width The width of the TextBox.
	 * @param height The height of the TextBox.
	 * @param font The font to use to display text.
	 * @param style The font style.
	 */
	public TextBox(Menu menu, int x, int y, int width, int height, Font font, FontStyle style) {
		this(menu, x, y, width, height, font, style, "");
	}
	
	/**
	 * Creates a new TextBox.
	 * 
	 * @param menu The menu containing the TextBox.
	 * @param x The x-coordinate of the TextBox.
	 * @param y The y-coordinate of the TextBox.
	 * @param width The width of the TextBox.
	 * @param height The height of the TextBox.
	 * @param font The font to use to display text.
	 * @param style The font style.
	 * @param defaultText The text to display on the box by default.
	 */
	public TextBox(Menu menu, int x, int y, int width, int height, Font font, FontStyle style, String defaultText) {
		this(menu, x, y, width, height, font, style, defaultText, 0);
	}
	
	/**
	 * Creates a new TextBox.
	 * 
	 * @param menu The menu containing the TextBox.
	 * @param x The x-coordinate of the TextBox.
	 * @param y The y-coordinate of the TextBox.
	 * @param width The width of the TextBox.
	 * @param height The height of the TextBox.
	 * @param font The font to use to display text.
	 * @param style The font style.
	 * @param defaultText The text to display on the box by default.
	 * @param charLimit The character limit of the textbox.
	 */
	public TextBox(Menu menu, int x, int y, int width, int height, Font font, FontStyle style, String defaultText, int charLimit) {
		this(menu, x, y, width, height, font, style, defaultText, charLimit, "");
	}
	
	/**
	 * Creates a new TextBox.
	 * 
	 * @param menu The menu containing the TextBox.
	 * @param x The x-coordinate of the TextBox.
	 * @param y The y-coordinate of the TextBox.
	 * @param width The width of the TextBox.
	 * @param height The height of the TextBox.
	 * @param font The font to use to display text.
	 * @param style The font style.
	 * @param defaultText The text to display on the box by default.
	 * @param charLimit The character limit of the TextBox.
	 * @param text The initial value for the entered text.
	 */
	public TextBox(Menu menu, int x, int y, int width, int height, Font font, FontStyle style, String defaultText, int charLimit, String text) {
		super(menu, x, y, width, height);
		
		this.font = font;
		this.style = style;
		//this.style = new FontStyle(style.px, style.colour, FontStyle.Alignment.LEFT, style.kerning, style.verticalKerning);
		this.defaultText = defaultText;
		this.charLimit = charLimit;
		//this.multiline = multiline;
		this.text = text;
	}
	
	/**
	 * This should be called by the Menu instance to notify the TextBox of a
	 * key press.
	 * 
	 * @param key The key which was pressed.
	 */
	public void handleKeyPress(int key) {
		switch(key) {
			case Keyboard.KEY_LEFT:
				if(InputManager.get().isCtrlKeyDown()) {
					index = nextWordIndex(false);
				} else {
					if(index > 0)
						index--;
				}
				updated = true;
				break;
			case Keyboard.KEY_RIGHT:
				if(InputManager.get().isCtrlKeyDown()) {
					index = nextWordIndex(true) + 1;
					if(index > text.length()) index--;
				} else {
					if(index < text.length())
						index++;
				}
				updated = true;
				break;
			// cases for down and up when multiple lines are implemented
			case Keyboard.KEY_BACK:
				if(InputManager.get().isCtrlKeyDown())
					deleteWord(false);
				else
					deleteCharacter(false);
				break;
			case Keyboard.KEY_DELETE:
				if(InputManager.get().isCtrlKeyDown())
					deleteWord(true);
				else
					deleteCharacter(true);
				break;
			default:
				if(text.length() < charLimit) {
					char keyChar = Keyboard.getEventCharacter();
					if(!isAllowedCharacter(keyChar)) return;
					if(index == text.length())
						text += keyChar;
					else if(index == 0)
						text = keyChar + text;
					else
						text = text.substring(0, index) + keyChar + text.substring(index, text.length());
					index++;
					updated = true;
				}
				break;
		}
		
		cursorState = true;
		cursorCounter = CURSOR_DURATION;
	}
	
	/**
	 * Deletes a word.
	 * 
	 * @param right True if the word to be deleted should be to the right
	 * of the cursor.
	 */
	private void deleteWord(boolean right) {
		if(right)
			deleteBetween(index, nextWordIndex(right), false);
		else
			deleteBetween(nextWordIndex(right), index, true);
		
		/*while(text.length() > 0 && index > 0 && text.charAt(index - 1) != ' ') {
			deleteCharacter(right);
		}
		deleteCharacter(right);*/
	}
	
	/**
	 * Deletes a character.
	 * 
	 * @param right True if the character to be deleted should be to the
	 * right of the cursor.
	 */
	private void deleteCharacter(boolean right) {
		if(right)
			deleteBetween(index, index + 1, false);
		else
			deleteBetween(index - 1, index, true);
		/*
		if(index > 0 && text.length() > 0) {
			if(index == text.length())
				text = text.substring(0, index - 1);
			else
				text = text.substring(0, index - 1) + text.substring(index, text.length());
			index--;
		}*/
	}
	
	/**
	 * Deletes all characters between two specified indexes.
	 * 
	 * @param startIndex The start index.
	 * @param endIndex The end index.
	 * @param moveIndex True if the cursor index should be moved in accordance
	 * with the delete operation. Generally this is true with backspace and
	 * false with delete.
	 */
	private void deleteBetween(int startIndex, int endIndex, boolean moveIndex) {
		if(startIndex < 0 || endIndex > text.length()) return;
		
		if(startIndex == 0 && endIndex == text.length())
			text = "";
		else if(startIndex == 0)
			text = text.substring(endIndex, text.length());
		else if(startIndex == text.length())
			text = text.substring(0, startIndex - 1);
		else if(endIndex == text.length())
			text = text.substring(0, startIndex);
		else
			text = text.substring(0, startIndex) + text.substring(endIndex, text.length());
		
		if(moveIndex) index += startIndex - endIndex;
		
		updated = true;
	}
	
	/**
	 * Gets the index of the next word.
	 * 
	 * @param checkRight True if this method should check to the right of the
	 * current cursor index, and false if it should check to the left.
	 * 
	 * @return The index of the next word.
	 */
	private int nextWordIndex(boolean checkRight) {
		if(text.length() == 0) return 0;
		
		int tempIndex = index == 0 ? 0 : index - 1;
		// For removing consecutive spaces separating words
		boolean wasLast = false;
		boolean initial = text.charAt(tempIndex) == ' ';
		
		if(checkRight) {
			while(tempIndex < text.length()) {
				if(text.charAt(tempIndex) == ' ')
					wasLast = true;
				else if(wasLast && initial)
					initial = wasLast = false;
				else if(wasLast)
					return tempIndex - 1;
				
				tempIndex++;
			}
			return text.length();
		} else {
			while(tempIndex > 0) {
				if(text.charAt(tempIndex) == ' ')
					wasLast = true;
				else if(wasLast && initial)
					initial = wasLast = false;
				else if(wasLast)
					return tempIndex + 1;
				
				tempIndex--;
			}
			return 0;
		}
	}
	
	@Override
	public void update() {
		super.update();
		
		if(focus) {
			cursorCounter--;
			if(cursorCounter == 0) {
				cursorState = !cursorState;
				cursorCounter = CURSOR_DURATION;
			}
			
			while(index < horizontalScroll)
				horizontalScroll--;
			while(index > horizontalScroll + getWidthInChars())
				horizontalScroll++;
			while(horizontalScroll > 0 && index - horizontalScroll < getWidthInChars())		// TODO: this seems to work for centred alignment
			//while(horizontalScroll > 0 && index - horizontalScroll < 0)					// TODO: this seems to work for left alignment
				horizontalScroll--;
		}
		
		// Update the visible text
		if(updated) {
			updated = false;
			int max = horizontalScroll + getWidthInChars();
			if(max > text.length() - 1) max = text.length();
			visibleText = text.substring(horizontalScroll, max);
		}
	}
	
	@Override
	public void render() {
		font.setSize(style.size);
		if(focus) {
			font.drawLine(visibleText, x, y/* - style.size / 2*/, style);
			if(cursorState) {
				int cursorX = x;
				if(style.alignment == FontStyle.Alignment.LEFT)
					cursorX += (int)((style.size + style.kerning) * (index - horizontalScroll + 0.3f));
				else if(style.alignment == FontStyle.Alignment.CENTRE)
					cursorX += (int)((style.size + style.kerning) * (index*2 - (getWidthInChars() < text.length() ? getWidthInChars() : text.length()) - horizontalScroll + 0.3f) / 2f);
					//cursorX += (int)((style.size + style.kerning) * (index - horizontalScroll + 0.3f) / 2f);
				
				font.drawLine("|", cursorX, y/* - style.size / 2*/, style);
			}
		} else {
			if(text == "") {
				font.drawLine(defaultText, x, y/* - style.size / 2*/, style);
			} else {
				font.drawLine(visibleText, x, y/* - style.size / 2*/, style);
			}
		}
	}
	
	@Override
	public boolean isMouseInBounds(int mouseX, int mouseY) {
		if(style.alignment == FontStyle.Alignment.CENTRE)
			return mouseX >= x - width/2 && mouseX <= x + width/2 && mouseY >= y && mouseY <= y + height;
		else if(style.alignment == FontStyle.Alignment.LEFT) 
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		else
			return false;			// TODO: right alignment
	}
	
	/**
	 * Sets the currently-selected index based on the user's mouse-coordinates.
	 * It is assumed that the user clicked on the text box to invoke this
	 * method.
	 * 
	 * @param mouseX The x-coordinate of the mouse on the screen.
	 */
	public void setIndex(int mouseX) {
		// So we know the mouseX relative to the textbox.
		mouseX -= x;
		
		int sk = style.size + style.kerning;	// sk -> style + kerning
		
		if(style.alignment == FontStyle.Alignment.CENTRE)
			index = (int)Math.round(mouseX/sk + (text.length() - (float)width/(sk*sk) + 1)/2f);
			//index = (int) ((Math.round(((float)mouseX / (style.size + style.kerning))) + (getWidthInChars() < text.length() ? getWidthInChars() : text.length()) + horizontalScroll)/2f);
		else if(style.alignment == FontStyle.Alignment.LEFT) 
			index = (int) Math.round(((float)mouseX / (style.size + style.kerning))) + horizontalScroll;
		else
			;			// TODO: right alignment
		
		//System.out.println(mouseX + "," + text.length() + "," + width + "," + sk);
		//System.out.println("index set to: " + index);
		
		if(index > text.length())
			index = text.length();
		else if(index < 0)
			index = 0;
		
		updated = true;
	}
	
	/**
	 * Gets the number of characters that can fit within the textbox.
	 * 
	 * @return The number of characters that can fit within the textbox.
	 */
	private int getWidthInChars() {
		return width / (style.size + style.kerning);
	}
	
	@Override
	public void focus() {
		focus(-1, -1);
	}
	
	/**
	 * Informs the TextBox that is has been focused on.
	 * 
	 * @param mouseX The x-coordinate of the mouse.
	 * @param mouseY The y-coordinate of the mouse.
	 */
	public void focus(int mouseX, int mouseY) {
		if(!enabled)
			return;
		
		focus = true;
		
		if(isMouseInBounds(mouseX, mouseY) && InputManager.get().isButtonDown(0))
			setIndex(mouseX);
		else
			index = text.length();
		
		cursorState = true;
		cursorCounter = CURSOR_DURATION;
		
		// A little cheaty, but it works.
		// This should carry through to the InputManager.
		Keyboard.enableRepeatEvents(true);
	}
	
	@Override
	public void unfocus() {
		focus = false;
		
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	public boolean canBeFocusedFromHover() {
		return false;
	}
	
	@Override
	public boolean canBeFocusedFromClick() {
		return true;
	}
	
	@Override
	public void destroy() {
		Keyboard.enableRepeatEvents(false);
	}
	
	/**
	 * Checks for whether or not a given character is capable of being
	 * displayed by the textbox.
	 * 
	 * @param c The character in question.
	 * 
	 * @return {@code true} if the character is legal.
	 */
	private static boolean isAllowedCharacter(char c) {
		if(c == ' ') return true;
		
		String allowedChars = Font.CHARS.toLowerCase();
		return allowedChars.indexOf(Character.toString(c).toLowerCase()) != -1;
	}
	
}
