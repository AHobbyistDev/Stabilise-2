package com.stabilise.tests.network;

import java.io.*;
import java.net.*;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.C001Disconnect;
import com.stabilise.network.protocol.handshake.S000VersionInfo;
import com.stabilise.util.Log;

public class Client {
	
	private Socket socket;
	public TCPConnection connection;
	
	private Log log = Log.getAgent("CLIENT");
	
	public Client() {
		try {
			log.postInfo("Initiating client...");
			socket = new Socket(InetAddress.getLocalHost(), DEFAULT_PORT);
			
			connection = new TCPConnection(socket, false);
			
			log.postInfo("Connected to server on " + socket.getLocalSocketAddress());
			log.postInfo("Sending version info...");
			
			connection.sendPacket(new C000VersionInfo().setVersionInfo());
		} catch (IOException e) {
			log.postSevere("Error creating client-server connection!");
			if(socket != null) {
				if(connection != null) {
					connection.closeConnection();
				} else {
					try {
						socket.close();
					} catch (IOException e1) { 
						// asdfghjkl
					}
				}
			}
			
			close();
			
			return;
		}
	}
	
	/**
	 * The main client update method - this should be called by the main game
	 * update loop.
	 */
	public void update() {
		try {
			Packet packet;
			while((packet = connection.getPacket()) != null)
				handlePacket(packet);
		} catch(Exception e) {
			log.postSevere("Client encountered error!", e);
			close();
		}
	}
	
	/**
	 * Closes the connection with the server.
	 */
	public void close() {
		log.postInfo("Closing client...");
		if(connection != null)
			connection.closeConnection();
	}
	
	/**
	 * Handles all received packets by redirecting them to their appropriate
	 * handler method.
	 * 
	 * @param packet The packet to handle.
	 */
	private void handlePacket(Packet packet) {
		if(packet.getClass() == S000VersionInfo.class)
			handleServerInfo((S000VersionInfo)packet);
		else
			log.postWarning("Unrecognised packet " + packet);
	}
	
	private void handleServerInfo(S000VersionInfo packet) {
		log.postInfo("Got info from server - " + packet.isCompatible());
		log.postInfo("Sending disconnect...");
		connection.sendPacket(new C001Disconnect());
		close();
	}
	
}
