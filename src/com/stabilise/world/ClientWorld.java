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
 * 
 * @param <T> Use {@link SingleplayerWorld} for singleplayer, and {@link
 * MultiplayerClientWorld} for a multiplayer client.
 */
public class ClientWorld<T extends BaseWorld & IClientWorld>
		extends WrappedWorld<T> implements IClientWorld {
	
	/** The player's character data. */
	private final CharacterData playerData;
	/** Holds a direct reference to the player controlled by the client. */
	public EntityMob player;
	/** The camera which follows the client's player. */
	public GameCamera camera;
	
	/** LinkedHashSet of particles. */
	public final Set<Particle> particles = new LinkedHashSet<Particle>();
	/** The total number of particles which have existed during the lifetime of
	 * the world. */
	public int particleCount = 0;
	
	
	/**
	 * Creates a new ClientWorld.
	 * 
	 * @param world The underlying world intended for the client.
	 * @param playerData The data of the player using this world.
	 */
	public ClientWorld(T world, CharacterData playerData) {
		super(world);
		this.playerData = playerData;
	}
	
	@Override
	public void prepare() {
		// TODO: deal with the multiplayer client case
		if(world instanceof HostWorld) {
			HostWorld host = (HostWorld)world;
			player = host.addPlayer(playerData, this);
			camera = new GameCamera(this, player);
		}
		
		super.prepare();
	}
	
	@Override
	public void update() {
		super.update();
		
		world.profiler.start("particles"); // root.update.game.world.particles
		updateObjects(getParticles());
		
		world.profiler.next("camera"); // root.update.game.world.camera
		camera.update();
		
		world.profiler.end(); // root.update.game.world
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
