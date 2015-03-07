package com.stabilise.network.protocol.handshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;

/**
 * Sent by a server to a client upon receiving a {@link C000VersionInfo}
 * packet, to inform the client of the server's game version. Also includes
 * a flag {@link #canLogin}, which is true if the client can play on the server
 * and false if not. A value of {@code false} either means the client or server
 * is outdated.
 */
public class S000VersionInfo extends C000VersionInfo {
	
	/** {@code true} if the client can proceed to log in to the server. */
	public boolean canLogin;
	
	public S000VersionInfo() {}
	
	public S000VersionInfo(boolean canLogin) {
		this.canLogin = canLogin;
	}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		super.readData(in);
		canLogin = in.readBoolean();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		super.writeData(out);
		out.writeBoolean(canLogin);
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		((IClientHandshake)handler).handleVersionInfo(this);
	}
	
}
