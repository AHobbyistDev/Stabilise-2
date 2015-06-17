package com.stabilise.tests.network;

import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;
import com.stabilise.util.AppDriver.Drivable;
import com.stabilise.util.Log.Level;


public class NetworkTest implements Drivable {
	
	ServerImpl server;
	ClientImpl client;
	AppDriver driver;
	
	public NetworkTest() {
		Log.setLogLevel(Level.ALL);
		server = new ServerImpl();
		server.runConcurrently();
		client = new ClientImpl();
		driver = AppDriver.driverFor(this, 60, 60, Log.get());
		driver.run();
	}
	
	public static void main(String[] args) {
		new NetworkTest();
	}
	
	@Override
	public void update() {
		client.update();
		if(!client.isConnected())
			driver.stop();
	}
	
	@Override
	public void render() {}
	
}
