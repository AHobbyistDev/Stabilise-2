package com.stabilise.core;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Objects;

import com.stabilise.network.Server;
import com.stabilise.network.ServerTCPConnection;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.IServerHandshake;
import com.stabilise.network.protocol.handshake.S000VersionInfo;
import com.stabilise.network.protocol.login.C000Login;
import com.stabilise.network.protocol.login.IServerLogin;
import com.stabilise.world.multiverse.HostMultiverse;

public class GameServer extends Server implements IServerHandshake, IServerLogin {
    
    public final HostMultiverse provider;
    private final int maxPlayers;
    
    
    /**
     * Creates a new game server.
     * 
     * @param world The world for the server to host.
     * @param maxPlayers The maximum number of players who can connect to the
     * server.
     * 
     * @throws NullPointerException if {@code world} is {@code null}.
     * @throws IllegalArgumentException if {@code maxPlayers < 1}.
     */
    public GameServer(HostMultiverse world, int maxPlayers) {
        super(Constants.TICKS_PER_SECOND, (s) -> {
            return new ServerTCPConnection(s);
        });
        
        if(maxPlayers < 1)
            throw new IllegalArgumentException("Invalid max number of players "
                    + maxPlayers);
        
        this.provider = Objects.requireNonNull(world);
        this.maxPlayers = maxPlayers;
    }
    
    @Override
    protected ServerSocket createSocket() throws IOException {
        return new ServerSocket(DEFAULT_PORT, maxPlayers, InetAddress.getLocalHost());
    }
    
    /**
     * Handles a switch with a client to a new protocol.
     */
    private void handleProtocolSwitch(ServerTCPConnection con, Protocol protocol) {
        switch(protocol) {
            case HANDSHAKE:
                // Client sends first so it has a harder time providing valid
                // falsified information.
                break;
            case LOGIN:
                break;
            case GAME:
                break;
            default:
                throw new IllegalArgumentException("Unrecognised protocol");
        }
    }
    
    @Override
    public void doUpdate() {
        provider.update();
    }
    
    @Override
    protected void onClientConnect(TCPConnection con) {
        con.setProtocolSyncListener((c,p) -> {
            handleProtocolSwitch((ServerTCPConnection)c, p);
        });
    }
    
    // HANDSHAKE --------------------------------------------------------------
    
    @Override
    public void handleVersionInfo(C000VersionInfo packet, TCPConnection con) {
        boolean compatible = packet.isCompatible();
        ((ServerTCPConnection)con).canLogIn = compatible;
        con.sendPacket(new S000VersionInfo(compatible).setVersionInfo());
        
        con.setProtocol(Protocol.LOGIN);
    }

    // LOGIN ------------------------------------------------------------------
    
    @Override
    public void handleLogin(C000Login packet, TCPConnection con) {
        
    }
    
}
