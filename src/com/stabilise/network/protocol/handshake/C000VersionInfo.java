package com.stabilise.network.protocol.handshake;

import static com.stabilise.core.Constants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.core.Constants;
import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;

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
	public void readData(DataInputStream in) throws IOException {
		senderVersion = new Version(in.readInt(), in.readInt(), in.readInt());
		senderBackwardsVersion = new Version(in.readInt(), in.readInt(), in.readInt());
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeInt(senderVersion.release);
		out.writeInt(senderVersion.patchMajor);
		out.writeInt(senderVersion.patchMinor);
		out.writeInt(senderBackwardsVersion.release);
		out.writeInt(senderBackwardsVersion.patchMajor);
		out.writeInt(senderBackwardsVersion.patchMinor);
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
	 * Returns {@code true} if the current game version is compatible with the
	 * sender's game version.
	 */
	public boolean isCompatible() {
		// true if our version is newer than the sender's oldest allowable version
		boolean weAreCompatible = !VERSION.precedes(senderBackwardsVersion);
		// true if the sender's version is newer than our oldest allowable version
		boolean senderIsCompatible = !senderVersion.precedes(BACKWARDS_VERSION);
		
		return weAreCompatible && senderIsCompatible;
	}
	
}
