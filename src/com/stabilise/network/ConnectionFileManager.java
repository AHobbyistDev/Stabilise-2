package com.stabilise.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.utils.IntMap;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.io.FileSource;

@Incomplete
public class ConnectionFileManager {
    
    /** Temporary maximum number of bytes to send per packet. */
    private static final int PACKET_BUF_BYTES = 8*1024; // 8kB
    
    /** Temporary cap on the maximum number of bytes to send per second until
     * I implement a congestion-avoidance algorithm. */ 
    private static final int MAX_BYTES_PER_SEC = 1024*1024; // 1MB
    /** Temporary packet rate-limiter until I implement a congestion-avoidance
     * algorithm. */
    private static final int MAX_PACKETS_PER_SEC = MAX_BYTES_PER_SEC / PACKET_BUF_BYTES;
    
    
    private final TCPConnection con;
    private final List<FileSendOp> send = new ArrayList<>();
    private final IntMap<FileRecvOp> recv = new IntMap<>(1);
    
    private int nextSendID = 0;
    
    // Temp rate-limiting variables
    private long lastTime = 0L;
    private int packetsThisSec = 0;
    
    
    ConnectionFileManager(TCPConnection con) {
        this.con = con;
    }
    
    void update() {
        long curTime = System.currentTimeMillis();
        if(curTime - lastTime > 1000L) { // 1 second
            lastTime = curTime;
            packetsThisSec = 0;
        }
        
        for(Iterator<FileSendOp> itr = send.iterator(); itr.hasNext();) {
            FileSendOp op = itr.next();
            if(!op.begun) {
                P251BeginFile p = new P251BeginFile();
                p.id = op.id;
                p.fileName = op.name;
                p.checksum = op.checksum;
                con.sendPacket(p);
            }
            
            // We poll op.available() and split off as many packets as that
            // estimate suggests. We do this in preference to reading from the
            // file source on this thread (i.e. no I/O on the main thread!!).
            int available = op.availableBytes(con.log);
            while(available > 0 && ++packetsThisSec <= MAX_PACKETS_PER_SEC) {
                P252FileChunk p = new P252FileChunk();
                p.bufferSize = PACKET_BUF_BYTES;
                p.src = op.src;
                con.sendPacket(p);
                
                available -= PACKET_BUF_BYTES;
            }
            
            if(op.isFullySent()) {
                P253EndFile p = new P253EndFile();
                p.id = op.id;
                con.sendPacket(p);
                
                op.closeSrc();
                itr.remove();
            }
            
            if(packetsThisSec == MAX_PACKETS_PER_SEC)
                break;
        }
    }
    
    @ThreadUnsafeMethod
    public void sendFile(String name, FileSource src, boolean checksum) {
        send.add(new FileSendOp(nextSendID++, name, src, checksum));
    }
    
    /**
     * Closes all running files-in-transfer.
     */
    public void close() {
        for(FileSendOp op : send) {
            op.closeSrc();
        }
    }
    
    private static class FileSendOp {
        
        final int id;
        final String name;
        final FileSource src;
        final boolean checksum;
        boolean begun = false;
        
        public FileSendOp(int id, String name, FileSource src, boolean checksum) {
            this.id = id;
            this.name = Objects.requireNonNull(name);
            this.src = Objects.requireNonNull(src);
            this.checksum = checksum;
        }
        
        /**
         * Polls {@link FileSource#available() src.available()}.
         */
        public int availableBytes(Log log) {
            try {
                synchronized(src) {
                    return src.available();
                }
            } catch(IOException e) {
                log.postWarning("Could not poll FileSource's available bytes (" + src + ")", e);
                return 0;
            }
        }
        
        /**
         * Polls {@link FileSource#hasRemainingBytes() !src.hasRemainingBytes()}.
         */
        public boolean isFullySent() {
            synchronized(src) {
                return !src.hasRemainingBytes();
            }
        }
        
        /**
         * Closes src and silently ignores exceptions.
         */
        public void closeSrc() {
            try {
                src.close();
            } catch(IOException ignored) {} // what could we even do in response?
        }
        
    }
    
    private static class FileRecvOp {
        
        
        
    }
    
}
