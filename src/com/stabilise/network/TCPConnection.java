package com.stabilise.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

import com.stabilise.network.packet.Packet;
import com.stabilise.util.Log;

/**
 * A TCPConnection object represents the connection between a server and a
 * client, and provides a gateway for them to interact.
 */
public class TCPConnection {
	
	/** The socket upon which to base the connection. */
	protected Socket socket;
	
	/** The socket's input stream. */
	private DataInputStream in;
	/** The socket's output stream. */
	private DataOutputStream out;
	
	/** The queue of received packets. */
	private LinkedBlockingQueue<Packet> packetQueueIn = new LinkedBlockingQueue<Packet>();
	/** The queue of packets to be sent. */
	private LinkedBlockingQueue<Packet> packetQueueOut = new LinkedBlockingQueue<Packet>();
	
	/** True if this is a server-side connection. */
	private boolean server;
	
	/** True if the connection is active. */
	private volatile boolean running = true;
	/** True if the connection has been terminated. */
	private volatile boolean terminated = false;
	
	/** The thread to handle all packet read operations. */
	private TCPReadThread readThread;
	/** The thread to handle all packet write operations. */
	private TCPWriteThread writeThread;
	
	/** Tracks the number of packets sent though the connection. */
	private int totalPacketsSent = 0;
	/** Tracks the total number of bytes sent through the connection. */
	private long totalBytesSent = 0;
	/** Tracks the total number of packets received through the connection. */
	private int totalPacketsReceived = 0;
	/** Tracks the total number of bytes received through the connection. */
	private long totalBytesReceived = 0;
	
	/** The connection's logging agent. */
	private Log log;
	
	
	/**
	 * Creates a new TCPConnection.
	 * 
	 * @param socket The socket upon which to base the connection.
	 * @param server Whether or not the connection represents a server-side
	 * connection.
	 * 
	 * @throws IOException Thrown if the connection could not be created.
	 */
	public TCPConnection(Socket socket, boolean server) throws IOException {
		this.socket = socket;
		this.server = server;
		
		log = Log.getAgent(server ? "SERVER" : "CLIENT");
		
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		
		/*
		try {
			socket.setSoTimeout(5);
		} catch(SocketException e) {
			log.logCritical("Could not set timeout", e);
		}
		*/
		
		readThread = new TCPReadThread(this, server ? "ServerSocket" : "ClientSocket");
		writeThread = new TCPWriteThread(this, server ? "ServerSocket" : "ClientSocket");
		
		readThread.start();
		writeThread.start();
	}
	
	/**
	 * Queues a packet for sending.
	 * 
	 * @param The packet to queue for sending.
	 * 
	 * @return True if the packet was successfully queued, and false if the
	 * packet is null, the packet is invalid, or the queue is full.
	 */
	public boolean queuePacket(Packet packet) {
		if(packet == null) return false;
		
		if((!server && packet.isClientPacket()) || (server && packet.isServerPacket())) {
			return packetQueueOut.offer(packet);
		} else {
			if(server) {
				log.logCritical("Attempting to send a client-only packet!");
			} else {
				log.logCritical("Attempting to send a server-only packet!");
			}
			return false;
		}
	}
	
	/**
	 * Queues a packet for sending, and blocks until the packet may be
	 * queued.
	 * Note that this WILL continue looping forever if the packet is invalid.
	 * This should be used sparingly, and only if the packet in question is
	 * critical to the operation of the program.
	 * 
	 * @param The packet to queue for sending.
	 */
	public void queuePacketWithBlock(Packet packet) {
		while(!queuePacket(packet)) {
			try {
				Thread.sleep(5L);
			} catch(InterruptedException e) {
				// meh
			}
		}
	}
	
	/**
	 * Polls the input packet queue for the next packet.
	 * 
	 * @return The oldest queued packet, or null if the queue is empty.
	 */
	public Packet getPacket() {
		return packetQueueIn.poll();
	}
	
	/**
	 * Blocks the current thread until a packet is received.
	 * This should be used sparingly and only when it is absolutely essential
	 * to block until the packet is received.
	 * 
	 * @return The oldest queued packet, or the first packet to be added if the
	 * queue is empty.
	 */
	public Packet getPacketWithBlock() {
		Packet packet;
		while((packet = getPacket()) == null) {
			try {
				Thread.sleep(5L);
			} catch(InterruptedException e) {
				// meh
			}
		}
		return packet;
	}
	
	/**
	 * Reads a packet from the socket's input stream.
	 * 
	 * @return True if a packet was successfully read. A value of false
	 * indicates either an I/O Exception or the end of the stream having been reached.
	 */
	public boolean readPacket() {
		try {
			Packet packet = Packet.readPacket(in);
			
			if(packet == null) return false;
			
			packetQueueIn.offer(packet);
			
			totalPacketsReceived++;
			totalBytesReceived += 1 + packet.getBytes();
			
			// TODO: Is it better to do it this way?
			//return packetQueueIn.offer(packet);
			return true;
		} catch (IOException e) {
			log.logCritical("Exception while reading packet", e);
		}
		return false;
	}
	
	/**
	 * Writes a packet to the socket's output stream.
	 * 
	 * @return True if the packet was successfully written.
	 */
	public boolean writePacket() {
		Packet packet = packetQueueOut.poll();
		
		if(packet != null) {
			try {
				if(Packet.writePacket(out, packet)) {
					totalPacketsSent++;
					totalBytesSent += 1 + packet.getBytes();
				}
				
				return true;
			} catch (IOException e) {
				log.logCritical("Exception while writing packet", e);
			}
		}
		
		return false;
	}
	
	/**
	 * Closes the connection and releases any resources held by it.
	 */
	public void closeConnection() {
		if(terminated) {
			log.logCritical("Connection already closed!");
			return;
		}
		
		terminated = true;
		
		log.logMessage("Closing connection...");
		
		int written = out.size();
		
		try {
			in.close();
		} catch (IOException e) {
			log.logCritical("Could not close input stream!");
		}
		
		in = null;
		
		try {
			out.close();
		} catch (IOException e) {
			log.logCritical("Could not close output stream!");
		}
		
		out = null;
		
		running =  false;
		
		// This needs to go before the thread-removing because only the closing
		// of the socket will wake the read thread from its constant blocking
		// from the read() method of the DataInputStream class.
		try {
			socket.close();
		} catch (IOException e) {
			log.logCritical("Connection socket could not close!");
		}
		
		readThread.interrupt();
		writeThread.interrupt();
		
		try {
			readThread.join();
		} catch (InterruptedException e) {
			log.logCritical("Interrupted while waiting for read thread to join!");
		}
		
		try {
			writeThread.join();
		} catch (InterruptedException e) {
			log.logCritical("Interrupted while waiting for write thread to join!");
		}
		
		readThread = null;
		writeThread = null;
		
		log.logMessage("Connection closed; " + 
				totalPacketsSent + " packet(s) sent (" + totalBytesSent + "/" + written + " bytes), " +
				totalPacketsReceived + " packet(s) received (" + totalBytesReceived + " bytes).");
	}
	
	//--------------------==========--------------------
	//-------------=====Getters/Setters=====------------
	//--------------------==========--------------------
	
	/** Returns the output stream being used for the connection. */
	public DataOutputStream getOutputStream() {
		return out;
	}
	
	/** Returns true if the connection is active; false otherwise. */
	public boolean isRunning() {
		return running;
	}

}
