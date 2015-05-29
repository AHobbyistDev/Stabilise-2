//package com.stabilise.screen.menu.component;
//
//import com.stabilise.screen.menu.Menu;
//import com.stabilise.util.maths.Interpolation;
//
///**
// * The MenuItem class is an abstract class representing an item which is
// * capable of being placed in a menu.
// */
//public abstract class MenuItem {
//	
//	/** A reference to the Menu containing this MenuItem. */
//	protected Menu menu;
//	
//	/** The name of the MenuItem, for referencing purposes. */
//	public String name;
//	
//	/** The x-coordinate of the MenuItem, in pixels.
//	 * <p><b>Note:</b> it is strongly recommended against modifying this
//	 * explicitly if the MenuItem has a position set or is being interpolated. */
//	public int x;
//	/** The y-coordinate of the MenuItem, in pixels.
//	 * <p><b>Note:</b> it is strongly recommended against modifying this
//	 * explicitly if the MenuItem has a position set or is being interpolated. */
//	public int y;
//	
//	/** The position of the MenuItem. This is {@code null} be default. */
//	protected Position position = null;
//	
//	/** The width of the MenuItem, in pixels. */
//	public int width;
//	/** The height of the MenuItem, in pixels. */
//	public int height;
//	
//	/** {@code true} if the MenuItem is capable of being interacted with. */
//	protected boolean enabled;
//	
//	/** {@code true} if the item should be retained on a typical removal sweep
//	 * of the items - that is, (though it is protected and hence invisible to
//	 * this class,) {@link Menu#removeComponents(boolean)}. */
//	private boolean anchored;
//	
//	/** The item's controller. */
//	protected InterpolationController controller;
//	
//	
//	/**
//	 * Creates a new MenuItem.
//	 * 
//	 * @param menu The menu containing the MenuItem.
//	 * @param x The x-coordinate of the MenuItem, in pixels.
//	 * @param y The y-coordinate of the MenuItem, in pixels.
//	 */
//	public MenuItem(Menu menu, int x, int y) {
//		this(menu, x, y, 0, 0);
//	}
//	
//	/**
//	 * Creates a new MenuItem.
//	 * 
//	 * @param menu The menu containing the MenuItem.
//	 * @param x The x-coordinate of the MenuItem, in pixels.
//	 * @param y The y-coordinate of the MenuItem, in pixels.
//	 * @param width The width of the MenuItem, in pixels.
//	 * @param height The height of the MenuItem, in pixels.
//	 */
//	public MenuItem(Menu menu, int x, int y, int width, int height) {
//		this.menu = menu;
//		this.x = x;
//		this.y = y;
//		this.width = width;
//		this.height = height;
//		
//		enabled = true;
//		
//		// TODO: Temporary default until I implement more controller types
//		controller = new InterpolationController(this);
//	}
//	
//	/**
//	 * Updates the MenuItem.
//	 */
//	public void update() {
//		controller.update();
//	}
//	
//	/**
//	 * Moves the MenuItem from its current position to given position using the
//	 * specified interpolation method. If the item is already being
//	 * interpolated, the current interpolation will halt and be replaced by this
//	 * one.
//	 * 
//	 * @param position The position to interpolate to.
//	 * @param interpFunc The interpolation function to use.
//	 * @param ticks The total number of ticks over which the interpolation is
//	 * to elapse.
//	 * 
//	 * @return This MenuItem.
//	 * @throws NullPointerException if either {@code position} or {@code
//	 * interpFunc} are {@code null}.
//	 * @throws IllegalArgumentException if {@code ticks < 1}.
//	 */
//	public MenuItem moveToPosition(Position position, Interpolation interpFunc, int ticks) {
//		controller.toPosition(position, interpFunc, ticks);
//		return this;
//	}
//	
//	/** 
//	 * Draws the MenuItem.
//	 */
//	public abstract void render();
//	
//	/**
//	 * Rescales and repositions this MenuItem based on the given screen
//	 * dimensions.
//	 * 
//	 * @param width The width of the screen.
//	 * @param height The height of the screen.
//	 */
//	public void rescale(int width, int height) {
//		if(position != null) {
//			x = position.getX(width);
//			y = position.getY(height);
//		}
//	}
//	
//	/**
//	 * Checks for whether or not the supplied mouse coordinate are within
//	 * bounds of this MenuItem.
//	 * 
//	 * @param mouseX The x-coordinate of the mouse, in pixels.
//	 * @param mouseY The y-coordinate of the mouse, in pixels.
//	 * 
//	 * @return {@code true} if the mouse coordinates are within bounds of this
//	 * MenuItem; {@code false} if not.
//	 */
//	public abstract boolean isMouseInBounds(int mouseX, int mouseY);
//	
//	/**
//	 * Informs this MenuItem that it has become the focus of its parent menu.
//	 */
//	public abstract void focus();
//	
//	/**
//	 * Informs this MenuItem that it is no longer the focus of its parent menu.
//	 */
//	public abstract void unfocus();
//	
//	/**
//	 * Checks for whether or not this MenuItem may be focused on from being
//	 * hovered over by the mouse.
//	 * 
//	 * @return {@code true} if this MenuItem may be focused on as such.
//	 */
//	public abstract boolean canBeFocusedFromHover();
//	
//	/**
//	 * Checks for whether or not this MenuItem may be focused on from being
//	 * clicked on by the mouse.
//	 * 
//	 * @return {@code true} if this MenuItem may be focused on as such.
//	 */
//	public abstract boolean canBeFocusedFromClick();
//	
//	/**
//	 * Destroys this MenuItem, and unloads any resources it has loaded.
//	 */
//	public abstract void destroy();
//	
//	//--------------------==========--------------------
//	//-----------=====Getters and Setters=====----------
//	//--------------------==========--------------------
//	
//	/**
//	 * Sets this MenuItem's name.
//	 * 
//	 * @param The name.
//	 * 
//	 * @return This MenuItem.
//	 */
//	public final MenuItem setName(String name) {
//		this.name = name;
//		return this;
//	}
//	
//	/**
//	 * Checks for whether or not this MenuItem is "enabled" - that is, capable
//	 * of being interacted with.
//	 * 
//	 * @return {@code true} if this MenuItem is enabled; {@code false}
//	 * otherwise.
//	 */
//	public final boolean isEnabled() {
//		return enabled;
//	}
//	
//	/**
//	 * Enables or disables the MenuItem.
//	 * 
//	 * @param enabled Whether or not the MenuItem is to be capable of being
//	 * interacted with.
//	 * 
//	 * @return This MenuItem.
//	 */
//	public final MenuItem setEnabled(boolean enabled) {
//		this.enabled = enabled;
//		return this;
//	}
//	
//	/**
//	 * Checks for whether or not the menu item is "anchored". An anchored menu
//	 * item will typically not be removed from a menu when a sweep to remove
//	 * items from the menu is made.
//	 * 
//	 * @return {@code true} if the menu item is anchored; {@code false}
//	 * otherwise.
//	 */
//	public final boolean isAnchored() {
//		return anchored;
//	}
//	
//	/**
//	 * Anchors the MenuItem; {#link {@link #isAnchored()}} will henceforth
//	 * return {@code true}.
//	 * 
//	 * @return The MenuItem, for chaining operations.
//	 */
//	public final MenuItem anchor() {
//		this.anchored = true;
//		return this;
//	}
//	
//	/**
//	 * Gets the MenuItem's position.
//	 * 
//	 * @return The MenuItem's position, or one as if by
//	 * {@link #getAbsolutePosition()} if it lacks one.
//	 */
//	public Position getPosition() {
//		return position;
//	}
//	
//	/**
//	 * Gets the MenuItem's absolute position - that is, one without relative
//	 * components. The position returned is created as if by:
//	 * <pre>new Position(0f, 0f, x, y)</pre>
//	 * 
//	 * @return The MenuItem's absolute position.
//	 */
//	public Position getAbsolutePosition() {
//		return new Position(0f, 0f, x, y);
//	}
//	
//	/**
//	 * Sets the MenuItem's position. The MenuItem's coordinates will change
//	 * immediately based on the position, if it is non-null.
//	 * 
//	 * @param position The position.
//	 * 
//	 * @return The MenuItem, for chaining operations.
//	 */
//	public MenuItem setPosition(Position position) {
//		this.position = position;
//		if(position != null) {
//			x = position.getX();
//			y = position.getY();
//		}
//		return this;
//	}
//	
//}
