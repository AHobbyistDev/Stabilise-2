package com.stabilise.network.protocol.handshake;

import static com.stabilise.core.Constants.*;

import java.io.IOException;

import com.stabilise.core.Constants;
import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.Version;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * Sent by a client to a server upon establishing a connection to inform the
 * server of this client's version info.
 */
public class C000VersionInfo extends Packet {
    
    public Version senderVersion, senderBackwardsVersion;
    
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IServerHandshake)handler).handleVersionInfo(this, con);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        senderVersion = new Version(in.readInt(), in.readInt(), in.readInt());
        senderBackwardsVersion = new Version(in.readInt(), in.readInt(), in.readInt());
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(senderVersion.major());
        out.writeInt(senderVersion.minor());
        out.writeInt(senderVersion.patch());
        out.writeInt(senderBackwardsVersion.major());
        out.writeInt(senderBackwardsVersion.minor());
        out.writeInt(senderBackwardsVersion.patch());
    }
    
    /**
     * Sets this Packet's version info to that located in {@link Constants}.
     */
    public C000VersionInfo setVersionInfo() {
        senderVersion = VERSION;
        senderBackwardsVersion = BACKWARDS_VERSION;
        return this;
    }
    
    /**
     * Returns {@code true} if our current game version is outdated compared to
     * our peer.
     */
    public boolean areWeOutdated() {
        return VERSION.precedes(senderBackwardsVersion);
    }
    
    /**
     * Returns {@code true} if our peer's game version is outdated compared to
     * us.
     */
    public boolean isPeerOutdated() {
        return senderVersion.precedes(BACKWARDS_VERSION);
    }
    
    /**
     * Returns {@code true} if the current game version is compatible with the
     * sender's game version.
     */
    public boolean isCompatible() {
        return !areWeOutdated() && !isPeerOutdated();
    }
    
}
