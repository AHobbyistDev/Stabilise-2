package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This packet is sent by the server to provide the client with all necessary
 * initial data to begin loading the world.
 * The login info packet essentially serves as validation that the client's
 * attempt to join the server was successful.
 */
public class Packet003LoginInfo extends Packet {
	
	/** The ID of the player entity. */
	public int id;
	/** The player's spawn position on the x-axis. */
	public double spawnX;
	/** The player's spawn position on the y-axis. */
	public double spawnY;
	
	
	/**
	 * Creates a new generic login info packet.
	 */
	public Packet003LoginInfo() {
		
	}
	
	/**
	 * Creates a new login info packet.
	 * 
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

	@Override
	public int getBytes() {
		return (2 * Double.SIZE + Integer.SIZE) / Byte.SIZE;
	}

}
