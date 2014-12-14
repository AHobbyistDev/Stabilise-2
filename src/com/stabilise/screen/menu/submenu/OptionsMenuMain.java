package com.stabilise.screen.menu.submenu;

import org.lwjgl.input.Keyboard;

import com.stabilise.screen.menu.SubMenuBasedMenu;

/**
 * The main screen of the options menu.
 * 
 * <p>It should be note that an {@code OptionsMenuMain} instance, in addition
 * to setting the value of its {@code action} variable to
 * {@link #ACTION_EXIT ACTION_EXIT} to indicate that the SubMenu is to
 * be exited, also sets the {@code parameter} variable is set as if by:
 * <pre>
 * parameter = new Integer(ACTION_EXIT);</pre>
 */
public class OptionsMenuMain extends SubMenu {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The value for the {@code action} variable of an OptionsMenuMain
	 * instance which indicates that the OptionsMenuMain is to be exited. */
	public static final int ACTION_EXIT = getActionID();
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	public OptionsMenuMain(SubMenuBasedMenu menu) {
		super(menu);
	}
	
	public OptionsMenuMain(SubMenuBasedMenu menu, Object parameter) {
		super(menu, parameter);
	}
	
	@Override
	protected void loadResources() {
		// TODO
	}
	
	@Override
	protected void rescale(int width, int height) {
		super.rescale(width, height);
		// TODO
	}
	
	@Override
	public void performAction(int action, Object parameter) {
		// TODO
	}
	
	@Override
	public void handleKeyPress(int key) {
		super.handleKeyPress(key);
		
		if(key == Keyboard.KEY_ESCAPE) {
			action = ACTION_EXIT;
			parameter = new Integer(ACTION_EXIT);
		}
	}
	
}
