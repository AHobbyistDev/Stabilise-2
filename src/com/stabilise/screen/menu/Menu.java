package com.stabilise.screen.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.stabilise.input.Focusable;
import com.stabilise.input.InputManager;
import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.opengl.Sprite;
import com.stabilise.screen.menu.component.Button;
import com.stabilise.screen.menu.component.ComponentGrid;
import com.stabilise.screen.menu.component.Image;
import com.stabilise.screen.menu.component.MenuItem;
import com.stabilise.screen.menu.component.TextBox;
import com.stabilise.util.shape.AxisAlignedBoundingBox;

/**
 * This class is the base for all menus.
 */
public abstract class Menu implements Focusable {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Whether or not the keyboard is being used as the mode of menu
	 * navigation; a value of {@code false} indicates that the mouse is being
	 * used instead. */
	private static boolean keyboardMode = false;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** A reference to the InputManager, such that the menu may do more with
	 * inputs than simply receive events. */
	protected InputManager input;
	
	/** The items in the menu. */
	private List<MenuItem> items = new ArrayList<MenuItem>();
	/** The menu's component grid, which handles keyboard navigation. */
	private ComponentGrid componentGrid = null;
	/** The current item 'focused' on. */
	protected MenuItem focus = null;
	/** Whether or not a focusable item is being hovered over. */
	//private boolean itemHoveredOver = false;
	
	/** The scheduled events. */
	private List<ScheduledEvent> scheduledEvents = new LinkedList<ScheduledEvent>();
	
	
	/** Whether or not the menu is to be closed; this is {@code false} by
	 * default. */
	public boolean close = false;
	
	
	/**
	 * Creates a new Menu, which sets itself as the focus of the InputManager.
	 */
	public Menu() {
		this(true);
	}
	
	/**
	 * Creates a new Menu.
	 * 
	 * @param setSelfAsInputFocus Whether or not the menu should set itself as
	 * the focus of the input manager.
	 * 
	 * @see com.stabilise.input.InputManager
	 */
	public Menu(boolean setSelfAsInputFocus) {
		input = InputManager.get();
		
		if(setSelfAsInputFocus)
			input.setFocus(this);
		
		loadResources();
		
		// Provide a way of initially scaling the menu appropriately
		//rescale(screen.getWidth(), screen.getHeight());
	}
	
	/**
	 * Updates the menu and all of its contents.
	 * 
	 * <p>While all essential menu logic is contained within the default
	 * implementation, subclasses of {@code Menu} should extends this to
	 * perform any additional update operations as seen fit.
	 */
	public void update() {
		// Perform any scheduled events if applicable
		Iterator<ScheduledEvent> events = scheduledEvents.iterator();
		while(events.hasNext()) {
			if(events.next().update(this))
				events.remove();
		}
		
		//boolean oldHoveredOver = itemHoveredOver;
		//itemHoveredOver = false;
		boolean buttonHeldDown = input.isButtonDown(0) || input.isKeyDown(Keyboard.KEY_RETURN);
		
		for(MenuItem item : items) {
			// First, update the item
			item.update();
			
			// Now, let's deal with any focusing stuff
			if(!keyboardMode) {
				boolean isFocus = item == focus;
				boolean isButton = item instanceof Button;
				
				if(item.isEnabled() && item.isMouseInBounds(input.getMouseX(), input.getMouseY())) {
					//itemHoveredOver = true;
					
					if(buttonHeldDown) {
						if(isFocus && isButton) {
							((Button)item).setState(Button.State.PRESSED);
						}
					} else if(item.canBeFocusedFromHover()) {
						if(componentGrid != null)
							componentGrid.setSelectedComponent(item);
						if(!isFocus) {
							// This doesn't account for overlapping items - it is the responsibility of
							// UI-design and such to make sure that doesn't happen.
							setFocus(item);
						}
					}
				} else {
					// nothing?
				}
			}
		}
		
		// TODO: Why did I include this in the first place?
		//if(oldHoveredOver && !itemHoveredOver && componentGrid != null && !buttonHeldDown)
		//	setFocus(componentGrid.getSelectedComponent());
	}
	
	/**
	 * Rescales the Menu.
	 * 
	 * <p><b>Note:</b> this method is invoked after {@link #loadResources()}
	 * when the Menu is constructed, and on an invocation of {@link #update()}
	 * if {@link Screen#wasResized()} returns {@code true}.
	 * 
	 * @param width The new screen width.
	 * @param height The new screen height.
	 */
	protected void rescale(int width, int height) {
		for(MenuItem i : items) {
			i.rescale(width, height);
		}
	}
	
