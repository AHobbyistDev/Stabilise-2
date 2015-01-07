package com.stabilise.network;

/**
 * The TCPWriteThread class handles the reading operations from a
 * TCPConnection's input stream.
 */
public class TCPReadThread extends Thread {
	
	/** The TCPConnection instance the thread is linked to. */
	private TCPConnection connection;
	
	
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
			while(connection.readPacket())
				// possibly check for interruptions here
				;
			
			try {
				sleep(10L);
			} catch(InterruptedException e) {}
		}
	}

}
