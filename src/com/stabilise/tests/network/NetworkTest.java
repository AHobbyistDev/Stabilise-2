package com.stabilise.tests.network;

import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;
import com.stabilise.util.AppDriver.Drivable;
import com.stabilise.util.Log.Level;


public class NetworkTest implements Drivable {
	
	Server server;
	Client client;
	AppDriver driver;
	
	public NetworkTest() {
		Log.setLogLevel(Level.ALL);
		server = new Server();
		server.runConcurrently();
		client = new Client();
		driver = AppDriver.getDriverFor(this, 20, 20, Log.get());
		driver.run();
	}
	
	public static void main(String[] args) {
		new NetworkTest();
	}
	
	@Override
	public void update() {
		client.update();
		if(client.connection.isTerminated())
			driver.stop();
	}
	
	@Override
	public void render() {
		
	}
	
}
