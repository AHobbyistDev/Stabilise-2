//package com.stabilise.screen.menu.submenu;
//
//import org.lwjgl.input.Keyboard;
//
//import com.stabilise.screen.menu.SubMenuBasedMenu;
//import com.stabilise.screen.menu.component.MenuItem;
//import com.stabilise.screen.menu.component.Position;
//import com.stabilise.screen.menu.customcomponents.MenuButton;
//
///**
// * The main screen of the pause menu.
// */
//public class PauseMenuMain extends SubMenu {
//	
//	//--------------------==========--------------------
//	//-----=====Static Constants and Variables=====-----
//	//--------------------==========--------------------
//	
//	public static final int ACTION_RETURN_TO_GAME = getActionID();
//	public static final int ACTION_HOW_TO_PLAY = getActionID();
//	public static final int ACTION_RETURN_TO_MENU = getActionID();
//	public static final int ACTION_QUIT_GAME = getActionID();
//	
//	
//	/**
//	 * Creates a new PauseMenu main screen.
//	 * 
//	 * @param The SubMenu's parent menu.
//	 */
//	public PauseMenuMain(SubMenuBasedMenu menu) {
//		this(menu, null);
//	}
//	
//	/**
//	 * Creates a new PauseMenu main screen. The parameter is unused.
//	 * 
//	 * @param menu The SubMenu's parent menu.
//	 * @param parameter The SubMenu's parameter, to determine circumstantial
//	 * behaviour.
//	 */
//	public PauseMenuMain(SubMenuBasedMenu menu, Object parameter) {
//		super(menu);
//		
//		setFocus(0, parameter instanceof Integer ? ((Integer)parameter).intValue() : 0);
//	}
//	
//	@Override
//	protected void loadResources() {
//		setComponentGrid(new MenuItem[][] {
//				{ addMenuItem(new MenuButton(this,0,0,380,40,ACTION_RETURN_TO_GAME,"Return to Game",16,true,true)).setPosition(new Position(0.5f, 0.56f, 0, 30)) },
//				{ addMenuItem(new MenuButton(this,0,0,380,40,ACTION_HOW_TO_PLAY,"How to Play",16,true,true)).setPosition(new Position(0.5f, 0.52f, 0, 10)) },
//				{ addMenuItem(new MenuButton(this,0,0,380,40,ACTION_RETURN_TO_MENU,"Return to Main Menu",16,true,true)).setPosition(new Position(0.5f, 0.48f, 0, -10)) },
//				{ addMenuItem(new MenuButton(this,0,0,380,40,ACTION_QUIT_GAME,"Quit Game",16,true,true)).setPosition(new Position(0.5f, 0.44f, 0, -30)) }
//		}, 0, 0);
//	}
//	
//	@Override
//	protected void rescale(int width, int height) {
//		super.rescale(width, height);
//		// nothing to see here, move along
//	}
//	
//	@Override
//	public void performAction(int action, Object parameter) {
//		this.action = action;
//	}
//	
//	@Override
//	public void handleKeyPress(int key) {
//		super.handleKeyPress(key);
//		
//		if(key == Keyboard.KEY_ESCAPE)
//			this.action = ACTION_RETURN_TO_GAME;
//	}
//	
//	@Override
//	public void unloadResources() {
//		super.unloadResources();
//	}
//	
//}
