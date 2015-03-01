package com.stabilise.network.protocol.handshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.stabilise.core.Constants;
import com.stabilise.network.Packet;

/**
 * Sent by a client to a server upon establishing a connection to inform the
 * server of this client's version info.
 */
public class C000VersionInfo extends Packet {
	
	/** @see com.stabilise.core.Constants */
	public int release, patchMajor, patchMinor, build;
	
	public C000VersionInfo() {}
	
	@Override
	public void readData(DataInputStream in) throws IOException {
		release = in.readInt();
		patchMajor = in.readInt();
		patchMinor = in.readInt();
		build = in.readInt();
	}
	
	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeInt(release);
		out.writeInt(patchMajor);
		out.writeInt(patchMinor);
		out.writeInt(build);
	}
	
	/**
	 * Sets this Packet's version info to that located in {@link Constants}.
	 */
	public C000VersionInfo setVersionInfo() {
		release = Constants.RELEASE;
		patchMajor = Constants.PATCH_MAJOR;
		patchMinor = Constants.PATCH_MINOR;
		build = Constants.BUILD;
		return this;
	}
	
}
