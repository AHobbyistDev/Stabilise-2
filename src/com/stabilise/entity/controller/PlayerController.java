package com.stabilise.entity.controller;

import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.Game;
import com.stabilise.core.main.Stabilise;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.EntityEnemy;
import com.stabilise.entity.EntityMob;
import com.stabilise.input.Controllable;
import com.stabilise.input.Controller;
import com.stabilise.input.Controller.Control;
import com.stabilise.opengl.Texture;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Log;
import com.stabilise.util.MathUtil;
import com.stabilise.world.Direction;
import com.stabilise.world.tile.Tiles;

/**
 * A PlayerController is a MobController which is managed by player input.
 */
public class PlayerController extends MobController implements Controllable {
	
	/** A reference to the PlayerController's controller. */
	private Controller controller;
	
	/** A reference to the game. */
	private Game game;
	/** A reference to the world renderer. */
	private WorldRenderer worldRenderer;
	
	/** The ID of the tile currently selected. */
	private int tileID = 1;
	
	/**
	 * Creates a new PlayerController.
	 * 
	 * @param controller The Controller which provides input for the
	 * PlayerController.
	 * @param The game which is currently active.
	 */
	public PlayerController(EntityMob mob, Controller controller, Game game) {
		super();
		
		this.controller = controller;
		this.game = game;
		
		// This also sets this.mob = mob via a chain of invocations
		mob.setController(this);
		
		/*
		try {
			musicBase = SoundManager.get().loadSound("sarabande.wav");
		} catch(IOException e) {
			Log.exception(e);
		}
		*/
	}
	
	@Override
	public void update() {
		if(controller.isControlPressed(Control.LEFT) && controller.isControlPressed(Control.RIGHT))
			;// do nothing
		else if(controller.isControlPressed(Control.LEFT))
			mob.move(Direction.LEFT);
		else if(controller.isControlPressed(Control.RIGHT))
			mob.move(Direction.RIGHT);
		
		//if(Constants.DEV_VERSION) {
		if(controller.isControlPressed(Control.FLYRIGHT))
			mob.dx += 0.2f;
		if(controller.isControlPressed(Control.FLYLEFT))
			mob.dx -= 0.2f;
		if(controller.isControlPressed(Control.FLYUP))
			mob.dy += 0.2f;
		if(controller.isControlPressed(Control.FLYDOWN))
			mob.dy -= 0.2f;
		//}
		
		if(worldRenderer == null) {
			// TODO: Temporary way of grabbing the renderer
			worldRenderer = ((SingleplayerState)Application.get().getState()).renderer;
		}
	}
	
	/**
	 * Converts the x-coordinate of the mouse to world space.
	 * 
	 * @param x The x-coordinate of the mouse, as defined by
	 * {@link Controllable#handleButtonPress(int, int, int)}.
	 * 
	 * @return The x-coordinate of the tile the mouse is pointing at, in
	 * tile-lengths.
	 */
	private int mouseXToWorldSpace(int x) {
		return MathUtil.fastFloor(((x - worldRenderer.offsetX) / worldRenderer.getScale()));
	}
	
	/**
	 * Converts the y-coordinate of the mouse to world space.
	 * 
	 * @param y The y-coordinate of the mouse, as defined by
	 * {@link Controllable#handleButtonPress(int, int, int)}.
	 * 
	 * @return The y-coordinate of the tile the mouse is pointing at, in
	 * tile-lengths.
	 */
	private int mouseYToWorldSpace(int y) {
		return MathUtil.fastFloor(((y - worldRenderer.offsetY) / worldRenderer.getScale()));
	}
	
	@Override
	public void handleButtonPress(int button, int x, int y) {
		if(button < 2) {
			x = mouseXToWorldSpace(x);
			y = mouseYToWorldSpace(y);
			
			if(button == 0)
				game.getWorld().breakTileAt(x, y);
			else
				game.getWorld().setTileAt(x, y, tileID);
		}
	}
	
	@Override
	public void handleButtonRelease(int button, int x, int y) {
		// nothing to see here, move along
	}
	
