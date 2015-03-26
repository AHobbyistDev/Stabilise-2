package com.stabilise.screen.menu;

import com.stabilise.screen.menu.submenu.*;

/**
 * The main game menu.
 */
public class MainMenu extends SubMenuBasedMenu {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The submenus within the main menu. */
	private static enum Submenu {
		
		MAIN(MainMenuMain.class, null),
		WORLD_SELECT(WorldSelectMenu.class, new SubmenuRedirect[] {
			new SubmenuRedirect(WorldSelectMenu.ACTION_EXIT, MainMenuMain.class, false)
		}),
		HOW_TO_PLAY(HowToPlayMenu.class, new SubmenuRedirect[] {
			new SubmenuRedirect(HowToPlayMenu.ACTION_EXIT, MainMenuMain.class, true)
		}),
		ABOUT(AboutMenuMain.class, new SubmenuRedirect[] {
			new SubmenuRedirect(AboutMenuMain.ACTION_EXIT, MainMenuMain.class, true)
		});
		
		/** The sub-menu's class. */
		private final Class<? extends SubMenu> submenuClass;
		/** The sub-menu's possible redirects. */
		private final SubmenuRedirect[] redirects;
		
		
		/**
		 * Creates a new Submenu.
		 * 
		 * @param submenuClass The sub-menu's class.
		 * @param redirects The sub-menu's possible redirects.
		 */
		private Submenu(Class<? extends SubMenu> submenuClass, SubmenuRedirect[] redirects) {
			this.submenuClass = submenuClass;
			this.redirects = redirects;
		}
		
		/**
		 * Gets the redirect invoked by a given action.
		 * 
		 * @param action The action.
		 * 
		 * @return The redirect, or {@code null} if there is no such redirect
		 * linked to the given action.
		 */
		private SubmenuRedirect getRedirect(int action) {
			for(SubmenuRedirect r : redirects) {
				if(r.action == action)
					return r;
			}
			return null;
		}
		
	}
	
	/**
	 * This class contains information about a sub-menu change which may be
	 * invoked by triggering an action in a sub-menu.
	 */
	private static class SubmenuRedirect {
		
		/** The action which must be invoked to trigger the sub-menu change. */
		private final int action;
		/** The class of the sub-menu to change to. */
		private final Class<? extends SubMenu> submenuClass;
		/** Whether or not to construct the new sub-menu with the sent
		 * parameter. */
		private final boolean sendParameter;
		
		
		/**
		 * Creates a new SubmenuRedirect.
		 * 
		 * @param action The action which must be invoked to trigger the
		 * sub-menu change.
		 * @param submenuClass The class of the sub-menu to change to.
		 * @param sendParameter  Whether or not to construct the new sub-menu
		 * with the sent parameter.
		 */
		private SubmenuRedirect(int action, Class<? extends SubMenu> submenuClass, boolean sendParameter) {
			this.action = action;
			this.submenuClass = submenuClass;
			this.sendParameter = sendParameter;
		}
		
	}
	
	/** The width around which the main menu layout is designed. */
	private static final int BASE_WIDTH = 900;
	/** The height around which the main menu layout is designed. */
	private static final int BASE_HEIGHT = 600;
	
	/** The background scaling. */
	private static final int SCALE = 4;
	/** The x offset of the character in the background, in pixels. */
	private static final int CHARACTER_OFFSET_X = 25 * 8 * SCALE;
	/** The y offset of the character in the background, in pixels. */
	private static final int CHARACTER_OFFSET_Y = 7 * 8 * SCALE;
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** The sky background. */
	private Rectangle sky;
	/** The main background image. */
	private Sprite background;
	/** The tiled portion of the background image, to support screen scaling. */
	private Sprite backgroundTile;
	/** The number of times the background tile will need to be drawn. */
	private int backgroundTiles;
	
	/** The character's cloak. */
	private Animation cloak;
	/** The character's body. */
	private Sprite character;
	/** The character's head. */
	private Sprite head;
	
	/** The effective x=0, for placing things relative to the background. */
	private int originX;
	/** The effective y=0, for placing things relative to the background. */
	private int originY;
	/** The amount of horizontal pixels left over after the main background has
	 * been considered. */
	private int screenSpace;
	
	/** The current sub-menu. */
	private Submenu currentSubmenu;
	
	
	/**
	 * Creates a new MainMenu.
	 */
	public MainMenu() {
		super();
		
		setSubMenu(new MainMenuMain(this));
	}
	
