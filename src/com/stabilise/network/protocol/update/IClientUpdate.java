package com.stabilise.network.protocol.update;

import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;


public interface IClientUpdate extends PacketHandler {
    
    void handleConfirmation(TCPConnection con, S003Confirmation p);
    void handleLauncherJar(TCPConnection con, S000LauncherJar p);
    void handleGameJar(TCPConnection con, S001GameJar p);
    void handleGameFiles(TCPConnection con, S002GameFiles p);
    
}
