package com.stabilise.network.protocol.update;

import java.io.IOException;

import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


public class S003Confirmation extends Packet {
    
    public boolean launcherNeedsUpdate;
    public boolean gameNeedsUpdate;
    public boolean gameFilesNeedUpdate;
    
    @Override
    public void readData(DataInStream in) throws IOException {
        launcherNeedsUpdate = in.readBoolean();
        gameNeedsUpdate = in.readBoolean();
        gameFilesNeedUpdate = in.readBoolean();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeBoolean(launcherNeedsUpdate);
        out.writeBoolean(gameNeedsUpdate);
        out.writeBoolean(gameFilesNeedUpdate);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IClientUpdate)handler).handleConfirmation(con, this);
    }
    
}
