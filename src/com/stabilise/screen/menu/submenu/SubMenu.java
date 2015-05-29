//package com.stabilise.screen.menu.submenu;
//
//import com.stabilise.screen.menu.Menu;
//import com.stabilise.screen.menu.SubMenuBasedMenu;
//
///**
// * A SubMenu is essentially a menu screen which is reusable in multiple
// * different menus.
// * 
// * <p>A SubMenu is capable of interacting with its parent menu through three
// * main ways: through explicit interaction with variables and methods specific
// * to a subclass of {@link SubMenuBasedMenu}, through invocation of any of the
// * {@code setSubMenu()} methods in {@code SubMenuBasedMenu}, or by setting the
// * value of {@code action}.
// * 
// * <ul>
// * <li>The first method affords the greatest control for the SubMenu, but
// *     limits its flexibility, as it will generally mean the SubMenu's
// *     functionality is limited to a specific menu.
// * <li>The second method allows the SubMenu to control when to switch to a
// *     different sub-menu, and is convenient when, say, pressing a button in
// *     one branch of a menu would open another branch.
// * <li>The third method allows the SubMenu's parent menu to determine how to
// *     act when the {@code action} variable is set. Additional data may be
// *     accompanied with this value by means of the value of the
// *     {@code parameter} variable.
// * </ul>
// */
//public abstract class SubMenu extends Menu {
//	
//	//--------------------==========--------------------
//	//-----=====Static Constants and Variables=====-----
//	//--------------------==========--------------------
//	
//	/** The value for the {@code action} variable of a SubMenu which indicates
//	 * the lack of an action. */
//	public static final int NO_ACTION = -1;
//	/** The value for the {@code action} variable of a SubMenu which indicates
//	 * that the SubMenu is to be exited. */
//	//public static final int ACTION_EXIT = getActionID();
//	
//	//--------------------==========--------------------
//	//-------------=====Member Variables=====-----------
//	//--------------------==========--------------------
//	
//	/** The Menu to which the sub-menu belongs. */
//	protected SubMenuBasedMenu menu;
//	
//	/** The action being performed by the sub-menu, which serves as a mode of
//	 * state communication between the sub-menu and its parent menu. This
//	 * value is reset to {@link #NO_ACTION} at the start of each update tick. */
//	public int action = NO_ACTION;
//	/** An additional parameter associated with an action, for sending
//	 * additional data to the sub-menu's parent menu during an action. */
//	public Object parameter;
//	
//	
//	/**
//	 * Creates a new SubMenu, which sets itself as the focus of the
//	 * InputManager.
//	 * 
//	 * @param menu The SubMenu's parent menu.
//	 */
//	public SubMenu(SubMenuBasedMenu menu) {
//		super(true);
//		
//		this.menu = menu;
//	}
//	
//	/**
//	 * Creates a new SubMenu, which sets itself as the focus of the
//	 * InputManager.
//	 * 
//	 * @param menu The SubMenu's parent menu.
//	 * @param parameter The SubMenu's parameter, to determine circumstantial
//	 * behaviour.
//	 */
//	public SubMenu(SubMenuBasedMenu menu, Object parameter) {
//		this(menu);
//	}
//	
//	@Override
//	public void update() {
//		if(action != NO_ACTION) {
//			action = NO_ACTION;
//			parameter = null;
//		}
//		
//		super.update();
//	}
//	
//	//--------------------==========--------------------
//	//------------=====Static Functions=====------------
//	//--------------------==========--------------------
//	
//	/** The last ID that was handed out. */
//	private static int lastID = NO_ACTION;
//	
//	/**
//	 * Generates a unique ID for a submenu action.
//	 * 
//	 * @return The action ID.
//	 */
//	protected static int getActionID() {
//		return ++lastID;
//	}
//}
