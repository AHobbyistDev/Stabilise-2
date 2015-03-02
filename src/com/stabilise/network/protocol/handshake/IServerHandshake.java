package com.stabilise.network.protocol.handshake;

import com.stabilise.network.protocol.PacketHandler;


public interface IServerHandshake extends PacketHandler {
	
	void handleVersionInfo(C000VersionInfo packet);
	void handleDisconnect(C001Disconnect packet);
	
}
