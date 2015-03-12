package com.stabilise.core;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;
import com.stabilise.network.Packet;
import com.stabilise.network.ServerTCPConnection;
import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;
import com.stabilise.util.AppDriver.Drivable;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.world.provider.HostProvider;

@Incomplete
public class GameServer implements Runnable, Drivable {
	
	/** The socket the server is being hosted on. */
	private ServerSocket socket;
	/** The list of client connections. */
	private final List<ServerTCPConnection> connections =
			Collections.synchronizedList(new ArrayList<ServerTCPConnection>());
	
	/** The thread on which this server runs. */
	@SuppressWarnings("unused")
	private Thread serverThread;
	/** The thread which will listen for client connections. */
	private Thread clientListenerThread;
	
	private AppDriver driver;
	
	/** True if the server is running. */
	public final AtomicBoolean running = new AtomicBoolean(false);
	/** Used to indicate that the server has been stopped. */
	public volatile boolean stopped;
	/** Whether or not the server is paused. Note this is only for
	 * singleplayer. */
	public boolean paused = false;
	
	/** The world the server is running. */
	private final HostProvider world;
	
	/** The maximum number of players that are allowed to join. */
	private final int maxPlayers;
	
	private final Log log = Log.getAgent("SERVER");
	
	
	
	public GameServer(HostProvider world, int maxPlayers) {
		this.world = Objects.requireNonNull(world);
		Preconditions.checkArgument(maxPlayers > 0, "maxPlayers <= 0");
		this.maxPlayers = maxPlayers;
	}
	
	/**
	 * Instantiates a new thread and runs the server on that thread.
	 */
	public void runConcurrently() {
		new Thread(this, "ServerThread").start();
	}
	
	/**
	 * Runs the server on the current thread. This method will not return until
	 * the server has shut down.
	 * 
	 * @throws IllegalStateException if the server is already running.
	 */
	@Override
	public void run() {
		if(!running.compareAndSet(false, true))
			throw new IllegalStateException("Server is already running!");
		
		serverThread = Thread.currentThread();
		
		try {
			log.postInfo("Starting server...");
			socket = new ServerSocket(DEFAULT_PORT, maxPlayers, InetAddress.getLocalHost());
			log.postInfo("Game hosted on " + socket.getLocalSocketAddress());
			
			clientListenerThread = new ClientListenerThread();
			clientListenerThread.start();
			
			int tps = Constants.TICKS_PER_SECOND;
			driver = AppDriver.getDriverFor(this, tps, tps, log);
			driver.run();
		} catch(Throwable t) {
			log.postSevere("Encountered error; shutting down server!", t);
			shutdown();
		}
	}
	
	@Override
	public void update() {
		world.update();
		
		Packet p;
		synchronized(connections) {
			for(ServerTCPConnection con : connections)
				while((p = con.getPacket()) != null)
					handlePacket(p, con);
		}
		
		// TODO: send packets back to the clients
	}
	
	@Override
	public void render() {
		// nothing to see here; a server doesn't render
	}
	
	public void shutdown() {
		
	}
	
	private void addConnection(Socket socket) {
		ServerTCPConnection con;
		
		try {
			con = new ServerTCPConnection(socket);
		} catch(IOException e) {
			log.postSevere("Error creating connection (" + e.getMessage() + ")");
			try {
				socket.close();
			} catch(IOException e1) {
				log.postWarning("Failed to close client socket (" + e.getMessage() + ")");
			}
			return;
		}
		
		connections.add(con);
		
		log.postInfo("Connected to client");
	}
	
	private void handlePacket(Packet packet, ServerTCPConnection con) {
		
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A thread which listens for, and adds clients.
	 */
	private class ClientListenerThread extends Thread {
		
		@Override
		public void run() {
			while(running.get()) {
				try {
					addConnection(socket.accept());
				} catch(IOException e) {
					if(running.get())
						log.postSevere("IOException thrown while waiting on the socket", e);
				}
			}
		}
		
	}
	
}
