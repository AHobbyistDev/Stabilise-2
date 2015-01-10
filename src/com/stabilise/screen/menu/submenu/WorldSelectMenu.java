package com.stabilise.screen.menu.submenu;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.stabilise.character.CharacterData;
import com.stabilise.core.Application;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.EntityPlayer;
import com.stabilise.screen.menu.SubMenuBasedMenu;
import com.stabilise.screen.menu.component.Button;
import com.stabilise.screen.menu.component.MenuItem;
import com.stabilise.screen.menu.component.TextBox;
import com.stabilise.screen.menu.customcomponents.MenuButton;
import com.stabilise.screen.menu.customcomponents.WorldListButton;
import com.stabilise.util.ArrayUtil;
import com.stabilise.util.Colour;
import com.stabilise.util.Log;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.StringUtil;
import com.stabilise.util.concurrent.Task;
import com.stabilise.util.concurrent.TaskThread;
import com.stabilise.util.concurrent.TaskTracker;
import com.stabilise.util.maths.Point;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.GameWorld;
import com.stabilise.world.BaseWorld;
import com.stabilise.world.IWorld;
import com.stabilise.world.SingleplayerWorld;
import com.stabilise.world.WorldInfo;

/**
 * The world selection menu.
 */
public class WorldSelectMenu extends SubMenu {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The value for the {@code action} variable of a WorldSelectMenu
	 * instance which indicates that the WorldSelectMenu is to be exited. */
	public static final int ACTION_EXIT = getActionID();
	
	private static final int ACTION_CANCEL = 0;
	private static final int ACTION_SELECT_WORLD = 1;
	private static final int ACTION_RENAME_WORLD = 2;
	private static final int ACTION_DELETE_WORLD = 3;
	private static final int ACTION_CREATE_WORLD = 4;
	private static final int ACTION_DELETE_WORLD_YES = 5;
	private static final int ACTION_DELETE_WORLD_NO = 6;
	private static final int ACTION_BACK = 7;
	
	/** The number of worlds which may be displayed. */
	private static final int NUM_WORLDS = 6;
	
	/** The texture's dimensions. */
	private static final Point GUI_TEXTURE_DIMENSIONS = new Point(256, 128);
	
	/** The main panel's origin point. */
	private static final Point GUI_TC_MAIN_ORIGIN = new Point(0, 0);
	/** The main panel's dimensions. */
	private static final Point GUI_TC_MAIN_DIMENSIONS = new Point(64, 96);
	/** The world info panel's origin point. */
	private static final Point GUI_TC_PANEL_ORIGIN = new Point(64, 0);
	/** The world info panel's dimensions. */
	private static final Point GUI_TC_PANEL_DIMENSIONS = new Point(64, 86);
	/** The panel's text's origin point. */
	private static final Point GUI_TC_TEXT_ORIGIN = new Point(0, 96);
	/** The panel's text's dimensions. */
	private static final Point GUI_TC_TEXT_DIMENSIONS = new Point(102, 9);
	/** The filled normal button's origin point. */
	private static final Point GUI_TC_FILLED_NORM_BUTTON_ORIGIN = new Point(128, 0);
	/** The filled normal button's dimensions. */
	private static final Point GUI_TC_FILLED_NORM_BUTTON_DIMENSIONS = new Point(61, 13);
	/** The filled normal button's origin point. */
	private static final Point GUI_TC_FILLED_HOVER_BUTTON_ORIGIN = new Point(128, 13);
	/** The filled normal button's dimensions. */
	private static final Point GUI_TC_FILLED_HOVER_BUTTON_DIMENSIONS = new Point(61, 13);
	/** The filled normal button's origin point. */
	private static final Point GUI_TC_FILLED_SELECTED_BUTTON_ORIGIN = new Point(128, 26);
	/** The filled normal button's dimensions. */
	private static final Point GUI_TC_FILLED_SELECTED_BUTTON_DIMENSIONS = new Point(61, 13);
	/** The filled normal button's origin point. */
	private static final Point GUI_TC_EMPTY_NORM_BUTTON_ORIGIN = new Point(128, 39);
	/** The filled normal button's dimensions. */
	private static final Point GUI_TC_EMPTY_NORM_BUTTON_DIMENSIONS = new Point(61, 13);
	/** The filled normal button's origin point. */
	private static final Point GUI_TC_EMPTY_HOVER_BUTTON_ORIGIN = new Point(128, 52);
	/** The filled normal button's dimensions. */
	private static final Point GUI_TC_EMPTY_HOVER_BUTTON_DIMENSIONS = new Point(61, 13);
	/** The filled normal button's origin point. */
	private static final Point GUI_TC_EMPTY_SELECTED_BUTTON_ORIGIN = new Point(128, 65);
	/** The filled normal button's dimensions. */
	private static final Point GUI_TC_EMPTY_SELECTED_BUTTON_DIMENSIONS = new Point(61, 13);
	
