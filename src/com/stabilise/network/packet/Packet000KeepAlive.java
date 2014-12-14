package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The KeepAlive packet is a packet which is periodically sent by both the
 * client and the server to ensure the connection is still active.
 */
public class Packet000KeepAlive extends Packet {

	public Packet000KeepAlive() {
		
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		// don't read anything - until I find it apt to include the 'random number + 1'
		// protocol, we'll just be sending unnecessary bytes
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		// ditto for writing
	}
	
	@Override
	public int getBytes() {
		return 0;
	}

}
