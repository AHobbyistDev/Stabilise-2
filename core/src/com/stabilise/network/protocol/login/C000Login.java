package com.stabilise.network.protocol.login;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.character.CharacterData;
import com.stabilise.network.Packet;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

public class C000Login extends Packet {
    
    public CharacterData player;
    
    public C000Login() {}
    
    /**
     * @throws NullPointerException if {@code player} is {@code null}.
     */
    public C000Login(CharacterData player) {
        this.player = Objects.requireNonNull(player);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        player = new CharacterData();
        player.readData(in);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        player.writeData(out);
    }
    
    @Override
    public void handle(PacketHandler handler, TCPConnection con) {
        ((IServerLogin)handler).handleLogin(this, con);
    }
    
}
