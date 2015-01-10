package com.stabilise.world;

import java.util.Collection;

import com.stabilise.character.CharacterData;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;


public class SingleplayerWorld extends HostWorld implements IClientWorld {
	
	/** Manages slices 'loaded' about the player. */
	private SliceMap sliceMap;
	
	
	/**
	 * Creates a new SingleplayerWorld.
	 * 
	 * @param info The world's info.
	 * @param profiler The profiler to use for profiling the world.
	 * @param log The log to use for the world.
	 * 
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public SingleplayerWorld(WorldInfo info, Profiler profiler, Log log) {
		super(info, profiler, log);
	}
	
	@Override
	public void setClientPlayer(CharacterData data, EntityMob mob) {
		loadCharacterData(data);
		if(data.newToWorld) {
			// TODO: For now I'm placing the character at (0,0) of the spawn
			// slice. In practice, we'll need to check to see whether or not
			// this location is valid, and keep searching until a valid
			// location is found.
			data.lastX = tileCoordFromSliceCoord(info.spawnSliceX);
			data.lastY = tileCoordFromSliceCoord(info.spawnSliceY);
			data.newToWorld = false;
			saveCharacterData(data);
		}
	}
	
	@Override
	public void saveClientPlayer(CharacterData data, EntityMob mob) {
		data.lastX = mob.x;
		data.lastY = mob.y;
		saveCharacterData(data);
	}
	
	@Override
	public void update() {
		super.update();
		
		profiler.start("sliceMap");
		sliceMap.update();
		profiler.end();
	}
	
	@Override
	public void addParticle(Particle p) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void removeParticle(Particle p) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Collection<Particle> getParticles() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void save() {
		super.save();
	}
	
}
