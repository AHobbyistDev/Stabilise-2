package com.stabilise.network;

import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


public class P251BeginFile extends Packet {
    
    /** Name of the file, for peer identification. */
    public String fileName;
    /** Internal id of the file transfer. */
    public int id;
    /** Whether or not the file checksum is also being sent. */
    public boolean checksum;
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        fileName = in.readUTF();
        id = in.readInt();
        checksum = in.readBoolean();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeUTF(fileName);
        out.writeInt(id);
        out.writeBoolean(checksum);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        
    }
    
}
