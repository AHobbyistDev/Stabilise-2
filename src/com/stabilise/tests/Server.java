package com.stabilise.tests;

import static com.stabilise.tests.NetworkingTesting.SERVER_BACKLOG;
import static com.stabilise.tests.NetworkingTesting.SERVER_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * To be run by {@link NetworkingTesting}
 */
public class Server implements Runnable {
	
	public Thread thread;
	
	public ServerSocket socket;
	public Socket clientSocket;
	
	//public DataInputStream in;
	//public DataOutputStream out;
	public BufferedReader in;
	public PrintWriter out;

	public Server() {
		System.out.println("Creating server");
		thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		try {
			System.out.println("Initiating server");
			socket = new ServerSocket(SERVER_PORT, SERVER_BACKLOG, InetAddress.getLocalHost());
			clientSocket = socket.accept();
			System.out.println("Connection with client established");
			
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			//out = new PrinStream(clientSocket.getOutputStream());
			
	        String fromClient;
	        
	        System.out.println("Ready to communicate with client");
	        
	        while((fromClient = in.readLine()) != null) {
	        	if(fromClient == "exit") break;
	        	System.out.println("Message from client: " + fromClient);
	        	out.println("Message received! Here is what you said, but in caps - " + fromClient.toUpperCase());
	        }
	        
	        System.out.println("Finished communication with client");
			
			in.close();
			out.close();
			clientSocket.close();
			socket.close();
			
			System.out.println("Server closed.");
		} catch(IOException e) {
			System.out.println("Server error!"/*, e*/);
		}
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Could not join"/*, e*/);
		}
	}

}
