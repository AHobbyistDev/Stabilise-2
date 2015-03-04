package com.stabilise.network;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;

/**
 * A TCPConnection instance maintains a connection between a server and a
 * client, and is the gateway for interaction between the two.
 */
public class TCPConnection {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** State values.
	 * 
	 * <p>{@code STARTING} is an intermediate state used only while a
	 * TCPConnection object is being constructed.
	 * <p>{@code ACTIVE} indicates that a connection is active.
	 * <p>{@code CLOSE_REQUESTED} indicates that either the read or write
	 * thread encountered an exception and shut down.
	 * <p>{@code SHUTDOWN} indicates that a connection is shutting down.
	 * <p>{@code TERMINATED} indicates that a connection has been terminated. */
	private static final int
			STATE_STARTING = 0,
			STATE_ACTIVE = 1,
			STATE_CLOSE_REQUESTED = 2,
			STATE_SHUTDOWN = 3,
			STATE_TERMINATED = 4;
	
	private static final AtomicInteger CONNECTIONS_SERVER = new AtomicInteger(0);
	private static final AtomicInteger CONNECTIONS_CLIENT = new AtomicInteger(0);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** {@code true} if this is a server-side connection. */
	private final boolean server;
	
	/** The current connection protocol. Default is {@link Protocol#HANDSHAKE}. */
	private Protocol protocol = Protocol.HANDSHAKE;
	private final AtomicInteger state = new AtomicInteger(STATE_STARTING);
	
	protected final Socket socket;
	
	private final DataInputStream in;
	private final DataOutputStream out;
	
	private final BlockingQueue<Packet> packetQueueIn = new LinkedBlockingQueue<>();
	private final BlockingQueue<Packet> packetQueueOut = new LinkedBlockingQueue<>();
	
	private final TCPReadThread readThread;
	private final TCPWriteThread writeThread;
	
	private int packetsSent = 0;
	private int packetsReceived = 0;
	
	private Log log;
	
	
	/**
	 * Creates a new TCPConnection.
	 * 
	 * @param socket The socket upon which to base the connection.
	 * @param server Whether or not this is a server-side connection.
	 * 
	 * @throws NullPointerException if {@code socket} is {@code null}.
	 * @throws IOException if the connection could not be established.
	 */
	public TCPConnection(Socket socket, boolean server) throws IOException {
		this.socket = socket;
		this.server = server;
		
		int id = server
				? CONNECTIONS_SERVER.getAndIncrement()
				: CONNECTIONS_CLIENT.getAndIncrement();
		
		log = Log.getAgent((server ? "SERVER" : "CLIENT") + id);
		
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		
		readThread = new TCPReadThread((server ? "ServerReader" : "ClientReader") + id);
		writeThread = new TCPWriteThread((server ? "ServerWriter" : "ClientWriter") + id);
		
		state.set(STATE_ACTIVE);
		
		readThread.start();
		writeThread.start();
	}
	
	/**
	 * Returns the protocol currently being used by this connection.
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Sets this connection's protocol.
	 * 
	 * @throws NullPointerException if {@code protocol} is {@code null}.
	 */
	public void setPotocol(Protocol protocol) {
		this.protocol = Objects.requireNonNull(protocol);
	}
	
	/**
	 * Queues a packet for sending.
	 * 
	 * @throws NullPointerException if {@code packet} is {@code null}.
	 * @throws IllegalStateException if the packet queue is full. This should
	 * never happen unless something is seriously wrong.
	 */
	@UserThread("MainThread")
	public void sendPacket(Packet packet) {
		if(server) {
			if(!protocol.isServerPacket(packet)) {
				log.postWarning("Attempting to send a non-server packet ("
						+ packet.getClass().getSimpleName() + ")");
				return;
			}
		} else {
			if(!protocol.isClientPacket(packet)) {
				log.postWarning("Attempting to send a non-client packet ("
						+ packet.getClass().getSimpleName() + ")");
				return;
			}
		}
		
		packetQueueOut.add(packet); // may throw ISE
	}
	
