package com.stabilise.network.protocol.update;

import java.io.IOException;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


public class C000Checksums extends Packet {
    
    public byte[] launcherChecksum;
    public byte[] gameChecksum;
    public byte[] gamefilesChecksum;
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        in.read(launcherChecksum  = new byte[in.readInt()]);
        in.read(gameChecksum      = new byte[in.readInt()]);
        in.read(gamefilesChecksum = new byte[in.readInt()]);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(launcherChecksum.length);
        out.write(launcherChecksum);
        out.writeInt(gameChecksum.length);
        out.write(gameChecksum);
        out.writeInt(gamefilesChecksum.length);
        out.write(gamefilesChecksum);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IServerUpdate)handler).handleChecksums(con, this);
    }
    
}
