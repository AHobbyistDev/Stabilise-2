package com.stabilise.network;

import static com.stabilise.util.collect.DuplicatePolicy.THROW_EXCEPTION;

import com.stabilise.network.packet.Packet;
import com.stabilise.util.collect.InstantiationRegistry;


public enum ConnectionState {
	
	;
	
	/** Registry of packets sent by the client to the server. */
	private final InstantiationRegistry<Packet> serverboundPackets =
			new InstantiationRegistry<Packet>(256, THROW_EXCEPTION, Packet.class);
	/** Registry of packets sent by the server to the client. */
	private final InstantiationRegistry<Packet> clientboundPackets =
			new InstantiationRegistry<Packet>(256, THROW_EXCEPTION, Packet.class);
	
}