	/**
	 * Renders the contents of the menu.
	 */
	public void render() {
		for(MenuItem item : items)
			item.render();
	}
	
	/**
	 * Adds a MenuItem to the menu.
	 * 
	 * @param item The MenuItem.
	 * 
	 * @return The MenuItem, for chaining operations.
	 */
	protected MenuItem addMenuItem(MenuItem item) {
		items.add(item);
		return item;
	}
	
	/**
	 * Creates a Button with the specified parameters.
	 * 
	 * @param x The x-coordinate of the Button.
	 * @param y The y-coordinate of the Button.
	 * @param action The identifier for the action the Button's parent menu is
	 * to perform when the Button is clicked/pressed. A value of -1 should be
	 * used to indicate that the Button has no functionality.
	 * @param boundingBox The Button's bounding box.
	 */
	protected Button addButton(int x, int y, int action, AxisAlignedBoundingBox boundingBox) {
		return (Button)addMenuItem(new Button(this, x, y, action, boundingBox));
	}
	
	/**
	 * Creates and returns a TextBox with the specified parameters.
	 * 
	 * @param x The x coordinate of the TextBox.
	 * @param y The y coordinate of the TextBox.
	 * @param width The width of the TextBox.
	 * @param height The height of the TextBox.
	 * @param font The font to use to display text.
	 * @param style The font style.
	 * @param defaultText The text to display on the box by default.
	 * @param charLimit The character limit of the TextBox.
	 * @param text The initial value for the entered text.
	 */
	protected TextBox addTextBox(int x, int y, int width, int height, Font font, FontStyle style, String defaultText, int charLimit, String text) {
		return (TextBox)addMenuItem(new TextBox(this, x, y, width, height, font, style, defaultText, charLimit, text));
	}
	
	/**
	 * Creates an Image with the specified parameters.
	 * 
	 * @param x The x-coordinate of the Image.
	 * @param y The y-coordinate of the Image.
	 * @param sprite The image's sprite.
	 * 
	 * @return The image.
	 */
	protected Image addImage(int x, int y, Sprite sprite) {
		return (Image)addMenuItem(new Image(this, x, y, sprite));
	}
	
	/**
	 * Creates a ComponentGrid component which will default to selecting the
	 * leftmost item in the top row, or removes the current grid if the
	 * {@code grid} parameter is {@code null}.
	 * 
	 * @param grid The grid of MenuItems.
	 * 
	 * @throws NullPointerException as per
	 * {@link ComponentGrid#ComponentGrid(MenuItem[][]) new ComponentGrid(grid)}.
	 * @throws IllegalArgumentException as per
	 * {@link ComponentGrid#ComponentGrid(MenuItem[][]) new ComponentGrid(grid)}.
	 * 
	 * @see com.stabilise.screen.menu.component.ComponentGrid
	 */
	protected void setComponentGrid(MenuItem[][] grid) {
		if(grid == null) {
			componentGrid = null;
			return;
		}
		
		// TODO: Add any MenuItems passed through but have not yet been added
		// to the menu
		
		componentGrid = new ComponentGrid(grid);
		setFocus(componentGrid.getSelectedComponent());
	}
	
	/**
	 * Creates a MenuComponentGrid component.
	 * 
	 * @param grid The grid of MenuItems.
	 * @param initialCol The column of the MenuItem to initially select.
	 * @param initialRow The row of the MenuItem to initially select.
	 * 
	 * @throws NullPointerException if {@code grid} or any of its top-level
	 * elements (rows) are {@code null}.
	 * @throws IllegalArgumentException if {@code grid} or any of its top-level
	 * elements (rows) are empty, or if any row contains only {@code null}
	 * elements.
	 * @see com.stabilise.screen.menu.component.ComponentGrid
	 */
	protected void setComponentGrid(MenuItem[][] grid, int initialCol, int initialRow) {
		componentGrid = new ComponentGrid(grid, initialCol, initialRow);
		setFocus(componentGrid.getSelectedComponent());
	}
	
	/**
	 * Sets the wrap of the component grid, if it is non-null.
	 * 
	 * @param wrap Whether or not the component grid should use wrapping.
	 * 
	 * @see com.stabilise.screen.menu.component.ComponentGrid
	 */
	protected void setComponentGridWrap(boolean wrap) {
		if(componentGrid != null)
			componentGrid.wrap = wrap;
	}
	
