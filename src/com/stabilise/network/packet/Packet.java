package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.stabilise.util.Log;

/**
 * Packets are modular chunks of data which may be sent over a network as a
 * means of transmitting information.
 */
public abstract class Packet {
	
	/** Maps each packet's id to its class. */
	private static HashMap<Integer, Class<? extends Packet>> packetMap = new HashMap<Integer, Class<? extends Packet>>();
	/** Maps each packet's class to its id. */
	private static HashMap<Class<? extends Packet>, Integer> packetIDMap = new HashMap<Class<? extends Packet>, Integer>();
	/** Boolean values corresponding to whether a packet may be sent by a
	 * client. */
	private static boolean[] clientPackets = new boolean[256];
	/** Boolean values corresponding to whether a packet may be sent by a
	 * server. */
	private static boolean[] serverPackets = new boolean[256];
	
	
	public Packet() {}
	
	/**
	 * Returns a packet's ID.
	 * 
	 * @return The packet's ID.
	 */
	public final int getID() {
		// TODO: This may need to be replaced by a manual implementation in each
		// packet subclass if this creates too much overhead. For now, at least, it
		// should be negligible and hence not warrant concern.
		return packetIDMap.get(this.getClass());
	}
	
	/**
	 * Reads the packet data from the given input stream.
	 * This is left for Packet subclasses to implement, as they may format
	 * their data differently.
	 * 
	 * @param in The input stream from which to read the packet.
	 * 
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * reading the packet.
	 */
	public abstract void readData(DataInputStream in) throws IOException;
	
	/**
	 * Writes the packet data to the given input stream.
	 * This is left for Packet subclasses to implement, as they may format
	 * their data differently.
	 * 
	 * @param out The output stream to write the packet to.
	 * 
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * writing the packet.
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
	 * Returns the size of the packet in bytes.
	 */
	public abstract int getBytes();
	
	/**
	 * Returns the number of bytes in a string.
	 * 
	 * @param string The string.
	 * 
	 * @return The number of bytes in the string (including the length
	 * indicator which is used to prefix strings in packet transmissions), or
	 * 0 if the string is null.
	 */
	protected final int getStringBytes(String string) {
		if(string == null) return 0;
		return (string.length() * Character.SIZE + Short.SIZE) / Byte.SIZE;
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
	 * Returns an instance of a packet of the given ID.
	 * 
	 * @param packetID The packet ID.
	 * 
	 * @return The generated packet, or null if a packet of the given ID could
	 * not be created.
	 */
	public static Packet createPacket(int packetID) {
		try {
			return packetMap.get(packetID).newInstance();
		} catch(InstantiationException e) {
			Log.critical("Packet of id " + packetID + " could not be instantiated!", e);
		} catch(IllegalAccessException e) {
			Log.critical("Packet of id " + packetID + " could not be instantiated!", e);
		}
		return null;
	}
	
	/**
	 * Reads the next packet from the given input stream.
	 * 
	 * @param in The input stream from which to read the packet.
	 * 
	 * @return The packet, or null if a packet could not be read.
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * reading the packet.
	 */
	public static Packet readPacket(DataInputStream in) throws IOException {
		// TODO: Make more safe (comprehensively account for all possible
		// exceptions)
		
		
		if(in == null) {
			Log.critical("The given DataInputStream from which to read a packet is null!");
			return null;
		}
		
		// The packet ID will be the first byte
		int id;
		try {
			// This will throw an exception if we've reached the end of the stream.
			// TODO: This causes method blocking. FIND A FIX
			// TODO: Actually is it even a problem?
			id = in.read();
		} catch(IOException e) {		// Includes SocketTimeoutExceptions
			return null;
		}
		
		if(id == -1) return null;
		
		if(!packetMap.containsKey(id)) {
			Log.critical("Invalid packet ID " + id + " - ID not registered.");
			return null;
		}
		
		Packet packet = createPacket(id);
		
		if(packet == null) {
			Log.critical("Invalid packet ID " + id + " - createPacket() returned null.");
			return null;
		}
		
		packet.readData(in);
		
		return packet;
	}
	
	/**
	 * Writes a packet to the given output stream.
	 * 
	 * @param out The output stream to write the packet to.
	 * @param packet The packet to write.
	 * 
	 * @return True if the packet was successfully sent.
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * writing the packet.
	 */
	public static boolean writePacket(DataOutputStream out, Packet packet) throws IOException {
		if(out == null) {
			Log.critical("The given DataOutputStream from which a packet is to be written is null!");
			return false;
		}
		
		out.writeByte(packet.getID());
		
		packet.writeData(out);
		
		return true;
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
		if(packetMap.containsKey(id)) {
			Log.critical("Could not register packet class " + packetClass.toString() + " with ID " + id +
					" - ID is already being used by packet class " + packetMap.get(id).toString() + "!");
			return;
		} else if(packetIDMap.containsKey(packetClass)) {
			Log.critical("Could not register packet class " + packetClass.toString() + " with ID " + id +
					" - class is already registed to ID " + packetIDMap.get(packetClass) + "!");
			return;
		}
		
		packetMap.put(id,  packetClass);
		packetIDMap.put(packetClass, id);
		clientPackets[id] = clientPacket;
		serverPackets[id] = serverPacket;
	}
	
	/**
	 * The registration of all packet types.
	 */
	static {								// Can send?    Client  Server
		registerPacket(0, Packet000KeepAlive.class, 		true, 	true);
		registerPacket(1, Packet001ServerInfo.class, 		false, 	true);
		registerPacket(2, Packet002Login.class, 			true, 	false);
		registerPacket(3, Packet003LoginInfo.class,			false,	true);
		registerPacket(4, Packet004Slice.class, 			false, 	true);
		registerPacket(5, Packet005BeginTick.class, 		true, 	true);
		registerPacket(6, Packet006PlayerPosition.class,	true,	true);
		
		registerPacket(252, Packet252SliceRequest.class,	true,	false);
		registerPacket(253, Packet253Pause.class,			true,	false);
		registerPacket(254, Packet254Disconnect.class, 		true, 	true);
		registerPacket(255, Packet255RequestPacket.class, 	true, 	true);
	}
	
}