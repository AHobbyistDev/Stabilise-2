package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;

/**
 * This packet contains the data sent from the client to a server when the
 * client attempts to log in.
 */
public class Packet002Login extends Packet {
	
	/** The name of the player making the login request. */
	public String playerName;
	
	
	public Packet002Login() {}
	
	/**
	 * Creates a new login request packet.
	 * 
	 * @param playerName The name of the player attempting to log in.
	 */
	public Packet002Login(String playerName) {
		this.playerName = playerName;
	}

	@Override
	public void readData(DataInputStream in) throws IOException {
		playerName = readString(in);
	}

	@Override
	public void writeData(DataOutputStream out) throws IOException {
		writeString(playerName, out);
	}
	
}
