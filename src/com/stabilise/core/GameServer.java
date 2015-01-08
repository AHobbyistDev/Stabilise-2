package com.stabilise.core;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import com.stabilise.network.ClientListenerThread;
import com.stabilise.network.ServerTCPConnection;
import com.stabilise.network.packet.*;
import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;
import com.stabilise.world.Slice;
import com.stabilise.world.WorldServer;

/**
 * The game server.
 * 
 * @deprecated Due to the removal of networking architecture.
 */
public class GameServer implements Runnable {
	
	/** The list of client connections. */
	private List<ServerTCPConnection> connections = Collections.synchronizedList(new ArrayList<ServerTCPConnection>());
	/** A map of all clients - sorted by ID. */
	private HashMap<Integer, ServerTCPConnection> onlineConnections = new HashMap<Integer, ServerTCPConnection>();
	
	/** The thread which will execute all of the server code. */
	private Thread serverThread;
	/** The thread which will listen for client connections. */
	private ClientListenerThread clientListenerThread;
	
	/** The socket the server is being hosted on. */
	private ServerSocket socket;
	
	private AppDriver driver;
	
	/** True if the server is running. */
	public volatile boolean running;
	/** Used to indicate that the server has been stopped. */
	public volatile boolean stopped;
	/** Whether or not the server is paused. Note this is only for
	 * singleplayer. */
	public boolean paused = false;
	
	/** The world the server is running. */
	private WorldServer world;
	
	/** Whether or not the server is an integrated singleplayer one as opposed
	 * to a fully-fledged proper server. */
	private boolean integrated;
	/** False if the game is being played on singleplayer - this will prevent
	 * other players from joining. TODO: Poor implementation. */
	private boolean multiplayer;
	/** The maximum number of players that are allowed to join. */
	private int maxPlayers;
	
	/** The server logging agent. */
	public Log log = Log.getAgent("SERVER");
	
	
	/**
	 * Creates a new game server.
	 * 
	 * @param worldName The name of the world on which to host the server.
	 * @param integrated Whether or not the server is an integrated one for
	 * singleplayer.
	 * @param maxPlayers The maximum number of players which are capable of
	 * joining the server.
	 */
	public GameServer(String worldName, boolean integrated, int maxPlayers) {
		this.integrated = integrated;
		this.maxPlayers = maxPlayers;
		multiplayer = maxPlayers > 1;
		
		// Load the world before setting up the server...
		log.postInfo("Loading world...");
		world = WorldServer.loadWorld(this, worldName);
		
		if(world == null)
			throw new RuntimeException("Server could not load the world!");
		
		int tps = Constants.TICKS_PER_SECOND;
		driver = new ServerAppDriver(tps, tps, log);
		
		serverThread = new Thread(this, "GameServerThread");
		serverThread.start();
	}
	
	@Override
	public void run() {
		try {
			log.postDebug("Setting up server socket...");
			socket = new ServerSocket(DEFAULT_PORT, maxPlayers, InetAddress.getLocalHost());
			log.postInfo("Game hosted on " + socket.getLocalSocketAddress());
			
			running = true;			// This is necessary for the client listener thread
			
			// If we're in multiplayer, keep listening for connections.
			// Otherwise, only take one.
			//if(multiplayer) {
			if(!integrated) {
				clientListenerThread = new ClientListenerThread(this, socket);
				clientListenerThread.start();
			} else {
				Socket clientSocket = socket.accept();
				addConnection(clientSocket);
			}
			
			driver.run();
		} catch(Throwable t) {
			log.postSevere("Encountered error!", t);
			shutdown();
		}
	}
	
	private class ServerAppDriver extends AppDriver {
		
		public ServerAppDriver(int updatesPerSecond, int fps, Log log) {
			super(updatesPerSecond, fps, log);
		}
		
		@Override
		protected void update() {
			Packet packet;
			synchronized(connections) {
				//Iterator i = connections.iterator();
				//while(i.hasNext())
				//	foo(i.next());
				for(ServerTCPConnection connection : connections) {
					while((packet = connection.getPacket()) != null) {
						handlePacket(packet, connection);
					}
				}
			}
			
			world.update();
			
			synchronized(connections) {
				for(ServerTCPConnection connection : connections) {
					if(connection.loggedIn) {
						//connection.queuePacket(getPacketDisconnect());
						//Thread.sleep(750L);
						//shutdown();
					}
				}
			}
		}
		
