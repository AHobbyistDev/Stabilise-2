package com.stabilise.tests.network;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.stabilise.core.Constants;
import com.stabilise.network.ServerBase;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.C001Disconnect;
import com.stabilise.network.protocol.handshake.IServerHandshake;
import com.stabilise.network.protocol.handshake.S000VersionInfo;

public class Server extends ServerBase implements IServerHandshake {
	
	public Server() {
		super(Constants.TICKS_PER_SECOND);
	}
	
	@Override
	protected ServerSocket createSocket() throws IOException {
		return new ServerSocket(DEFAULT_PORT, 4, InetAddress.getLocalHost());
	}
	
	@Override
	public void update() {
		if(checkShutdown())
			return;
		handleIncomingPackets();
	}
	
	@Override
	public void handleVersionInfo(C000VersionInfo packet, TCPConnection con) {
		log.postInfo("Got info from client!");
		con.sendPacket(new S000VersionInfo().setVersionInfo());
	}

	@Override
	public void handleDisconnect(C001Disconnect packet, TCPConnection con) {
		log.postInfo("Got disconnect request!");
		shutdown();
	}
	
}
