package com.stabilise.network.protocol.update;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


public class C000Checksums extends Packet {
    
    public Map<String, byte[]> checksums = new HashMap<>();
    
    
    /**
     * Adds a file checksum for sending.
     * 
     * @param path The relativised path of the file.
     * @param checksum That file's checksum. 
     */
    public void add(String path, byte[] checksum) {
        checksums.put(path, checksum);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int len = in.readInt();
        for(int i = 0; i < len; i++) {
            String path = in.readUTF();
            int count = in.readInt();
            byte[] checksum = new byte[count];
            in.read(checksum);
            checksums.put(path, checksum);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(checksums.size());
        for(Map.Entry<String, byte[]> entry : checksums.entrySet()) {
            out.writeUTF(entry.getKey());
            byte[] checksum = entry.getValue();
            out.writeInt(checksum.length);
            out.write(checksum);
        }
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IServerUpdate)handler).handleChecksums(con, this);
    }
    
}
