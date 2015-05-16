package com.stabilise.network.protocol.login;

import java.io.IOException;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * Packet request which indicates a login request was accepted.
 */
public class S001LoginAccepted extends Packet {
	
	public S001LoginAccepted() {
		
	}
	
	@Override
	public void readData(DataInStream in) throws IOException {
		
	}
	
	@Override
	public void writeData(DataOutStream out) throws IOException {
		
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		
	}
	
}
