package com.stabilise.network.protocol.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


public class S000FileList extends Packet {
    
    public final List<String> files = new ArrayList<>();
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int count = in.readInt();
        for(int i = 0; i < count; i++) {
            String localPath = in.readUTF();
            files.add(localPath);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(files.size());
        for(String path : files) {
            out.writeUTF(path);
        }
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IClientUpdate)handler).handleFileList(con, this);
    }
    
}
