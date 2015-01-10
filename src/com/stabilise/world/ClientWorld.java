package com.stabilise.world;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.stabilise.character.CharacterData;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.GameCamera;
import com.stabilise.entity.particle.Particle;


/**
 * The world as viewed by a client.
 */
public class ClientWorld extends WrappedWorld {
	
	/** The player's character. */
	private CharacterData playerChar;
	/** The player. */
	public EntityMob player;
	/** The game camera. */
	public GameCamera camera;
	
	/** LinkedHashSet of particles. */
	public final Set<Particle> particles = new LinkedHashSet<Particle>();
	/** The total number of particles which have existed during the lifetime of
	 * the world. */
	public int particleCount = 0;
	
	
	/**
	 * Use this constructor for a multiplayer client.
	 */
	public ClientWorld(MultiplayerClientWorld world) {
		super(world);
	}
	
	/**
	 * Use this constructor for a singleplayer world.
	 */
	public ClientWorld(HostWorld world) {
		super(world);
	}
	
	@Override
	public void update() {
		super.update();
		
		world.profiler.start("particles");
		updateObjects(getParticles());
		
		world.profiler.next("camera");
		camera.update();
		
		world.profiler.end();
	}
	
	@Override
	public void addParticle(Particle p) {
		particleCount++;
		particles.add(p);
	}
	
	@Override
	public void removeParticle(Particle p) {
		particles.remove(p);
	}
	
	@Override
	public Collection<Particle> getParticles() {
		return particles;
	}
	
}
