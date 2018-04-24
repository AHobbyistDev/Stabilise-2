package com.stabilise.network.protocol.handshake;

import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;


public interface IClientHandshake extends PacketHandler {
    
    void handleVersionInfo(S000VersionInfo packet, TCPConnection con);
    
}
