package com.stabilise.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;

public class P254ProtocolSwitch extends Packet {
	
	public Protocol protocol;
	
	public P254ProtocolSwitch() {}
	
	
	/**
	 * @throws NullPointerException if newProtocol is null.
	 */
	public P254ProtocolSwitch(Protocol newProtocol) {
		protocol = newProtocol;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		protocol = Protocol.getProtocol(in.readByte());
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeByte(protocol.getID());
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		con.handlePeerProtocolSwitch(protocol);
	}
	
}
