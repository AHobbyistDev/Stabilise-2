package com.stabilise.world.old;

import java.util.Collection;
import java.util.List;

import com.stabilise.character.CharacterData;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.collect.LightweightLinkedList;
import com.stabilise.world.BaseWorld;
import com.stabilise.world.HostWorld;

/**
 * The world as viewed by a client.
 * 
 * @param <W> Use {@link HostWorld} for singleplayer, and {@link
 * MultiplayerClientWorld} for a multiplayer client.
 */
public class ClientWorld<W extends BaseWorld> extends WorldWrapper<W> {
	
	/** The player's character data. */
	private final CharacterData playerData;
	/** Holds a direct reference to the player controlled by the client. */
	public EntityMob player;
	
	/** LightweightLinkedList of particles. */
	public final List<Particle> particles = new LightweightLinkedList<Particle>();
	/** The total number of particles which have existed during the lifetime of
	 * the world. */
	public int particleCount = 0;
	
	
	/**
	 * Creates a new ClientWorld.
	 * 
	 * @param world The underlying world intended for the client.
	 * @param playerData The data of the player using this world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public ClientWorld(W world, CharacterData playerData) {
		super(world);
		if(playerData == null)
			throw new NullPointerException("playerData is null");
		this.playerData = playerData;
	}
	
	
	public void prepare() {
		if(world instanceof HostWorld)
			addPlayerAsHost((HostWorld)world);
		else if(world instanceof MultiplayerClientWorld)
			addPlayerAsClient((MultiplayerClientWorld)world);
	}
	
	protected void addPlayerAsHost(HostWorld world) {
		player = world.addPlayer(this, playerData, 0D, 0D);
	}
	
	protected void addPlayerAsClient(MultiplayerClientWorld world) {
		// TODO
	}
	
	@Override
	public void addParticle(Particle p) {
		particleCount++;
		particles.add(p);
	}
	
	@Override
	public Collection<Particle> getParticles() {
		return particles;
	}
	
}
