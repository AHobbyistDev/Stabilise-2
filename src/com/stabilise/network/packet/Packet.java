package com.stabilise.network.packet;

import static com.stabilise.util.collect.Registry.DuplicatePolicy.*;

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
			new InstantiationRegistry<Packet>("packets", 256, THROW_EXCEPTION);
	/** Flags corredponsing to whether a packet may be sent by a client. */
	private static final boolean[] clientPackets = new boolean[256];
	/** Flags corredponsing to whether a packet may be sent by a server. */
	private static final boolean[] serverPackets = new boolean[256];
	
	
	public Packet() {}
	
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
	 * Reads and returns a string from the input stream.
	 * This should be used instead of in.readUTF() or in.readChar() in
	 * subclasses of packet.
	 * 
	 * @param in The input stream from which to read the string.
	 * 
	 * @return The read string.
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * reading the string.
	 */
	protected final String readString(DataInputStream in) throws IOException {
		short length = in.readShort();
		
		// Note: No < 0 or > Short.MAX_VALUE checks for now
		
		StringBuilder builder = new StringBuilder();
		
		for(short i = 0; i < length; i++)
			builder.append(in.readChar());
		
		return builder.toString();
	}
	
	/**
	 * Writes a string to the output stream.
	 * This should be used instead of out.writeUTF() or out.writeChars() in
	 * subclasses of packet.
	 * 
	 * @param string The string to write to the output stream.
	 * @param out The output stream to write the string to.
	 * 
	 * @throws IllegalArgumentException Thrown if the given string is null, or
	 * exceeds 32767 characters.
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * writing the string.
	 */
	protected final void writeString(String string, DataOutputStream out) throws IOException {
		if(string == null) {
			throw new IllegalArgumentException("The given string is null!");
		} else if(string.length() > Short.MAX_VALUE) {
			throw new IllegalArgumentException("The given string is too large!");
		} else {
			out.writeShort(string.length());
			out.writeChars(string);
		}
	}
	
	/**
	 * Checks for whether or not the packet may be sent by a client.
	 * 
	 * @return True if the packet may be sent by a client.
	 */
	public final boolean isClientPacket() {
		return clientPackets[getID()];
	}
	
	/**
	 * Checks for whether or not the packet may be sent by a server.
	 * 
	 * @return True if the packet may be sent by a server.
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
	 * @return The packet, or {@code null} if it could not be created.
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