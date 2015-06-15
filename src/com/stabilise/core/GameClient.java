package com.stabilise.core;

import java.net.InetAddress;

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
import com.stabilise.world.Worlds;
import com.stabilise.world.Worlds.WorldBundle;
import com.stabilise.world.multiverse.ClientMultiverse;


public class GameClient extends Client implements IClientHandshake, IClientLogin {
	
	private boolean firstTimeConnecting = true;
	
	/** True if our game version matches up with the server's, and we can join. */
	private boolean weCanLogin = false;
	/** If {@link #weCanLogin} is false, true indicates we are outdated, and
	 * false indicates the server is outdated. */
	private boolean weAreOutdated = false;
	
	private ClientMultiverse provider;
	private ClientWorld world;
	private CharacterData player;
	
	private TrackableFuture<WorldBundle> loader;
	private WorldLoadHandle loadHandle;
	
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
		if(loader != null) {
			
		} else if(provider != null) {
			provider.update();
		}
	}
	
	/**
	 * Joins the server.
	 * 
	 * @param player The player to play as.
	 * 
	 * @return The TrackableFuture with which to track the login process, or
	 * {@code null} if: we're not connected to the server, we're not currently
	 * in the login protocol, or we're currently loading the world from the
	 * server.
	 */
	public TrackableFuture<WorldBundle> login(CharacterData player) {
		if(loader != null || getConnection() == null
				|| getConnection().getProtocol() != Protocol.LOGIN)
			return null;
		return loader = Worlds.builder()
				.setClient(this, new WorldLoadHandle())
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
	
	public static class WorldLoadHandle {
		public volatile boolean rejected = false;
		public volatile String rejectReason = null;
		
		private WorldLoadHandle() {}
	}
	
	@Override
	public void handleLoginReject(S000LoginRejected packet, TCPConnection con) {
		// Set rejectReason before rejected to avoid race conditions.
		loadHandle.rejectReason = packet.reason;
		loadHandle.rejected = true;
	}
	
}
