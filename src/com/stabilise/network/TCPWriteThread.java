package com.stabilise.network;

import java.io.IOException;

import com.stabilise.util.Log;

/**
 * The TCPWriteThread class handles the writing operations to a TCPConnection's
 * output stream.
 */
public class TCPWriteThread extends Thread {
	
	/** The TCPConnection instance the thread is linked to. */
	private TCPConnection connection;
	
	/**
	 * Creates a new TCPWriteThread object.
	 * 
	 * @param connection The TCPConnection instance to handle the writing for.
	 * @param threadName The thread's name - to assist with debugging.
	 */
	public TCPWriteThread(TCPConnection connection, String threadName) {
		super(threadName + "WriteThread");
		
		this.connection = connection;
	}
	
	@Override
	public void run() {
		while(connection.isRunning()) {
			boolean flushStream = false;
			
			while(connection.writePacket())
				flushStream = true;
			
			// Flush the stream if any packets have been sent
			if(flushStream) {
				try {
					connection.getOutputStream().flush();
				} catch(IOException e) {
					Log.getAgent("TCP_WRITE_THREAD").postSevere("Could not flush output stream!", e);
				}
			}
			
			try {
				sleep(2L);
			} catch (InterruptedException ignored) {}
		}
	}
	
}
