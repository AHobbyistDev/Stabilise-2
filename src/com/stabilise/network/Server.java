package com.stabilise.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.AppDriver;
import com.stabilise.util.AppDriver.Drivable;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.LightLinkedList;
import com.stabilise.util.concurrent.Task;


/**
 * This class provides the basis architecture for constructing a server
 * implementation. To use this, subclass this class and appropriately implement
 * {@link #update()} (and optionally {@link #render()}) as you see fit.
 * 
 * <p>There are three ways to run a server:
 * 
 * <ul>
 * <li>Invoke {@link #start()} to start the server, and then manually invoke
 *     {@link #update()} and {@code #render()} repeatedly at your convenience.
 *     That is, {@code update} and {@code render} should be invoked from within
 *     a preexisting driver loop.
 * <li>Invoke {@link #run()}, which constructs an {@link AppDriver} linked to
 *     the server, and then {@link AppDriver#run() runs} it. {@code run} will
 *     of course not return until the server terminates. Note that for this
 *     method of running to work, the Server must be constructed with {@link
 *     #Server(int)}, as to specify the tick rate when driving the server.
 * <li>Invoke {@link #runConcurrently()}, which starts a new thread and
 *     invokes {@code run} on that thread. As with the above point, the
 *     Server must be constructed through {@link #Server(int)}.
 * </ul>
 * 
 * <p>To close a server, either invoke {@link #requestShutdown()} and wait for
 * the server to close itself, or directly invoke {@link #shutdown()}.
 * 
 * <!--
 * <p>Each active {@code Server} associates with it the following resources:
 * 
 * <ul>
 * <li>A {@code ServerSocket} object.
 * <li>A thread which listens for client connections using {@link
 *     ServerSocket#accept()}.
 * <li>Any number of {@link TCPConnection} objects which represent connections
 *     to connected clients.
 * </ul>
 * -->
 */
public abstract class Server implements Runnable, Drivable, PacketHandler {
	
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
	
	private final ClientConnectionFactory clientFactory;
	/** The list of client connections. Does not contain {@code null} elements.
	 * This list should be manually synchronised on when being iterated over. */
	protected final List<TCPConnection> connections =
			Collections.synchronizedList(new LightLinkedList<>());
	
	private Thread clientListenerThread;
	
	/** Optionally-used driver used to run this server, if {@link #run()} is
	 * used instead of an external driver. This will be {@code null} if an
	 * external driver is being used. */
	private AppDriver driver;
	/** > 1 if valid; -1 indicates this server must be driven externally. */
	private final int tps;
	
	private final AtomicInteger state = new AtomicInteger(STATE_UNSTARTED);
	
	protected final Log log = Log.getAgent("SERVER");
	
	
	/**
	 * Creates a new Server.
	 * 
	 * <p>A server constructed with this method may <i>not</i> be run using
	 * {@link #run()} or {@link #runConcurrently()}, as for this a {@code
	 * ticksPerSecond} value must be specified. For this, refer to the other
	 * constructor: {@link #Server(int)}.
	 */
	public Server() {
		this(s -> new TCPConnection(s, true));
	}
	
	/**
	 * Creates a new Server.
	 * 
	 * @param ticksPerSecond The number of update ticks per second to perform
	 * while running as per {@link #run()} or {@link #runConcurrently()}.
	 * 
	 * @throws IllegalArgumentException if {@code ticksPerSecond < 1}.
	 */
	public Server(int ticksPerSecond) {
		this(ticksPerSecond, s -> new TCPConnection(s, true));
	}
	
	/**
	 * Creates a new Server.
	 * 
	 * <p>A server constructed with this method may <i>not</i> be run using
	 * {@link #run()} or {@link #runConcurrently()}, as for this a {@code
	 * ticksPerSecond} value must be specified. For this, refer to the other
	 * constructor: {@link #Server(int)}.
	 * 
	 * @param clientFactory The factory to use to create clients.
	 * 
	 * @throws NullPointerException if {@code clientFactory} is {@code null}.
	 */
	public Server(ClientConnectionFactory clientFactory) {
		this.clientFactory = Objects.requireNonNull(clientFactory);
		tps = -1;
	}
	
	/**
	 * Creates a new Server.
	 * 
	 * @param ticksPerSecond The number of update ticks per second to perform
	 * while running as per {@link #run()} or {@link #runConcurrently()}.
	 * @param clientFactory The factory to use to create clients.
	 * 
	 * @throws NullPointerException if {@code clientFactory} is {@code null}.
	 * @throws IllegalArgumentException if {@code ticksPerSecond < 1}.
	 */
	public Server(int ticksPerSecond, ClientConnectionFactory clientFactory) {
		this.clientFactory = Objects.requireNonNull(clientFactory);
		if(ticksPerSecond < 1)
			throw new IllegalArgumentException("ticksPerSecond < 1");
		this.tps = ticksPerSecond;
	}
	
