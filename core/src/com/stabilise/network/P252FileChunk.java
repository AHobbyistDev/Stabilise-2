package com.stabilise.network;

import java.io.IOException;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.FileSource;


public class P252FileChunk extends Packet {
    
    // Receiver-side only
    
    public byte[] data;
    
    // Sender-side only
    
    /** Used only on sender side. We read from the source directly in writeData
     * to keep all IO on the sender thread as to minimise blocking on the main
     * thread. This is synchronised on when read from in writeData(). */
    public FileSource src;
    /** Sender-side only. */
    public int bufferSize;
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data = new byte[in.readInt()];
        in.read(data);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        byte[] buf = new byte[bufferSize];
        int count;
        synchronized(src) {
            count = src.read(buf);
        }
        out.write(count);
        out.write(buf, 0, count);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        
    }
    
}
