package com.stabilise.network.protocol.update;

import java.io.IOException;

import com.stabilise.core.Resources;
import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.IOUtil;


public class S000LauncherJar extends Packet {
    
    @Override
    public void readData(DataInStream in) throws IOException {
        IOUtil.receiveFile(in, Resources.LAUNCHER_JAR);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        IOUtil.sendFile(Resources.US_LAUNCHER_JAR, out, true);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        
    }
    
}
