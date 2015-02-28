package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;

/**
 * This packet indicates a packet send request. e.g. This can be sent by the
 * client to request the server to resend the ServerInfo packet when the user
 * refreshes the server list.
 */
public class Packet255RequestPacket extends Packet {
	
	/** The ID of the packet requested to be sent. */
	public int requestedPacketID;
	
	
	public Packet255RequestPacket() {}
	
	public Packet255RequestPacket(int requestedPacketID) {
		this.requestedPacketID = requestedPacketID;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		requestedPacketID = in.read();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeByte(requestedPacketID);
	}
	
}
