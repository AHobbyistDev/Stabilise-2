package com.stabilise.network.protocol;

import static com.stabilise.util.collect.DuplicatePolicy.THROW_EXCEPTION;
import static com.stabilise.util.maths.Maths.UBYTE_MAX_VALUE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import com.stabilise.network.Packet;
import com.stabilise.network.protocol.handshake.*;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.InstantiationRegistry;
import com.stabilise.util.maths.Maths;

/**
 * Defines the set of protocols a client-server connection may occupy. Each
 * protocol may define up to 256 client (i.e. packets to be sent by a client to
 * a server, or <i>serverbound</i>) packets and 256 server (<i>clientbound</i>)
 * packets, whose IDs range from {@code 0} to {@code 255}.
 */
public enum Protocol {
	
	HANDSHAKE {{
		registerClientPacket(0, C000VersionInfo.class);
		registerClientPacket(1, C001Disconnect.class);
		registerServerPacket(0, S000VersionInfo.class);
	}},
	LOGIN {{
		
	}},
	GAME {
		
	};
	
	/** Registry of packets sent by the server to the client (i.e. clientbound
	 * packets). */
	private final InstantiationRegistry<Packet> serverPackets =
			new InstantiationRegistry<>(UBYTE_MAX_VALUE, THROW_EXCEPTION, Packet.class);
	/** Registry of packets sent by the client to the server (i.e. serverbound
	 * packets). */
	private final InstantiationRegistry<Packet> clientPackets =
			new InstantiationRegistry<>(UBYTE_MAX_VALUE, THROW_EXCEPTION, Packet.class);
	
	
	/**
	 * Registers a server (i.e. clientbound) packet.
	 * 
	 * @see InstantiationRegistry#register(int, Class, Class...)
	 */
	protected void registerServerPacket(int id, Class<? extends Packet> packetClass) {
		if(id > MAX_PACKET_ID)
			throw new IllegalArgumentException("Packet ID for " + packetClass.getSimpleName()
					+ " (" + id + ") too large!");
		serverPackets.register(id, packetClass);
	}
	
	/**
	 * Registers a client (i.e. serverbound) packet.
	 * 
	 * @see InstantiationRegistry#register(int, Class, Class...)
	 */
	protected void registerClientPacket(int id, Class<? extends Packet> packetClass) {
		if(id > MAX_PACKET_ID)
			throw new IllegalArgumentException("Packet ID for " + packetClass.getSimpleName()
					+ " (" + id + ") too large!");
		clientPackets.register(id, packetClass);
	}
	
	/**
	 * Instantiates an instance of a packet with the specified ID. If {@code
	 * server} is {@code true}, the created packet will be a serverbound/client
	 * packet; otherwise, it will be a clientbound/server packet.
	 * 
	 * @param server {@code true} if this is a server; {@code false} if this is
	 * a client.
	 * @param id The ID of the packet.
	 * 
	 * @return The packet.
	 * @throws FaultyPacketRegistrationException if the packet could not be
	 * created, either due to {@code id} lacking a mapping or instantiation
	 * outright failing.
	 */
	@UserThread("ReadThread")
	private Packet createPacket(boolean server, int id) {
		try {
			return server
					? clientPackets.instantiate(id)
					: serverPackets.instantiate(id);
		} catch(RuntimeException e) {
			throw new FaultyPacketRegistrationException((server ? "Serverbound" : "Clientbound")
					+ " packet of ID " + id
					+ " could not be instantiated! (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * Reads the next packet from the provided input stream.
	 * 
	 * @param server {@code true} if the packet is to originate from a server
	 * (i.e. we want a clientbound packet); {@code false} if we want a
	 * serverbound packet.
	 * @param in The input stream from which to read the packet.
	 * 
	 * @return The packet, or {@code null} if the end of stream has been
	 * reached (i.e. the socket has closed).
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws FaultyPacketRegistrationException if the packet was registered
	 * incorrectly (in this case the error lies in the registration code).
	 * @throws IOException if an I/O error occurs.
	 */
	@UserThread("ReadThread")
	public Packet readPacket(boolean server, DataInputStream in) throws IOException {
		int id = in.read(); // ID is always first byte
		if(id == -1) // end of stream; abort!
			return null;
		Packet packet = createPacket(server, id);
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
	public void writePacket(DataOutputStream out, Packet packet) throws IOException {
		out.writeByte(packet.getID()); // ID is always first byte
		packet.writeData(out);
	}
	
	/**
	 * @return {@code true} if the given packet is a server packet; {@code
	 * false} otherwise.
	 * @throws NullPointerException if {@code packet} is {@code null}.
	 */
	public boolean isServerPacket(Packet packet) {
		return serverPackets.getID(packet.getClass()) != -1;
	}
	
	/**
	 * @return {@code true} if the given packet is a client packet; {@code
	 * false} otherwise.
	 * @throws NullPointerException if {@code packet} is {@code null}.
	 */
	public boolean isClientPacket(Packet packet) {
		return clientPackets.getID(packet.getClass()) != -1;
	}
	
	//--------------------==========--------------------
	//--------------=====Static Stuff=====--------------
	//--------------------==========--------------------
	
	public static final int MAX_PACKET_ID = Maths.UBYTE_MAX_VALUE;
	
	/** Maps Packet Class -> Packet ID for all packets across all protocols. */
	private static final Map<Class<? extends Packet>, Integer> PACKET_IDS =
			new IdentityHashMap<>();
	
	static {
		for(Protocol protocol : Protocol.values()) {
			checkPackets(protocol, protocol.clientPackets);
			checkPackets(protocol, protocol.serverPackets);
		}
	}
	
	private static void checkPackets(Protocol protocol, InstantiationRegistry<Packet> registry) {
		registry.lock();
		for(Class<? extends Packet> pClass : registry) {
			if(PACKET_IDS.containsKey(pClass))
				throw new RuntimeException("Packet \"" + pClass.getSimpleName()
						+ "\" already registered; cannot reassign to protocol \""
						+ protocol.toString() + "\"");
			PACKET_IDS.put(pClass, registry.getID(pClass));
		}
	}
	
	/**
	 * Gets the ID of a packet.
	 * 
	 * @return The packet's ID, or {@code -1} if the packet is of a type that
	 * has not been assigned to any protocol.
	 * @throws NullPointerException if {@code packet} is {@code null}.
	 */
	public static int getPacketID(Packet packet) {
		Integer id = PACKET_IDS.get(packet.getClass());
		return id == null ? -1 : id.intValue();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * An exception type which indicates that a packet type was registered
	 * incorrectly.
	 */
	public static class FaultyPacketRegistrationException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private FaultyPacketRegistrationException(String msg) {
			super(msg);
		}
	}
	
}
