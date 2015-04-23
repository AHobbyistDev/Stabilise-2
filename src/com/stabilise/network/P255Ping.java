package com.stabilise.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;

public class P255Ping extends Packet {
	
	public boolean request;
	public int pingID;
	
	public P255Ping() {}
	
	public P255Ping(int pingCount, boolean request) {
		pingID = pingCount;
		this.request = request;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		request = in.readBoolean();
		pingID = in.readInt();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeBoolean(request);
		out.writeInt(pingID);
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		con.handlePing(this);
	}
	
}
