package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;

/**
 * This packet is sent by the server to provide the client with all necessary
 * initial data to begin loading the world.
 */
public class Packet003LoginInfo extends Packet {
	
	/** The ID of the player entity. */
	public int id;
	/** The player's spawn location. */
	public double spawnX, spawnY;
	
	
	public Packet003LoginInfo() {}
	
	/**
	 * @param id The ID of the player entity.
	 * @param spawnX The x-coordinate of the player entity.
	 * @param spawnY The y-coordinate of the player entity.
	 */
	public Packet003LoginInfo(int id, double spawnX, double spawnY) {
		this.id = id;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		id = in.readInt();
		spawnX = in.readDouble();
		spawnY = in.readDouble();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeDouble(spawnX);
		out.writeDouble(spawnY);
	}
	
}