	/** A list button's dimensions. */
	private static final Point GUI_BUTTON_DIMENSIONS = new Point(61, 11);
	/** A list button's sprite's location, relative to the button. */
	private static final Point GUI_BUTTON_SPRITE_LOCATION = new Point(0, -1);
	/** A list button's sprite's base vertex data. */
	private static final Point GUI_BUTTON_SPRITE_DIMENSIONS = new Point(61, 13);
	
	
	/** The number of pixels padding at the top of the main GUI screen. */
	private static final int GUI_MAIN_PADDING_TOP = 14;
	/** The number of pixels padidng at the side of the main GUI screen. */
	private static final int GUI_MAIN_PADDING_SIDE = 4;
	
	/** The number of pixels padding at the top of the GUI panel. */
	private static final int GUI_PANEL_PADDING_TOP = 2;
	/** The number of pixels padding at the bottom of the GUI panel. */
	private static final int GUI_PANEL_PADDING_BOTTOM = 2;
	/** The number of pixels padding at the side of the GUI panel. */
	private static final int GUI_PANEL_PADDING_SIDE = 2;
	
	/** The location of the back button. */
	private static final Point GUI_BUTTON_BACK_LOCATION = new Point(4,2);
	/** The dimensions of the back button. */
	private static final Point GUI_BUTTON_BACK_DIMENSIONS = new Point(56,12);
	
	/** The title text's additional relative scaling. */
	private static final float GUI_TEXT_SCALING = 0.5f;
	
	/** The template font style to use for the name of a world. */
	private static final FontStyle STYLE_WORLD_NAME = new FontStyle(3, Colour.BLACK, FontStyle.Alignment.CENTRE, 1, 0);
	/** The template font style to use for a world's seed. */
	private static final FontStyle STYLE_WORLD_SEED = new FontStyle(2, Colour.BLACK, FontStyle.Alignment.CENTRE, 1, 2);
	
	/** The padding between elements in the create world menu. */
	private static final int GUI_CREATE_WORLD_PADDING = 8;
	
	/** The scaled GUI dimensions. */
	private static final Point GUI_DIMENSIONS = new Point(1280, 640);
	/** The GUI scale. TODO: Temporary? */
	private static final float GUI_SCALE = (float)GUI_DIMENSIONS.getX() / GUI_TEXTURE_DIMENSIONS.getX();
	
	
	/** States. */
	private static enum State {
		LIST, SELECT, CREATE, DELETE_CONFIRM;
	}
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** The current state. */
	private State state;
	
	/** The loaded worlds. */
	private WorldInfo[] worlds;
	/** The currently selected world. */
	private int selectedWorld;
	
	/** The effective (0,0) for the menu's components, for relative placement. */
	private Point origin;
	
	/** The GUI scaling. */
	private float guiScale;
	
	/** The GUI sprite batch. */
	private SpriteBatch gui;
	/** The texture coordinates for the main part of the GUI. */
	private float[] guiMainTC;
	/** The location for the main part of the GUI. */
	private Point guiMainLocation;
	/** The dimensions for the main part of the GUI. */
	private Point guiMainDimensions;
	/** The texture coordinates for the GUI panel. */
	private float[] guiPanelTC;
	/** The location for the GUI panel. */
	private Point guiPanelLocation;
	/** The dimensions for the GUI panel. */
	private Point guiPanelDimensions;
	/** The panel's top padding. */
	private int guiPanelPaddingTop;
	/** The panel's bottom padding. */
	@SuppressWarnings("unused")
	private int guiPanelPaddingBottom;
	/** The panel's side padding. */
	private int guiPanelPaddingSide;
	/** The texture coordinates for the GUI header text. */
	private float[] guiTextTC;
	/** The location for the GUI header text. */
	private Point guiTextLocation;
	/** The dimensions for the GUI header text. */
	private Point guiTextDimensions;
	/** The location for the delete world confirmation text. */
	private Point guiDeleteWorldConfirmationLocation;
	/** The padding between elements in the create world screen. */
	private int guiCreateWorldPadding;
	
