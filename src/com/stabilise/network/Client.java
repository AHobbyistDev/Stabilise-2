package com.stabilise.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.Log;

/**
 * This class provides all the basis architecture for a client which can
 * connect to a {@link Server}. To use this, subclass this class and implement
 * {@link #doUpdate()} as you see fit.
 * 
 * <p>A client can connect to the server specified in it's constructor at any
 * time using {@link #connect()}, and may disconnect using {@link
 * #disconnect()}. {@link #update()} should be invoked at regular intervals by
 * a driver loop.
 */
public abstract class Client implements PacketHandler {
	
	private static final int STATE_DISCONNECTED = 0,
			STATE_CONNECTED = 1;
	
	/** Current client state. */
	private int state = STATE_DISCONNECTED;
	
	// The IP and port to connect to
	private final InetAddress address;
	private final int port;
	
	private final PacketHandler handler;
	
	/** The underlying connection. This null until we first connect. */
	private TCPConnection connection;
	
	protected final Log log = Log.getAgent("CLIENT");
	
	
	/**
	 * Creates a new client to connect to the specified IP address and port.
	 * 
	 * @param address The IP to connect to.
	 * @param port The port to use.
	 * 
	 * @throws NullPointerException if {@code address} is {@code null}.
	 * @throws IllegalArgumentException if the port parameter is outside the
	 * specified range of valid port values, which is between 0 and 65535,
	 * inclusive.
	 */
	public Client(InetAddress address, int port) {
		this(address, port, null);
	}
	
	/**
	 * Creates a new client to connect to the specified IP address and port.
	 * 
	 * @param address The IP to connect to.
	 * @param port The port to use.
	 * @param The packet handler to use. If {@code null}, this client is used
	 * as its own handler.
	 * 
	 * @throws NullPointerException if {@code address} is {@code null}.
	 * @throws IllegalArgumentException if the port parameter is outside the
	 * specified range of valid port values, which is between 0 and 65535,
	 * inclusive.
	 */
	public Client(InetAddress address, int port, PacketHandler handler) {
		this.address = Objects.requireNonNull(address);
        if(port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException("Port out of range: " + port);
		this.port = port;
		this.handler = handler == null ? this : handler;
	}
	
	/**
	 * Attempts to connect to the server.
	 * 
	 * @throws IllegalStateException if this client is already connected.
	 */
	public final void connect() {
		if(state != STATE_DISCONNECTED && !recheckDisconnect())
			throw new IllegalStateException("Cannot connect unless disconnected!");
		try {
			Socket socket = new Socket(address, port);
			connection = new TCPConnection(socket, false);
			state = STATE_CONNECTED;
			onConnect();
		} catch(IOException e) {
			log.postWarning("Could not connect to server at "
					+ address.getHostAddress() + ":" + port + " (" +
					e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
		}
	}
	
	/**
	 * Invoked by {@link #connect()} immediately after a connection is
	 * established.
	 * 
	 * <p>The default implementation does nothing.
	 */
	protected void onConnect() {}
	
	/**
	 * Closes this client's connection, if it is currently connected.
	 */
	public final void disconnect() {
		if(state == STATE_CONNECTED) {
			connection.closeConnection();
			state = STATE_DISCONNECTED;
		}
	}
	
	/**
	 * Invokes {@link #disconnect()} and then {@link #connect()}.
	 */
	public final void reconnect() {
		disconnect();
		connect();
	}
	
	/**
	 * Returns {@code true} if this client has disconnected from the server.
	 */
	public final boolean isDisconnected() {
		//return state == STATE_DISCONNECTED;
		return connection.isTerminated();
	}
	
	/**
	 * Performs an update tick.
	 */
	public final void update() {
		if(!checkDisconnect()) {
			handleIncomingPackets();
			connection.update();
			doUpdate();
		}
	}
	
	/**
	 * Performs any custom update logic. This is invoked by {@link #update()}
	 * unless the client has disconnected.
	 * 
	 * <p>This method does nothing by default.
	 */
	protected void doUpdate() {}
	
	/**
	 * Checks for whether or not this client has been disconnected from the
	 * server. This should be invoked from within {@link #update()}.
	 * 
	 * @return {@code true} if this client has been disconnected; {@code false}
	 * otherwise.
	 */
	private boolean checkDisconnect() {
		if(state == STATE_DISCONNECTED)
			return true;
		if(connection != null && !connection.isActive()) {
			disconnect();
			return true;
		}
		return false;
	}
	
	/**
	 * Rechecks the disconnected status in case {@link #connection} has closed
	 * without this client finding out yet.
	 * 
	 * @returns true if disconnected
	 */
	private boolean recheckDisconnect() {
		if(isDisconnected()) {
			state = STATE_DISCONNECTED;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets this client's underlying connection.
	 * 
	 * @throws IllegalStateException if this client has not {@link #connect()
	 * connected}. This is not thrown if a client has disconnected after
	 * connecting.
	 */
	public TCPConnection getConnection() {
		if(connection == null)
			throw new IllegalStateException("Client not connected!");
		return connection;
	}
	
	/**
	 * Handles any queued incoming packets. This should typically be invoked
	 * from within {@link #update()}.
	 */
	private void handleIncomingPackets() {
		Packet p;
		while((p = connection.getPacket()) != null)
			p.handle(handler, connection);
	}
	
}
