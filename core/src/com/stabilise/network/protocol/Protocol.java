package com.stabilise.network.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.stabilise.network.P254ProtocolSwitch;
import com.stabilise.network.P255Ping;
import com.stabilise.network.Packet;
import com.stabilise.network.protocol.handshake.*;
import com.stabilise.network.protocol.update.*;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.maths.Maths;

/**
 * Defines the set of protocols a client-server connection may occupy. Each
 * protocol may define up to 256 client (i.e. packets to be sent by a client to
 * a server, or <i>serverbound</i>) packets and 256 server (<i>clientbound</i>)
 * packets, whose IDs range from {@code 0} to {@code 255}.
 */
public enum Protocol {
    
    HANDSHAKE {{
        registerClientPacket(0, C000VersionInfo.class, C000VersionInfo::new);
        registerServerPacket(0, S000VersionInfo.class, S000VersionInfo::new);
    }},
    LOGIN {
        // TODO
    },
    GAME {
        // TODO
    },
    UPDATE{{
        registerClientPacket(0, C000Checksums.class, C000Checksums::new);
        registerServerPacket(0, S000FileList.class, S000FileList::new);
        registerServerPacket(1, S001FileTransfer.class, S001FileTransfer::new);
    }};
    
    /** Registry of packets sent by the server to the client (i.e. clientbound
     * packets). */
    private final TypeFactory<Packet> serverPackets =
            new TypeFactory<>(new RegistryParams("ServerPackets",
                    MAX_NORMAL_PACKET_ID));
    /** Registry of packets sent by the client to the server (i.e. serverbound
     * packets). */
    private final TypeFactory<Packet> clientPackets =
            new TypeFactory<>(new RegistryParams("ClientPackets",
                    MAX_NORMAL_PACKET_ID));
    
    
    /**
     * Registers a server (i.e. clientbound) packet.
     */
    protected void registerServerPacket(int id, Class<? extends Packet> packetClass,
            Supplier<Packet> supplier) {
        checkID(id, packetClass);
        serverPackets.register(id, packetClass, supplier);
    }
    
    /**
     * Registers a client (i.e. serverbound) packet.
     */
    protected void registerClientPacket(int id, Class<? extends Packet> packetClass,
            Supplier<Packet> supplier) {
        checkID(id, packetClass);
        clientPackets.register(id, packetClass, supplier);
    }
    
