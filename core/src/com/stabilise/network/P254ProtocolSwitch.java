package com.stabilise.network;

import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

public class P254ProtocolSwitch extends Packet {
    
    public Protocol protocol;
    
    public P254ProtocolSwitch() {}
    
    
    /**
     * @throws NullPointerException if newProtocol is null.
     */
    public P254ProtocolSwitch(Protocol newProtocol) {
        protocol = newProtocol;
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        protocol = Protocol.getProtocol(in.readByte());
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(protocol.getID());
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        con.handlePeerProtocolSwitch(protocol);
    }
    
}
