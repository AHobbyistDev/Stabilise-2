package com.stabilise.network.packet;

import static com.stabilise.util.collect.DuplicatePolicy.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.util.Log;
import com.stabilise.util.collect.InstantiationRegistry;

/**
 * Packets are modular chunks of data which may be sent over a network as a
 * means of transmitting information.
 */
public abstract class Packet {
	
	/** The packet registry. */
	private static final InstantiationRegistry<Packet> PACKETS =
			new InstantiationRegistry<Packet>(256, THROW_EXCEPTION, Packet.class);
	/** Flags corresponding to whether a packet may be sent by a client. */
	private static final boolean[] clientPackets = new boolean[256];
	/** Flags corresponding to whether a packet may be sent by a server. */
	private static final boolean[] serverPackets = new boolean[256];
	
	
	/**
	 * @return This packet's ID.
	 */
	public final int getID() {
		return PACKETS.getID(getClass());
	}
	
	/**
	 * Reads the packet data from the provided input stream.
	 */
	public abstract void readData(DataInputStream in) throws IOException;
	
	/**
	 * Writes the packet data to the provided output stream.
	 */
	public abstract void writeData(DataOutputStream out) throws IOException;
	
	/**
	 * Reads and returns a string from the provided input stream.
	 * 
	 * @param in The input stream from which to read the string.
	 * 
	 * @return The read string.
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException
	 */
	protected final String readString(DataInputStream in) throws IOException {
		short length = in.readShort();
		StringBuilder sb = new StringBuilder();
		while(length-- > 0)
			sb.append(in.readChar());
		return sb.toString();
	}
	
	/**
	 * Writes a string to the provided output stream.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IllegalArgumentException {@code string} exceeds 32767
	 * characters.
	 * @throws IOException
	 */
	protected final void writeString(String string, DataOutputStream out) throws IOException {
		if(string.length() > Short.MAX_VALUE)
			throw new IllegalArgumentException("The given string is too large!");
		out.writeShort(string.length());
		out.writeChars(string);
	}
	
	/**
	 * @return {@code true} if this packet may be sent by a client; {@code
	 * false} otherwise.
	 */
	public final boolean isClientPacket() {
		return clientPackets[getID()];
	}
	
	/**
	 * @return {@code true} if this packet may be sent by a server; {@code
	 * false} otherwise.
	 */
	public final boolean isServerPacket() {
		return serverPackets[getID()];
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Instantiates an instance of a packet with the specified ID.
	 * 
	 * @return The packet, or {@code null} if it could not be created, either
	 * due to {@code id} lacking a mapping or instantiation outright failing.
	 */
	public static Packet createPacket(int id) {
		try {
			return PACKETS.instantiate(id);
		} catch(RuntimeException e) {
			Log.get().postWarning("Packet of ID " + id + " could not be instantiated!", e);
		}
		return null;
	}
	
	/**
	 * Reads the next packet from the provided input stream.
	 * 
	 * @return The packet, or {@code null} if a packet could not be read.
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException
	 */
	public static Packet readPacket(DataInputStream in) throws IOException {
		int id = in.read(); // ID is always first byte
		if(id == -1)
			return null;
		Packet packet = createPacket(id);
		if(packet == null)
			return null;
		packet.readData(in);
		return packet;
	}
	
	/**
	 * Writes a packet to the provided output stream.
	 * 
	 * @param out The output stream to write the packet to.
	 * @param packet The packet to write.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException
	 */
	public static void writePacket(DataOutputStream out, Packet packet) throws IOException {
		out.writeByte(packet.getID()); // ID is always first byte
		packet.writeData(out);
	}
	
	/**
	 * Registers and lists a packet type so that it may be properly referenced.
	 * 
	 * @param id The packet's ID.
	 * @param packetClass The packet's class.
	 * @param clientPacket True if the packet may be sent by a client.
	 * @param serverPacket True if the packet may be sent by a server.
	 */
	private static void registerPacket(int id, Class<? extends Packet> packetClass, boolean clientPacket, boolean serverPacket) {
		PACKETS.register(id, packetClass);
		clientPackets[id] = clientPacket;
		serverPackets[id] = serverPacket;
	}
	
	// Register all the packets!
	static {								// Can send?    Client  Server
		registerPacket(0, Packet000KeepAlive.class, 		true, 	true);
		registerPacket(1, Packet001ServerInfo.class, 		false, 	true);
		registerPacket(2, Packet002Login.class, 			true, 	false);
		registerPacket(3, Packet003LoginInfo.class,			false,	true);
		registerPacket(4, Packet004Slice.class, 			false, 	true);
		registerPacket(5, Packet005BeginTick.class, 		true, 	true);
		registerPacket(6, Packet006PlayerPosition.class,	true,	true);
		
		registerPacket(253, Packet253Pause.class,			true,	false);
		registerPacket(254, Packet254Disconnect.class, 		true, 	true);
		registerPacket(255, Packet255RequestPacket.class, 	true, 	true);
	}
	
}