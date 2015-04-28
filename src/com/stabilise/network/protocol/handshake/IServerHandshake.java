package com.stabilise.network.protocol.handshake;

import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;


public interface IServerHandshake extends PacketHandler {
	
	void handleVersionInfo(C000VersionInfo packet, TCPConnection con);
	
}