		@Override
		protected void render() {
			// nothing to do here
		}
		
	}
	
	/**
	 * Adds a connection to the server.
	 * 
	 * @param clientSocket The socket through which the connection to the
	 * client is to be made.
	 */
	public void addConnection(Socket clientSocket) {
		ServerTCPConnection connection;
		
		try {
			connection = new ServerTCPConnection(clientSocket);
		} catch (IOException e) {
			log.postSevere("Error creating server-client connection!", e);
			try {
				clientSocket.close();
			} catch (IOException e1) {
				// ASDFGHJKL
				log.postSevere("y u no close client socket", e1);
			}
			return;
		}
		
		synchronized(connections) {
			connections.add(connection);
		}
		
		// Give the client the server's information
		connection.queuePacket(getPacketServerInfo());
		
		log.postInfo("Server-client connection successfully created.");
	}
	
	/**
	 * Logs a player into the server.
	 * 
	 * @param c The player's connection.
	 */
	private void logInPlayer(ServerTCPConnection c, Packet002Login packet) {
		// TODO: Check for banned IPs, etc.
		
		log.postInfo("Player \"" + packet.playerName + "\" logged in!");
		
		int id = world.addPlayer(packet.playerName, world.info.spawnSliceX, world.info.spawnSliceY);
		
		c.playerName = packet.playerName;
		c.id = id;
		
		// Allow the client to log in by sending it the 'clear to join' packet
		c.queuePacket(getPacketLoginInfo(id, world.info.spawnSliceX, world.info.spawnSliceY));
		
		c.loggedIn = true;
		
		onlineConnections.put(c.getHash(), c);
	}
	
	/**
	 * Sends a slice to a client.
	 * 
	 * @param clientHash The hash of the client to send the slice to.
	 * @param slice The slice.
	 */
	public void sendSliceToClient(int clientHash, Slice slice) {
		if(onlineConnections.containsKey(clientHash)) {
			 sendSliceToClient(onlineConnections.get(clientHash), slice);
		}
		// else do something maybe?
	}
	
	/**
	 * Sends a slice to a client.
	 * 
	 * @param client The client to send the slice to.
	 * @param slice The slice.
	 */
	public void sendSliceToClient(ServerTCPConnection client, Slice slice) {
		if(client == null) {
			log.postWarning("The client to send the slice to has disappeared!");
			return;
		}
		client.queuePacket(new Packet004Slice(slice));
	}
	
	/**
	 * Shuts down the server.
	 */
	public void shutdown() {
		if(stopped) return;
		
		stopped = true;
		
		log.postInfo("Shutting down server...");
		
		// This should result in the other threads being shut down
		running = false;
		
		for(ServerTCPConnection connection : connections) {
			connection.closeConnection();
		}
		
		world.generator.shutdown();
		
		serverThread.interrupt();
		if(clientListenerThread != null) clientListenerThread.interrupt();
		
		serverThread = null;
		clientListenerThread = null;
		
		try {
			socket.close();
		} catch (IOException e) {
			log.postSevere("Error closing server socket!");
		}
		
		world.save();
		
		log.postInfo("Server shut down.");
	}
	
	//--------------------==========--------------------
	//-----------=====Generating Packets=====-----------
	//--------------------==========--------------------
	
	/**
	 * Generates the server info packet.
	 * 
	 * @return The server info packet.
	 */
	private Packet getPacketServerInfo() {
		Packet001ServerInfo packet = new Packet001ServerInfo();
		packet.serverName = world.info.name;
		packet.serverRelease = Constants.RELEASE;
		packet.serverPatchMajor = Constants.PATCH_MAJOR;
		packet.serverPatchMinor = Constants.PATCH_MINOR;
		packet.onlinePlayers = 0;					// TODO: Temporary
		packet.maxPlayers = multiplayer ? 10 : 1;	// TODO: Temporary
		
		return packet;
	}
	
	/**
	 * Generates the packet to inform a newly-connected client of their
	 * spawn point.
	 * 
	 * @param id The ID of the player entity.
	 * @param spawnX The x-coordinate of the player entity.
	 * @param spawnY The y-coordinate of the player entity.
	 * 
	 * @return The login info packet.
	 */
	private Packet003LoginInfo getPacketLoginInfo(int id, double spawnX, double spawnY) {
		return new Packet003LoginInfo(id, world.info.spawnSliceX, world.info.spawnSliceY);
	}
	
