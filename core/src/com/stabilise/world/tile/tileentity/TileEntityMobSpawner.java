package com.stabilise.world.tile.tileentity;

import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.entity.particle.manager.ParticleEmitter;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;
import com.stabilise.world.tile.tileentity.TileEntity.Updated;

/**
 * The tile entity for mob spawners. For now I'm having this try to mimic
 * minecraft mob spawner behaviour to a fair extent.
 */
public class TileEntityMobSpawner extends TileEntity implements Updated {
    
    private static final int ACTIVATION_RANGE_SQUARED = 16*16;
    private static final int TICKS_BETWEEN_SPAWNS = 180;
    private static final int TICKS_BETWEEN_EXTRA = 120;
    private static final int MIN_SPAWNS = 1;
    private static final int MAX_SPAWNS = 2;
    
    private int ticksUntilNextSpawn = TICKS_BETWEEN_SPAWNS;
    private final Position centrePos = Position.create();
    
    private ParticleEmitter<?> fireGen;
    private ParticleEmitter<?> smokeGen;
    
    
    @Override
    protected void update(World w) {
        if(playerInRange(w)) {
            // Ugly way of lazily initialising...
            if(fireGen == null) {
                fireGen = w.particleEmitter(ParticleFlame.class);
                smokeGen = w.particleEmitter(ParticleSmoke.class);
            }
            
            if(--ticksUntilNextSpawn == 0) {
                ticksUntilNextSpawn = TICKS_BETWEEN_SPAWNS + w.rnd().nextInt(TICKS_BETWEEN_EXTRA);
                
                int spawns = MIN_SPAWNS + w.rnd().nextInt(1 + MAX_SPAWNS - MIN_SPAWNS);
                while(spawns-- > 0)
                    trySpawn(w);
            }
            
            if(w.rnd().nextInt(5) == 0)
                spawnParticle(w);
        }
    }
    
    /**
     * Checks for whether or not a player is in range of the spawner.
     * 
     * @return {@code true} if a player is in range; {@code false} otherwise.
     */
    private boolean playerInRange(World world) {
        return world.getPlayers().any(p -> centrePos.diffSq(p.pos) <= ACTIVATION_RANGE_SQUARED);
    }
    
    /**
     * Tries to spawn a mob.
     * 
     * @return {@code true} if the spawn was successful; {@code false}
     * otherwise.
     */
    private boolean trySpawn(World world) {
        Entity e = Entities.enemy();
        e.pos.set(centrePos).add(0f, 0.5f);
        world.addEntity(e);
        
        for(int i = 0; i < 10; i++) {
            spawnParticle(world);
            spawnParticleOnMob(world, e);
        }
        
        return true;
    }
    
    /**
     * Spawns a flame particle on the spawner.
     */
    private void spawnParticle(World world) {
        fireGen.createBurstOnTile(1, pos, 0.02f, 0.07f,
                (float)Math.PI / 6.0f, (float)Math.PI * 5.0f / 6.0f);
    }
    
    /**
     * Spawns a flame particle on the spawned mob.
     * 
     * @param e The mob.
     */
    private void spawnParticleOnMob(World world, Entity e) {
        smokeGen.createBurst(1, 0.04f, 0.12f, 0, (float)Maths.TAU, e);
    }
    
    @Override
    public void handleAdd(World world) {
        centrePos.set(pos).clampToTile().add(0.5f, 0.5f);
    }
    
    @Override
    public void handleRemove(World world) {
        // nothing to see here, move along
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        super.importFromCompound(c);
        
        ticksUntilNextSpawn = c.getI32("ticksUntilNextSpawn");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        super.exportToCompound(c);
        
        c.put("ticksUntilNextSpawn", ticksUntilNextSpawn);
    }
    
}