	/** The first font. */
	private Font font1;
	/** The second font. */
	private Font font2;
	/** The style of the font to use for the world name text. */
	private FontStyle styleWorldName;
	/** The point at which the world name text will be placed. */
	private Point guiSelectWorldNameLocation;
	/** The world name text. */
	private String selectedWorldName;
	/** The style of the font to use for the world seed text. */
	private FontStyle styleWorldInfo;
	/** The point at which the world seed text will be placed. */
	private Point guiSelectWorldInfoLocation;
	/** The world info text. */
	private String[] selectedWorldInfo;
	
	/** The world name header's location. */
	private Point worldNameHeaderLocation;
	/** The world name input textbox. */
	private TextBox worldNameTextBox;
	/** The world name input textbox's background. */
	private Rectangle worldNameTextboxBackground;
	/** The world seed header's location. */
	private Point worldSeedHeaderLocation;
	/** The world seed input textbox. */
	private TextBox worldSeedTextBox;
	/** The world seed input textbox's background. */
	private Rectangle worldSeedTextboxBackground;
	
	/** The buttons for the world list. */
	private WorldListButton[] buttons;
	
	// Stuff for preparing a world
	/** Whether or not a world is being loaded. */
	private boolean loadingWorld = false;
	/** The world loader. */
	private TaskThread worldLoader;
	/** The world. */
	private ClientWorld<SingleplayerWorld> world;
	
	
	/**
	 * Creates a new WorldSelectMenu.
	 * 
	 * @param menu The menu's parent menu.
	 */
	public WorldSelectMenu(SubMenuBasedMenu menu) {
		super(menu);
	}
	
	/**
	 * Creates a new WorldSelectMenu.
	 * 
	 * @param menu The menu's parent menu.
	 * @param parameter A parameter.
	 */
	public WorldSelectMenu(SubMenuBasedMenu menu, Object parameter) {
		super(menu, parameter);
	}
	
