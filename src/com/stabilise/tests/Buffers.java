package com.stabilise.tests;

import java.nio.ByteBuffer;

public class Buffers {

	private Buffers() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ByteBuffer b = ByteBuffer.allocate(5);
		b.putFloat(0.5f);
		b.put((byte)25);
		
		byte[] bytes = b.array();
		float f = Float.intBitsToFloat((bytes[0] << 24) + (bytes[1] << 16) + (bytes[2] << 8) + bytes[3]);
		System.out.println(f + "," + bytes[4]);
	}

}
