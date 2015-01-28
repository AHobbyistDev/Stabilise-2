package com.stabilise.world;


/**
 * Defines methods required for a client world to function.
 */
interface IClientWorld extends IWorld {
	
	/**
	 * Sets the client's player entity and adds it to the world.
	 * 
	 * @param data The client's character data.
	 * @param mob The player mob.
	 */
	//void addClientPlayer(CharacterData data, EntityMob mob);
	
	/**
	 * Saves the data for the client player.
	 * 
	 * <p>TODO: This is only really a thing for singleplayer, and I'm just
	 * emulating the old GameWorld code at the moment.
	 * 
	 * @param data The client's character data.
	 * @param mob The player mob.
	 */
	//void saveClientPlayer(CharacterData data, EntityMob mob);
	
}
