package com.stabilise.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;

public class P254ProtocolSwitch extends Packet {
	
	private int protocolID;
	
	public P254ProtocolSwitch() {}
	
	
	/**
	 * @throws NullPointerException if newProtocol is null.
	 */
	public P254ProtocolSwitch(Protocol newProtocol) {
		protocolID = newProtocol.getID();
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		protocolID = in.readByte();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeByte(protocolID);
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		con.handlePeerProtocolSwitch(protocolID);
	}
	
}
