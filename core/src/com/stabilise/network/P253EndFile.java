package com.stabilise.network;

import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


public class P253EndFile extends Packet {
    
    public int id;
    public byte[] checksum;
    
    @Override
    public void readData(DataInStream in) throws IOException {
        id = in.readInt();
        checksum = new byte[in.readInt()];
        in.read(checksum);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(id);
        if(checksum != null) {
            out.writeInt(checksum.length);
            out.write(checksum);
        }
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        
    }
    
}
