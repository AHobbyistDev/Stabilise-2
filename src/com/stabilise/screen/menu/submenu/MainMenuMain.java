package com.stabilise.screen.menu.submenu;

import com.stabilise.core.app.Application;
import com.stabilise.core.main.Stabilise;
import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.screen.menu.Menu;
import com.stabilise.screen.menu.SubMenuBasedMenu;
import com.stabilise.screen.menu.component.Button;
import com.stabilise.screen.menu.component.MenuItem;
import com.stabilise.screen.menu.component.Position;
import com.stabilise.screen.menu.customcomponents.MenuButton;
import com.stabilise.util.Colour;
import com.stabilise.util.maths.Interpolation;

/**
 * The main sub-menu of the main menu.
 */
@SuppressWarnings("unused")
public class MainMenuMain extends SubMenu {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	public static final int ACTION_PLAY_GAME = getActionID();
	public static final int ACTION_HOW_TO_PLAY = getActionID();
	public static final int ACTION_ABOUT = getActionID();
	public static final int ACTION_QUIT = getActionID();
	
	/** The font style to use for the game header. */
	private static final FontStyle STYLE_HEADER = new FontStyle(48, Colour.BLACK, FontStyle.Alignment.CENTRE, 4, 0);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** A font. */
	private Font font;
	
	
	/**
	 * Creates the main menu's main screen.
	 * 
	 * @param menu The main menu.
	 */
	public MainMenuMain(SubMenuBasedMenu menu) {
		this(menu, null);
	}
	
	/**
	 * Creates the main menu's main screen.
	 * 
	 * @param menu The main menu.
	 * @param parameter The parameter.
	 */
	public MainMenuMain(SubMenuBasedMenu menu, Object parameter) {
		super(menu);
		
		int selectedItem = 0;
		
		if(parameter != null && parameter instanceof Integer) {
			int param = ((Integer)parameter).intValue();
			if(param == HowToPlayMenu.ACTION_EXIT) {
				selectedItem = 1;
			} else if(param == AboutMenuMain.ACTION_EXIT) {
				selectedItem = 2;
			}
		}
		
		setFocus(0, selectedItem);
	}
	
	@Override
	protected void loadResources() {
		font = new Font("sheets/font1", this);
		
		/*
		setComponentGrid(new MenuItem[][] {
				{ addButton(new MenuButton(this,0,0,300,48,ACTION_PLAY_GAME,"Play Game",16,true,true)).setPosition(new Position(0.5f, 0.66f, 0, 0)) },
				{ addButton(new MenuButton(this,0,0,300,48,ACTION_HOW_TO_PLAY,"How to Play",16,true,true)).setPosition(new Position(0.5f, 0.62f, 0, -40)) },
				{ addButton(new MenuButton(this,0,0,300,48,ACTION_ABOUT,"About",16,true,true)).setPosition(new Position(0.5f, 0.58f, 0, -80)) },
				{ addButton(new MenuButton(this,0,0,300,48,ACTION_QUIT,"Quit",16,true,true)).setPosition(new Position(0.5f, 0.54f, 0, -160)) }
		}, 0, 0);
		*/
		
		// The buttons
		final Button playGameButton 	= new MenuButton(this, 0, 0, 300, 48, ACTION_PLAY_GAME, "Play Game", 16, true, true);
		final Button howToPlayButton 	= new MenuButton(this, 0, 0, 300, 48, ACTION_HOW_TO_PLAY, "How to Play", 16, true, true);
		final Button aboutButton 		= new MenuButton(this, 0, 0, 300, 48, ACTION_ABOUT, "About", 16, true, true);
		final Button quitButton 		= new MenuButton(this, 0, 0, 300, 48, ACTION_QUIT, "Quit", 16, true, true);
		
		// Set their initial positions
		playGameButton.setPosition(new Position(0f, 0.66f, -300, 0));
		howToPlayButton.setPosition(new Position(0f, 0.62f, -300, -40));
		aboutButton.setPosition(new Position(0f, 0.58f, -300, -80));
		quitButton.setPosition(new Position(0f, 0.54f, -300, -160));
		
		// Add them to the component grid
		setComponentGrid(new MenuItem[][] {
				{ addMenuItem(playGameButton) },
				{ addMenuItem(howToPlayButton) },
				{ addMenuItem(aboutButton) },
				{ addMenuItem(quitButton) }
		});
		
		final Interpolation interp = Interpolation.BACK.EASE_OUT;
		final int interpTime = 50;
		
		// Play their animations
		playGameButton.moveToPosition(new Position(0.5f, 0.66f, 0, 0), interp, interpTime);
		
		scheduleEvent(new ScheduledEvent(10) {
			@Override
			protected void execute(Menu menu) {
				howToPlayButton.moveToPosition(new Position(0.5f, 0.62f, 0, -40), interp, interpTime);
			}
		});
		
		scheduleEvent(new ScheduledEvent(20) {
			@Override
			protected void execute(Menu menu) {
				aboutButton.moveToPosition(new Position(0.5f, 0.58f, 0, -80), interp, interpTime);
			}
		});
		
		scheduleEvent(new ScheduledEvent(30) {
			@Override
			protected void execute(Menu menu) {
				quitButton.moveToPosition(new Position(0.5f, 0.54f, 0, -160), interp, interpTime);
			}
		});
	}
	
	@Override
	public void performAction(int action, Object parameter) {
		// Using a switch() won't work since these constants aren't compile-time constants
		if(action == ACTION_PLAY_GAME) {
			menu.setSubMenu(new WorldSelectMenu(menu));
		} else if(action == ACTION_HOW_TO_PLAY) {
			menu.setSubMenu(new HowToPlayMenu(menu));
		} else if(action == ACTION_ABOUT) {
			menu.setSubMenu(new AboutMenuMain(menu));
		} else if(action == ACTION_QUIT) {
			Application.get().shutdown();
		}
	}
	
	@Override
	public void render() {
		super.render();
		
		//font.drawLine(Stabilise.GAME_NAME, screen.getCentreX(), screen.getCentreY() + screen.getHeight() / 3, STYLE_HEADER);
	}
	
	@Override
	public void unloadResources() {
		super.unloadResources();
		font.destroy();
	}

}
