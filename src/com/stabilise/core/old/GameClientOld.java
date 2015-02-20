package com.stabilise.core.old;

import java.io.*;
import java.net.*;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import com.stabilise.core.Constants;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.packet.*;
import com.stabilise.util.Log;
import com.stabilise.world.Slice;
import com.stabilise.world.old.WorldClientOld;

/**
 * The game client.
 * 
 * @deprecated Due to the removal of networking architecture.
 */
public class GameClientOld {
	
	/** The socket through which the client will communicate with the server. */
	private Socket socket;
	/** The TCPConnection object through which the client will communicate with
	 * the server. */
	private TCPConnection connection;
	
	/** Whether or not the client is currently in-game. */
	public boolean running;
	/** Whether or not the client is currently paused. */
	public boolean paused = false;
	
	/** The client's world instance. */
	private WorldClientOld world;
	
	/** The client's logging agent. */
	private Log log = Log.getAgent("CLIENT");
	
	/**
	 * Creates a new GameClient instance.
	 * Note this should ideally be created <i>after</i> the server.
	 */
	public GameClientOld() {
		try {
			log.postInfo("Initiating client...");
			socket = new Socket(InetAddress.getLocalHost(), DEFAULT_PORT);
			
			connection = new TCPConnection(socket, false);
			
			// Get the server info; we're going to block until the info is
			// obtained.
			// If the server is running a different game version, the client
			// will abort.
			/*
			if(!handlePacketServerInfo((Packet001ServerInfo)connection.getPacketWithBlock())) {
				close();
				return;
			}
			*/
			
			log.postDebug("Got server data!");
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
			//Stabilise.crashGame(e);
			
			return;
		}
	}
	
	/**
	 * Makes the client attempt to join the server - that is, log in.
	 */
	public void joinServer() {
		// Queue the login packet
		connection.queuePacket(new Packet002Login("Player"));
		
		// Time to initialise everything...
		world = new WorldClientOld(this);
		
		running = true;
	}
	
	/**
	 * The main client update method - this should be called by the main game
	 * update loop.
	 */
	public void update() {
		if(running) {
			try {
				Packet packet;
				while((packet = connection.getPacket()) != null) {
					handlePacket(packet);
				}
				
				if(!paused)
					world.update();
				
				connection.queuePacket(getPacketPlayerPosition());
				
				// TODO: Send packets and whatnot
				
			} catch(Exception e) {
				log.postSevere("Client encountered error!");
				e.printStackTrace();	// temporary
				close();
				return;
			}
		}
	}
	
	/**
	 * Closes the connection with the server.
	 */
	public void close() {
		running = false;
		if(connection != null)
			connection.closeConnection();
	}
	
	/**
	 * Returns the world, as viewed by the client.
	 * 
	 * @return The game world, as viewed by the client.
	 */
	public WorldClientOld getWorld() {
		return world;
	}
	
	/**
	 * Toggles the pause state of the game.
	 * Note that this will do nothing in multiplayer.
	 */
	public void togglePause() {
		paused = !paused;
		connection.queuePacket(getPacketPause());
	}
	
	//--------------------==========--------------------
	//-----------=====Generating Packets=====-----------
	//--------------------==========--------------------
	
	/**
	 * Generates a new packet to inform the server of the player's position.
	 * 
	 * @return The player position packet.
	 */
	private Packet006PlayerPosition getPacketPlayerPosition() {
		return new Packet006PlayerPosition(world.player.x, world.player.y);
	}
	
	/**
	 * Generates a new packet to inform the server of the client's pause state.
	 */
	private Packet253Pause getPacketPause() {
		return new Packet253Pause(paused);
	}
	
	//--------------------==========--------------------
	//------------=====Handling Packets=====------------
	//--------------------==========--------------------
	
