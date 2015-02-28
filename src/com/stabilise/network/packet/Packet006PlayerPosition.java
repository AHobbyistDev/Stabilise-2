package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Packet;

/**
 * This packet is used to send a player's location and state.
 */
public class Packet006PlayerPosition extends Packet {
	
	/** The player's coordinates. */
	public double x, y;
	
	
	public Packet006PlayerPosition() {
		
	}
	
	public Packet006PlayerPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
	}
	
}
