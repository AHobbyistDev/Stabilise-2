package com.stabilise.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.Log;

public abstract class Client implements PacketHandler {
	
	private static final int STATE_DISCONNECTED = 0,
			STATE_CONNECTING = 1,
			STATE_CONNECTED = 2;
	
	/** Current client state. */
	private int state = STATE_DISCONNECTED;
	
	// The IP and port to connect to
	private final InetAddress address;
	private final int port;
	
	/** The underlying connection. This is never null while state ==
	 * STATE_CONNECTED. */
	private TCPConnection connection;
	
	private final Log log = Log.getAgent("CLIENT");
	
	
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
		this.address = Objects.requireNonNull(address);
        if(port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException("Port out of range: " + port);
		this.port = port;
	}
	
	/**
	 * Attempts to connect to the server.
	 * 
	 * @throws IllegalStateException if this client is already connected or
	 * trying to connect.
	 */
	public void connect() {
		if(state != STATE_DISCONNECTED)
			throw new IllegalStateException("Cannot connect unless disconnected!");
		state = STATE_CONNECTING;
		try {
			Socket socket = new Socket(address, port);
			connection = new TCPConnection(socket, false);
			state = STATE_CONNECTED;
		} catch(IOException e) {
			state = STATE_DISCONNECTED;
			log.postWarning("Could not connect to server at "
					+ address.getHostAddress() + ":" + port + " (" +
					e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
		}
	}
	
	/**
	 * Closes this client's connection, if it is currently connected.
	 */
	public void disconnect() {
		if(state == STATE_CONNECTED) {
			connection.closeConnection();
			state = STATE_DISCONNECTED;
		}
	}
	
	/**
	 * Invokes {@link #disconnect()} and then {@link #connect()}.
	 */
	public void reconnect() {
		disconnect();
		connect();
	}
	
	/**
	 * Performs an update tick.
	 */
	public final void update() {
		if(!checkDisconnect()) {
			handleIncomingPackets();
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
			state = STATE_DISCONNECTED;
			return true;
		}
		return false;
	}
	
	/**
	 * Handles any queued incoming packets. This should typically be invoked
	 * from within {@link #update()}.
	 */
	private void handleIncomingPackets() {
		if(state == STATE_CONNECTED) {
			Packet p;
			while((p = connection.getPacket()) != null)
				p.handle(this, connection);
		}
	}
	
}
