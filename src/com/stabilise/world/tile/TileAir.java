package com.stabilise.world.tile;

import com.stabilise.entity.EntityEnemy;
import com.stabilise.world.AbstractWorld;

/**
 * An air tile is effectively an empty tile.
 */
public class TileAir extends Tile {
	
	/**
	 * Creates an air tile.
	 */
	TileAir() {
		super();
		solid = false;
	}
	
	@Override
	public void update(AbstractWorld world, int x, int y) {
		Tile t = world.getTileAt(x, y-1);
		if(t.solid && t.getID() != Tiles.BEDROCK_INVISIBLE.getID()) {
			// spawn a guy
			//if(world.rng.nextFloat() < 1.0f/*0.002f*/)
			world.spawnMob(new EntityEnemy(world), x + 0.5D, y);
		}
	}
	
}