	/**
	 * Handles all received packets by redirecting them to their appropriate
	 * handler method.
	 * 
	 * @param packet The packet to handle.
	 */
	private void handlePacket(Packet packet) {
		switch(packet.getID()) {
			//case 1:		// Packet001ServerInfo
			//	break;
			case 3:			// Packet003LoginInfo
				handlePacketLoginInfo((Packet003LoginInfo)packet);
				break;
			case 4:			// Packet004Slice
				handlePacketSlice((Packet004Slice)packet);
				break;
			case 6:			// Packet006PlayerPosition
				handlePacketPlayerPosition((Packet006PlayerPosition)packet);
				break;
			case 254:		// Packet254Disconnect
				handlePacketDisconnect((Packet254Disconnect)packet);
				break;
			case 255:		// Packet255RequestPacket
				break;
		}
	}
	
	/**
	 * Handles the info packet sent by a server once a connection is
	 * established.
	 * 
	 * @param packet The server info packet.
	 * 
	 * @return Returns true if the client and server are running compatible
	 * versions. (Note that for now, compatible implies same versions.)
	 */
	@SuppressWarnings("unused")
	private boolean handlePacketServerInfo(Packet001ServerInfo packet) {
		boolean serverOutdated = false;
		boolean clientOutdated = false;
		
		// Here we'll ensure that the client and server's game versions match up
		if(packet.serverRelease > Constants.RELEASE) {
			clientOutdated = true;
		} else if(packet.serverRelease < Constants.RELEASE) {
				serverOutdated = true;
		} else {
			if(packet.serverPatchMajor > Constants.PATCH_MAJOR) {
				clientOutdated = true;
			} else if(packet.serverPatchMajor < Constants.PATCH_MAJOR) {
				serverOutdated = true;
			} else {
				if(packet.serverPatchMinor > Constants.PATCH_MINOR) {
					clientOutdated = true;
				} else if(packet.serverPatchMinor < Constants.PATCH_MINOR) {
					serverOutdated = true;
				}
			}
		}
		
		if(serverOutdated) {
			log.postWarning("Outdated server!");
			return false;
		} else if(clientOutdated) {
			log.postWarning("Outdated client!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Handles the login info packet sent by the server.
	 * 
	 * @param packet The login info packet.
	 */
	private void handlePacketLoginInfo(Packet003LoginInfo packet) {
		world.addPlayer(packet.id, packet.spawnX, packet.spawnY);
		
		// Now that the client knows the player's position, it's free to start
		// requesting slices.
		world.slices.init();
	}
	
	/**
	 * Handles a slice data packet sent by the server.
	 * 
	 * @param packet The slice packet.
	 */
	private void handlePacketSlice(Packet004Slice packet) {
		Slice slice = new Slice(packet.x, packet.y, null);
		slice.setTilesAsIntArray(packet.tiles);
		world.addSlice(slice);
	}
	
	/**
	 * Handles a player position packet from the server.
	 * 
	 * @param packet The player position packet.
	 */
	private void handlePacketPlayerPosition(Packet006PlayerPosition packet) {
		// TODO: implement
	}
	
	/**
	 * Handles a disconnection packet from the server.
	 * 
	 * @param packet The disconnect packet.
	 */
	private void handlePacketDisconnect(Packet254Disconnect packet) {
		log.postInfo("Was kicked for reason \"" + packet.reason + "\"");
		close();
	}
	
	//--------------------==========--------------------
	//------------------=====Misc=====------------------
	//--------------------==========--------------------
	
	/**
	 * This method has the client request the server for a slice.
	 * 
	 * @param x The slice's x-coordinate, in slice-lengths.
	 * @param y The slice's y-coordinate, in slice-lengths.
	 */
	public void requestSlice(int x, int y) {
		//connection.queuePacket(new Packet252SliceRequest(true, x, y));
	}
	
	/**
	 * This method has the client notify the server of a slice having been
	 * unloaded.
	 * 
	 * @param x The slice's x-coordinate, in slice-lengths.
	 * @param y The slice's y-coordinate, in slice-lengths.
	 */
	public void notifyOfSliceUnload(int x, int y) {
		//connection.queuePacket(new Packet252SliceRequest(false, x, y));
	}
	
}
