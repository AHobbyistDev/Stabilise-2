package com.stabilise.world;

import java.util.Collection;
import java.util.Random;

import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;


/**
 * A WorldWrapper wraps another world and delegates all implemented methods.
 * 
 * <p>This class is intended to allow worlds of different types have unified
 * functionality under the same wrapper.
 * 
 * @param <T> The type of world to wrap.
 */
public class WorldWrapper<T extends BaseWorld> extends AbstractWorld {
	
	/** The world being wrapped. */
	public final T world;
	
	
	/**
	 * @param world The world being wrapped.
	 * 
	 * @throws NullPointerException if {@code world} is {@code null}.
	 */
	public WorldWrapper(T world) {
		if(world == null)
			throw new NullPointerException("world is null");
		this.world = world;
	}
	
	@Override
	public void prepare() {
		world.prepare();
	}
	
	@Override
	public boolean isLoaded() {
		return world.isLoaded();
	}
	
	@Override
	public void update() {
		world.update();
	}
	
	@Override
	public void setPlayer(EntityMob m) {
		world.setPlayer(m);
	}
	
	@Override
	public void unsetPlayer(EntityMob m) {
		world.unsetPlayer(m);
	}
	
	@Override
	public void addEntity(Entity e) {
		world.addEntity(e);
	}
	
	@Override
	public void removeEntity(Entity e) {
		world.removeEntity(e);
	}
	
	@Override
	public void removeEntity(int id) {
		world.removeEntity(id);
	}
	
	@Override
	public void addHitbox(Hitbox h) {
		world.addHitbox(h);
	}
	
	@Override
	public void addParticle(Particle p) {
		world.addParticle(p);
	}
	
	@Override
	public Collection<EntityMob> getPlayers() {
		return world.getPlayers();
	}
	
	@Override
	public Collection<Entity> getEntities() {
		return world.getEntities();
	}
	
	@Override
	public Collection<Hitbox> getHitboxes() {
		return world.getHitboxes();
	}
	
	@Override
	public Collection<TileEntity> getTileEntities() {
		return world.getTileEntities();
	}
	
	@Override
	public Collection<Particle> getParticles() {
		return world.getParticles();
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		return world.getSliceAt(x, y);
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		return world.getSliceAtTile(x, y);
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		return world.getTileAt(x, y);
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		world.setTileAt(x, y, id);
	}
	
	@Override
	public void breakTileAt(int x, int y) {
		world.breakTileAt(x, y);
	}
	
	@Override
	public TileEntity getTileEntityAt(int x, int y) {
		return world.getTileEntityAt(x, y);
	}
	
	@Override
	public void setTileEntityAt(int x, int y, TileEntity t) {
		world.setTileEntityAt(x, y, t);
	}
	
	@Override
	public void removeTileEntityAt(int x, int y) {
		world.removeTileEntityAt(x, y);
	}
	
	@Override
	public void blowUpTile(int x, int y, float explosionPower) {
		world.blowUpTile(x, y, explosionPower);
	}
	
	@Override
	public float getGravity() {
		return world.getGravity();
	}
	
	@Override
	public float getGravityIncrement() {
		return world.getGravityIncrement();
	}
	
	@Override
	public float getGravity2ndOrder() {
		return world.getGravity2ndOrder();
	}
	
	@Override
	public void setTimeDelta(float delta) {
		world.setTimeDelta(delta);
	}
	
	@Override
	public float getTimeDelta() {
		return world.getTimeDelta();
	}
	
	@Override
	public float getTimeIncrement() {
		return world.getTimeIncrement();
	}
	
	@Override
	public Random getRnd() {
		return world.getRnd();
	}
	
	@Override
	public void save() {
		world.save();
	}
	
	@Override
	public void close() {
		world.close();
	}
	
}
