package com.stabilise.tests.network;

import java.net.*;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import com.stabilise.network.Client;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.IClientHandshake;
import com.stabilise.network.protocol.handshake.S000VersionInfo;

public class ClientImpl extends Client implements IClientHandshake {
	
	private TCPConnection con;
	private int ticks = 0;
	
	public ClientImpl() {
		super(getLocalHost(), DEFAULT_PORT);
	}
	
	@Override
	protected void handleProtocolSwitch(TCPConnection con, Protocol protocol) {
		this.con = con;
		con.sendPacket(new C000VersionInfo().setVersionInfo());
	}
	
	@Override
	public void doUpdate() {
		if(ticks++ % 60 == 0)
			log.postInfo("Ping: " + con.getPing());
		if(ticks % 600 == 0) {
			//con.sendPacket(new C001Disconnect());
			disconnect();
		}
	}
	
	@Override
	public void handleVersionInfo(S000VersionInfo packet) {
		log.postInfo("Got info from server... is compatible: " + packet.isCompatible());
	}
	
	private static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
}
