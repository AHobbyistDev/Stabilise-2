package com.stabilise.core;

import java.net.InetAddress;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.stabilise.character.CharacterData;
import com.stabilise.network.Client;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.handshake.C000VersionInfo;
import com.stabilise.network.protocol.handshake.IClientHandshake;
import com.stabilise.network.protocol.handshake.S000VersionInfo;
import com.stabilise.network.protocol.login.IClientLogin;
import com.stabilise.network.protocol.login.S000LoginRejected;
import com.stabilise.util.concurrent.TrackableFuture;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.World;
import com.stabilise.world.World.WorldBundle;
import com.stabilise.world.provider.ClientProvider;


public class GameClient extends Client implements IClientHandshake, IClientLogin {
	
	private boolean firstTimeConnecting = true;
	
	/** True if our game version matches up with the server's, and we can join. */
	private boolean weCanLogin = false;
	/** If {@link #weCanLogin} is false, true indicates we are outdated, and
	 * false indicates the server is outdated. */
	private boolean weAreOutdated = false;
	
	private ClientProvider provider;
	private ClientWorld world;
	private CharacterData player;
	
	private TrackableFuture<WorldBundle> loader;
	
	public GameClient(InetAddress address, int port) {
		super(address, port);
	}
	
	@Override
	protected void handleProtocolSwitch(TCPConnection con, Protocol protocol) {
		if(firstTimeConnecting) {
			firstTimeConnecting = false;
			// do something which i forgot here
		}
		
		switch(protocol) {
			case HANDSHAKE:
				con.sendPacket(new C000VersionInfo().setVersionInfo());
				con.setProtocol(Protocol.LOGIN);
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
	protected void doUpdate() {
		
	}
	
	public TrackableFuture<WorldBundle> login(CharacterData player) {
		if(getConnection().getProtocol() != Protocol.LOGIN)
			return null;
		return World.builder()
				.setClient(this)
				.setPlayer(this.player = player)
				.buildClient();
	}
	
	// HANDSHAKE --------------------------------------------------------------
	
	@Override
	public void handleVersionInfo(S000VersionInfo packet, TCPConnection con) {
		weCanLogin = packet.canLogin;
		weAreOutdated = packet.areWeOutdated();
		
		con.setProtocol(Protocol.LOGIN);
	}

	// LOGIN ------------------------------------------------------------------
	
	public class WorldLoadHandle {
		
	}
	
	@Override
	public void handleLoginReject(S000LoginRejected packet, TCPConnection con) {
		
	}
	
	
	
}
