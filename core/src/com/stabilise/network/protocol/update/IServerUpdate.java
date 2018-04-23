package com.stabilise.network.protocol.update;

import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;


public interface IServerUpdate extends PacketHandler {
    
    void handleChecksums(TCPConnection con, C000Checksums p);
    
}
