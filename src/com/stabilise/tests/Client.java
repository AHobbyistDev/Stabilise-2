package com.stabilise.tests;

import static com.stabilise.tests.NetworkingTesting.SERVER_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * To be run by {@link NetworkingTesting}
 */
public class Client implements Runnable {
	
	public Thread thread;
	
	public Socket socket;
	
	//public DataInputStream in;
	//public DataOutputStream out;
	public BufferedReader in;
	public PrintWriter out;

	public Client() {
		System.out.println("Creating client.");
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			System.out.println("Initiating client.");
			socket = new Socket(InetAddress.getLocalHost(), SERVER_PORT);
			System.out.println("Connected to server.");
			
			//in = new DataInputStream(socket.getInputStream());
			//out = new DataOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			//in = new InputStreamReader(socket.getInputStream());
			//out = new PrintStream(socket.getOutputStream());
			
			BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
			String fromServer;
			String toServer;
			
			System.out.println("Ready to communicate with server.");
			out.println("ping");
			
			while((fromServer = in.readLine()) != null) {
				System.out.println("Message from server: " + fromServer);
				
				toServer = consoleIn.readLine();
				if(toServer.equals("exit")) break;
				if(toServer != null)
					out.println(toServer);
			}
			
			System.out.println("Finished communication with server");
			
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Client error!"/*, e*/);
		}
		System.out.println("Client finished");
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Could not join"/*, e*/);
		}
	}

}
