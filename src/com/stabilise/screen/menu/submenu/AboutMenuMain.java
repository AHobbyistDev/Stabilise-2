//package com.stabilise.screen.menu.submenu;
//
//import org.lwjgl.input.Keyboard;
//import org.lwjgl.util.Point;
//
//import com.stabilise.opengl.Font;
//import com.stabilise.opengl.FontStyle;
//import com.stabilise.opengl.Sprite;
//import com.stabilise.screen.menu.SubMenuBasedMenu;
//import com.stabilise.screen.menu.component.MenuItem;
//import com.stabilise.screen.menu.customcomponents.MenuButton;
//import com.stabilise.util.Colour;
//
///**
// * The main screen of the about menu.
// * 
// * <p>It should be note that an {@code AboutMenuMain} instance, in addition to
// * setting the value of its {@code action} variable to
// * {@link #ACTION_EXIT ACTION_EXIT} to indicate that the SubMenu is to
// * be exited, also sets the {@code parameter} variable is set as if by:
// * <pre>
// * parameter = new Integer(ACTION_EXIT);</pre>
// */
//public class AboutMenuMain extends SubMenu {
//	
//	//--------------------==========--------------------
//	//-----=====Static Constants and Variables=====-----
//	//--------------------==========--------------------
//	
//	/** The value for the {@code action} variable of an AboutMenuMain instance
//	 * which indicates that the AboutMenuMain is to be exited. */
//	public static final int ACTION_EXIT = getActionID();
//	
//	/** The font style to use for the header. */
//	private static final FontStyle STYLE_HEADER = new FontStyle(32, Colour.BLACK, FontStyle.Alignment.CENTRE, 4, 0);
//	/** The font style to use for the body text. */
//	private static final FontStyle STYLE_BODY = new FontStyle(8, Colour.BLACK, FontStyle.Alignment.CENTRE, 1, 4);
//	
//	/** The body text. */
//	private static final String[] TEXT_BODY = new String[] {
//		"Made by Adam Lackner, 2014",
//		"",
//		"Technologies used:",
//		"Java by Sun Microsystems",
//		"LWJGL",
//		"Apache Lang, Collections & Commons IO"
//	};
//	
//	//--------------------==========--------------------
//	//-------------=====Member Variables=====-----------
//	//--------------------==========--------------------
//	
//	/** The display font. */
//	private Font font;
//	/** The display panel. */
//	private Sprite panel;
//	/** The back button. */
//	private MenuButton backButton;
//	
//	/** The location of the header. */
//	private MutablePoint headerOrigin;
//	/** The location of the text. */
//	private MutablePoint textOrigin;
//	
//	
//	/**
//	 * Creates the about menu's main screen.
//	 * 
//	 * @param menu The about's menu's parent menu.
//	 */
//	public AboutMenuMain(SubMenuBasedMenu menu) {
//		super(menu);
//	}
//	
//	@Override
//	protected void loadResources() {
//		font = new Font("sheets/font1", this);
//		panel = new Sprite("about");
//		panel.setScaledDimensions(640, 480);
//		
//		headerOrigin = new MutablePoint();
//		textOrigin = new MutablePoint();
//		
//		backButton = new MenuButton(this, 0, 0, 300, 48, ACTION_EXIT, "Back to menu", 16, true, false);
//		addMenuItem(backButton);
//		setComponentGrid(new MenuItem[][] {{backButton}});
//	}
//	
//	/**
//	 * Creates the about menu's main screen.
//	 * 
//	 * @param menu The about's menu's parent menu.
//	 * @param parameter The parameter (unused).
//	 */
//	/*
//	public AboutMenuMain(SubMenuBasedMenu menu, Object parameter) {
//		super(menu, parameter);
//	}
//	*/
//	
//	@Override
//	protected void rescale(int width, int height) {
//		super.rescale(width, height);
//		
//		/*
//		panel.x = screen.getCentreX() - 320;
//		panel.y = screen.getCentreY() - 240;
//		
//		headerOrigin.setLocation(screen.getCentreX(), (int)panel.y + 480 - 52);
//		textOrigin.setLocation(screen.getCentreX(), (int)panel.y + 480 - 94);
//		
//		backButton.x = screen.getCentreX();
//		backButton.y = screen.getCentreY() - 240 + 8;
//		*/
//	}
//	
//	@Override
//	public void update() {
//		super.update();
//	}
//	
//	@Override
//	public void render() {
//		panel.draw();
//		
//		super.render();
//		
//		font.drawLine("About the Game", headerOrigin.getX(), headerOrigin.getY(), STYLE_HEADER);
//		font.drawLines(TEXT_BODY, textOrigin.getX(), textOrigin.getY(), STYLE_BODY);
//	}
//	
//	@Override
//	public void performAction(int action, Object parameter) {
//		if(action == ACTION_EXIT)
//			exitMenu();
//	}
//	
//	@Override
//	public void handleKeyPress(int key) {
//		super.handleKeyPress(key);
//		
//		if(key == Keyboard.KEY_ESCAPE)
//			exitMenu();
//	}
//	
//	/**
//	 * Exits the how to play menu.
//	 */
//	private void exitMenu() {
//		action = ACTION_EXIT;
//		parameter = new Integer(ACTION_EXIT);
//	}
//	
//	@Override
//	public void unloadResources() {
//		super.unloadResources();
//		font.destroy();
//		panel.destroy();
//	}
//	
//}
