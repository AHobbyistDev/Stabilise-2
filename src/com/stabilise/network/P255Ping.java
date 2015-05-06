package com.stabilise.network;

import java.io.IOException;

import com.stabilise.network.Packet.Important;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * This universal packet allows different machines to send ping requests which
 * double as keepalives.
 */
public class P255Ping extends Packet implements Important {
	
	/** true if this packet represents a request for a return ping packet;
	 * false if this is the return packet. */
	public boolean request;
	/** The ID of this ping transaction. */
	// Only sent as a byte because we really don't care about ID discrepancies
	// greater than 1; only that they're different.
	public int pingID;
	
	
	public P255Ping() {}
	
	public P255Ping(int pingCount, boolean request) {
		pingID = pingCount;
		this.request = request;
	}
	
	@Override
	public void readData(DataInStream in) throws IOException {
		request = in.readBoolean();
		pingID = in.readByte();
	}
	
	@Override
	public void writeData(DataOutStream out) throws IOException {
		out.writeBoolean(request);
		out.writeByte(pingID);
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		con.handlePing(this);
	}
	
}