	@Override
	protected void loadResources() {
		loadWorlds();
		
		selectedWorld = -1;
		
		origin = new Point();
		guiScale = GUI_SCALE;		// TODO: temporary initial assignment
		guiMainLocation = new Point();
		guiMainDimensions = new Point();
		guiPanelLocation = new Point();
		guiPanelDimensions = new Point();
		guiTextLocation = new Point();
		guiTextDimensions = new Point();
		guiDeleteWorldConfirmationLocation = new Point();
		guiSelectWorldNameLocation = new Point();
		guiSelectWorldInfoLocation = new Point();
		
		gui = new SpriteBatch("worldselect", this);
		gui.filter(Texture.NEAREST);
		//gui.tint(Colour.MAGENTA, 0.3f);		// for fun
		
		// Tedious texcoords setting up
		guiMainTC = SpriteBatch.getTextureData(GUI_TC_MAIN_ORIGIN, GUI_TC_MAIN_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		guiPanelTC = SpriteBatch.getTextureData(GUI_TC_PANEL_ORIGIN, GUI_TC_PANEL_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		guiTextTC = SpriteBatch.getTextureData(GUI_TC_TEXT_ORIGIN, GUI_TC_TEXT_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		float[] guiFilledNormTC = SpriteBatch.getTextureData(GUI_TC_FILLED_NORM_BUTTON_ORIGIN, GUI_TC_FILLED_NORM_BUTTON_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		float[] guiFilledHoverTC = SpriteBatch.getTextureData(GUI_TC_FILLED_HOVER_BUTTON_ORIGIN, GUI_TC_FILLED_HOVER_BUTTON_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		float[] guiFilledSelectedTC = SpriteBatch.getTextureData(GUI_TC_FILLED_SELECTED_BUTTON_ORIGIN, GUI_TC_FILLED_SELECTED_BUTTON_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		float[] guiEmptyNormTC = SpriteBatch.getTextureData(GUI_TC_EMPTY_NORM_BUTTON_ORIGIN, GUI_TC_EMPTY_NORM_BUTTON_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		float[] guiEmptyHoverTC = SpriteBatch.getTextureData(GUI_TC_EMPTY_HOVER_BUTTON_ORIGIN, GUI_TC_EMPTY_HOVER_BUTTON_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		float[] guiEmptySelectedTC = SpriteBatch.getTextureData(GUI_TC_EMPTY_SELECTED_BUTTON_ORIGIN, GUI_TC_EMPTY_SELECTED_BUTTON_DIMENSIONS, GUI_TEXTURE_DIMENSIONS);
		
		// Create the list of worlds
		buttons = new WorldListButton[NUM_WORLDS];
		for(int i = 0; i < NUM_WORLDS; i++) {
			buttons[i] = new WorldListButton(this, 0, 0, worlds[i],
					GUI_BUTTON_DIMENSIONS, gui, GUI_BUTTON_SPRITE_LOCATION, GUI_BUTTON_SPRITE_DIMENSIONS, 1.0f,
					guiFilledNormTC, guiFilledHoverTC, guiFilledSelectedTC,
					guiEmptyNormTC, guiEmptyHoverTC, guiEmptySelectedTC);
			buttons[i].anchor();			// For convenience wiping
			addMenuItem(buttons[i]);
		}
		
		MenuButton backButton = new MenuButton(this,0,0,0,0,ACTION_BACK,"Back",16,false,false);
		backButton.setName("backButton");
		backButton.anchor();
		addMenuItem(backButton);
		
		worldNameTextboxBackground = new Rectangle();
		worldNameTextboxBackground.fill(new Colour(0xEEEEEE));
		worldSeedTextboxBackground = new Rectangle();
		worldSeedTextboxBackground.fill(new Colour(0xEEEEEE));
		
		font1 = new Font("sheets/font1", this);
		font2 = new Font("sheets/font2", this);
		
		setState(State.LIST, 0);
	}
	
	/**
	 * Loads the worlds.
	 */
	private void loadWorlds() {
		worlds = BaseWorld.getWorldsList();
		worlds = ArrayUtil.setMinArrayLength(worlds, NUM_WORLDS);
	}
	
	/**
	 * Sets the menu's state.
	 * 
	 * @param state The state.
	 * @param selectedWorld The currently selected world.
	 */
	private void setState(State state, int selectedWorld) {
		if((this.state == state && selectedWorld == this.selectedWorld) || selectedWorld == -1)
			return;
		
		this.state = state;
		this.selectedWorld = selectedWorld;
		
		// remove all components but the buttons
		removeComponents(false);
		
		// reset all of the buttons
		for(int i = 0; i < NUM_WORLDS; i++) {
			buttons[i].selected = false;
			buttons[i].setState(Button.State.OFF);
			buttons[i].setEnabled(!(i == selectedWorld && state != State.LIST));
			//buttons[i].setEnabled(state == State.LIST);
		}
		
		switch(state) {
			case LIST:
				// Convert to a 2D array for componentGrid applicability
				MenuItem[][] buttonList = new MenuItem[NUM_WORLDS+1][1];
				for(int i = 0; i < NUM_WORLDS; i++)
					buttonList[i][0] = buttons[i];
				buttonList[NUM_WORLDS][0] = getComponentByName("backButton");
				setComponentGrid(buttonList, 0, selectedWorld);
				break;
			case SELECT:
				buttons[selectedWorld].selected = true;
				//refreshDisplayText();  - handled in rescale() for now
				setComponentGrid(new MenuItem[][] {	//TODO: hardcoded button width and height
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_SELECT_WORLD,"Select World",16,false,false)).setName("selectWorldButton") },
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_DELETE_WORLD,"Delete World",16,false,false)).setName("deleteWorldButton") },
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_CANCEL,"Cancel",16,false,false)).setName("cancelButton") }
				});
				break;
			case CREATE:
				buttons[selectedWorld].selected = true;
				worldNameTextBox = new TextBox(this,0,0,300,30,font2,styleWorldInfo,"",19,"");	// TODO: temporary cap on size
				addMenuItem(worldNameTextBox);
				worldSeedTextBox = new TextBox(this,0,0,300,30,font2,styleWorldInfo,"",27,"");	// TODO: temporary cap on size
				addMenuItem(worldSeedTextBox);
				
				setComponentGrid(new MenuItem[][] {
						{ worldNameTextBox },
						{ worldSeedTextBox },
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_CREATE_WORLD,"Create World",16,false,false)).setName("createWorldButton").setEnabled(false) },
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_CANCEL,"Cancel",16,false,false)).setName("cancelButton") },
				});
				
				setFocus(worldNameTextBox);
				break;
			case DELETE_CONFIRM:
				buttons[selectedWorld].selected = true;
				setComponentGrid(new MenuItem[][] {	//TODO: hardcoded button width and height
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_DELETE_WORLD_YES,"Yes",16,false,false)).setName("deleteWorldYesButton") },
						{ addMenuItem(new MenuButton(this,0,0,300,48,ACTION_DELETE_WORLD_NO,"No",16,false,false)).setName("deleteWorldNoButton") }
				});
				break;
		}
		
		// TODO: Crude way to set locations of new components, as it also rescales
		// pre-existing components
		rescale();
	}
	
	@Override
	protected void rescale(int width, int height) {
		super.rescale(width, height);
		rescale();
	}
	
	/**
	 * Rescales the menu.
	 */
	private void rescale() {
		rescale(guiScale);
	}
	
	/**
	 * Rescales the menu.
	 * 
	 * @param scale The new GUI scale.
	 */
	private void rescale(float scale) {
		guiScale = scale;
		
		// Storing these as floats temporarily for possible floating point precision
		float mainX = GUI_TC_MAIN_DIMENSIONS.getX() * guiScale;
		float mainY = GUI_TC_MAIN_DIMENSIONS.getY() * guiScale;
		
		//origin.setLocation((int)(screen.getCentreX() - mainX), (int)((screen.getHeight() - mainY) / 2));
		
		guiMainLocation.setLocation(origin.getX(), origin.getY());
		guiMainDimensions.setLocation((int)mainX, (int)mainY);
		
		guiPanelLocation.setLocation(origin.getX() + guiMainDimensions.getX(), origin.getY());
		guiPanelDimensions.setLocation((int)(GUI_TC_PANEL_DIMENSIONS.getX() * guiScale), (int)(GUI_TC_PANEL_DIMENSIONS.getY() * guiScale));
		guiPanelPaddingTop = (int)(GUI_PANEL_PADDING_TOP * guiScale);
		guiPanelPaddingBottom = (int)(GUI_PANEL_PADDING_BOTTOM * guiScale);
		guiPanelPaddingSide = (int)(GUI_PANEL_PADDING_SIDE * guiScale);
		
		guiTextDimensions.setLocation((int)(GUI_TC_TEXT_DIMENSIONS.getX() * guiScale * GUI_TEXT_SCALING), (int)(GUI_TC_TEXT_DIMENSIONS.getY() * guiScale * GUI_TEXT_SCALING));
		guiTextLocation.setLocation(guiMainLocation.getX() + (guiMainDimensions.getX() - guiTextDimensions.getX()) / 2,
			(guiMainLocation.getY() + guiMainDimensions.getY() - (int)((GUI_MAIN_PADDING_TOP * guiScale + guiTextDimensions.getY()) / 2)));
		
		int buttonX = (int)(origin.getX() + GUI_MAIN_PADDING_SIDE * guiScale);
		int buttonY;
		for(int i = 0; i < NUM_WORLDS; i++) {
			buttonY = (int) (origin.getY() + guiMainDimensions.getY() - GUI_MAIN_PADDING_TOP * guiScale - (i+1)*GUI_BUTTON_DIMENSIONS.getY()*guiScale);
			buttons[i].x = buttonX;
			buttons[i].y = buttonY;
			buttons[i].setScale(guiScale);
		}
		
		MenuButton backButton = (MenuButton)getComponentByName("backButton");
		backButton.x = origin.getX() + (int)(GUI_BUTTON_BACK_LOCATION.getX() * guiScale);
		backButton.y = origin.getY() + (int)(GUI_BUTTON_BACK_LOCATION.getY() * guiScale);
		backButton.setSize((int)(GUI_BUTTON_BACK_DIMENSIONS.getX() * scale), (int)(GUI_BUTTON_BACK_DIMENSIONS.getY() * scale));
		
		styleWorldName = new FontStyle((int)(STYLE_WORLD_NAME.size * guiScale),
				STYLE_WORLD_NAME.colour,
				STYLE_WORLD_NAME.alignment,
				STYLE_WORLD_NAME.kerning,
				STYLE_WORLD_NAME.verticalKerning
		);
		styleWorldInfo = new FontStyle((int)(STYLE_WORLD_SEED.size * guiScale),
				STYLE_WORLD_SEED.colour,
				STYLE_WORLD_SEED.alignment,
				STYLE_WORLD_SEED.kerning,
				STYLE_WORLD_SEED.verticalKerning
		);
		
		if(state == State.CREATE) {
			guiCreateWorldPadding = (int)(GUI_CREATE_WORLD_PADDING * guiScale);
			
			int displayX = guiPanelLocation.getX() + guiPanelPaddingSide;
			int displayY = guiPanelLocation.getY() + guiPanelDimensions.getY() - guiPanelPaddingTop - styleWorldInfo.size;
			int centredX = guiPanelLocation.getX() + guiPanelDimensions.getX() / 2;
			int textBoxWidth = guiPanelDimensions.getX() - guiPanelPaddingSide*2;
			
			// "World name:" text
			
			worldNameHeaderLocation = new Point(centredX, displayY);
			
			displayY -= styleWorldInfo.size + guiCreateWorldPadding;
			
			// world name textbox & background
			worldNameTextboxBackground.x = displayX;
			worldNameTextboxBackground.y = displayY;
			worldNameTextboxBackground.setSize(textBoxWidth, styleWorldInfo.size + guiCreateWorldPadding/2);
			
			worldNameTextBox.x = centredX;
			worldNameTextBox.y = displayY + guiCreateWorldPadding / 2 - styleWorldInfo.size;
			worldNameTextBox.width = textBoxWidth;
			
			displayY -= styleWorldInfo.size + guiCreateWorldPadding;
			
			// "World seed:" text
			
			worldSeedHeaderLocation = new Point(centredX, displayY);
			
			displayY -= styleWorldInfo.size + guiCreateWorldPadding;
			
			// world seed textbox & background
			
			worldSeedTextboxBackground.x = displayX;
			worldSeedTextboxBackground.y = displayY;
			worldSeedTextboxBackground.setSize(textBoxWidth, styleWorldInfo.size + guiCreateWorldPadding/2);
			
			worldSeedTextBox.x = centredX;
			worldSeedTextBox.y = displayY + guiCreateWorldPadding / 2 - styleWorldInfo.size;
			worldSeedTextBox.width = textBoxWidth;
			
			// And finally the "create" and "cancel" buttons
			
			buttonX = guiPanelLocation.getX() + (int)(GUI_PANEL_PADDING_SIDE * guiScale);
			buttonY = guiPanelLocation.getY() + (int)(GUI_PANEL_PADDING_BOTTOM * guiScale);
			final int buttonPadding = (int)(1 * guiScale);		// padding between buttons
			int buttonWidth = guiPanelDimensions.getX() - 2 * guiPanelPaddingSide;
			int buttonHeight = 36;		// TODO: crude blanket height
			
			MenuButton button = (MenuButton)getComponentByName("cancelButton");
			button.x = buttonX;
			button.y = buttonY;
			button.setSize(buttonWidth, buttonHeight);
			
			buttonY += buttonHeight + buttonPadding;
			
			button = (MenuButton)getComponentByName("createWorldButton");
			button.x = buttonX;
			button.y = buttonY;
			button.setSize(buttonWidth, buttonHeight);
		} else if(state == State.SELECT || state == State.DELETE_CONFIRM) {
			// The display text
			// Not quite a scale-related thing, but it's convenient to put this here
			selectedWorldName = worlds[selectedWorld].name;
			Date creationDate = new Date(worlds[selectedWorld].creationDate);
			Date lastPlayedDate = new Date(worlds[selectedWorld].lastPlayedDate);
			selectedWorldInfo = new String[] {
					"Seed:",
					Long.toString(worlds[selectedWorld].seed),
					"Date Created:",
					StringUtil.getDateWithoutTimeZone(creationDate.toString()),
					"Last Played:",
					StringUtil.getDateWithoutTimeZone(lastPlayedDate.toString())
			};
			//selectedWorldInfo = Long.toString(worlds[selectedWorld].seed);
			
			int worldNameChars = font1.getNumFittingCharacters(guiPanelDimensions.getX() - 2 * guiPanelPaddingSide, styleWorldName);
			if(selectedWorldName.length() > worldNameChars)
				selectedWorldName = selectedWorldName.substring(0, worldNameChars - 3) + "...";
			
			int worldInfoChars = font1.getNumFittingCharacters(guiPanelDimensions.getX() - 2 * guiPanelPaddingSide, styleWorldInfo);
			if(selectedWorldInfo[1].length() > worldInfoChars)
				selectedWorldInfo[1] = selectedWorldInfo[1].substring(0, worldInfoChars-3) + "...";
			if(selectedWorldInfo[3].length() > worldInfoChars)
				selectedWorldInfo[3] = selectedWorldInfo[3].substring(0, worldInfoChars-3) + "...";
			
			guiSelectWorldNameLocation.setX(guiPanelLocation.getX() + guiPanelDimensions.getX() / 2);
			guiSelectWorldNameLocation.setY(guiPanelLocation.getY() + guiPanelDimensions.getY() - 2 * guiPanelPaddingTop - styleWorldName.size);
			
			guiSelectWorldInfoLocation.setX(guiPanelLocation.getX() + guiPanelDimensions.getX() / 2);
			guiSelectWorldInfoLocation.setY(guiSelectWorldNameLocation.getY() - styleWorldInfo.size * 2);
			
			// The buttons
			buttonX = guiPanelLocation.getX() + (int)(GUI_PANEL_PADDING_SIDE * guiScale);
			buttonY = guiPanelLocation.getY() + (int)(GUI_PANEL_PADDING_BOTTOM * guiScale);
			final int buttonPadding = (int)(1 * guiScale);		// padding between buttons
			int buttonWidth = guiPanelDimensions.getX() - 2 * guiPanelPaddingSide;
			int buttonHeight = 36;		// TODO: crude blanket height
			
			if(state == State.SELECT) {
				MenuButton button = (MenuButton)getComponentByName("cancelButton");
				button.x = buttonX;
				button.y = buttonY;
				button.setSize(buttonWidth, buttonHeight);
				
				buttonY += buttonHeight + buttonPadding;
				
				button = (MenuButton)getComponentByName("deleteWorldButton");
				button.x = buttonX;
				button.y = buttonY;
				button.setSize(buttonWidth, buttonHeight);
				
				buttonY += buttonHeight + buttonPadding;
				
				button = (MenuButton)getComponentByName("selectWorldButton");
				button.x = buttonX;
				button.y = buttonY;
				button.setSize(buttonWidth, buttonHeight);
			} else {
				MenuButton button = (MenuButton)getComponentByName("deleteWorldNoButton");
				button.x = buttonX;
				button.y = buttonY;
				button.setSize(buttonWidth, buttonHeight);
				
				buttonY += buttonHeight + buttonPadding;
				
				button = (MenuButton)getComponentByName("deleteWorldYesButton");
				button.x = buttonX;
				button.y = buttonY;
				button.setSize(buttonWidth, buttonHeight);
				
				buttonY += buttonHeight + buttonPadding;
				
				guiDeleteWorldConfirmationLocation.setLocation(guiSelectWorldInfoLocation.getX(), buttonY);
			}
		}
	}
	
	@Override
	public void update() {
		if(loadingWorld) {
			if(worldLoader.completed()) {
				Application.get().setState(new SingleplayerState(world));
			}
		}
		
		if(state == State.CREATE) {
			if(worldNameTextBox.updated)
				getComponentByName("createWorldButton").setEnabled(worldNameTextBox.text.length() > 0);
		}
		
		super.update();
	}
	
	@Override
	public void render() {
		gui.setTextureData(guiMainTC);
		gui.setScaledDimensions(guiMainDimensions.getX(), guiMainDimensions.getY());
		gui.drawSprite(guiMainLocation.getX(), guiMainLocation.getY());
		
		gui.setTextureData(guiTextTC);
		gui.setScaledDimensions(guiTextDimensions.getX(), guiTextDimensions.getY());
		gui.drawSprite(guiTextLocation.getX(), guiTextLocation.getY());
		
		if(state != State.LIST) {
			gui.setTextureData(guiPanelTC);
			gui.setScaledDimensions(guiPanelDimensions.getX(), guiPanelDimensions.getY());
			gui.drawSprite(guiPanelLocation.getX(), guiPanelLocation.getY());
			
			if(state == State.SELECT || state == State.DELETE_CONFIRM) {
				font1.drawLine(selectedWorldName, guiSelectWorldNameLocation.getX(), guiSelectWorldNameLocation.getY(), styleWorldName);
				//font.drawLines(new String[] {"Seed:", selectedWorldInfo}, guiSelectWorldSeedLocation.getX(), guiSelectWorldSeedLocation.getY(), styleWorldInfo);
				font1.drawLines(selectedWorldInfo, guiSelectWorldInfoLocation.getX(), guiSelectWorldInfoLocation.getY(), styleWorldInfo);
				
				if(state == State.DELETE_CONFIRM) {
					font1.drawLine("Are you sure?", guiDeleteWorldConfirmationLocation.getX(), guiDeleteWorldConfirmationLocation.getY(), styleWorldInfo);
				}
			} else if(state == State.CREATE) {
				font1.drawLine("World name:", worldNameHeaderLocation.getX(), worldNameHeaderLocation.getY(), styleWorldInfo);
				font1.drawLine("World seed (optional):", worldSeedHeaderLocation.getX(), worldSeedHeaderLocation.getY(), styleWorldInfo);
				
				worldNameTextboxBackground.draw();
				worldSeedTextboxBackground.draw();
			}
		}
		
		super.render();
	}
	
	@Override
	public void performAction(int action, Object parameter) {
		if(loadingWorld)
			return;
		
		switch(action) {
			case ACTION_BACK:
				exitMenu();
				break;
			case WorldListButton.ACTION_SELECT:
				selectWorld(ArrayUtil.indexOf(buttons, parameter));
				break;
			case ACTION_CANCEL:
				if(state != State.LIST)
					setState(State.LIST, selectedWorld);
				break;
			case ACTION_SELECT_WORLD:
				loadingWorld = true;
				final WorldInfo info = worlds[selectedWorld];
				worldLoader = new TaskThread(new Task(new TaskTracker(1)) {
					@Override
					protected void execute() throws Exception {
						TaskTimer timer = new TaskTimer("World loading");
						
						world = new ClientWorld<>(
								new SingleplayerWorld(
										info, Application.get().profiler, Log.getAgent("world")
								)
						);
						
						timer.start();
						
						world.prepare();
						world.setClientPlayer(CharacterData.defaultCharacter(), new EntityPlayer(world));
						
						tracker.increment();
						
						while(!world.isLoaded()) {
							Thread.sleep(5L); // cancelled if interrupted
						}
						
						timer.stop();
						timer.logResult(TimeUnit.MILLISECONDS);
					}
				});
				worldLoader.start();
				break;
			case ACTION_RENAME_WORLD:
				
				break;
			case ACTION_DELETE_WORLD:
				setState(State.DELETE_CONFIRM, selectedWorld);
				break;
			case ACTION_DELETE_WORLD_YES:
				IWorld.deleteWorld(worlds[selectedWorld].fileSystemName);
				loadWorlds();
				for(int i = 0; i < buttons.length; i++) {
					buttons[i].setWorld(worlds[i]);
				}
				setState(State.LIST, 0);
				break;
			case ACTION_DELETE_WORLD_NO:
				setState(State.SELECT, selectedWorld);
				break;
			case ACTION_CREATE_WORLD:
				String worldName = worldNameTextBox.text;
				String worldSeed = worldSeedTextBox.text;
				
				long seed;
				if(worldSeed.length() == 0) {
					Random rnd = new Random();
					seed = rnd.nextLong();
				} else {
					try {
						seed = Long.parseLong(worldSeed);
					} catch(NumberFormatException e) {
						seed = (long)worldSeed.hashCode();
					}
				}
				
				if(IWorld.createWorld(worldName, seed) == null) {
					Log.get().postSevere("Could not create world \"" + worldName + "\"!");
				} else {
					loadWorlds();
					for(int i = 0; i < buttons.length; i++) {
						buttons[i].setWorld(worlds[i]);
					}
					setState(State.SELECT, 0);
				}
				break;
		}
	}
	
	@Override
	public void handleKeyPress(int key) {
		if(loadingWorld)
			return;
		
		super.handleKeyPress(key);
		
		switch(state) {
			case LIST:
				// The default Button code handles the return key already (with help from the overridden performAction())
				if(key == Keyboard.KEY_RIGHT/* || key == Keyboard.KEY_RETURN*/)
					selectWorld(ArrayUtil.indexOf(buttons, getFocus()));
				else if(key == Keyboard.KEY_ESCAPE)
					exitMenu();
				break;
			case SELECT:
				if(key == Keyboard.KEY_LEFT || key == Keyboard.KEY_ESCAPE)
					setState(State.LIST, selectedWorld);
				break;
			case CREATE:
				if(key == Keyboard.KEY_ESCAPE)
					setState(State.LIST, selectedWorld);
				break;
			case DELETE_CONFIRM:
				if(key == Keyboard.KEY_ESCAPE)
					setState(State.SELECT, selectedWorld);
				break;
		}
	}
	
	/**
	 * Selects a world. The submenu's state will be set to the
	 * {@link State#SELECT SELECT} or {@link State#CREATE CREATE} state if the
	 * index is valid.
	 * 
	 * @param world The world's index.
	 */
	private void selectWorld(int world) {
		if(world == -1 || (state != State.LIST && world == selectedWorld))
			return;
		//selectedWorld = world;
		if(worlds[world] != null)
			setState(State.SELECT, world);
		else
			setState(State.CREATE, world);
	}
	
	/**
	 * Exits the world select menu.
	 */
	private void exitMenu() {
		action = ACTION_EXIT;
		//parameter = String.valueOf(ACTION_EXIT);
	}
	
	@Override
	public void unloadResources() {
		super.unloadResources();
		gui.destroy();
		font1.destroy();
		font2.destroy();
		worldNameTextboxBackground.destroy();
		worldSeedTextboxBackground.destroy();
		
		if(loadingWorld) {
			worldLoader.cancel();
			try {
				worldLoader.waitUninterruptibly();
			} catch(ExecutionException ignored) {}
		}
	}
	
}
