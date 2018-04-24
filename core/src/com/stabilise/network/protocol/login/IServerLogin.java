package com.stabilise.network.protocol.login;

import com.stabilise.network.TCPConnection;

public interface IServerLogin {
    
    void handleLogin(C000Login packet, TCPConnection con);
    
}
