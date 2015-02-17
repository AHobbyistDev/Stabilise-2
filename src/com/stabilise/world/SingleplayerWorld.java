package com.stabilise.world;

import com.stabilise.character.CharacterData;


public class SingleplayerWorld extends ClientWorld<HostWorld> {
	
	/** Manages slices 'loaded' about the player. */
	private SliceMap sliceMap;
	
	
	/**
	 * Creates a new SingleplayerWorld.
	 * 
	 * @param world The underlying world intended for the client.
	 * @param playerData The data of the player using this world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public SingleplayerWorld(HostWorld world, CharacterData playerData) {
		super(world, playerData);
	}
	
	@Override
	protected void addPlayerAsHost(HostWorld world) {
		super.addPlayerAsHost(world);
		sliceMap = new SliceMap(world, player);
	}
	
	@Override
	public void update() {
		super.update();
		
		world.profiler.start("sliceMap"); // root.update.game.world.sliceMap
		sliceMap.update();
		world.profiler.end(); // root.update.game.world
	}
	
}
