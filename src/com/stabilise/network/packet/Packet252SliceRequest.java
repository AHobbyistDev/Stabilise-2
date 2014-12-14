package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This packet indicates a notification from the client that it requires a
 * slices to be loaded or unloaded.
 * In the case of a 'loaded' notification, the server will send that slice to
 * the client; in the case of an 'unloaded' notification, the server will mark
 * the slice as 'unloaded', potentially allowing for a region to be unloaded
 * from memory.
 */
public class Packet252SliceRequest extends Packet {
	
	/** True if the client is requesting for the slice, false if the client is
	 * stating that it has unloaded the slice. */
	public boolean requested;
	
	/** The x-coordinate of the slice. */
	public int x;
	/** The y-coordinate of the slice. */
	public int y;
	
	
	public Packet252SliceRequest() {}
	
	/**
	 * Creates a slice request packet.
	 * @param requested True if the server should send the slice data.
	 * @param x The x-coordinate of the slice.
	 * @param y The y-coordinate of the slice.
	 */
	public Packet252SliceRequest(boolean requested, int x, int y) {
		this.requested = requested;
		this.x = x;
		this.y = y;
	}

	@Override
	public void readData(DataInputStream in) throws IOException {
		requested = in.readBoolean();
		x = in.readInt();
		y = in.readInt();
	}

	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeBoolean(requested);
		out.writeInt(x);
		out.writeInt(y);
	}

	@Override
	public int getBytes() {					// Booleans count as 1 byte
		return 2 * Integer.SIZE / Byte.SIZE + 1;
	}

}
