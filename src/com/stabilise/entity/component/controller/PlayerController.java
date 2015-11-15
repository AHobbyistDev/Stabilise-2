package com.stabilise.entity.component.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.stabilise.core.Constants;
import com.stabilise.core.Game;
import com.stabilise.core.app.Application;
import com.stabilise.core.main.Stabilise;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.Entity;
import com.stabilise.entity.component.ComponentEvent;
import com.stabilise.entity.component.state.CBaseMob;
import com.stabilise.input.Controllable;
import com.stabilise.input.Controller;
import com.stabilise.input.Controller.Control;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.BiIntConsumer;
import com.stabilise.util.Direction;
import com.stabilise.util.Log;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tiles;

/**
 * A PlayerController is a MobController which is managed by player input.
 */
public class PlayerController implements CController, Controllable, InputProcessor {
    
    private Entity e;
    private CBaseMob mob;
    
    /** A reference to the PlayerController's controller. */
    public Controller controller;
    
    /** A reference to the game. */
    public Game game;
    /** A reference to the world renderer. */
    private WorldRenderer worldRenderer;
    
    /** The ID of the tile currently selected. */
    public int tileID = Tiles.STONE.getID();
    /** Radius of the tile brush. */
    public float radius = 0.5f;
    
    
    /**
     * Creates a new PlayerController.
     * 
     * @param controller The Controller which provides input for the
     * PlayerController.
     * @param The game which is currently active.
     */
    public PlayerController(Controller controller, Game game) {
        this.controller = controller;
        this.game = game;
    }
    
    @Override
    public void init(World w, Entity e) {
        this.e = e;
        mob = (CBaseMob)e.state;
    }
    
    @Override
    public void update(World world, Entity e) {
        if(controller.isControlPressed(Control.LEFT) && controller.isControlPressed(Control.RIGHT))
            ;// do nothing
        else if(controller.isControlPressed(Control.LEFT))
            ;//e.move(Direction.LEFT);
        else if(controller.isControlPressed(Control.RIGHT))
            ;//e.move(Direction.RIGHT);
        
        //if(Constants.DEV_VERSION) {
        if(controller.isControlPressed(Control.FLYRIGHT))
            e.dx += 1f;
        if(controller.isControlPressed(Control.FLYLEFT))
            e.dx -= 1f;
        if(controller.isControlPressed(Control.FLYUP))
            e.dy += 1f;
        if(controller.isControlPressed(Control.FLYDOWN))
            e.dy -= 1f;
        //}
        
        if(worldRenderer == null) {
            // TODO: Temporary way of grabbing the renderer
            worldRenderer = ((SingleplayerState)Application.get().getState()).renderer;
        }
        
        if(Gdx.input.isButtonPressed(Buttons.LEFT))
            doInRadius(worldRenderer, (x,y) -> game.world.breakTileAt(x, y));
        else if(Gdx.input.isButtonPressed(Buttons.RIGHT))
            doInRadius(worldRenderer, (x,y) -> game.world.setTileAt(x, y, tileID));
    }
    
    public void doInRadius(WorldRenderer renderer, BiIntConsumer func) {
        Vector2 wc = renderer.mouseCoords();
        float x = Maths.floor(wc.x);
        float y = Maths.floor(wc.y);
        float r2 = radius*radius;
        int minX = (int)(x - radius);
        int maxX = (int)Math.ceil(x + radius);
        int minY = (int)(y - radius);
        int maxY = (int)Math.ceil(y + radius);
        
        for(int tx = minX; tx <= maxX; tx++) {
            for(int ty = minY; ty <= maxY; ty++) {
                float xDiff = x - tx;
                float yDiff = y - ty;
                if(xDiff*xDiff + yDiff*yDiff < r2)
                    func.accept(tx, ty);
            }
        }
    }
    
