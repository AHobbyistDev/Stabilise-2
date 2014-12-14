package com.stabilise.tests;

public class NetworkingTesting {
	
	public static final int SERVER_PORT = 9001;
	public static final int SERVER_BACKLOG = 2;

	public NetworkingTesting() {
		
	}
	
	public static void main(String[] args) {
		new Server();
		new Client();
	}

}