	/**
	 * Sets the focus item as the item at the given coordinates of the
	 * component grid. If the component grid does not exist, the given
	 * coordinates are illegal, or there exists no enabled item at those
	 * coordinates, the focus will not be set.
	 * 
	 * @param col The column of the item to select (i.e. its x-coordinate).
	 * @param row The row of the item to select (i.e. its y-coordinate).
	 */
	protected void setFocus(int col, int row) {
		if(componentGrid == null)
			return;
		
		if(componentGrid.setSelectedComponent(col, row))
			setFocus(componentGrid.getSelectedComponent());
	}
	
	/**
	 * Sets a new focus, and informs the old focus (if it exists) and the new
	 * focus of this change.
	 * 
	 * @param focus The new menu focus.
	 * 
	 * @return {@code true} if the new focus was successfully set.
	 */
	protected boolean setFocus(MenuItem focus) {
		// We don't want to focus on items which aren't interactive...
		if(!focus.isEnabled())
			return false;
		
		if(this.focus == focus)
			return true;
		
		if(this.focus != null)
			this.focus.unfocus();
		
		this.focus = focus;
		
		if(focus instanceof TextBox) 
			((TextBox)focus).focus(input.getMouseX(), input.getMouseY());
		else
			focus.focus();
		
		// This is allowed, actually
		//if(componentGrid != null && !componentGrid.setSelectedComponent(focus))
		//	throw new RuntimeException("Focused on an object which isn't a member of the ComponentGrid. Be sure to assign all enabled menu components to the grid!");
		
		return true;
	}
	
	/**
	 * Gets the MenuItem that is currently being focused on.
	 * 
	 * @return The focus, or {@code null} if the focus has not been assigned.
	 */
	protected MenuItem getFocus() {
		return focus;
	}
	
