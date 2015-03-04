package com.stabilise.network;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.core.Constants;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.C001Disconnect;
import com.stabilise.network.protocol.handshake.S000VersionInfo;
import com.stabilise.util.AppDriver;
import com.stabilise.util.AppDriver.Drivable;
import com.stabilise.util.Log;
import com.stabilise.util.collect.LightArrayList;
import com.stabilise.util.collect.LightLinkedList;


public class ServerBase implements Runnable, Drivable, PacketHandler {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** State values.
	 * 
	 * <p>{@code UNSTARTED} indicates a server has not yet started.
	 * <p>{@code BOOTING} indicates a server's thread is starting.
	 * <p>{@code STARTING} indicates a server is in the process of starting.
	 * <p>{@code ACTIVE} indicates that a server is active.
	 * <p>{@code CLOSE_REQUESTED} indicates that a server has been requested
	 * to close.
	 * <p>{@code SHUTDOWN} indicates that a server is shutting down.
	 * <p>{@code TERMINATED} indicates that a server has been terminated. */
	private static final int
			STATE_UNSTARTED = 0,
			STATE_BOOTING = 1,
			STATE_STARTING = 2,
			STATE_ACTIVE = 3,
			STATE_CLOSE_REQUESTED = 4,
			STATE_SHUTDOWN = 5,
			STATE_TERMINATED = 6;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The socket the server is being hosted on. */
	private ServerSocket socket;
	/** The list of client connections. */
	private final List<ServerTCPConnection> connections =
			Collections.synchronizedList(new LightLinkedList<>());
	
	/** The thread on which this server runs. */
	private Thread serverThread;
	/** The thread which will listen for client connections. */
	private Thread clientListenerThread;
	
	private AppDriver driver;
	
	private final AtomicInteger state = new AtomicInteger(STATE_UNSTARTED);
	
	private final Log log = Log.getAgent("SERVER");
	
	
	
	public ServerBase() {
		
	}
	
	/**
	 * Instantiates a new thread and runs the server on that thread.
	 * 
	 * @throws IllegalStateException if the server is already running.
	 */
	public void runConcurrently() {
		if(state.compareAndSet(STATE_UNSTARTED, STATE_BOOTING))
			new Thread(this, "ServerThread").start();
		else
			throw new IllegalArgumentException("Server is already running!");
	}
	
	/**
	 * Runs the server on the current thread. This method will not return until
	 * the server has shut down.
	 * 
	 * @throws IllegalStateException if the server is already running.
	 */
	@Override
	public void run() {
		if(!state.compareAndSet(STATE_UNSTARTED, STATE_STARTING) &&
				!state.compareAndSet(STATE_BOOTING, STATE_STARTING))
			throw new IllegalStateException("Server is already running!");
		
		serverThread = Thread.currentThread();
		
		try {
			log.postInfo("Starting server...");
			socket = new ServerSocket(DEFAULT_PORT, 4, InetAddress.getLocalHost());
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
	
	public void start() {
		if(!state.compareAndSet(STATE_UNSTARTED, STATE_STARTING) &&
				!state.compareAndSet(STATE_BOOTING, STATE_STARTING))
			throw new IllegalStateException("Server is already running!");
		
		serverThread = Thread.currentThread();
	}
	
	@Override
	public void update() {
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
		// Using synchronized guarantees to update the value of driver.running
		// across threads
		synchronized(driver) {
			// Unimportant if() statement to prevent this from getting optimised
			// away by a compiler
			if(!driver.running)
				System.out.println("not running");
		}
	}
	
	public void shutdown() {
		state.set(STATE_SHUTDOWN);
		synchronized(driver) {
			driver.running = false;
		}
		synchronized(connections) {
			for(ServerTCPConnection con : connections)
				con.closeConnection();
			connections.clear();
		}
		try {
			socket.close();
		} catch(IOException e) {
			log.postWarning("Error closing socket", e);
		}
		try {
			clientListenerThread.join();
		} catch(InterruptedException e) {
			log.postWarning("Client listener thread failed to join", e);
		}
		stopped = true;
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
		
		log.postInfo("Connected to client on " + socket.getLocalSocketAddress());
	}
	
	private void handlePacket(Packet packet, ServerTCPConnection con) {
		if(packet.getClass() == C000VersionInfo.class)
			handleClientInfo((C000VersionInfo)packet, con);
		else if(packet.getClass() == C001Disconnect.class)
			handleClientDisconnect((C001Disconnect)packet, con);
		else
			log.postWarning("Unrecognised packet " + packet);
	}
	
	private void handleClientInfo(C000VersionInfo packet, ServerTCPConnection con) {
		log.postInfo("Got info from client!");
		con.sendPacket(new S000VersionInfo().setVersionInfo());
	}
	
	private void handleClientDisconnect(C001Disconnect packet, ServerTCPConnection con) {
		log.postInfo("Got disconnect request!");
		shutdown();
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
