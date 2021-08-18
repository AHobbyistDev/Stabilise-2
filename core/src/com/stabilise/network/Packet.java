package com.stabilise.network;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.Sendable;

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
        @Override public void readData(DataInStream in) {}
        @Override public void writeData(DataOutStream out) {}
        @Override public void handle(PacketHandler handler, TCPConnection con) {}
    };
    
    /**
     * Implement this interface to indicate that a packet of a particular class
     * is 'important' - when queued for sending, important packets are added to
     * the head of the packet queue rather than the tail.
     * 
     * <p>This is package-private so that only protocol-independent packets can
     * be labelled important.
     */
    interface Important {}
    
    
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
     * Gets this packet's ID. {@code -1} is returned if this packet is of a
     * type which has not been assigned to any protocol.
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
    
    /**
     * Checks for whether this packet is a universal, or
     * <i>protocol-independent</i>, packet.
     * 
     * <p>Protocol-independent packets may be sent using any protocol, and by
     * both clients and servers.
     * 
     * @return {@code true} if {@code packet} is protocol-independent; {@code
     * false} otherwise.
     */
    public final boolean isUniversal() {
        return Protocol.isPacketUniversal(this);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
}