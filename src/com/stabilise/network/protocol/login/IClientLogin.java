package com.stabilise.network.protocol.login;

import com.stabilise.network.TCPConnection;

public interface IClientLogin {
	
	void handleLoginReject(S000LoginRejected packet, TCPConnection con);
	
}
