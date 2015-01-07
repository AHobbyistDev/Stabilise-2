package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.core.Constants;

/**
 * The ServerInfo packet is a packet which is sent by a server when a client
 * makes an initial connection. This is so that the client may know the
 * server's info.
 */
public class Packet001ServerInfo extends Packet {
	
	/** The name of the sever. */
	public String serverName = "";
	
	/** The server's release version. */
	public int serverRelease;
	/** The server's major patch version. */
	public int serverPatchMajor;
	/** The server's minor patch version. */
	public int serverPatchMinor;
	
	/** The number of online players. */
	public int onlinePlayers;
	/** The maximum number of players able to be online. */
	public int maxPlayers;
	
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		serverName = readString(in);
		
		serverRelease = in.read();
		serverPatchMajor = in.read();
		serverPatchMinor = in.read();
		
		onlinePlayers = in.read();
		maxPlayers = in.read();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		// Send the name of the sever
		writeString(serverName, out);
		
		// Send the version of the game being run by the server; we only want
		// clients with the same version connecting
		out.write(Constants.RELEASE);
		out.write(Constants.PATCH_MAJOR);
		out.write(Constants.PATCH_MINOR);
		
		out.write(onlinePlayers);
		out.write(maxPlayers);
	}
	
}
