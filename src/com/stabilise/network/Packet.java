package com.stabilise.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.maths.Maths;

/**
 * Packets are modular chunks of data which may be sent over a network as a
 * means of transmitting information.
 * 
 * <p>All subclasses of {@code Packet} require a blank constructor so that they
 * may be instantiated reflectively.
 */
public abstract class Packet implements Sendable {
	
	/** A dummy packet which does nothing, to use when a non-null packet is
	 * otherwise required for API purposes. */
	public static final Packet DUMMY_PACKET = new Packet() {
		@Override public void readData(DataInputStream in) throws IOException {}
		@Override public void writeData(DataOutputStream out) throws IOException {}
		@Override public void handle(PacketHandler handler, TCPConnection con) {}
	};
	
	/**
	 * Implement this interface to indicate that a packet of a particular class
	 * is 'important' - when queued for sending, important packets are added to
	 * the head of the packet queue rather than the tail.
	 */
	public static interface Important {}
	
	
	/**
	 * Handles this packet - that is, performs some action in response to
	 * receiving this packet.
	 * 
	 * @param handler The handler with which to handle this packet.
	 * @param con The {@code TCPConnection} object with which this packet is
	 * associated.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws ClassCastException if the specified handler is not of the proper
	 * type to handle this packet.
	 */
	public abstract void handle(PacketHandler handler, TCPConnection con);
	
	/**
	 * Gets this packet's ID.
	 */
	public final int getID() {
		return Protocol.getPacketID(this);
	}
	
	/**
	 * Returns true if this is an {@link Important} packet.
	 */
	public final boolean isImportant() {
		return this instanceof Important;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Reads and returns a string from the provided input stream.
	 * 
	 * @param in The input stream from which to read the string.
	 * 
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException
	 */
	protected static String readString(DataInputStream in) throws IOException {
		int length = (int)in.readShort();
		StringBuilder sb = new StringBuilder(length);
		while(length-- > 0)
			sb.append(in.readChar());
		return sb.toString();
	}
	
	/**
	 * Writes a string to the provided output stream.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IllegalArgumentException if {@code string} exceeds 65535
	 * characters.
	 * @throws IOException
	 */
	protected static void writeString(DataOutputStream out, String string) throws IOException {
		if(string.length() > Maths.USHORT_MAX_VALUE)
			throw new IllegalArgumentException("The given string is too large!");
		out.writeShort(string.length());
		out.writeChars(string);
	}
	
	/**
	 * Writes an int array to the provided output stream.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException
	 */
	protected static void readIntArray(DataInputStream in, int[] arr) throws IOException {
		for(int i = 0; i < arr.length; i++)
			arr[i] = in.readInt();
	}
	
	/**
	 * Reads an int array from the provided output stream and stores the data
	 * in the provided array. The number of ints read is equal to the length of
	 * the array.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException
	 */
	protected static void writeIntArray(DataOutputStream out, int[] arr) throws IOException {
		for(int i = 0; i < arr.length; i++)
			out.writeInt(arr[i]);
	}
	
}