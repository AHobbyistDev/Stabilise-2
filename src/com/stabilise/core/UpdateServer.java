package com.stabilise.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.stabilise.network.Server;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.update.IServerUpdate;


public class UpdateServer extends Server implements IServerUpdate {
    
    public UpdateServer() {
        super(5, Protocol.UPDATE);
    }
    
    @Override
    protected ServerSocket createSocket() throws IOException {
        return new ServerSocket(Constants.SERVER_PORT, 8, InetAddress.getLocalHost());
    }
    
}
