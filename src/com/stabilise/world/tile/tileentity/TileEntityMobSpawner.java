package com.stabilise.world.tile.tileentity;

import com.stabilise.entity.EntityEnemy;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleGenerator;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.IWorld;

/**
 * The tile entity for mob spawners. For now I'm having this try to mimick
 * minecraft mob spawner behaviour to a fair extent.
 */
public class TileEntityMobSpawner extends TileEntity {
	
	private static final int ACTIVATION_RANGE_SQUARED = 16*16;
	private static final int TICKS_BETWEEN_SPAWNS = 180;
	private static final int MIN_SPAWNS = 1;
	private static final int MAX_SPAWNS = 2;
	
	private int ticksUntilNextSpawn = TICKS_BETWEEN_SPAWNS;
	private double xPos, yPos;
	
	
	/**
	 * Creates a new mob spawner tile entity.
	 * 
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public TileEntityMobSpawner(int x, int y) {
		super(x, y);
		init();
	}
	
	/**
	 * Creates a new mob spawner tile entity.
	 * 
	 * @param world The world in which the tile entity is to be placed.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public TileEntityMobSpawner(IWorld world, int x, int y) {
		super(world, x, y);
		init();
	}
	
	/**
	 * Sets up the mob spawner.
	 */
	private void init() {
		xPos = x + 0.5;
		yPos = y + 0.5;
	}
	
	@Override
	public void update() {
		if(playerInRange()) {
			if(--ticksUntilNextSpawn == 0) {
				ticksUntilNextSpawn = TICKS_BETWEEN_SPAWNS;
				
				int spawns = MIN_SPAWNS + world.getRnd().nextInt(1 + MAX_SPAWNS - MIN_SPAWNS);
				while(spawns-- > 0)
					trySpawn();
			}
			
			if(world.getRnd().nextInt(5) == 0)
			spawnParticle();
		}
	}
	
	/**
	 * Checks for whether or not a player is in range of the spawner.
	 * 
	 * @return {@code true} if a player is in range; {@code false} otherwise.
	 */
	private boolean playerInRange() {
		for(EntityMob p : world.getPlayers()) {
			double dx = xPos - p.x;
			double dy = yPos - p.y;
			if(dx*dx + dy*dy <= ACTIVATION_RANGE_SQUARED)
				return true;
		}
		return false;
	}
	
	/**
	 * Tries to spawn a mob.
	 * 
	 * @return {@code true} if the spawn was successful; {@code false}
	 * otherwise.
	 */
	private boolean trySpawn() {
		EntityEnemy e = new EntityEnemy(world);
		world.addEntity(e, xPos, yPos+1);
		
		for(int i = 0; i < 10; i++) {
			spawnParticle();
			spawnParticleOnMob(e);
		}
		
		return true;
	}
	
	/**
	 * Spawns a flame particle on the spawner.
	 */
	private void spawnParticle() {
		ParticleFlame p = new ParticleFlame(world);
		p.x = x + world.getRnd().nextFloat();
		p.y = y + world.getRnd().nextFloat();
		
		ParticleGenerator.directParticle(p, 0.02f, 0.07f, Math.PI / 6.0D, Math.PI * 5.0D / 6.0D);
		
		world.addParticle(p);
	}
	
	/**
	 * Spawns a flame particle on the spawned mob.
	 * 
	 * @param e The mob.
	 */
	private void spawnParticleOnMob(EntityMob e) {
		ParticleSmoke p = new ParticleSmoke(world);
		p.x = e.x + e.boundingBox.getV00().x + world.getRnd().nextFloat() * e.boundingBox.width;
		p.y = e.y + e.boundingBox.getV11().y + world.getRnd().nextFloat() * e.boundingBox.height;
		
		ParticleGenerator.directParticle(p, 0.04f, 0.12f, 0, MathsUtil.TAU);
		p.dy *= 0.1;
		
		world.addParticle(p);
	}
	
	@Override
	public void handleAdd(IWorld world, int x, int y) {
		
	}
	
	@Override
	public void handleRemove(IWorld world, int x, int y) {
		
	}
	
	@Override
	protected void writeNBT(NBTTagCompound tag) {
		tag.addInt("ticksUntilNextSpawn", ticksUntilNextSpawn);
	}
	
	@Override
	public void fromNBT(NBTTagCompound tag) {
		ticksUntilNextSpawn = tag.getInt("ticksUntilNextSpawn");
	}
	
}