	/**
	 * Starts a new thread and runs the server on that thread.
	 * 
	 * @throws IllegalStateException if this server may not be internally
	 * driven, or this server has already been started.
	 */
	public final void runConcurrently() {
		checkCanRun();
		if(state.compareAndSet(STATE_UNSTARTED, STATE_BOOTING))
			new Thread(this, "ServerThread").start();
		else
			throw new IllegalStateException("Server is already running!");
	}
	
	/**
	 * Runs the server on the current thread. This method will not return until
	 * the server has shut down.
	 * 
	 * @throws IllegalStateException if this server may not be internally
	 * driven, or this server has already been started.
	 */
	@Override
	public final void run() {
		checkCanRun();
		if(start()) {
			driver = AppDriver.driverFor(this, tps, tps, log);
			try {
				driver.run();
			} catch(Throwable t) {
				shutdown();
				throw t;
			}
		}
	}
	
	private void checkCanRun() {
		if(tps == -1)
			throw new IllegalStateException("Cannot run a server constructed " + 
					" without a ticksPerSecond value defined!");
	}
	
	/**
	 * Attempts to start the server.
	 * 
	 * @return {@code true} if the server was successfully started; {@code
	 * false} otherwise.
	 * @throws IllegalStateException if this server has already been started.
	 */
	public boolean start() {
		if(!state.compareAndSet(STATE_UNSTARTED, STATE_STARTING) &&
				!state.compareAndSet(STATE_BOOTING, STATE_STARTING))
			throw new IllegalStateException("Server has already been started!");
		
		try {
			socket = createSocket();
			log.postInfo("Server hosted on " + socket.getInetAddress().getHostAddress());
			
			clientListenerThread = new ClientListenerThread();
			clientListenerThread.start();
			
			if(!state.compareAndSet(STATE_STARTING, STATE_ACTIVE))
				throw new AssertionError();
			
			return true;
		} catch(Throwable t) {
			log.postSevere("Encountered error while starting; shutting down server!", t);
			shutdown();
			return false;
		}
	}
	
	/**
	 * Creates and returns this server's {@code ServerSocket} instance.
	 * 
	 * @throws IOException if an I/O error occurs when opening the socket.
	 */
	protected abstract ServerSocket createSocket() throws IOException;
	
	/**
	 * This method is invoked when this server synchronises protocols with a
	 * client, but only when this {@code Server} is constructed using {@link
	 * #Server()} or {@link #Server(int)}. To make use of this method when
	 * either the {@link #Server(ClientConnectionFactory)} or {@link
	 * #Server(int, ClientConnectionFactory)} constructors are used, construct
	 * {@code TCPConnection} objects with protocol switch listeners which
	 * appropriately redirect to this method. For example:
	 * 
	 * <pre>
	 * class MyServer extends Server {
	 * public MyServer() {
	 *     super((s) ->
	 *         new TCPConnection(s, true, (c,p) -> handleProtocolSwitch(c,p));
	 *     });
	 * }
	 * }</pre>
	 * 
	 * <p>This method does nothing in the default implementation.
	 * 
	 * @param con The {@code TCPConnection} connected to a client.
	 * @param protocol The new protocol.
	 * 
	 * @deprecated Turns out we can't refer to an instance method in a
	 * constructor.
	 */
	@SuppressWarnings("unused")
	private void handleProtocolSwitch(TCPConnection con, Protocol protocol) {
		// nothing in the default implementation
	}
	
	/**
	 * Performs an update tick.
	 */
	@Override
	@UserThread("ServerThread/MainThread")
	public final void update() {
		if(!checkShutdown()) {
			synchronized(connections) {
				Iterator<TCPConnection> i = connections.iterator();
				while(i.hasNext()) {
					TCPConnection con = i.next();
					
					// Update the connection and then remove it if the client
					// has been disconnected.
					con.update(this);
					
					if(!con.isActive()) {
						con.closeConnection();
						onClientDisconnect(con);
						i.remove();
					}
				}
			}
			doUpdate();
		}
	}
	
	/**
	 * Performs any custom update logic. This is invoked by {@link #update()}
	 * unless this server has been shut down.
	 * 
	 * <p>This method does nothing by default.
	 */
	@UserThread("ServerThread/MainThread")
	protected void doUpdate() {}
	
