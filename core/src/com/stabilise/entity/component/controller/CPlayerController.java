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
import com.stabilise.entity.event.EntityEvent.Type;
import com.stabilise.input.Controllable;
import com.stabilise.input.Controller;
import com.stabilise.input.GameControl;
import com.stabilise.item.IContainer;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.Log;
import com.stabilise.util.Printable;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;

/**
 * A PlayerController is an entity controller component which is managed by
 * player input.
 */
public class CPlayerController extends CController implements Controllable<GameControl>, InputProcessor {
    
    private Entity e;
    private CBaseMob mob;
    
    /** A reference to the PlayerController's controller. */
    public Controller<GameControl> controller;
    
    /** A reference to the game. (TODO: hopefully temporary?) */
    public final Game game;
    /** A reference to the world the player is in. */
    public World world;
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
     * @param controller The Controller which provides input for this
     * PlayerController.
     */
    public CPlayerController(Controller controller, Game game, World world) {
        this.controller = controller;
        this.game = game;
        this.world = world;
        
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
    public void update(World world, Entity e, float dt) {
        if(controller.isControlPressed(GameControl.LEFT) && controller.isControlPressed(GameControl.RIGHT))
            ;// do nothing
        else if(controller.isControlPressed(GameControl.LEFT))
            mob.move(Direction.LEFT);
        else if(controller.isControlPressed(GameControl.RIGHT))
            mob.move(Direction.RIGHT);
        
        if(controller.isControlPressed(GameControl.FLYRIGHT))
            e.dx += 1f;
        if(controller.isControlPressed(GameControl.FLYLEFT))
            e.dx -= 1f;
        if(controller.isControlPressed(GameControl.FLYUP))
            e.dy += 1f;
        if(controller.isControlPressed(GameControl.FLYDOWN))
            e.dy -= 1f;
        
        if(worldRenderer == null) {
            // TODO: Temporary way of grabbing the renderer
            worldRenderer = ((SingleplayerState)Application.get().getState()).renderer;
        }
        
        if(Gdx.input.isButtonPressed(Buttons.LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
            doInRadius(worldRenderer, world::breakTileAt);
        else if(Gdx.input.isButtonPressed(Buttons.RIGHT) && !Gdx.input.isKeyPressed(Keys.ALT_LEFT))
            doInRadius(worldRenderer, (pos) -> world.setTileAt(pos, tileID));
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
    public boolean handleControlPress(GameControl control, int screenX, int screenY, float amount) {
        switch(control) {
            case JUMP:
                mob.jump();
                break;
            case ATTACK:
                if(controller.isControlPressed(GameControl.UP))
                    mob.attack(world, Direction.UP);
                else if(controller.isControlPressed(GameControl.DOWN))
                    mob.attack(world, Direction.DOWN);
                else
                    mob.attack(world, mob.facingRight ? Direction.RIGHT : Direction.LEFT);
                break;
            case SPECIAL:
                if(controller.isControlPressed(GameControl.UP))
                    mob.specialAttack(world, Direction.UP);
                else if(controller.isControlPressed(GameControl.DOWN))
                    mob.specialAttack(world, Direction.DOWN);
                else
                    mob.specialAttack(world, mob.facingRight ? Direction.RIGHT : Direction.LEFT);
                break;
            case SUMMON:
                {
                    Entity m = Entities.enemy();
                    m.pos.set(e.pos, (mob.facingRight ? 5 : -5), 0f);
                    world.addEntity(m);
                }
                break;
            case SUMMON_SWARM:
                {
                    int max = 1000;
                    for(int i = 0; i < max; i++) {
                        Entity m = Entities.enemy();
                        m.pos.set(e.pos,
                                - 10 + world.rnd().nextFloat() * 20,
                                1 + world.rnd().nextFloat() * 10
                        );
                        world.addEntity(m);
                    }
                }
                break;
            case KILL_MOBS:
                if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    for(Entity en : world.getEntities()) {
                        if(en.core instanceof CBaseMob) {
                            en.core.damage(world, en, GeneralSource.voidDamage(9999));
                        }
                    }
                } else
                    world.asAbstract().destroyEntities();
                break;
            case RESTORE:
                mob.restore();
                break;
            case INTERACT:
                Position p = e.pos.clone().clampToTile().add(0,-1).align();
                world.getTileAt(p).handleInteract(world, p, e);
                break;
            case PREV_TILE:
                scrolled(0,-1);
                break;
            case NEXT_TILE:
                scrolled(0,1);
                break;
            case CLEAR_INVENTORY:
                if(e.core instanceof IContainer)
                    ((IContainer)e.core).clear();
                break;
            case PRINT_INVENTORY:
                Log.get().postInfo(e.core.toString());
                break;
            case PORTAL:
                String dim = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
                        ? null
                        : ( world.getDimensionName().equals("overworld")
                            ? game.playerData.data.getDimensionName()
                            : "overworld" );
                Entity pe = Entities.portal(dim);
                pe.pos.set(e.pos, mob.facingRight ? 3f : -3f, 1.5f).align();
                CPortal pc = (CPortal) pe.core;
                pc.rotation = mob.facingRight ? Maths.PIf : 0f;
                
                if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    // For now put the exit portal twice as far away
                    pc.otherPortalPos.set(pe.pos).addX(mob.facingRight ? 12f : -12f);
                } else
                	// if other dim, spawn the other portal at the same place
                	pc.otherPortalPos.set(pe.pos);
                
                if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
                    pc.doubleSided = true;
                
                world.addEntity(pe);
                break;
            case ROTATE_LEFT:
                Log.get().postDebug("ROTATE LEFTO");
                e.angle -= 90;
                e.upDirection.rotate90(-1);
                break;
            case ROTATE_RIGHT:
                e.angle += 90;
                e.upDirection.rotate90(1);
                break;
            case ROTATE_UP:
                e.angle += 180;
                e.upDirection.rotate90(1).rotate90(1); // I'm lazy
                break;
            case TEST_RANDOM:
                e.components.forEach(Printable::debugPrint);
                break;
            default:
                return false;
        }
        return false;
    }
    
    @Override
    public boolean handleControlRelease(GameControl control, int screenX, int screenY) {
        return false;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        // TODO awful temp code, should all go through handleControlPress
        if(keycode == Keys.LEFT_BRACKET)
            world.setTimeDelta(world.getTimeDelta() / 2);
        else if(keycode == Keys.RIGHT_BRACKET)
            world.setTimeDelta(world.getTimeDelta() * 2);
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
            String dim = world.getDimensionName().equals("overworld")
                    ? game.playerData.data.getDimensionName()
                    : "overworld";
            Entity pe = Entities.portal(dim);
            
            Position mouseCoords = worldRenderer.mouseCoords();
            pe.pos.set(mouseCoords);
            
            CPortal pc = (CPortal) pe.core;
            float dx = e.pos.diffX(mouseCoords);
            float dy = e.pos.diffY(mouseCoords);
            pc.rotation = MathUtils.atan2(-dy, -dx);
            
            if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
                pc.otherPortalPos.set(0, 0, 0f, 0f); // origin of other dim
            else
                // spawn the other portal at the same place
                pc.otherPortalPos.set(pe.pos);
            
            world.addEntity(pe);
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
    public boolean scrolled(float amountX, float amountY) {
        if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
            radius -= amountY;
            if(radius < 1)
                radius = 0.5f;
        } else
            tileID = 1 + Maths.remainder(tileID + (int)amountY - 1, maxTileID);
        return true;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type().equals(Type.THROUGH_PORTAL_INTER)) {
            world = w;
            this.e = e;
        }
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
