package com.stabilise.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.stabilise.network.Client;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.update.IClientUpdate;


public class Updater extends Client implements IClientUpdate {
    
    private static final InetAddress LAN;
    
    static {
        try {
            LAN = InetAddress.getLocalHost();
        } catch(UnknownHostException e) {
            throw new Error(e);
        }
    }
    
    public Updater(InetAddress address, int port, Protocol initialProtocol) {
        super(LAN, Constants.SERVER_PORT, Protocol.UPDATE);
    }
    
}
