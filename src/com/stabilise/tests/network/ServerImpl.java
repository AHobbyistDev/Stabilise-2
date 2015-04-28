package com.stabilise.tests.network;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.stabilise.core.Constants;
import com.stabilise.network.Server;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.IServerHandshake;
import com.stabilise.network.protocol.handshake.S000VersionInfo;

public class ServerImpl extends Server implements IServerHandshake {
	
	volatile TCPConnection client;
	int ticks = 0;
	
	public ServerImpl() {
		super(Constants.TICKS_PER_SECOND);
	}
	
	@Override
	protected ServerSocket createSocket() throws IOException {
		return new ServerSocket(DEFAULT_PORT, 4, InetAddress.getLocalHost());
	}
	
	public void doUpdate() {
		if(++ticks % 60 == 0 && client != null)
			log.postInfo("Ping to client: " + client.getPing());
	}
	
	@Override
	protected void onClientConnect(TCPConnection con) {
		client = con;
	}
	
	@Override
	protected void onClientDisconnect(TCPConnection con) {
		shutdown();
	}
	
	@Override
	public void handleVersionInfo(C000VersionInfo packet, TCPConnection con) {
		log.postInfo("Got info from client... is compatible: " + packet.isCompatible());
		con.sendPacket(new S000VersionInfo().setVersionInfo());
	}
	
}
