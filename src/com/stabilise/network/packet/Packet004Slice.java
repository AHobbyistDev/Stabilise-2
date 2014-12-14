package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.world.Slice;

/**
 * A slice packet contains all the info about a slice.
 */
public class Packet004Slice extends Packet {
	
	/** The slice's x-coordinate, in slice-lengths. */
	public int x;
	/** The slice's y coordinate, in slice-lengths. */
	public int y;
	
	/** The tiles constituting the slice. */
	public int[] tiles;
	
	
	public Packet004Slice() {
		
	}
	
	public Packet004Slice(Slice slice) {
		x = slice.x;
		y = slice.y;
		tiles = slice.getTilesAsIntArray();
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		
		tiles = new int[Slice.SLICE_SIZE * Slice.SLICE_SIZE];
		for(int i = 0; i < Slice.SLICE_SIZE * Slice.SLICE_SIZE; i++)
			tiles[i] = in.read();	// TODO: For now, sending the tile IDs as bytes, not ints
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		
		for(int i = 0; i < tiles.length; i++)
			out.write(tiles[i]);
	}
	
	@Override
	public int getBytes() {
		return tiles.length + 2 * Integer.SIZE / Byte.SIZE;
	}
	
}