	/**
	 * Gets a MenuItem by name.
	 * 
	 * <p>As MenuItems are stored as a List as opposed to a Map, it is
	 * recommended that this isn't overused, due to the linear nature of the
	 * search used.
	 * 
	 * @param name The name of the MenuItem.
	 * 
	 * @return The MenuItem by the given name, or {@code null} if a MenuItem
	 * by the given name does not exist.
	 */
	protected final MenuItem getComponentByName(String name) {
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).name == name)
				return items.get(i);
		}
		return null;
	}
	
	/**
	 * Removes all components from the menu. Note that if
	 * {@code removeAnchoredItems} is {@code false}, anchored items will remain
	 * in the menu. Any removed components will have their resources unloaded
	 * as per an invocation of {@link MenuItem#destroy()}.
	 * 
	 * @param removeAnchoredItems Whether or not items for which
	 * {@link MenuItem#isAnchored()} returns {@code true} should be removed.
	 */
	protected final void removeComponents(boolean removeAnchoredItems) {
		componentGrid = null;
		
		for(int i = 0; i < items.size();) {
			if(!removeAnchoredItems && items.get(i).isAnchored()) {
				i++;
			} else {
				items.get(i).destroy();
				items.remove(i);
			}
		}
		
		focus = null;
	}
	
	/**
	 * Schedules an event.
	 * 
	 * <p>The number of ticks after which the event is to occur is defined in
	 * the event's constructor, and the event itself must be defined in
	 * {@link ScheduledEvent#execute(Menu)}. The {@code menu} parameter passed
	 * to {@code execute} is this menu.
	 * 
	 * <p>A scheduled event should typically be implemented as such (replacing
	 * 100 with the desired number of ticks):
	 * 
	 * <pre>
	 * scheduleEvent(new ScheduledEvent(100) {
	 *     &#64;Override
	 *     protected void execute(Menu menu) {
	 *        // perform the event
	 *     }
	 * });</pre>
	 * 
	 * @param event The event.
	 */
	protected final void scheduleEvent(ScheduledEvent event) {
		scheduledEvents.add(event);
	}
	
	@Override
	public void handleButtonPress(int button, int x, int y) {
		keyboardMode = false;
		
		if(button == 0) {
			for(MenuItem item : items) {
				if(item.canBeFocusedFromClick() && item.isMouseInBounds(x, y)) {
					if(!setFocus(item))
						break;
					if(focus instanceof Button) {
						((Button)focus).setState(Button.State.PRESSED);
					} else if(focus instanceof TextBox) {
						((TextBox)focus).focus(x, y);
					}
					break;
				}
			}
		}
	}
	
	@Override
	public void handleButtonRelease(int button, int x, int y) {
		keyboardMode = false;
		
		if(button == 0 && focus != null && focus instanceof Button && !input.isKeyDown(Keyboard.KEY_RETURN)) {
			if(focus.isMouseInBounds(x, y))
				((Button)focus).setState(Button.State.OVER);
			else
				((Button)focus).setState(Button.State.OVER, false);
		}
	}
	
	@Override
	public void handleKeyPress(int key) {
		keyboardMode = true;
		
		if(componentGrid != null) {
			if(key == Keyboard.KEY_RETURN) {
				if(componentGrid.getSelectedComponent() instanceof Button)
					((Button)componentGrid.getSelectedComponent()).setState(Button.State.PRESSED);
			} else if(!(focus instanceof Button) || ((Button)focus).getState() != Button.State.PRESSED) {
				if(focus instanceof TextBox)
					((TextBox)focus).handleKeyPress(key);
				
				//if(itemHoveredOver)
				//	return;
				
				switch(key) {
					case Keyboard.KEY_UP:
						setFocus(componentGrid.scrollUp());
						break;
					case Keyboard.KEY_DOWN:
						setFocus(componentGrid.scrollDown());
						break;
					case Keyboard.KEY_LEFT:
						setFocus(componentGrid.scrollLeft());
						break;
					case Keyboard.KEY_RIGHT:
						setFocus(componentGrid.scrollRight());
						break;
					case Keyboard.KEY_TAB:
						setFocus(componentGrid.scroll());
						break;
				}
			}
		} else if(focus != null) {
			if(focus instanceof Button && key == Keyboard.KEY_RETURN) {
				((Button)focus).setState(Button.State.PRESSED);
			} else if(focus instanceof TextBox) {
				((TextBox)focus).handleKeyPress(key);
			}
		} else {
			for(MenuItem i : items) {
				if(i instanceof TextBox)
					setFocus(i);
			}
		}
	}
	
	@Override
	public void handleKeyRelease(int key) {
		if(key == Keyboard.KEY_RETURN && focus != null && focus instanceof Button)
			((Button)focus).setState(Button.State.OVER);
	}
	
	@Override
	public void handleMouseMove(int dx, int dy) {
		keyboardMode = false;
	}
	
	@Override
	public void handleMouseWheelScroll(int dScroll) {
		// nothing to see here, move along
	}
	
	/**
	 * Performs an action as specified by the {@code action} and, optionally,
	 * the {@code parameter} parameter(s).
	 * 
	 * <p>This is called by a component of the menu when it is 'activated'
	 * (e.g. a button, when it is clicked/pressed).
	 * 
	 * <p>Any subclass of Menu should implement this method in order to respond
	 * to component interactions in a manner deemed appropriate.
	 * 
	 * @param action The ID of the action. This should ideally be a static
	 * constant belonging to said subclass for easy internal referencing.
	 * @param parameter An optional extra parameter to send along with the
	 * action when more data will be required.
	 */
	public abstract void performAction(int action, Object parameter);
	
	/**
	 * Loads any resources to be held by the menu. This method is invoked upon
	 * Menu construction;.
	 */
	protected abstract void loadResources();
	
	/**
	 * Unloads any resources held by the menu. This method should be called
	 * when the menu falls out of use.
	 */
	public void unloadResources() {
		removeComponents(true);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * This class holds a scheduled event which is to execute after the defined
	 * number of ticks have elapsed.
	 */
	protected static abstract class ScheduledEvent {
		
		/** The number of ticks until the event is to occur. */
		private int ticks;
		
		
		/**
		 * Creates a new scheduled event.
		 * 
		 * @param ticks The number of ticks after which {@link #execute(Menu)}
		 * will be invoked.
		 * 
		 * @throws IllegalArgumentException Thrown if {@code ticks < 1}.
		 */
		protected ScheduledEvent(int ticks) {
			if(ticks < 1)
				throw new IllegalArgumentException("Can not schedule an event to occur in < 1 ticks!");
			
			this.ticks = ticks;
		}
		
		/**
		 * Updates the ScheduledEvent.
		 * 
		 * @param menu The menu.
		 * 
		 * @return {@code true} if the event has been executed; {@code false}
		 * if it has not.
		 */
		private boolean update(Menu menu) {
			if(--ticks == 0) {
				execute(menu);
				return true;
			}
			return false;
		}
		
		/**
		 * Executes the event.
		 * 
		 * <p>This is invoked automatically when the number of ticks defined in
		 * the constructor has elapsed.
		 * 
		 * @param menu A reference to the menu.
		 */
		protected abstract void execute(Menu menu);
		
	}
	
}
