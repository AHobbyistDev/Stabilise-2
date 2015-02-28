package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;

/**
 * This packet indicates that a client is disconnecting.
 * If it is sent by the server, the client will be kicked.
 */
public class Packet254Disconnect extends Packet {
	
	/** The reason for the disconnect. */
	public String reason;
	
	
	public Packet254Disconnect() {}
	
	public Packet254Disconnect(String reason) {
		this.reason = reason;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		reason = readString(in);
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		writeString(reason, out);
	}
	
}
