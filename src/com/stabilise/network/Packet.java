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
 * 
 * @param <V> The interface defining the method with which to handle this type
 * of packet.
 */
public abstract class Packet implements Sendable {
	
	/**
	 * Handles this packet - that is, performs some action in response to
	 * receiving this packet.
	 * 
	 * @param handler The handler with which to handle this packet.
	 * @param con The {@code TCPConnection} object with which this packet is
	 * associated.
	 * 
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
	protected static void writeString(String string, DataOutputStream out) throws IOException {
		if(string.length() > Maths.USHORT_MAX_VALUE)
			throw new IllegalArgumentException("The given string is too large!");
		out.writeShort(string.length());
		out.writeChars(string);
	}
	
}