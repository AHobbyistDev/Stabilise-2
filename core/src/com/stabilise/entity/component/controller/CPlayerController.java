package com.stabilise.entity.component.controller;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.stabilise.core.Application;
import com.stabilise.core.game.Game;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.core.CBaseMob;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.entity.damage.GeneralSource;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.input.Controllable;
import com.stabilise.input.Controller;
import com.stabilise.input.Controller.Control;
import com.stabilise.item.IContainer;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.Log;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;

/**
 * A PlayerController is an entity controller component which is managed by
 * player input.
 */
public class CPlayerController extends CController implements Controllable, InputProcessor {
    
    private Entity e;
    private CBaseMob mob;
    
    /** A reference to the PlayerController's controller. */
    public Controller controller;
    
    /** A reference to the game. */
    public Game game;
    /** A reference to the world renderer. */
    private WorldRenderer worldRenderer;
    
    /** The ID of the tile currently selected. */
    public int tileID = Tiles.stone.getID();
    private final int maxTileID;
    /** Radius of the tile brush. */
    public float radius = 0.5f;
    
    
    /**
     * Creates a new PlayerController.
     * 
     * @param controller The Controller which provides input for the
     * PlayerController.
     * @param The game which is currently active.
     */
    public CPlayerController(Controller controller, Game game) {
        this.controller = controller;
        this.game = game;
        
        int tmpMaxID = 1;
        while(Tile.TILES.containsID(tmpMaxID))
            tmpMaxID++;
        maxTileID = tmpMaxID - 1;
    }
    
    @Override
    public void init(Entity e) {
        this.e = e;
        mob = (CBaseMob)e.core;
    }
    