	@Override
	public void handleControlPress(Control control) {
		switch(control) {
			case JUMP:
				mob.jump();
				break;
			case ATTACK:
				if(controller.isControlPressed(Control.UP))
					mob.attack(Direction.UP);
				else if(controller.isControlPressed(Control.DOWN))
					mob.attack(Direction.DOWN);
				else
					mob.attack(mob.facingRight ? Direction.RIGHT : Direction.LEFT);
				break;
			case SPECIAL:
				if(controller.isControlPressed(Control.UP))
					mob.specialAttack(Direction.UP);
				else if(controller.isControlPressed(Control.DOWN))
					mob.specialAttack(Direction.DOWN);
				else
					mob.specialAttack(mob.facingRight ? Direction.RIGHT : Direction.LEFT);
				break;
			case SAVE_LOG:
				Log.saveLog(false, Stabilise.GAME_NAME + " v" + Constants.VERSION);
				break;
			case SUMMON:
				{	// Bracing because I don't like using the variable names 'e1', 'e2' that much
					EntityEnemy e = new EntityEnemy(mob.world);
					e.x = mob.x + (mob.facingRight ? 5 : -5);
					e.y = mob.y;
					mob.world.addEntity(e);
				}
				//}
				break;
			case SUMMON_SWARM:
				{
					int max = 50 + mob.world.rng.nextInt(25);
					for(int i = 0; i < max; i++) {
						EntityEnemy e = new EntityEnemy(mob.world);
						e.x = mob.x - 10 + mob.world.rng.nextFloat() * 20;
						e.y = mob.y + mob.world.rng.nextFloat() * 10;
						mob.world.addEntity(e);
					}
				}
				//}
				break;
			case KILL_MOBS:
				mob.world.exterminateMobs();
				break;
			case RESTORE:
				mob.restore();
				break;
			case DESTROY_TILES:
				float radius = 5.5f;
				float radiusSquared = radius * radius;
				int minX = (int)(mob.x - radius);
				int maxX = (int)Math.ceil(mob.x + radius);
				int minY = (int)(mob.y - radius);
				int maxY = (int)Math.ceil(mob.y + radius);
				
				for(int tx = minX; tx <= maxX; tx++) {
					for(int ty = minY; ty <= maxY; ty++) {
						double xDiff = mob.x - tx;
						double yDiff = mob.y - ty;
						if(xDiff*xDiff + yDiff*yDiff <= radiusSquared)
							mob.world.setTileAt(tx, ty, 0);
							//mob.world.blowUpTile(tx, ty, 12);
					}
				}
				break;
			case ZOOM_IN:
				{
					SingleplayerState state = (SingleplayerState)Application.get().getState();
					state.renderer.setScale(state.renderer.getScale() * 2f);
				}
				break;
			case ZOOM_OUT:
				{
					SingleplayerState state = (SingleplayerState)Application.get().getState();
					state.renderer.setScale(state.renderer.getScale() / 2);
				}
				break;
			case PLACE_TILE:
				mob.world.setTileAt(MathUtil.fastFloor(mob.x), MathUtil.fastFloor(mob.y), Tiles.CHEST.getID());
				mob.y++;
				break;
			case INTERACT:
				mob.world.getTileAt(mob.x, mob.y-1).handleInteract(mob.world, MathUtil.fastFloor(mob.x), MathUtil.fastFloor(mob.y-1), mob);
				break;
			case TEST_RANDOM_THING:
				//mob.x = 0;
				//mob.y = 0;
				//game.getWorld().camera.snapToFocus();
				Log.message(Texture.texturesToString());
				break;
			default:
				// nothing
				break;
		}
	}
	
	@Override
	public void handleKeyPress(int key) {
		// nothing to see here, move along
	}
	
	@Override
	public void handleKeyRelease(int key) {
		// nothing to see here, move along
	}
	
	@Override
	public void handleControlRelease(Control control) {
		// nothing to see here, move along
	}
	
	@Override
	public void handleMouseWheelScroll(int scroll) {
		// TODO: temporary
		scroll /= 120;			// For some reason this is the base scroll, on my computer at least
		tileID -= scroll;
		tileID = MathUtil.wrappedRemainder(tileID, 20);
	}
	
}
