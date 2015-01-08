package com.stabilise.network;

/**
 * The TCPWriteThread class handles the reading operations from a
 * TCPConnection's input stream.
 */
public class TCPReadThread extends Thread {
	
	/** The TCPConnection instance the thread is linked to. */
	private final TCPConnection connection;
	
	
	/**
	 * Creates a new TCPReadThread object.
	 * 
	 * @param connection The TCPConnection instance to handle the reading for.
	 * @param threadName The thread's name - to assist with debugging.
	 */
	public TCPReadThread(TCPConnection connection, String threadName) {
		super(threadName + "ReadThread");
		
		this.connection = connection;
	}
	
	@Override
	public void run() {
		while(connection.isRunning()) {
			boolean gotPacket = false;
			while(connection.readPacket()) {
				if(Thread.interrupted())
					break;
				gotPacket = true;
			}
			if(gotPacket) {
				synchronized(connection) {
					connection.notifyAll(); // notify in case the main thread is blocking
				}
			}
			try {
				sleep(10L);
			} catch(InterruptedException ignored) {}
		}
	}

}