    private void checkID(int id, Class<? extends Packet> packetClass) {
        if(id < 0 || id > MAX_NORMAL_PACKET_ID)
            throw new IllegalArgumentException("Packet ID for "
                    + packetClass.getSimpleName() + " (" + id + ") too "
                    + (id > MAX_NORMAL_PACKET_ID ? "large" : "small") + "!");
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
     * @return The packet, or {@code null} if there is no packet type with the
     * specified ID.
     * @throws FaultyPacketRegistrationException if the packet could not be
     * created due to instantiation outright failing.
     */
    @UserThread("ReadThread")
    private Packet createPacket(boolean server, int id) {
        try {
            if(id > MAX_NORMAL_PACKET_ID)
                // Offset of "- MAX_NORMAL_PACKET_ID - 1" to keep registry memory
                // footprint minimal.
                return RESERVED_PACKETS.create(id - MAX_NORMAL_PACKET_ID - 1);
            return server
                    ? clientPackets.create(id)
                    : serverPackets.create(id);
        } catch(RuntimeException e) {
            throw new FaultyPacketRegistrationException(
                      (server ? "Serverbound" : "Clientbound") + " packet of ID "
                    + id + " could not be instantiated! (" + e.getMessage()
                    + ")");
        }
    }
    
    /**
     * Reads the next packet from the provided input stream.
     * 
     * @param server {@code true} if the packet is to originate from a server
     * (i.e. we want a clientbound packet); {@code false} if we want a
     * serverbound packet.
     * @param in The input stream from which to read the packet.
     * @param log The logging agent to which to report problems.
     * 
     * @return The packet, or {@code null} if the end of stream has been
     * reached (i.e. the socket has closed), or {@link Packet#DUMMY_PACKET} if
     * there is no packet on this protocol with the specified ID.
     * @throws NullPointerException if {@code in} is {@code null}.
     * @throws FaultyPacketRegistrationException if the packet was registered
     * incorrectly (in this case the error lies in the registration code).
     * @throws IOException if an I/O error occurs.
     */
    @UserThread("ReadThread")
    public Packet readPacket(boolean server, DataInStream in, Log log) throws IOException {
        int id;
        try {
            id = in.read(); // ID is always first byte
        } catch(EOFException e) {
            // Since InflaterInputStream and prefers to throw an EOFException
            // rather than return -1 when the stream ends, we'll have to
            // catch this.
            return null;
        }
        if(id == -1) // end of stream; abort!
            return null;
        Packet packet = createPacket(server, id);
        if(packet == null) {
            log.postWarning("Unrecognised packet id " + id + " in protocol " + this
                    + " (perhaps it was sent before a protocol switch?)");
            return Packet.DUMMY_PACKET;
        }
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
    public void writePacket(DataOutStream out, Packet packet) throws IOException {
        out.writeByte(packet.getID()); // ID is always first byte
        packet.writeData(out);
    }
    
    /**
     * @return {@code true} if the given packet is a server packet; {@code
     * false} otherwise.
     * @throws NullPointerException if {@code packet} is {@code null}.
     */
    public boolean isServerPacket(Packet packet) {
        return serverPackets.getID(packet.getClass()) != -1
                || RESERVED_PACKETS.getID(packet.getClass()) != -1;
    }
    
    /**
     * @return {@code true} if the given packet is a client packet; {@code
     * false} otherwise.
     * @throws NullPointerException if {@code packet} is {@code null}.
     */
    public boolean isClientPacket(Packet packet) {
        return clientPackets.getID(packet.getClass()) != -1
                || RESERVED_PACKETS.getID(packet.getClass()) != -1;
    }
    
    /**
     * Returns this protocol's ID.
     */
    public int getID() {
        return ordinal();
    }
    
    //--------------------==========--------------------
    //--------------=====Static Stuff=====--------------
    //--------------------==========--------------------
    
    
    /** Largest allowable packet ID. */
    public static final int MAX_PACKET_ID = Maths.UBYTE_MAX_VALUE;
    /** The number of reserved IDs. */
    private static final int RESERVED_IDS = 8;
    /** The largest allowable ID a protocol-specific packet may have. */
    private static final int MAX_NORMAL_PACKET_ID = MAX_PACKET_ID - RESERVED_IDS;
    
    /** Maps Packet Class -> Packet ID for all packets across all protocols. */
    private static final Map<Class<? extends Packet>, Integer> PACKET_IDS =
            new IdentityHashMap<>();
    /** Registry of reserved/universal/protocol-independent packets - i.e.
     * packets which can be sent by any protocol, and by both the client and
     * server.
     * <p>To avoid wasting unnecessary memory, we give this registry a small
     * size, and offset IDs by {@code -MAX_NORMAL_PACKET_ID - 1} when accessing
     * entries. */
    private static final TypeFactory<Packet> RESERVED_PACKETS =
            new TypeFactory<>(new RegistryParams("ReservedPackets",
                    RESERVED_IDS));
    
    static {
        registerReservedPacket(255, P255Ping.class, P255Ping::new);
        registerReservedPacket(254, P254ProtocolSwitch.class, P254ProtocolSwitch::new);
        RESERVED_PACKETS.lock();
        
        for(Protocol protocol : Protocol.values()) {
            checkPackets(protocol, protocol.clientPackets);
            checkPackets(protocol, protocol.serverPackets);
        }
    }
    
    /**
     * Registers a reserved/universal packet.
     * 
     * @param id The ID, between {@link #MAX_NORMAL_PACKET_ID} (exclusive), and
     * {@link #MAX_PACKET_ID} (inclusive).
     * @param packetClass The class of the packet.
     */
    private static void registerReservedPacket(int id, Class<? extends Packet> packetClass,
            Supplier<Packet> supplier) {
        if(id > MAX_PACKET_ID || id <= MAX_NORMAL_PACKET_ID)
            throw new IllegalArgumentException("Invalid id for a reserved packet ("
                    + id + ") - must be in range " + (MAX_NORMAL_PACKET_ID + 1)
                    + " - " + MAX_PACKET_ID + " (inclusive).");
        // Offset of "- MAX_NORMAL_PACKET_ID - 1" to keep registry memory
        // footprint minimal.
        RESERVED_PACKETS.register(id - MAX_NORMAL_PACKET_ID - 1, packetClass, supplier);
        PACKET_IDS.put(packetClass, id);
    }
    
    private static void checkPackets(Protocol protocol, TypeFactory<Packet> registry) {
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
    
    /**
     * Checks for whether or not the specified packet is a universal, or
     * <i>protocol-independent</i>, packet.
     * 
     * <p>Protocol-independent packets may be sent using any protocol, and by
     * both clients and servers.
     * 
     * @return {@code true} if {@code packet} is protocol-independent; {@code
     * false} otherwise.
     */
    public static boolean isPacketUniversal(Packet packet) {
        return packet.getID() > MAX_NORMAL_PACKET_ID;
    }
    
    /**
     * Gets the Protocol with the specified ID, as returned by {@link
     * #getID()}.
     */
    public static Protocol getProtocol(int id) {
        return Protocol.values()[id];
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
