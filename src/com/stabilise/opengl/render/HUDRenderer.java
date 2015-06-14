package com.stabilise.opengl.render;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.stabilise.core.Constants;
import com.stabilise.core.Game;
import com.stabilise.core.app.Application;
import com.stabilise.entity.EntityPerson;
import com.stabilise.util.Profiler;
import com.stabilise.util.StringUtil;

/**
 * Renders the in-game HUD.
 */
@SuppressWarnings("unused")
public abstract class HUDRenderer implements Renderer {
	
//	//--------------------==========--------------------
//	//-----=====Static Constants and Variables=====-----
//	//--------------------==========--------------------
//	
//	/** TODO: Temporary scaling constant */
//	private static final int SCALE = 4;
//	
//	
//	// Inventory hotbar
//	
//	private static final int ITEM_SLOT_WIDTH = 12 * SCALE;
//	@SuppressWarnings("unused")
//	private static final int ITEM_SLOT_SPACING = 0 * SCALE;
//	@SuppressWarnings("unused")
//	private static final int ITEM_SLOT_VERTICAL_OFFSET = 20;
//	private static final int ITEM_FRAME_OFFSET = 2 * SCALE;
//	@SuppressWarnings("unused")
//	private static final int ITEM_WIDTH = ITEM_SLOT_WIDTH - ITEM_FRAME_OFFSET*2;
//	private static final int COUNT_HEIGHT = 2 * SCALE;
//	
//	/** The font style used for the hotbar item counts. */
//	@SuppressWarnings("unused")
//	private static final FontStyle STYLE_HOTBAR_COUNT = new FontStyle(COUNT_HEIGHT, Colour.WHITE, FontStyle.Alignment.RIGHT, 1, 0);
//	/** The font style used for the debug text. */
//	private static final FontStyle STYLE_DEBUG = new FontStyle(8, Colour.BLACK, FontStyle.Alignment.LEFT, 1, 1);
//	
//	
//	// Status display
//	
//	@SuppressWarnings("unused")
//	private static final int STATUS_PANEL_VERTICAL_OFFSET = 20;
//	private static final int STATUS_PANEL_WIDTH = 64 * SCALE;
//	private static final int STATUS_PANEL_HEIGHT = 22 * SCALE;
//	private static final int STATUS_PANEL_PADDING = 3 * SCALE;
//	private static final int BAR_PADDING = 2 * SCALE;
//	private static final int MANA_BAR_X = STATUS_PANEL_PADDING;
//	private static final int MANA_BAR_WIDTH = STATUS_PANEL_WIDTH - 2*STATUS_PANEL_PADDING;
//	private static final int MANA_BAR_Y = STATUS_PANEL_PADDING;
//	private static final int MANA_BAR_HEIGHT = 4 * SCALE;
//	private static final int STAMINA_BAR_X = STATUS_PANEL_PADDING;
//	private static final int STAMINA_BAR_WIDTH = STATUS_PANEL_WIDTH - 2*STATUS_PANEL_PADDING;
//	private static final int STAMINA_BAR_Y = STATUS_PANEL_PADDING + MANA_BAR_HEIGHT + BAR_PADDING;
//	private static final int STAMINA_BAR_HEIGHT = 4 * SCALE;
//	private static final int HEALTH_BAR_X = STATUS_PANEL_PADDING;
//	private static final int HEALTH_BAR_WIDTH = STATUS_PANEL_WIDTH - 2*STATUS_PANEL_PADDING;
//	private static final int HEALTH_BAR_Y = STAMINA_BAR_Y + STAMINA_BAR_HEIGHT + BAR_PADDING;
//	private static final int HEALTH_BAR_HEIGHT = 4 * SCALE;
//	
//	
//	//--------------------==========--------------------
//	//-------------=====Member Variables=====-----------
//	//--------------------==========--------------------
//	
//	/** A reference to the world renderer. */
//	public final WorldRenderer worldRenderer;
//	
//	/** A reference to the game. */
//	private final Game game;
//	
//	/** A casted reference to the player. */
//	private EntityPerson player;
//	
//	/** The item slot sprite. */
//	public Sprite itemSlot;
//	/** The item count font. */
//	public Font font;
//	
//	/** The panel for such statuses as health, stamina and mana. */
//	public Sprite statusPanel;
//	/** The status panel origin. */
//	private MutablePoint statusOrigin = new MutablePoint();
//	
//	/** The health bar. */
//	private Rectangle healthBar;
//	/** The stamina bar. */
//	private Rectangle staminaBar;
//	/** The mana bar. */
//	private Rectangle manaBar;
//	
//	// Profiler stuff
//	/** The profiler. */
//	private Profiler profiler = Application.get().profiler;
//	/** The profiler data. */
//	private Profiler.SectionData profilerData;
//	/** The current level of profiler data being viewed. */
//	private Profiler.SectionData profilerLevel;
//	/** The current view stack of the profiler data. */
//	private Deque<String> profilerStack = new LinkedList<String>();
//	
//	
//	/**
//	 * Creates a new HUD renderer.
//	 * 
//	 * @param game The game.
//	 * @param worldRenderer The world renderer.
//	 */
//	public HUDRenderer(Game game, WorldRenderer worldRenderer) {
//		super();
//		
//		this.game = game;
//		this.worldRenderer = worldRenderer;
//		player = (EntityPerson)worldRenderer.world.player;
//		
//		profilerData = profiler.getData();
//		profilerLevel = profilerData;
//	}
//	
//	@Override
//	public void update() {
//		if(player.healthChanged)
//			healthBar.setWidth((HEALTH_BAR_WIDTH * player.health) / player.maxHealth);
//		if(player.staminaChanged)
//			staminaBar.setWidth((STAMINA_BAR_WIDTH * player.stamina) / player.maxStamina);
//		if(player.manaChanged)
//			manaBar.setWidth((MANA_BAR_WIDTH * player.mana) / player.maxMana);
//		
//		if(game.debug) {
//			profilerData = profiler.getData();
//			profilerLevel = profilerData;
//			base:
//			for(String s : profilerStack) {
//				for(Profiler.SectionData data : profilerLevel.getConstituents()) {
//					if(data.name == s) {
//						profilerLevel = data;
//						continue base;
//					}
//				}
//				// No matching thing found -> use the closest thing
//				// TODO: Might want to consider snipping profilerStack if this
//				// is the case
//				break;
//			}
//		}
//	}
//	
//	@Override
//	public void render() {
//		/*
//		worldRenderer.itemSprites.setScale((float)ITEM_WIDTH / worldRenderer.itemSprites.getSpriteWidth());
//		for(int i = 0; i < Constants.HOTBAR_SIZE; i++) {
//			itemSlot.x = screen.getCentreX() + ITEM_SLOT_WIDTH * (i-(Constants.HOTBAR_SIZE / 2)-0.5f) + ITEM_SLOT_SPACING * (i-(Constants.HOTBAR_SIZE / 2));
//			itemSlot.draw();
//			if(worldRenderer.world.player.inventory.getItem(i) != null) {
//				worldRenderer.itemSprites.drawSprite(worldRenderer.world.player.inventory.getItem(i).getID() - 1,
//						(int)(screen.getCentreX() + ITEM_SLOT_WIDTH * (i-(Constants.HOTBAR_SIZE / 2)-0.5f) + ITEM_SLOT_SPACING * (i-(Constants.HOTBAR_SIZE / 2)) + ITEM_FRAME_OFFSET),
//						ITEM_SLOT_VERTICAL_OFFSET + ITEM_FRAME_OFFSET);
//				font.drawLine("" + worldRenderer.world.player.inventory.getItemQuantity(i),
//						(int)(screen.getCentreX() + ITEM_SLOT_WIDTH * (i-(Constants.HOTBAR_SIZE / 2)+0.5f) + ITEM_SLOT_SPACING * (i-(Constants.HOTBAR_SIZE / 2)) - ITEM_FRAME_OFFSET),
//						ITEM_SLOT_VERTICAL_OFFSET + ITEM_FRAME_OFFSET,
//						STYLE_HOTBAR_COUNT);
//			}
//		}
//		*/
//		
//		profiler.start("statusPanel");
//		statusPanel.draw();
//		healthBar.draw();
//		staminaBar.draw();
//		manaBar.draw();
//		
//		profiler.next("debug");
//		if(game.debug) {
//			font.drawLines(ArrayUtils.addAll(new String[] {
//					"Stabilise II v" + Constants.VERSION,
//					//"FPS: " + screen.getFPS() + " (" + screen.getFPSCap() + ")",
//					"x: " + StringUtil.cullFP(worldRenderer.world.player.x, 2),
//					"y: " + StringUtil.cullFP(worldRenderer.world.player.y, 2),
//					"Entities:  " + worldRenderer.world.getEntities().size() + "/" + worldRenderer.world.entityCount,
//					"Hitboxes:  " + worldRenderer.world.getHitboxes().size() + "/" + worldRenderer.world.hitboxCount,
//					"Particles: " + worldRenderer.world.particles.size() + "/" + worldRenderer.world.particleCount,
//					"World seed: " + worldRenderer.world.info.seed,
//					"World age: " + worldRenderer.world.info.age,
//					"",
//					""
//			}, getProfilerStrings()),
//			0,
//			0/*screen.getHeight() - STYLE_DEBUG.size*/,
//			STYLE_DEBUG);
//		}
//		
//		profiler.end();
//	}
//	
//	/**
//	 * Gets the array of strings representing the profiler results.
//	 * 
//	 * @return The profiler results.
//	 */
//	private String[] getProfilerStrings() {
//		List<String> lines = new ArrayList<String>();
//		lines.add(
//				profilerLevel.absoluteName + "    "
//				+ StringUtil.cullFP(profilerLevel.totalPercent, 2) + "%"
//		);
//		int num = 1;
//		for(Profiler.SectionData data : profilerLevel.getConstituents()) {
//			lines.add(
//					"[" + num + "] "
//					+ StringUtil.cullFP(data.totalPercent, 2) + "% "
//					+ StringUtil.cullFP(data.localPercent, 2) + "% "
//					+ data.name
//			//		+ " ("
//			//		+ data.duration
//			//		+ " nanos)"
//			);
//			num++;
//		}
//		
//		return lines.toArray(new String[0]);
//	}
//	
//	/**
//	 * Sets the currently-displayed profiler section.
//	 * 
//	 * @param section The section number. Negative values are ignored.
//	 */
//	public void setProfilerSection(int section) {
//		if(section == 0) {
//			// go up a level
//			profilerStack.pollLast();
//		} else if(section > 0 && section <= profilerLevel.getConstituents().length) {
//			Profiler.SectionData level = profilerLevel.getConstituents()[section - 1];
//			if(level.hasConstituents())
//				profilerStack.add(level.name);
//		}
//	}
//	
//	@Override
//	public void loadResources() {
//		/*
//		itemSlot = new Sprite("itemslot");
//		itemSlot.filter(Texture.NEAREST);
//		itemSlot.setScaledWidth(ITEM_SLOT_WIDTH);
//		itemSlot.setScaledHeight(ITEM_SLOT_WIDTH);
//		itemSlot.y = ITEM_SLOT_VERTICAL_OFFSET;
//		*/
//		
//		statusPanel = new Sprite("statuspanel");
//		statusPanel.filter(Texture.NEAREST);
//		statusPanel.setScaledWidth(STATUS_PANEL_WIDTH);
//		statusPanel.setScaledHeight(STATUS_PANEL_HEIGHT);
//		
//		healthBar = new Rectangle();
//		healthBar.fill(new Colour(0xF10000));
//		staminaBar = new Rectangle();
//		staminaBar.fill(new Colour(0x00F100));
//		manaBar = new Rectangle();
//		manaBar.fill(new Colour(0x00F7F4));
//		
//		font = new Font("sheets/font1", this);
//		//font.setScale(COUNT_WIDTH / font.getSpriteWidth());
//		
//		resize();
//	}
//	
//	@Override
//	public void resize(int width, int height) {
//		//statusOrigin.setLocation(screen.getCentreX() - STATUS_PANEL_WIDTH / 2, STATUS_PANEL_VERTICAL_OFFSET);
//		
//		statusPanel.x = statusOrigin.x;
//		statusPanel.y = statusOrigin.y;
//		
//		healthBar.x = statusOrigin.x + HEALTH_BAR_X;
//		healthBar.y = statusOrigin.y + HEALTH_BAR_Y;
//		healthBar.setWidth(HEALTH_BAR_WIDTH);
//		healthBar.setHeight(HEALTH_BAR_HEIGHT);
//		
//		staminaBar.x = statusOrigin.x + STAMINA_BAR_X;
//		staminaBar.y = statusOrigin.y + STAMINA_BAR_Y;
//		staminaBar.setWidth(STAMINA_BAR_WIDTH);
//		staminaBar.setHeight(STAMINA_BAR_HEIGHT);
//		
//		manaBar.x = statusOrigin.x + MANA_BAR_X;
//		manaBar.y = statusOrigin.y + MANA_BAR_Y;
//		manaBar.setWidth(MANA_BAR_WIDTH);
//		manaBar.setHeight(MANA_BAR_HEIGHT);
//	}
//	
//	@Override
//	public void unloadResources() {
//		font.destroy();
//		
//		//itemSlot.destroy();
//		statusPanel.destroy();
//		healthBar.destroy();
//		staminaBar.destroy();
//		manaBar.destroy();
//	}
//	
//	@Override
//	public String toString() {
//		return "HUDRenderer";
//	}
	
}
