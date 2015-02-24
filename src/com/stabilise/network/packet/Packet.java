package com.stabilise.network.packet;

import static com.stabilise.util.collect.DuplicatePolicy.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.Sendable;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.InstantiationRegistry;
import com.stabilise.util.maths.Maths;

/**
 * Packets are modular chunks of data which may be sent over a network as a
 * means of transmitting information.
 */
public abstract class Packet implements Sendable {
	
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
	
	/**
	 * Instantiates an instance of a packet with the specified ID.
	 * 
	 * @return The packet.
	 * @throws FaultyPacketRegistrationException if the packet could not be
	 * created, either due to {@code id} lacking a mapping or instantiation
	 * outright failing.
	 */
	@UserThread("ReadThread")
	private static Packet createPacket(int id) {
		try {
			return PACKETS.instantiate(id);
		} catch(RuntimeException e) {
			throw new FaultyPacketRegistrationException("Packet of ID " + id
					+ " could not be instantiated! (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * Reads the next packet from the provided input stream.
	 * 
	 * @return The packet.
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws FaultyPacketRegistrationException if the packet was registered
	 * incorrectly (in this case the error lies in the registration code).
	 * @throws IOException if an I/O error occurs, or the stream has ended.
	 */
	@UserThread("ReadThread")
	public static Packet readPacket(DataInputStream in) throws IOException {
		int id = in.read(); // ID is always first byte
		if(id == -1) // end of stream; abort!
			throw new IOException("End of stream reached");
		Packet packet = createPacket(id);
		packet.readData(in);
		return packet;
	}
	
	/**
	 * Writes a packet to the provided output stream.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if an I/O error occurs.
	 */
	@UserThread("WriteThread")
	public static void writePacket(DataOutputStream out, Packet packet) throws IOException {
		out.writeByte(packet.getID()); // ID is always first byte
		packet.writeData(out);
	}
	
	/**
	 * Registers a packet type.
	 * 
	 * @param id The packet's ID. Must be positive and not a duplicate.
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
		
		PACKETS.lock();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * An exception type which indicates that a packet type was registered
	 * incorrectly.
	 */
	public static class FaultyPacketRegistrationException extends RuntimeException {
		private static final long serialVersionUID = -5809914071064815896L;
		private FaultyPacketRegistrationException(String msg) {
			super(msg);
		}
	}
	
}