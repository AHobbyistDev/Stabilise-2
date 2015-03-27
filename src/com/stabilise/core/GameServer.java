package com.stabilise.core;

import static com.stabilise.core.Constants.DEFAULT_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Objects;

import com.stabilise.network.ServerBase;
import com.stabilise.world.provider.HostProvider;

public class GameServer extends ServerBase {
	
	public final HostProvider world;
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
	public GameServer(HostProvider world, int maxPlayers) {
		super(Constants.TICKS_PER_SECOND);
		
		if(maxPlayers < 1)
			throw new IllegalArgumentException("Invalid max number of players " + maxPlayers);
		
		this.world = Objects.requireNonNull(world);
		this.maxPlayers = maxPlayers;
	}
	
	@Override
	protected ServerSocket createSocket() throws IOException {
		return new ServerSocket(DEFAULT_PORT, maxPlayers, InetAddress.getLocalHost());
	}
	
	@Override
	public void update() {
		if(checkShutdown())
			return;
		handleIncomingPackets();
	}

}
