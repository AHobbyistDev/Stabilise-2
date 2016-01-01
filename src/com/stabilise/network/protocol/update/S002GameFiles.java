package com.stabilise.network.protocol.update;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.Log;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.IOUtil;


public class S002GameFiles extends Packet {
    
    private static final FileHandle ZIP = Resources.DIR_GAMEDATA.child("gamefiles.zip");
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        IOUtil.receiveFile(in, ZIP);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        IOUtil.sendFile(Resources.US_GAMEFILES, out, true);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        try {
            IOUtil.unzip(ZIP, Resources.DIR_APP);
        } catch(IOException e) {
            Log.get().postSevere("Couldn't unzip gamefiles!", e);
        }
    }
    
}
