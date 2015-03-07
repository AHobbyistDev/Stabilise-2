package com.stabilise.network.protocol.handshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;


public class C001Disconnect extends Packet {
	
	public C001Disconnect() {}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		((IServerHandshake)handler).handleDisconnect(this, con);
	}
	
}
