package com.stabilise.core;

import java.net.InetAddress;

import com.stabilise.network.Client;


public class GameClient extends Client {
	
	public GameClient(InetAddress address, int port) {
		super(address, port);
	}
	
	@Override
	protected void doUpdate() {
		
	}
	
}
