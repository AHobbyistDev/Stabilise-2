package com.stabilise.network;

import java.io.IOException;
import java.net.Socket;

/**
 * The GameServerConnection class is essentially a TCPConnection, but with
 * extended functionality such that servers can more easily manage client
 * interaction.
 */
public class ServerTCPConnection extends TCPConnection {
	
	/** True if the client is logged in, and false if they're waiting at the
	 * server select screen or for their login request to be validated. */
	public boolean loggedIn = false;
	
	/** The name of the player using this connection. */
	public String playerName = "";
	/** The player's entity's ID. */
	public int id;
	/** The ID of the client. */
	private int hash;
	
	/** The tick number to which the client is currently sending packets for. */
	public long tick = 0;
	
	
	/**
	 * Creates a new ServerTCPConnection.
	 * 
	 * @param socket The socket upon which to base the connection.
	 * 
	 * @throws IOException Thrown if the connection could not be created.
	 */
	public ServerTCPConnection(Socket socket) throws IOException {
		super(socket, true);
		
		hash = socket.hashCode();
	}
	
	//--------------------==========--------------------
	//---------=====Getter/Setter Wrappers=====---------
	//--------------------==========--------------------
	
	/**
	 * Gets the client's hash.
	 * 
	 * @return The client's hash.
	 */
	public int getHash() {
		return hash;
	}
	
}
