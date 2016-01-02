package com.stabilise.network.protocol.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.IOUtil;


public class S001FileTransfer extends Packet {
    
    /** 
     * On sender side: files to send. .zip files should be followed by an
     * extraction destination.
     * On receiver side: received .zip files and their extraction target
     * directories (so client code should walk the entries in pairs of two). */
    public final List<String> files = new ArrayList<>();
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int count = in.readInt();
        boolean prevZip = false;
        for(int i = 0; i < count; i++) {
            String path = in.readUTF();
            FileHandle f = Resources.DIR_APP.child(path);
            if(prevZip) {
                files.add(path);
                prevZip = false;
            } else {
                IOUtil.receiveFile(in, f);
                if(path.endsWith(".zip"))
                    prevZip = true;
            }
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(files.size());
        
        boolean prevZip = false;
        for(int i = 0; i < files.size(); i++) {
            String path = files.get(i);
            FileHandle f = Resources.DIR_UPDATE_SERVER.child(path);
            out.writeUTF(path);
            if(prevZip) {
                prevZip = false;
                continue;
            } else if(!f.exists() || f.isDirectory())
                continue;
            else {
                IOUtil.sendFile(f, out, true);
                if(path.endsWith(".zip"))
                    prevZip = true;
            }
        }
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IClientUpdate)handler).handleFileTransfer(con, this);
    }
    
}
