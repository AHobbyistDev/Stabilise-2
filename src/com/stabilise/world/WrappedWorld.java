package com.stabilise.world;

import java.util.Collection;

import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.collision.Hitbox;
import com.stabilise.entity.particle.Particle;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;


public class WrappedWorld extends AbstractWorld {
	
	/** The world being wrapped. */
	protected BaseWorld world;
	
	
	public WrappedWorld(BaseWorld world) {
		this.world = world;
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
	public void removeParticle(Particle p) {
		world.removeParticle(p);
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
	
}
