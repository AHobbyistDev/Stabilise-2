package com.stabilise.network.protocol.login;

import java.util.Objects;

import com.stabilise.network.ReflectivePacket;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;

public class S000LoginRejected extends ReflectivePacket {
	
	public String reason;
	
	public S000LoginRejected() {}
	
	public S000LoginRejected(String reason) {
		this.reason = Objects.requireNonNull(reason);
	}
	
	@Override
	public void handle(PacketHandler handler, TCPConnection con) {
		((IClientLogin)handler).handleLoginReject(this, con);
	}
	
}
