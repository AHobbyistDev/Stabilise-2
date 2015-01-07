package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The KeepAlive packet is a packet which is periodically sent by both the
 * client and the server to ensure the connection is still active.
 */
public class Packet000KeepAlive extends Packet {
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		// don't read anything - until I find it apt to use a "process random
		// number" protocol, we'll just be sending useless data
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		// ditto for writing
	}
	
}
