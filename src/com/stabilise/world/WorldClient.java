package com.stabilise.world;

import java.util.Collection;

import com.stabilise.character.CharacterData;
import com.stabilise.entity.particle.Particle;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;


public class WorldClient extends BaseWorld {
	
	public WorldClient() {
		
	}
	
	@Override
	public Collection<Particle> getParticles() {
		return null;
	}
	
	@Override
	public void addPlayer(CharacterData player) {
		
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		return null;
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		return null;
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		return null;
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		
	}
	
	@Override
	public void breakTileAt(int x, int y) {
		
	}
	
	@Override
	public TileEntity getTileEntityAt(int x, int y) {
		return null;
	}
	
	@Override
	public void setTileEntityAt(int x, int y, TileEntity t) {
		
	}
	
	@Override
	public void removeTileEntityAt(int x, int y) {
		
	}
	
	@Override
	public void blowUpTile(int x, int y, float explosionPower) {
		
	}
	
}