    @Override
    public void update(World world, Entity e) {
        if(controller.isControlPressed(Control.LEFT) && controller.isControlPressed(Control.RIGHT))
            ;// do nothing
        else if(controller.isControlPressed(Control.LEFT))
            mob.move(Direction.LEFT);
        else if(controller.isControlPressed(Control.RIGHT))
            mob.move(Direction.RIGHT);
        
        if(controller.isControlPressed(Control.FLYRIGHT))
            e.dx += 1f;
        if(controller.isControlPressed(Control.FLYLEFT))
            e.dx -= 1f;
        if(controller.isControlPressed(Control.FLYUP))
            e.dy += 1f;
        if(controller.isControlPressed(Control.FLYDOWN))
            e.dy -= 1f;
        
        if(worldRenderer == null) {
            // TODO: Temporary way of grabbing the renderer
            worldRenderer = ((SingleplayerState)Application.get().getState()).renderer;
        }
        
        if(Gdx.input.isButtonPressed(Buttons.LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
            doInRadius(worldRenderer, (pos) -> game.world.breakTileAt(pos));
        else if(Gdx.input.isButtonPressed(Buttons.RIGHT) && !Gdx.input.isKeyPressed(Keys.ALT_LEFT))
            doInRadius(worldRenderer, (pos) -> game.world.setTileAt(pos, tileID));
    }
    
    /**
     * Performs a given action on all tiles within {@link #radius} of the
     * cursor as given by {@link WorldRenderer#mouseCoords()}. The position
     * given to the consumer will always be {@link Position#align() aligned}.
     */
    public void doInRadius(WorldRenderer renderer, Consumer<Position> func) {
        Position mc = renderer.mouseCoords();
        mc.clampToTile();
        Position pos = Position.create();
        int rad = Maths.ceil(radius);
        float r2 = radius*radius;
        
        for(int ty = -rad; ty <= rad; ty++) {
            for(int tx = -rad; tx <= rad; tx++) {
                if(tx*tx + ty*ty < r2)
                    func.accept(pos.set(mc, tx, ty).align());
            }
        }
    }
    
    @Override
    public boolean handleControlPress(Control control) {
        switch(control) {
            case JUMP:
                mob.jump();
                break;
            case ATTACK:
                if(controller.isControlPressed(Control.UP))
                    mob.attack(game.world, Direction.UP);
                else if(controller.isControlPressed(Control.DOWN))
                    mob.attack(game.world, Direction.DOWN);
                else
                    mob.attack(game.world, e.facingRight ? Direction.RIGHT : Direction.LEFT);
                break;
            case SPECIAL:
                if(controller.isControlPressed(Control.UP))
                    mob.specialAttack(game.world, Direction.UP);
                else if(controller.isControlPressed(Control.DOWN))
                    mob.specialAttack(game.world, Direction.DOWN);
                else
                    mob.specialAttack(game.world, e.facingRight ? Direction.RIGHT : Direction.LEFT);
                break;
            case SUMMON:
                {
                    Entity m = Entities.enemy();
                    m.pos.set(e.pos, (e.facingRight ? 5 : -5), 0f);
                    game.world.addEntity(m);
                }
                break;
            case SUMMON_SWARM:
                {
                    int max = 1000;
                    for(int i = 0; i < max; i++) {
                        Entity m = Entities.enemy();
                        m.pos.set(e.pos,
                                - 10 + game.world.rnd().nextFloat() * 20,
                                1 + + game.world.rnd().nextFloat() * 10
                        );
                        game.world.addEntity(m);
                    }
                }
                break;
            case KILL_MOBS:
                if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    for(Entity en : game.world.getEntities()) {
                        if(en.core instanceof CBaseMob) {
                            ((CBaseMob)en.core).damage(game.world, en, GeneralSource.voidDamage(9999));
                        }
                    }
                } else
                    game.world.destroyEntities();
                break;
            case RESTORE:
                mob.restore();
                break;
            case INTERACT:
                Position p = e.pos.clone().clampToTile().add(0,-1).align();
                game.world.getTileAt(p).handleInteract(game.world, p, e);
                break;
            case PREV_TILE:
                scrolled(-1);
                break;
            case NEXT_TILE:
                scrolled(1);
                break;
            case CLEAR_INVENTORY:
                if(e.core instanceof IContainer)
                    ((IContainer)e.core).clear();
                break;
            case PRINT_INVENTORY:
                Log.get().postInfo(e.core.toString());
                break;
            case PORTAL:
                //String dim = "overworld";
                String dim = game.playerData.data.getDimensionName();
                //String dim = "flatland";
                Entity portal = Entities.portal(dim);
                portal.pos.set(e.pos, e.facingRight ? 3f : -3f, 1.5f).align();
                //portal.facingRight = !e.facingRight;
                CPortal pCore = (CPortal) portal.core;
                pCore.rotation = e.facingRight ? Maths.PIf : 0f;
                
                if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
                	pCore.otherPortalPos.set(0, 0, 0f, 0f);
                else
                	// spawn the other portal at the same place
                	pCore.otherPortalPos.set(portal.pos);
                
                game.world.addEntity(portal);
                break;
            default:
                return false;
        }
        return false;
    }
    
    @Override
    public boolean handleControlRelease(Control control) {
        return false;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.LEFT_BRACKET)
            game.world.setTimeDelta(game.world.getTimeDelta() / 2);
        else if(keycode == Keys.RIGHT_BRACKET)
            game.world.setTimeDelta(game.world.getTimeDelta() * 2);
        else if(keycode == Keys.P)
            ;//System.out.println(game.profiler.getData().toString());
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
        // Ctrl + leftclick = teleport
        if(button == Buttons.LEFT && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
            e.pos.set(worldRenderer.mouseCoords());
        // Alt + rightclick = portal
        else if(button == Buttons.RIGHT && Gdx.input.isKeyPressed(Keys.ALT_LEFT)) {
            String dim = game.playerData.data.getDimensionName();
            Entity portal = Entities.portal(dim);
            
            Position mouseCoords = worldRenderer.mouseCoords();
            portal.pos.set(mouseCoords);
            //portal.facingRight = !e.facingRight;
            
            CPortal pCore = (CPortal) portal.core;
            float dx = e.pos.diffX(mouseCoords);
            float dy = e.pos.diffY(mouseCoords);
            pCore.rotation = MathUtils.atan2(-dy, -dx);
            
            if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
                pCore.otherPortalPos.set(0, 0, 0f, 0f); // origin of other dim
            else
                // spawn the other portal at the same place
                pCore.otherPortalPos.set(portal.pos);
            
            game.world.addEntity(portal);
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
            tileID = 1 + Maths.remainder(tileID + amount - 1, maxTileID);
        return true;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        // nothing to do
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        // nothing to do
    }
    
}
