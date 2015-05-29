//package com.stabilise.screen.menu2;
//
//import static com.badlogic.gdx.Input.*;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Input;
//import com.badlogic.gdx.InputProcessor;
//import com.stabilise.screen.menu.component.Button;
//import com.stabilise.screen.menu.component.ComponentGrid;
//import com.stabilise.screen.menu.component.MenuItem;
//import com.stabilise.screen.menu.component.TextBox;
//import com.stabilise.util.collect.LightLinkedList;
//
///**
// * This class is the base for all menus.
// */
//public abstract class Menu implements InputProcessor {
//	
//	//--------------------==========--------------------
//	//-----=====Static Constants and Variables=====-----
//	//--------------------==========--------------------
//	
//	/** Whether or not the keyboard is being used as the mode of menu
//	 * navigation; a value of {@code false} indicates that the mouse is being
//	 * used instead. */
//	private static boolean keyboardMode = false;
//	
//	//--------------------==========--------------------
//	//-------------=====Member Variables=====-----------
//	//--------------------==========--------------------
//	
//	private Input input;
//	
//	/** The items in the menu. */
//	private List<MenuItem> items = new ArrayList<MenuItem>();
//	/** The menu's component grid, which handles keyboard navigation. */
//	private ComponentGrid componentGrid = null;
//	/** The current item 'focused' on. */
//	protected MenuItem focus = null;
//	/** Whether or not a focusable item is being hovered over. */
//	//private boolean itemHoveredOver = false;
//	
//	/** The scheduled events. */
//	private List<ScheduledEvent> scheduledEvents = new LightLinkedList<>();
//	
//	
//	/** Whether or not the menu is to be closed; this is {@code false} by
//	 * default. */
//	public boolean close = false;
//	
//	
//	/**
//	 * Creates a new Menu.
//	 */
//	public Menu() {
//		input = Gdx.input;
//		input.setInputProcessor(this);
//		
//		loadResources();
//	}
//	
//	/**
//	 * Updates the menu and all of its contents.
//	 * 
//	 * <p>While all essential menu logic is contained within the default
//	 * implementation, subclasses of {@code Menu} should extends this to
//	 * perform any additional update operations as seen fit.
//	 */
//	public void update() {
//		// Perform any scheduled events if applicable
//		Iterator<ScheduledEvent> events = scheduledEvents.iterator();
//		while(events.hasNext()) {
//			if(events.next().update(this))
//				events.remove();
//		}
//		
//		//boolean oldHoveredOver = itemHoveredOver;
//		//itemHoveredOver = false;
//		boolean buttonHeldDown = input.isButtonPressed(0) || input.isKeyPressed(Keys.ENTER);
//		
//		for(MenuItem item : items) {
//			// First, update the item
//			item.update();
//			
//			// Now, let's deal with any focusing stuff
//			if(!keyboardMode) {
//				boolean isFocus = item == focus;
//				boolean isButton = item instanceof Button;
//				
//				if(item.isEnabled() && item.isMouseInBounds(input.getX(), input.getY())) {
//					//itemHoveredOver = true;
//					
//					if(buttonHeldDown) {
//						if(isFocus && isButton) {
//							((Button)item).setState(Button.State.PRESSED);
//						}
//					} else if(item.canBeFocusedFromHover()) {
//						if(componentGrid != null)
//							componentGrid.setSelectedComponent(item);
//						if(!isFocus) {
//							// This doesn't account for overlapping items - it is the responsibility of
//							// UI-design and such to make sure that doesn't happen.
//							setFocus(item);
//						}
//					}
//				} else {
//					// nothing?
//				}
//			}
//		}
//		
//		// TODO: Why did I include this in the first place?
//		//if(oldHoveredOver && !itemHoveredOver && componentGrid != null && !buttonHeldDown)
//		//	setFocus(componentGrid.getSelectedComponent());
//	}
//	
//	/**
//	 * Rescales the Menu in accordance with the screen dimensions.
//	 * 
//	 * @param width The screen width, in pixels.
//	 * @param height The screen height, in pixels.
//	 */
//	protected void resize(int width, int height) {
//		for(MenuItem i : items)
//			i.rescale(width, height);
//	}
//	
//	/**
//	 * Renders the contents of the menu.
//	 */
//	public void render() {
//		for(MenuItem item : items)
//			item.render();
//	}
//	
//	/**
//	 * Adds a MenuItem to the menu.
//	 * 
//	 * @param item The MenuItem.
//	 * 
//	 * @return The MenuItem, for chaining operations.
//	 */
//	protected MenuItem addMenuItem(MenuItem item) {
//		items.add(item);
//		return item;
//	}
//	
//	/**
//	 * Creates a ComponentGrid component which will default to selecting the
//	 * leftmost item in the top row, or removes the current grid if the
//	 * {@code grid} parameter is {@code null}.
//	 * 
//	 * @param grid The grid of MenuItems.
//	 * 
//	 * @throws NullPointerException as per
//	 * {@link ComponentGrid#ComponentGrid(MenuItem[][])
//	 * new ComponentGrid(grid)}.
//	 * @throws IllegalArgumentException as per
//	 * {@link ComponentGrid#ComponentGrid(MenuItem[][])
//	 * new ComponentGrid(grid)}.
//	 * 
//	 * @see com.stabilise.screen.menu.component.ComponentGrid
//	 */
//	protected void setComponentGrid(MenuItem[][] grid) {
//		if(grid == null) {
//			componentGrid = null;
//			return;
//		}
//		
//		// TODO: Add any MenuItems passed through but have not yet been added
//		// to the menu
//		
//		componentGrid = new ComponentGrid(grid);
//		setFocus(componentGrid.getSelectedComponent());
//	}
//	
//	/**
//	 * Creates a MenuComponentGrid component.
//	 * 
//	 * @param grid The grid of MenuItems.
//	 * @param initialCol The column of the MenuItem to initially select.
//	 * @param initialRow The row of the MenuItem to initially select.
//	 * 
//	 * @throws NullPointerException if {@code grid} or any of its top-level
//	 * elements (rows) are {@code null}.
//	 * @throws IllegalArgumentException if {@code grid} or any of its top-level
//	 * elements (rows) are empty, or if any row contains only {@code null}
//	 * elements.
//	 * @see com.stabilise.screen.menu.component.ComponentGrid
//	 */
//	protected void setComponentGrid(MenuItem[][] grid, int initialCol, int initialRow) {
//		componentGrid = new ComponentGrid(grid, initialCol, initialRow);
//		setFocus(componentGrid.getSelectedComponent());
//	}
//	
//	/**
//	 * Sets the wrap of the component grid, if it is non-null.
//	 * 
//	 * @param wrap Whether or not the component grid should use wrapping.
//	 * 
//	 * @see com.stabilise.screen.menu.component.ComponentGrid
//	 */
//	protected void setComponentGridWrap(boolean wrap) {
//		if(componentGrid != null)
//			componentGrid.wrap = wrap;
//	}
//	
//	/**
//	 * Sets the focus item as the item at the given coordinates of the
//	 * component grid. If the component grid does not exist, the given
//	 * coordinates are illegal, or there exists no enabled item at those
//	 * coordinates, the focus will not be set.
//	 * 
//	 * @param col The column of the item to select (i.e. its x-coordinate).
//	 * @param row The row of the item to select (i.e. its y-coordinate).
//	 */
//	protected void setFocus(int col, int row) {
//		if(componentGrid == null)
//			return;
//		
//		if(componentGrid.setSelectedComponent(col, row))
//			setFocus(componentGrid.getSelectedComponent());
//	}
//	
//	/**
//	 * Sets a new focus, and informs the old focus (if it exists) and the new
//	 * focus of this change.
//	 * 
//	 * @param focus The new menu focus.
//	 * 
//	 * @return {@code true} if the new focus was successfully set.
//	 */
//	protected boolean setFocus(MenuItem focus) {
//		// We don't want to focus on items which aren't interactive...
//		if(!focus.isEnabled())
//			return false;
//		
//		if(this.focus == focus)
//			return true;
//		
//		if(this.focus != null)
//			this.focus.unfocus();
//		
//		this.focus = focus;
//		
//		if(focus instanceof TextBox) 
//			((TextBox)focus).focus(input.getX(), input.getY());
//		else
//			focus.focus();
//		
//		// This is allowed, actually
//		//if(componentGrid != null && !componentGrid.setSelectedComponent(focus))
//		//	throw new RuntimeException("Focused on an object which isn't a member of the ComponentGrid. Be sure to assign all enabled menu components to the grid!");
//		
//		return true;
//	}
//	
//	/**
//	 * Gets the MenuItem that is currently being focused on.
//	 * 
//	 * @return The focus, or {@code null} if the focus has not been assigned.
//	 */
//	protected MenuItem getFocus() {
//		return focus;
//	}
//	
//	/**
//	 * Gets a MenuItem by name.
//	 * 
//	 * <p>As MenuItems are stored as a List as opposed to a Map, it is
//	 * recommended that this isn't overused, due to the linear nature of the
//	 * search used.
//	 * 
//	 * @param name The name of the MenuItem.
//	 * 
//	 * @return The MenuItem by the given name, or {@code null} if a MenuItem
//	 * by the given name does not exist.
//	 */
//	protected final MenuItem getComponentByName(String name) {
//		for(int i = 0; i < items.size(); i++) {
//			if(items.get(i).name == name)
//				return items.get(i);
//		}
//		return null;
//	}
//	
//	/**
//	 * Removes all components from the menu. Note that if
//	 * {@code removeAnchoredItems} is {@code false}, anchored items will remain
//	 * in the menu. Any removed components will have their resources unloaded
//	 * as per an invocation of {@link MenuItem#destroy()}.
//	 * 
//	 * @param removeAnchoredItems Whether or not items for which
//	 * {@link MenuItem#isAnchored()} returns {@code true} should be removed.
//	 */
//	protected final void removeComponents(boolean removeAnchoredItems) {
//		componentGrid = null;
//		
//		for(int i = 0; i < items.size();) {
//			if(!removeAnchoredItems && items.get(i).isAnchored()) {
//				i++;
//			} else {
//				items.get(i).destroy();
//				items.remove(i);
//			}
//		}
//		
//		focus = null;
//	}
//	
//	/**
//	 * Schedules an event.
//	 * 
//	 * <p>The number of ticks after which the event is to occur is defined in
//	 * the event's constructor, and the event itself must be defined in
//	 * {@link ScheduledEvent#execute(Menu)}. The {@code menu} parameter passed
//	 * to {@code execute} is this menu.
//	 * 
//	 * <p>A scheduled event should typically be implemented as such (replacing
//	 * 100 with the desired number of ticks):
//	 * 
//	 * <pre>
//	 * scheduleEvent(new ScheduledEvent(100) {
//	 *     &#64;Override
//	 *     protected void execute(Menu menu) {
//	 *        // perform the event
//	 *     }
//	 * });</pre>
//	 * 
//	 * @param event The event.
//	 */
//	protected final void scheduleEvent(ScheduledEvent event) {
//		scheduledEvents.add(event);
//	}
//	
//	@Override
//	public boolean keyDown(int keycode) {
//		keyboardMode = true;
//		
//		if(componentGrid != null) {
//			if(keycode == Keys.ENTER) {
//				if(componentGrid.getSelectedComponent() instanceof Button)
//					((Button)componentGrid.getSelectedComponent()).setState(Button.State.PRESSED);
//			} else if(!(focus instanceof Button) || ((Button)focus).getState() != Button.State.PRESSED) {
//				if(focus instanceof TextBox)
//					((TextBox)focus).handleKeyPress(keycode);
//				
//				//if(itemHoveredOver)
//				//	return;
//				
//				switch(keycode) {
//					case Keys.UP:
//						setFocus(componentGrid.scrollUp());
//						break;
//					case Keys.DOWN:
//						setFocus(componentGrid.scrollDown());
//						break;
//					case Keys.LEFT:
//						setFocus(componentGrid.scrollLeft());
//						break;
//					case Keys.RIGHT:
//						setFocus(componentGrid.scrollRight());
//						break;
//					case Keys.TAB:
//						setFocus(componentGrid.scroll());
//						break;
//				}
//			}
//		} else if(focus != null) {
//			if(focus instanceof Button && keycode == Keys.ENTER)
//				((Button)focus).setState(Button.State.PRESSED);
//			else if(focus instanceof TextBox)
//				((TextBox)focus).handleKeyPress(keycode);
//		} else {
//			for(MenuItem i : items)
//				if(i instanceof TextBox)
//					setFocus(i);
//		}
//		return true;
//	}
//	
//	@Override
//	public boolean keyUp(int keycode) {
//		if(keycode == Keys.ENTER && focus != null && focus instanceof Button)
//			((Button)focus).setState(Button.State.OVER);
//		return true;
//	}
//	
//	@Override
//	public boolean keyTyped(char character) {
//		return false;
//	}
//	
//	@Override
//	public boolean touchDown(int x, int y, int pointer, int button) {
//		keyboardMode = false;
//		
//		if(button == 0) {
//			for(MenuItem item : items) {
//				if(item.canBeFocusedFromClick() && item.isMouseInBounds(x, y)) {
//					if(!setFocus(item))
//						break;
//					if(focus instanceof Button)
//						((Button)focus).setState(Button.State.PRESSED);
//					else if(focus instanceof TextBox)
//						((TextBox)focus).focus(x, y);
//					break;
//				}
//			}
//		}
//		return false;
//	}
//	
//	@Override
//	public boolean touchUp(int x, int y, int pointer, int button) {
//		keyboardMode = false;
//		
//		if(button == 0 && focus != null && focus instanceof Button && !input.isKeyPressed(Keys.ENTER)) {
//			if(focus.isMouseInBounds(x, y))
//				((Button)focus).setState(Button.State.OVER);
//			else
//				((Button)focus).setState(Button.State.OVER, false);
//		}
//		return false;
//	}
//	
//	@Override
//	public boolean touchDragged(int screenX, int screenY, int pointer) {
//		return false; // ignore
//	}
//	
//	@Override
//	public boolean mouseMoved(int screenX, int screenY) {
//		keyboardMode = false;
//		return false;
//	}
//	
//	@Override
//	public boolean scrolled(int amount) {
//		return false; // ignore scrolls
//	}
//	
//	/**
//	 * Performs an action as specified by the {@code action} and, optionally,
//	 * the {@code parameter} parameter(s).
//	 * 
//	 * <p>This is called by a component of the menu when it is 'activated'
//	 * (e.g. a button, when it is clicked/pressed).
//	 * 
//	 * <p>Any subclass of Menu should implement this method in order to respond
//	 * to component interactions in a manner deemed appropriate.
//	 * 
//	 * @param action The ID of the action. This should ideally be a static
//	 * constant belonging to said subclass for easy internal referencing.
//	 * @param parameter An optional extra parameter to send along with the
//	 * action when more data will be required.
//	 */
//	public abstract void performAction(int action, Object parameter);
//	
//	/**
//	 * Loads any resources to be held by the menu. This method is invoked upon
//	 * Menu construction;.
//	 */
//	protected abstract void loadResources();
//	
//	/**
//	 * Unloads any resources held by the menu. This method should be called
//	 * when the menu falls out of use.
//	 */
//	public void unloadResources() {
//		removeComponents(true);
//	}
//	
//	//--------------------==========--------------------
//	//-------------=====Nested Classes=====-------------
//	//--------------------==========--------------------
//	
//	/**
//	 * This class holds a scheduled event which is to execute after the defined
//	 * number of ticks have elapsed.
//	 */
//	protected static abstract class ScheduledEvent {
//		
//		/** The number of ticks until the event is to occur. */
//		private int ticks;
//		
//		
//		/**
//		 * Creates a new scheduled event.
//		 * 
//		 * @param ticks The number of ticks after which {@link #execute(Menu)}
//		 * will be invoked.
//		 * 
//		 * @throws IllegalArgumentException Thrown if {@code ticks < 1}.
//		 */
//		protected ScheduledEvent(int ticks) {
//			if(ticks < 1)
//				throw new IllegalArgumentException("Can not schedule an event to occur in < 1 ticks!");
//			
//			this.ticks = ticks;
//		}
//		
//		/**
//		 * Updates the ScheduledEvent.
//		 * 
//		 * @param menu The menu.
//		 * 
//		 * @return {@code true} if the event has been executed; {@code false}
//		 * if it has not.
//		 */
//		private boolean update(Menu menu) {
//			if(--ticks == 0) {
//				execute(menu);
//				return true;
//			}
//			return false;
//		}
//		
//		/**
//		 * Executes the event.
//		 * 
//		 * <p>This is invoked automatically when the number of ticks defined in
//		 * the constructor has elapsed.
//		 * 
//		 * @param menu A reference to the menu.
//		 */
//		protected abstract void execute(Menu menu);
//		
//	}
//	
//}
