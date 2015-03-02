package com.stabilise.network.protocol.handshake;

import com.stabilise.network.protocol.PacketHandler;


public interface IClientHandshake extends PacketHandler {
	
	void handleVersionInfo(S000VersionInfo info);
	
}
