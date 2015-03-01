package com.stabilise.network.protocol.handshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;


public class C001Disconnect extends Packet {
	
	public C001Disconnect() {}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		
	}
	
}
