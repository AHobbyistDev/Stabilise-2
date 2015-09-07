package com.stabilise.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
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
    
    /** The underlying connection. This is null if every connection attempt
     * has failed. */
    private TCPConnection connection;
    
    protected final Log log = Log.getAgent("CLIENT");
    
    
    /**
     * Creates a new client to connect to the specified IP address and port,
     * and immediately {@link #connect() connects}.
     * 
     * @param address The IP to connect to.
     * @param port The port to use.
     * 
     * @throws NullPointerException if {@code address} is {@code null}.
     * @throws IllegalArgumentException if the port parameter is outside the
     * range of valid port values, which is between 0 and 65535, inclusive.
     */
    public Client(InetAddress address, int port) {
        this(address, port, null);
    }
    
    /**
     * Creates a new client to connect to the specified IP address and port,
     * and immediately {@link #connect() connects}.
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
        this.port = port;
        this.handler = handler == null ? this : handler;
        
        connect();
    }
    
    /**
     * Attempts to connect to the server.
     * 
     * @throws IllegalStateException if this client is already connected.
     */
    public final void connect() {
        if(isConnected() && !recheckDisconnect())
            throw new IllegalStateException("Cannot connect unless disconnected!");
        try {
            Socket socket = new Socket(address, port);
            // Set state before actually establishing connection so there's
            // some sense of continuity in handleProtocolSwitch() when it is
            // first invoked.
            state = STATE_CONNECTED;
            connection = new TCPConnection(socket, false,
                    (c,p) -> handleProtocolSwitch(c,p));
            connection.open();
        } catch(IOException e) {
            state = STATE_DISCONNECTED;
            log.postWarning("Could not connect to server at "
                    + address.getHostAddress() + ":" + port + " (" +
                    e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }
    
    /**
     * This method is invoked when this client synchronises protocols with the
     * server, and as such should be used to perform any desired actions upon
     * entering a protocol.
     * 
     * <p>This method is invoked by {@link #connect()} when a connection is
     * established, with the {@link Protocol#HANDSHAKE HANDSHAKE} protocol.
     * 
     * <p>This method does nothing in the default implementation.
     * 
     * @param con This client's underlying {@code TCPConnection}.
     * @param protocol The new protocol.
     */
    protected void handleProtocolSwitch(TCPConnection con, Protocol protocol) {
        // nothing in the default implementation
    }
    
    /**
     * Closes this client's connection, if it is currently connected.
     */
    public final void disconnect() {
        if(isConnected()) {
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
     * Returns {@code true} if this client is connected to the server.
     */
    public final boolean isConnected() {
        return state == STATE_CONNECTED;
        //return !connection.isTerminated();
    }
    
    /**
     * Performs an update tick.
     */
    public final void update() {
        if(!checkDisconnect()) {
            connection.update(handler);
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
        if(!isConnected())
            return true;
        if(!connection.isActive()) {
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
        if(connection != null && connection.isTerminated()) {
            state = STATE_DISCONNECTED;
            return true;
        }
        return false;
    }
    
    /**
     * Gets this client's underlying connection. Returns {@code null} if this
     * client has not successfully established a connection.
     */
    public TCPConnection getConnection() {
        return connection;
    }
    
}
