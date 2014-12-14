package com.stabilise.screen.menu;

import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;

import com.stabilise.screen.menu.component.MenuItem;
import com.stabilise.screen.menu.submenu.SubMenu;
import com.stabilise.util.Log;

/**
 * A submenu-based menu is a menu composed simply of submenus.
 */
public abstract class SubMenuBasedMenu extends Menu {
	
	/** The currently active SubMenu. */
	public SubMenu submenu;
	
	
	/**
	 * Creates a new SubMenuBasedMenu.
	 */
	public SubMenuBasedMenu() {
		super(false);
	}
	
	/**
	 * Updates the menu by updating its current SubMenu.
	 * 
	 * <p>If a subclass of {@code SubMenuBasedMenu} overrides this method,
	 * {@code super.update()} should ideally be called at the end of the
	 * method body, as the current SubMenu's {@link SubMenu#action action}
	 * flag may otherwise reset before its value can be read.
	 */
	@Override
	public void update() {
		super.update();
		if(submenu != null)
			submenu.update();
	}
	
	@Override
	public void render() {
		if(submenu != null)
			submenu.render();
	}
	
	/**
	 * Sets the sub-menu. The sub-menu will be reflectively instantiated. Any
	 * exceptions thrown as a result of failing to reflectively instantiate the
	 * sub-menu will be ignored and the sub-menu will not be set.
	 * 
	 * @param submenuClass The sub-menu's class.
	 */
	public final void setSubMenuSafe(Class<? extends SubMenu> submenuClass) {
		try {
			setSubMenu(submenuClass);
		} catch(Exception ignored) {}
	}
	
	/**
	 * Sets the sub-menu. The sub-menu will be reflectively instantiated. Any
	 * exceptions thrown as a result of failing to reflectively instantiate the
	 * sub-menu will be ignored and the sub-menu will not be set.
	 * 
	 * @param submenuClass The sub-menu's class.
	 * @param parameter The sub-menu's parameter, to determine circumstantial
	 * behaviour.
	 */
	public final void setSubMenuSafe(Class<? extends SubMenu> submenuClass, Object parameter) {
		try {
			setSubMenu(submenuClass, parameter);
		} catch(Exception ignored) {}
	}
	
	/**
	 * Sets the sub-menu. The sub-menu will be reflectively instantiated.
	 * 
	 * @param submenuClass The sub-menu's class.
	 * 
	 * @throws Exception if the sub-menu could not be reflectively
	 * instantiated.
	 */
	public final void setSubMenu(Class<? extends SubMenu> submenuClass) throws Exception {
		try {
			Constructor<? extends SubMenu> c = submenuClass.getConstructor(SubMenuBasedMenu.class);
			setSubMenu(c.newInstance(this));
		//} catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		} catch(Exception e) {
			Log.critical("Could not reflectively instantiate sub-menu!");
			throw new Exception(e);
		}
	}
	
	/**
	 * Sets the sub-menu. The sub-menu will be reflectively instantiated.
	 * 
	 * @param submenuClass The sub-menu's class.
	 * @param parameter The sub-menu's parameter, to determine circumstantial
	 * behaviour.
	 * 
	 * @throws Exception if the sub-menu could not be reflectively
	 * instantiated.
	 */
	public final void setSubMenu(Class<? extends SubMenu> submenuClass, Object parameter) throws Exception {
		try {
			Constructor<? extends SubMenu> c = submenuClass.getConstructor(SubMenuBasedMenu.class, Object.class);
			setSubMenu(c.newInstance(this, parameter));
		//} catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		} catch(Exception e) {
			Log.critical("Could not reflectively instantiate sub-menu!");
			throw new Exception(e);
		}
	}
	
	/**
	 * Sets the sub-menu.
	 * 
	 * @param submenu The new sub-menu.
	 * 
	 * @throws NullPointerException if {@code submenu} is {@code null}.
	 */
	public void setSubMenu(SubMenu submenu) {
		if(submenu == null)
			throw new NullPointerException("Cannot set a null SubMenu!");
		
		if(this.submenu != null)
			this.submenu.unloadResources();
		
		this.submenu = submenu;
	}
	
	/**
	 * Not permitted for a SubMenuBasedMenu.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	protected final MenuItem addMenuItem(MenuItem item) {
		throw new UnsupportedOperationException("Generic MenuItems may not be added to a SubMenuBasedMenu!");
	}
	
	/**
	 * Not permitted for a SubMenuBasedMenu.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	protected final void setComponentGrid(MenuItem[][] grid) {
		throw new UnsupportedOperationException("ComponentGrids may not be added to a SubMenuBasedMenu!");
	}
	
	/**
	 * Not permitted for a SubMenuBasedMenu.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	protected final void setComponentGrid(MenuItem[][] grid, int initialCol, int initialRow) {
		throw new UnsupportedOperationException("ComponentGrids may not be added to a SubMenuBasedMenu!");
	}
	
	/**
	 * Not permitted for a SubMenuBasedMenu.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	protected final void setComponentGridWrap(boolean wrap) {
		throw new UnsupportedOperationException("ComponentGrids may not be added to a SubMenuBasedMenu!");
	}
	
	/**
	 * Not permitted for a SubMenuBasedMenu.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	protected final boolean setFocus(MenuItem focus) {
		throw new UnsupportedOperationException("SubMenuBasedMenus may not have a focus set!");
	}
	
	@Override
	public final void performAction(int action, Object parameter) {
		// nothing to see here, move along
	}
	
	@Override
	public void unloadResources() {
		super.unloadResources();
		if(submenu != null)
			submenu.unloadResources();
	}
	
}