	/**
	 * Checks for whether or not this server has been requested to shut down,
	 * and shuts it down via {@link #shutdown()} if so.
	 * 
	 * @return {@code true} if the server was shut down; {@code false}
	 * otherwise.
	 */
	private boolean checkShutdown() {
		if(!isActive()) {
			if(driver != null)
				driver.stop();
			shutdown();
			return true;
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>The default implementation does nothing and may be optionally
	 * overridden.
	 */
	@Override
	@UserThread("ServerThread/MainThread")
	public void render() {
		// nothing to see here, move along
	}
	
	/**
	 * Requests for the server to shut down.
	 */
	public final void requestShutdown() {
		state.compareAndSet(STATE_ACTIVE, STATE_CLOSE_REQUESTED);
	}
	
	/**
	 * Shuts the server down using the current thread. Invoking this does
	 * nothing if the server is not currently running.
	 */
	public void shutdown() {
		if(!state.compareAndSet(STATE_ACTIVE, STATE_SHUTDOWN) &&
				!state.compareAndSet(STATE_CLOSE_REQUESTED, STATE_SHUTDOWN))
			return;
		
		if(clientListenerThread != null)
			clientListenerThread.interrupt();
		
		synchronized(connections) {
			for(TCPConnection con : connections)
				con.closeConnection();
			connections.clear();
		}
		
		try {
			if(socket != null)
				socket.close();
		} catch(IOException e) {
			log.postWarning("Error closing socket", e);
		}
		
		try {
			if(clientListenerThread != null)
				clientListenerThread.join();
		} catch(InterruptedException e) {
			log.postWarning("Interrupted while waiting for client listener "
					+ "thread to join");
		}
		
		log.postInfo("Shut down.");
		
		Task.doThenNotify(state, () -> state.set(STATE_TERMINATED));
	}
	
	@Override
	protected void finalize() {
		// Use finalisation to ensure shutdown always occurs
		shutdown();
	}
	
	/**
	 * Returns {@code true} if this server is currently active.
	 */
	public boolean isActive() {
		return state.get() == STATE_ACTIVE;
	}
	
	/**
	 * Returns {@code true} if this server has been terminated.
	 */
	public boolean isTerminated() {
		return state.get() == STATE_TERMINATED;
	}
	
	/**
	 * Waits for this server to terminate.
	 * 
	 * @throws InterruptedException
	 */
	public void waitUntilTerminated() throws InterruptedException {
		Task.waitOnInterruptibly(state, () -> isTerminated());
	}
	
	/**
	 * Adds a client connection through the specified client socket.
	 */
	@UserThread("ClientListenerThread")
	private void addConnection(Socket socket) {
		try {
			TCPConnection con = clientFactory.create(socket);
			con.open();
			onClientConnect(con);
			connections.add(con);
			
			log.postInfo("Connected to client on " + socket.getLocalSocketAddress());
		} catch(IOException e) {
			log.postSevere("Error creating connection (" + e.getMessage() + ")");
			try {
				socket.close();
			} catch(IOException e1) {
				log.postWarning("Failed to close client socket (" + e.getMessage() + ")");
			}
			return;
		}
	}
	
	/**
	 * This method is invoked by the client listener thread when it connects to
	 * a new client, before the specified {@code TCPConnection} is added to
	 * {@link #connections the list of connections}. In other words, actions
	 * performed by this method happen-before actions on a thread which sees
	 * {@code con} in {@link #connections}.
	 * 
	 * <p>The default implementation does nothing.
	 */
	@UserThread("ClientListenerThread")
	protected void onClientConnect(TCPConnection con) {}
	
	/**
	 * This method is invoked when a client disconnects from this server from
	 * within {@link #update()}, after the connection is {@link
	 * TCPConnection#closeConnection() closed}, and before connection is
	 * removed from {@link #connections the list of connections}. In other
	 * words, actions performed by this method happen-before actions on a
	 * thread which sees that {@code con} has been removed from {@link
	 * #connections}.
	 * 
	 * <p>The default implementation does nothing.
	 */
	@UserThread("ServerThread/MainThread")
	protected void onClientDisconnect(TCPConnection con) {}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A thread which listens for - and adds - clients.
	 */
	private class ClientListenerThread extends Thread {
		
		@Override
		public void run() {
			while(isActive()) {
				try {
					addConnection(socket.accept());
				} catch(IOException e) {
					if(isActive())
						log.postSevere("IOException thrown while waiting on the socket", e);
				}
			}
		}
		
	}
	
	/**
	 * A factory for client TCPConnection handles.
	 */
	public static interface ClientConnectionFactory {
		
		/**
		 * Creates a TCPConnection object around the specified client socket.
		 */
		public TCPConnection create(Socket socket) throws IOException;
		
	}
	
}
