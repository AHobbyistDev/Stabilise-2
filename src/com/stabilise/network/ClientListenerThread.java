package com.stabilise.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.stabilise.core.GameServer;

/**
 * The ClientListenerThread class is used by the game server to wait for
 * multiple client connections.
 */
@SuppressWarnings("deprecation")
public final class ClientListenerThread extends Thread {
	
	/** The GameServer for which the client is listening. */
	
	private GameServer server;
	/** The ServerSocket the thread is to listen for connections through. */
	private ServerSocket serverSocket;

	public ClientListenerThread(GameServer server, ServerSocket serverSocket) {
		super("ClientListenerThread");
		
		this.server = server;
		this.serverSocket = serverSocket;
	}
	
	@Override
	public void run() {
		try {
			while(server.running) {
				Socket socket = serverSocket.accept();
				server.addConnection(socket);
			}
		} catch (IOException e) {
			if(server.running) server.log.postWarning("(This can probably be ignored)", e);
			//Stabilise.crashGame(e);
		}
	}

}