	/**
	 * Generates a disconnect packet to 'kick' the client.
	 * TODO: Support for multiple clients
	 * 
	 * @param The disconnect packet.
	 */
	@SuppressWarnings("unused")
	private Packet254Disconnect getPacketDisconnect() {
		return new Packet254Disconnect("[Insert reason here]!");
	}
	
	//--------------------==========--------------------
	//------------=====Handling Packets=====------------
	//--------------------==========--------------------
	
	/**
	 * Handles all received packets by redirecting them to their appropriate
	 * handler method.
	 * 
	 * @param packet The packet to handle.
	 * @param connection The ServerTCPConnection through which the packet
	 * should be handled.
	 */
	private void handlePacket(Packet packet, ServerTCPConnection connection) {
		switch(packet.getID()) {
			case 2:			// Packet002Login
				handleLoginPacket((Packet002Login)packet, connection);
				break;
			case 5:			// Packet005BeginTick
				handlePacketBeginTick((Packet005BeginTick)packet, connection);
				break;
			case 6:			// Packet006PlayerPosition
				handlePacketPlayerPosition((Packet006PlayerPosition)packet, connection);
				break;
			case 252:		// Packet252RequestSlice
				//handlePacketRequestSlice((Packet252SliceRequest)packet, connection);
				break;
			case 253:		// Packet253Pause
				handlePacketPause((Packet253Pause)packet, connection);
				break;
			case 254:		// Packet254Disconnect
				handlePacketDisconnect((Packet254Disconnect)packet, connection);
				break;
			case 255:		// Packet255RequestPacket
				break;
		}
	}
	
	/**
	 * Handles a login request packet from a client.
	 * 
	 * @param packet The login packet.
	 * @param connection The connection through which the packet should be
	 * handled.
	 */
	private void handleLoginPacket(Packet002Login packet, ServerTCPConnection connection) {
		logInPlayer(connection, packet);
	}
	
	/**
	 * Handles a 'begin tick' packet from a client.
	 * 
	 * @param The begin tick packet.
	 * @param connection The connection through which the packet should be
	 * handled.
	 */
	private void handlePacketBeginTick(Packet005BeginTick packet, ServerTCPConnection connection) {
		connection.tick = packet.tick;
		// TODO: possibly catchup stuff if the client is significantly behind
	}
	
	/**
	 * Handles a position update packet from a client.
	 * 
	 * @param packet The player position packet.
	 * @param connection The connection through which the packet should be
	 * handled.
	 */
	private void handlePacketPlayerPosition(Packet006PlayerPosition packet, ServerTCPConnection connection) {
		// TODO: update player entity and inform other clients
	}
	
	/**
	 * Handles a slice request packet from a client.
	 * 
	 * @param packet The slice request packet.
	 * @param connection The connection through which the packet should be
	 * handled.
	 */
	/*
	private void handlePacketRequestSlice(Packet252SliceRequest packet, ServerTCPConnection connection) {
		// TODO: Install a cap as to the number of slices a client is capable of requesting,
		// as to prevent the server from overloading or flooding the network
		if(packet.requested) {
			//Slice s = world.loadSlice(packet.x, packet.y);
			//connection.queuePacket(new Packet004Slice(world.loadSlice(packet.x, packet.y)));
			Slice s = world.loadSlice(connection.getHash(), packet.x, packet.y);
			if(s != null) {
				connection.queuePacket(new Packet004Slice(s));
			}
		} else
			world.unloadSlice(packet.x, packet.y);
	}
	*/
	
	/**
	 * Handles a pause packet from a client.
	 * This will pause execution of most in-game logic if the server is an
	 * integrated singleplayer instance.
	 * 
	 * @param packet The pause packet.
	 * @param connection The connection through which the packet should be
	 * handled.
	 */
	private void handlePacketPause(Packet253Pause packet, ServerTCPConnection connection) {
		if(integrated)
			paused = packet.pause;
	}
	
	/**
	 * Handles a disconnect packet from a client.
	 * 
	 * @param packet The disconnect packet.
	 * @param connection The connection through which the packet should be
	 * handled.
	 */
	private void handlePacketDisconnect(Packet254Disconnect packet, ServerTCPConnection connection) {
		// TODO: disconnect the player, remove player entity, shutdown server if necessary, etc.
		shutdown();		// Temporary
	}
	
}
