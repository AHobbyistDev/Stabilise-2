package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The BeginTick packet indicates that any following packets will be associated
 * with the specified tick.
 */
public class Packet005BeginTick extends Packet {
	
	/** The tick to associate the following packets with.
	 * @see com.stabilise.world.WorldInfo.age */
	public long tick;
	
	
	public Packet005BeginTick() {
		
	}
	
	public Packet005BeginTick(long tick) {
		this.tick = tick;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		tick = in.readLong();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeLong(tick);
	}
	
}
