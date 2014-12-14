package com.stabilise.screen.menu.submenu;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Point;

import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.opengl.Sprite;
import com.stabilise.screen.menu.SubMenuBasedMenu;
import com.stabilise.screen.menu.component.MenuItem;
import com.stabilise.screen.menu.customcomponents.MenuButton;
import com.stabilise.util.Colour;

/**
 * The main screen of the how to play menu.
 * 
 * <p>It should be note that a {@code HowToPlayMenu} instance, in addition to
 * setting the value of its {@code action} variable to
 * {@link #ACTION_EXIT ACTION_EXIT} to indicate that the SubMenu is to
 * be exited, also sets the {@code parameter} variable is set as if by:
 * <pre>
 * parameter = new Integer(ACTION_EXIT);</pre>
 */
public class HowToPlayMenu extends SubMenu {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The value for the {@code action} variable of a HowToPlayMenu
	 * instance which indicates that the HowToPlayMenu is to be exited. */
	public static final int ACTION_EXIT = getActionID();
	
	/** The font style to use for the 'controls' header. */
	private static final FontStyle STYLE_HEADER = new FontStyle(32, Colour.BLACK, FontStyle.Alignment.CENTRE, 4, 0);
	/** The font style to use for the different controls text. */
	private static final FontStyle STYLE_CONTROLS = new FontStyle(16, Colour.BLACK, FontStyle.Alignment.RIGHT, 1, 18);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The display font. */
	private Font font;
	/** The 'how to play' display panel. */
	private Sprite panel;
	/** The back button. */
	private MenuButton backButton;
	
	/** The location of the header. */
	private Point headerOrigin;
	/** The location of the controls text. */
	private Point controlsOrigin;
	
	
	/**
	 * Creates a new 'how to play' SubMenu, which sets itself as the focus of
	 * the InputManager.
	 * 
	 * @param menu The SubMenu's parent menu.
	 */
	public HowToPlayMenu(SubMenuBasedMenu menu) {
		super(menu);
	}
	
	/*
	public HowToPlayMenu(SubMenuBasedMenu menu, Object parameter) {
		super(menu, parameter);
	}
	*/
	
	@Override
	protected void loadResources() {
		font = new Font("sheets/font1", this);
		panel = new Sprite("howtoplay");
		panel.setScaledDimensions(640, 480);
		
		headerOrigin = new Point();
		controlsOrigin = new Point();
		
		backButton = new MenuButton(this, 0, 0, 300, 48, ACTION_EXIT, "Back to menu", 16, true, false);
		addMenuItem(backButton);
		setComponentGrid(new MenuItem[][] {{backButton}});
	}
	
	@Override
	protected void rescale(int width, int height) {
		super.rescale(width, height);
		
		/*
		panel.x = screen.getCentreX() - 320;
		panel.y = screen.getCentreY() - 240;
		
		headerOrigin.setLocation(screen.getCentreX(), (int)panel.y + 480 - 52);
		controlsOrigin.setLocation(screen.getCentreX() - 8, (int)panel.y + 480 - 128);
		
		backButton.x = screen.getCentreX();
		backButton.y = screen.getCentreY() - 240 + 8;
		*/
	}
	
	@Override
	public void render() {
		panel.draw();
		
		super.render();
		
		font.drawLine("Controls", headerOrigin.getX(), headerOrigin.getY(), STYLE_HEADER);
		font.drawLines(new String[] {
				"Move",
				"Jump",
				"Attack",
				"Special attack",
				"Pause"
		}, controlsOrigin.getX(), controlsOrigin.getY(), STYLE_CONTROLS);
	}
	
	@Override
	public void performAction(int action, Object parameter) {
		if(action == ACTION_EXIT)
			exitMenu();
	}
	
	@Override
	public void handleKeyPress(int key) {
		super.handleKeyPress(key);
		
		if(key == Keyboard.KEY_ESCAPE)
			exitMenu();
	}
	
	/**
	 * Exits the how to play menu.
	 */
	private void exitMenu() {
		action = ACTION_EXIT;
		parameter = new Integer(ACTION_EXIT);
	}
	
	@Override
	public void unloadResources() {
		super.unloadResources();
		font.destroy();
		panel.destroy();
	}
	
}
