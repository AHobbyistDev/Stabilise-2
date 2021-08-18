package com.stabilise.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.network.TCPConnection.ProtocolSyncEvent;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.Checks;
import com.stabilise.util.Log;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventHandler;
import com.stabilise.util.maths.Maths;

/**
 * This class provides all the basis architecture for a client which can
 * connect to a {@link Server}. To use this, subclass this class and implement
 * {@link #doUpdate()} as you see fit.
 * 
 * <p>A client can connect to the server specified in its constructor at any
 * time using {@link #connect()}, and may disconnect using {@link
 * #disconnect()}. {@link #update()} should be invoked at regular intervals by
 * a driver loop.
 */
@NotThreadSafe
public abstract class Client implements PacketHandler {
    
    /** This event is posted when a client successfully connects to a server. */
    public static final Event EVENT_CONNECTED = TCPConnection.EVENT_OPENED;
    /** This event is posted when a client disconnects from a server. */
    public static final Event EVENT_DISCONNECTED = TCPConnection.EVENT_CLOSED;
    /** This event is posted when a client synchronises protocols with the
     * server. Such a hook may be used to perform initial protocol-dependent
     * actions. */
    public static final ProtocolSyncEvent EVENT_PROTOCOL_SYNC = TCPConnection.EVENT_PROTOCOL_SYNC;
    
    private enum State {
            DISCONNECTED,
            CONNECTED
    }
    
    
    /** Current client state. */
    private State state = State.DISCONNECTED;
    
    // The IP and port to connect to
    private final InetAddress address;
    private final int port;
    
    private final PacketHandler handler;
    
    /** The underlying connection. This is null if every connection attempt
     * has failed. */
    private TCPConnection connection;
    private final Protocol initialProtocol;
    
    protected final Log log = Log.getAgent("CLIENT");
    
    /** Internal event dispatcher. Protected so subclasses can post additional
     * lifecycle events if they wish. */
    protected final EventDispatcher events = EventDispatcher.normal();
    
    
    /**
     * Creates a new client to connect to the specified IP address and port.
     * 
     * @param address The IP to connect to.
     * @param port The port to use.
     * @param initialProtocol The initial protocol.
     * 
     * @throws NullPointerException if either {@code address} or {@code
     * initialProtocol} are {@code null}.
     * @throws IllegalArgumentException if the port parameter is outside the
     * range of valid port values, which is between 0 and 65535, inclusive.
     */
    public Client(InetAddress address, int port, Protocol initialProtocol) {
        this(address, port, initialProtocol, null);
    }
    
    /**
     * Creates a new client to connect to the specified IP address and port.
     * 
     * @param address The IP to connect to.
     * @param port The port to use.
     * @param initialProtocol The initial protocol.
     * @param handler The packet handler to use. If {@code null}, this client is
     * used as its own handler.
     * 
     * @throws NullPointerException if either {@code address} or {@code
     * initialProtocol} are {@code null}.
     * @throws IllegalArgumentException if the port parameter is outside the
     * specified range of valid port values, which is between 0 and 65535,
     * inclusive.
     */
    public Client(InetAddress address, int port, Protocol initialProtocol,
            PacketHandler handler) {
        this.address = Objects.requireNonNull(address);
        this.port = Checks.test(port, 0, Maths.USHORT_MAX_VALUE);
        this.initialProtocol = Objects.requireNonNull(initialProtocol);
        this.handler = handler == null ? this : handler;
    }
    
    /**
     * Attempts to connect to the server.
     * 
     * @throws IllegalStateException if this client is already connected.
     */
    public final void connect() {
        if(isConnected())
            throw new IllegalStateException("Cannot connect unless disconnected!");
        try {
            Socket socket = new Socket(address, port);
            // Set state before actually establishing connection so there's
            // some sense of continuity in handleProtocolSwitch() when it is
            // first invoked.
            state = State.CONNECTED;
            connection = new TCPConnection(socket, false, initialProtocol);
            connection.addListener(Tasks.currentThreadExecutor(),
                    TCPConnection.EVENT_PROTOCOL_SYNC,
                    this::handleProtocolSwitch
            );
            connection.addListener(Tasks.currentThreadExecutor(),
                    TCPConnection.EVENT_CLOSED,
                    this::handleDisconnect
            );
            connection.open();
            events.dispatch(EVENT_CONNECTED);
        } catch(IOException e) {
            state = State.DISCONNECTED;
            log.postWarning("Could not connect to server at "
                    + address.getHostAddress() + ":" + port + " (" +
                    e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }
    
    /**
     * Closes this client's connection, if it is currently connected.
     */
    public final void disconnect() {
        if(isConnected()) {
            connection.closeConnection();
            state = State.DISCONNECTED;
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
        return state == State.CONNECTED;
        //return !connection.isTerminated();
    }
    
    /**
     * Performs an update tick.
     */
    public final void update() {
        if(isConnected()) {
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
     * Gets this client's underlying connection. Returns {@code null} if this
     * client has not successfully established a connection.
     */
    public TCPConnection getConnection() {
        return connection;
    }
    
    /**
     * Adds an event listener to this client.
     * 
     * @param exec The executor with which to execute the handler.
     * @param event The event to listen for.
     * @param handler The handler to invoke when the specified event is posted.
     * 
     * @throws NullPointerException if any argument is null.
     * @see #EVENT_CONNECTED
     * @see #EVENT_DISCONNECTED
     * @see #EVENT_PROTOCOL_SYNC
     */
    public <E extends Event> void addListener(Executor exec, E event,
            EventHandler<? super E> handler) {
        events.addListener(exec, event, handler);
    }
    
    private void handleProtocolSwitch(ProtocolSyncEvent e) {
        events.dispatch(e);
    }
    
    private void handleDisconnect(Event e) {
        disconnect();
        events.dispatch(e);
    }
    
}
