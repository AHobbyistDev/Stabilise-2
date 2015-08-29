package com.stabilise.world.tile;

/**
 * An air tile is effectively an empty tile.
 */
public class TileAir extends Tile {
	
	/**
	 * Creates an air tile.
	 */
	TileAir(TileBuilder b) {
		super(b.solid(false));
	}
	
	/*
	@Override
	public void update(World world, int x, int y) {
		Tile t = world.getTileAt(x, y-1);
		if(t.isSolid() && !t.equals(Tiles.BEDROCK_INVISIBLE)) {
			// spawn a guy
			//if(world.rng.nextFloat() < 0.002f)
			
			// TODO
			//world.spawnMob(new EntityEnemy(world), x + 0.5D, y);
		}
	}
	*/
	
}
