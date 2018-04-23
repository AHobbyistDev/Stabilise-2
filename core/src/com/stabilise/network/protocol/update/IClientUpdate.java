package com.stabilise.network.protocol.update;

import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;


public interface IClientUpdate extends PacketHandler {
    
    void handleFileList(TCPConnection con, S000FileList p);
    void handleFileTransfer(TCPConnection con, S001FileTransfer p);
    
}