    /**
     * Converts the x-coordinate of the mouse to world space.
     * 
     * <p>TODO: Libgdx treats the top-left as the origin instead of the
     * bottom-left, which is really annoying.
     * 
     * @param x The x-coordinate of the mouse, as defined by
     * {@link Controllable#handleControlPress(int, int, int)}.
     * 
     * @return The x-coordinate of the tile the mouse is pointing at, in
     * tile-lengths.
     */
    @SuppressWarnings("unused")
    private int mouseXToWorldSpace(int x) {
        return Maths.floor(((x + worldRenderer.playerCamera.x) / worldRenderer.getPixelsPerTile()));
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
    @SuppressWarnings("unused")
    private int mouseYToWorldSpace(int y) {
        return Maths.floor(((y + worldRenderer.playerCamera.y) / worldRenderer.getPixelsPerTile()));
    }
    
    @Override
    public boolean handleControlPress(Control control) {
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
                    mob.attack(e.facingRight ? Direction.RIGHT : Direction.LEFT);
                break;
            case SPECIAL:
                if(controller.isControlPressed(Control.UP))
                    mob.specialAttack(Direction.UP);
                else if(controller.isControlPressed(Control.DOWN))
                    mob.specialAttack(Direction.DOWN);
                else
                    mob.specialAttack(e.facingRight ? Direction.RIGHT : Direction.LEFT);
                break;
            case SAVE_LOG:
                Log.saveLog(false, Stabilise.GAME_NAME + " v" + Constants.VERSION);
                break;
            case SUMMON:
                {    // Bracing because I don't like using the variable names 'e1', 'e2' that much
                    /*
                    EntityEnemy e = new EntityEnemy();
                    e.x = e.x + (mob.facingRight ? 5 : -5);
                    e.y = e.y;
                    game.world.addEntity(e);
                    */
                }
                break;
            case SUMMON_SWARM:
                {
                    /*
                    int max = 750 + game.world.getRnd().nextInt(250);
                    for(int i = 0; i < max; i++) {
                        EntityEnemy e = new EntityEnemy();
                        e.x = mob.x - 10 + game.world.getRnd().nextFloat() * 20;
                        e.y = 1 + mob.y + game.world.getRnd().nextFloat() * 10;
                        game.world.addEntity(e);
                    }
                    */
                }
                break;
            case KILL_MOBS:
                game.world.destroyEntities();
                break;
            case RESTORE:
                mob.restore();
                break;
            case ZOOM_IN:
                {
                    SingleplayerState state = (SingleplayerState)Application.get().getState();
                    state.renderer.setPixelsPerTile(state.renderer.getPixelsPerTile() * 2, true);
                }
                break;
            case ZOOM_OUT:
                {
                    SingleplayerState state = (SingleplayerState)Application.get().getState();
                    state.renderer.setPixelsPerTile(state.renderer.getPixelsPerTile() / 2, true);
                }
                break;
            case INTERACT:
                game.world.getTileAt(e.x, e.y-1).handleInteract(game.world, Maths.floor(e.x), Maths.floor(e.y-1), e);
                break;
            case TEST_RANDOM:
                //mob.x = 0;
                //mob.y = 0;
                //game.getWorld().camera.snapToFocus();
                //Log.message(Texture.texturesToString());
                Runtime r = Runtime.getRuntime();
                Log.get().postDebug(r.freeMemory()/(1024*1024) + "/" +
                        r.totalMemory()/(1024*1024) + "/" + r.maxMemory()/(1024*1024));
                System.out.println(game.profiler.getData().toString());
                break;
            case PREV_TILE:
                scrolled(-1);
                break;
            case NEXT_TILE:
                scrolled(1);
                break;
            default:
                return false;
        }
        return true;
    }
    
    @Override
    public boolean handleControlRelease(Control control) {
        return false;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.LEFT_BRACKET)
            game.world.setTimeDelta(game.world.getTimeDelta() * 0.5f);
        else if(keycode == Keys.RIGHT_BRACKET)
            game.world.setTimeDelta(game.world.getTimeDelta() * 2f);
        else if(keycode == Keys.P)
            System.out.println(game.profiler.getData().toString());
        else if(keycode == Keys.O)
            game.profiler.reset();
        return false;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if(button < 2) {
            Vector2 wc = worldRenderer.mouseCoords();
            x = Maths.floor(wc.x);
            y = Maths.floor(wc.y);
            
            if(button == 0)
                game.getWorld().breakTileAt(x, y);
            else
                game.getWorld().setTileAt(x, y, tileID);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    
    @Override
    public boolean scrolled(int amount) {
        if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
            radius -= amount;
            if(radius < 1)
                radius = 0.5f;
        } else
            tileID = 1 + Maths.remainder(tileID + amount - 1, 21);
        return true;
    }
    
    @Override
    public void handle(World w, Entity e, ComponentEvent ev) {
        // TODO Auto-generated method stub
    }
    
}