	/**
	 * Polls the input packet queue for the next packet.
	 * 
	 * @return The oldest queued packet, or {@code null} if the queue is empty.
	 */
	@UserThread("MainThread")
	public Packet getPacket() {
		return packetQueueIn.poll();
	}
	
	/**
	 * Reads a packet from the socket's input stream. A return value of {@code
	 * false} indicates either an I/O exception or the end of the stream having
	 * been reached.
	 * 
	 * @throws IOException if an I/O error occurs, or the stream has ended.
	 * @throws FaultyPacketRegistrationException if the packet was registered
	 * incorrectly (in this case the error lies in the registration code).
	 */
	@UserThread("ReadThread")
	private void readPacket() throws IOException {
		Packet packet = protocol.readPacket(server, in);
		if(packet == null)
			requestClose();
		else {
			packetQueueIn.add(packet);
			packetsReceived++;
		}
	}
	
	/**
	 * Writes a packet to the socket's output stream. A return value of {@code
	 * false} indicates that the packet queue was empty when polled.
	 * 
	 * @return {@code true} if the packet was sent; {@code false} otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	@UserThread("WriteThread")
	private boolean writePacket() throws IOException {
		Packet packet = packetQueueOut.poll();
		if(packet == null)
			return false;
		doWritePacket(packet);
		return true;
	}
	
	/**
	 * Writes a packet to the socket's output stream, blocking if necessary
	 * until a packet is available to send.
	 * 
	 * @throws InterruptedException if the current thread is interrupted while
	 * waiting for a packet.
	 * @throws IOException if an I/O error occurs.
	 */
	@UserThread("WriteThread")
	private void writePacketWithBlock() throws InterruptedException, IOException {
		doWritePacket(packetQueueOut.take());
	}
	
	@UserThread("WriteThread")
	private void doWritePacket(Packet packet) throws IOException {
		protocol.writePacket(out, packet);
		packetsSent++;
	}
	
	/**
	 * Requests for this connection to close, if an error occurs.
	 */
	@UserThread({"ReadThread", "WriteThread"})
	private void requestClose() {
		state.compareAndSet(STATE_ACTIVE, STATE_CLOSE_REQUESTED);
	}
	
	/**
	 * Returns {@code true} if closing this connection has been requested by
	 * either the reader thread or the writer thread due to encountering an
	 * exception.
	 * 
	 * <p>If {@code true} is returned, this means that this TCPConnection is
	 * no longer functioning, and should be properly closed via {@link
	 * #closeConnection()}.
	 */
	public boolean closeRequested() {
		return state.get() == STATE_CLOSE_REQUESTED;
	}
	
	/**
	 * Closes the connection and releases any resources held by it.
	 */
	public void closeConnection() {
		if(!state.compareAndSet(STATE_ACTIVE, STATE_SHUTDOWN) &&
				!state.compareAndSet(STATE_CLOSE_REQUESTED, STATE_SHUTDOWN)) {
			log.postWarning("Attempting to close while inactive!");
			return;
		}
		
		log.postInfo("Closing connection...");
		
		readThread.interrupt();
		writeThread.interrupt();
		
		close(in, "input stream");
		close(out, "output stream");
		close(socket, "socket");
		
		readThread.doJoin();
		writeThread.doJoin();
		
		state.set(STATE_TERMINATED);
		
		log.postInfo("Connection closed; "
				+ packetsSent + (packetsSent == 1 ? " packet" : " packets")
				+ " sent (" + out.size() + " bytes), "
				+ packetsReceived + (packetsReceived == 1 ? " packet" : " packets")
				+ " received.");
	}
	
