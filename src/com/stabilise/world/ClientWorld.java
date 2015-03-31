package com.stabilise.world;

import com.stabilise.entity.Entity;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.provider.WorldProvider;
import com.stabilise.world.tile.tileentity.TileEntity;

public class ClientWorld extends AbstractWorld {

	public ClientWorld(WorldProvider<? extends AbstractWorld> provider,
			Dimension dimension) {
		super(provider, dimension);
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		return null;
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		
	}
	
	@Override
	public void breakTileAt(int x, int y) {
		
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
	
	@Override
	public void sendToDimension(String dimension, Entity e, double x, double y) {
		
	}
	
	@Override
	public boolean updateAndCheck() {
		return false;
	}
	
	@Override
	public void prepare() {

	}
	
	@Override
	public boolean isLoaded() {
		return false;
	}
	
	@Override
	public void save() {

	}
	
	@Override
	public void close() {

	}
	
}