	@Override
	protected void loadResources() {
		sky = new Rectangle(false, false);
		sky.gradientTopToBottom(new Colour(0x1F80E0), new Colour(0xB36F4B));		//0x1F80E0, 0xEB6913
		
		background = new Sprite("mainbg", this);
		background.setScale(SCALE);
		background.filter(Texture.NEAREST);
		backgroundTile = new Sprite("mainbgtile", this);
		backgroundTile.setScale(SCALE);
		backgroundTile.filter(Texture.NEAREST);
		
		//originX = (screen.getWidth() - background.getWidth()) / 2;
		
		float charScale = 0.5f;
		character = new Sprite("stickfigure", this);
		character.setScale(0.9f * charScale);
		cloak = new Animation("sheets/cloak", 8, 8, 29, this);
		cloak.setFrameDurations(2);
		cloak.setScale(1.23f * charScale);
		head = new Sprite("head", this);
		head.setScale(0.82f * charScale);
		
		/*
		SoundBase backgroundMusic = SoundManager.get().getSound("souleater.wav");
		if(backgroundMusic != null) {
			music = backgroundMusic.createBackgroundSound(false, 1f, 1.5f, true, false);
			music.play();
		}
		*/
	}
	
	@Override
	public void update() {
		if(submenu.action != SubMenu.NO_ACTION && currentSubmenu != null) {
			SubmenuRedirect r = currentSubmenu.getRedirect(submenu.action);
			if(r != null) {
				if(r.sendParameter)
					setSubMenuSafe(r.submenuClass, submenu.parameter);
				else
					setSubMenuSafe(r.submenuClass);
			}
		}
		
		cloak.update();
		
		super.update();
	}
	
	@Override
	public void render() {
		sky.draw();
		background.draw();
		
		for(int i = 0; i < backgroundTiles; i++) {
			backgroundTile.x = background.x + background.width() + i * backgroundTile.width();
			backgroundTile.draw();
			/*
			if(screen.getWidth() >= BASE_WIDTH) {
				backgroundTile.x = background.x - (i+1) * backgroundTile.getWidth();
				backgroundTile.draw();
			}
			*/
		}
		
		character.draw();
		cloak.draw();
		head.draw();
		
		super.render();
	}
	
	@Override
	public void setSubMenu(SubMenu submenu) {
		super.setSubMenu(submenu);
		
		for(Submenu s : Submenu.values()) {
			if(s.submenuClass == submenu.getClass()) {
				currentSubmenu = s;
				return;
			}
		}
		currentSubmenu = null;
	}
	
	@Override
	protected void rescale(int width, int height) {
		super.rescale(width, height);
		
		sky.setSize(width, height);
		screenSpace = width - background.width();
		
		if(width < BASE_WIDTH) {
			// If the screen starts getting small, reposition the background so the character stays on screen
			originX = screenSpace / 2 - (int)((float)CHARACTER_OFFSET_X * (BASE_WIDTH - width) / BASE_WIDTH / 2);
			backgroundTiles = (int) Math.ceil((float)(width - backgroundTile.width() - originX) / backgroundTile.width());
		} else {
			originX = screenSpace / 2;
			backgroundTiles = (int) Math.ceil(screenSpace / (backgroundTile.width() * 2f));
		}
		
		if(height < BASE_HEIGHT) {
			originY = (int)((float)-CHARACTER_OFFSET_Y * (BASE_HEIGHT - height) / BASE_HEIGHT);
		} else {
			originY = 0;
		}
		
		background.x = originX;
		background.y = originY;
		backgroundTile.y = originY;
		
		character.x = originX + CHARACTER_OFFSET_X;
		character.y = originY + CHARACTER_OFFSET_Y;
		
		head.x = originX + CHARACTER_OFFSET_X + 0;
		head.y = originY + CHARACTER_OFFSET_Y + 120;
		
		cloak.x = originX + CHARACTER_OFFSET_X - 20;
		cloak.y = originY + CHARACTER_OFFSET_Y - 27;
	}
	
	@Override
	public void unloadResources() {
		super.unloadResources();
		
		sky.destroy();
		background.destroy();
		backgroundTile.destroy();
		cloak.destroy();
		character.destroy();
		head.destroy();
	}
	
}