	/**
	 * Closes a Closeable and logs an IOException, if it is thrown.
	 */
	private void close(Closeable closeable, String identifier) {
		try {
			closeable.close();
		} catch(IOException e) {
			log.postWarning("IOException while closing " + identifier + " (" + e.getMessage() + ")");
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Getters/Setters=====------------
	//--------------------==========--------------------
	
	/**
	 * @return {@code true} if this connection is active; {@code false}
	 * otherwise.
	 */
	public boolean isActive() {
		return state.get() == STATE_ACTIVE;
	}
	
	/**
	 * @return {@code true} if this connection is terminated; {@code false}
	 * otherwise.
	 */
	public boolean isTerminated() {
		return state.get() == STATE_TERMINATED;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Base implementation for TCP read/write threads. Provides an uncaught
	 * exception handler which invokes {@link TCPConnection#requestClose()},
	 * and the {@link #doJoin()} method.
	 */
	private abstract class TCPThread extends Thread {
		
		public TCPThread(String threadName) {
			super(threadName);
			
			setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log.postSevere("Uncaught exception " + e.getClass().getSimpleName()
							+ " in " + t.getName(), e);
					requestClose();
				}
			});
		}
		
		/**
		 * Invokes join() on this thread and silently ignores an
		 * InterruptedException if it is thrown.
		 */
		public void doJoin() {
			try {
				join();
			} catch(InterruptedException e) {
				log.postWarning("Interrupted while waiting for " + getName() + " to die!");
			}
		}
	}
	
	/**
	 * The TCPWriteThread class handles the reading operations from a
	 * TCPConnection's input stream.
	 */
	private class TCPReadThread extends TCPThread {
		
		public TCPReadThread(String threadName) {
			super(threadName);
		}
		
		@Override
		public void run() {
			try {
				while(isActive())
					readPacket();
			} catch(IOException e) {
				// An IOException being thrown is a standard part of the
				// shutdown procedure (as shutting down the socket will cause
				// any in-progress IO operation to fail). However, if an
				// IOException is thrown outside of shutdown procedures,
				// something has gone wrong.
				if(isActive()) {
					if(!socket.isClosed())
						log.postSevere("IOException thrown in read thread before connection shutdown!", e);
					requestClose();
				} else
					log.postDebug("Read thread shutting down (exception caught)...");
				return;
			}
			log.postInfo("Read thread shutting down...");
		}
		
	}
	
	/**
	 * The TCPWriteThread class handles the writing operations to a TCPConnection's
	 * output stream.
	 */
	private class TCPWriteThread extends TCPThread {
		
		public TCPWriteThread(String threadName) {
			super(threadName);
		}
		
		@Override
		public void run() {
			/*
			 * The writing strategy is simple:
			 * 1. Block until we receive a packet, and write it.
			 * 2. Keep writing packets until the queue is emptied.
			 * 3. Flush the output stream.
			 * 4. Repeat.
			 * 
			 * This strategy has some nice advantages:
			 * a) We only wait when we've caught up to the packet producer
			 *    thread, and thus keep up with demand.
			 * b) Packets are (hopefully) batched nicely enough that we don't
			 *    flush too often.
			 */
			try {
				while(isActive()) {
					writePacketWithBlock();	// wait for a packet
					while(writePacket());	// empty the queue
					out.flush();			// flush the batch of packets
				}
			} catch(InterruptedException | IOException e) {
				// An IOException being thrown is a standard part of the
				// shutdown procedure (as shutting down the socket will cause
				// any in-progress IO operation to fail). However, if an
				// IOException is thrown outside of shutdown procedures,
				// something has gone wrong. Ditto with InterruptedException.
				if(isActive()) {
					if(!socket.isClosed())
						log.postSevere(e.getClass().getSimpleName() + " thrown in write "
								+ "thread before connection shutdown!");
					requestClose();
				} else
					log.postDebug("Write thread shutting down (exception caught)...");
				return;
			}
			log.postInfo("Write thread shutting down...");
		}
		
	}
	
}
