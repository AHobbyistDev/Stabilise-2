package com.stabilise.world;

import com.stabilise.character.CharacterData;
import com.stabilise.entity.EntityMob;

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
	void setClientPlayer(CharacterData data, EntityMob mob);
	
}
