package com.stabilise.tests.network;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.stabilise.core.Constants;
import com.stabilise.network.Packet;
import com.stabilise.network.ServerTCPConnection;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.C001Disconnect;
import com.stabilise.network.protocol.handshake.S000VersionInfo;
import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;
import com.stabilise.util.AppDriver.Drivable;

public class Server implements Runnable, Drivable {
	
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
	
	private final Log log = Log.getAgent("SERVER");
	
	
	
	public Server() {
	}
	
	/**
	 * Instantiates a new thread and runs the server on that thread.
	 */
	public void runConcurrently() {
		new Thread(this, "ServerThread").start();
	}
	
	/**
	 * Runs the server on the current thread. This method will not return until
	 * the server has shut donw.
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
		running.set(false);
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
