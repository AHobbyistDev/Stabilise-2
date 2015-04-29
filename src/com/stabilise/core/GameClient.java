package com.stabilise.core;

import java.net.InetAddress;

import com.stabilise.character.CharacterData;
import com.stabilise.network.Client;
import com.stabilise.network.protocol.handshake.IClientHandshake;
import com.stabilise.network.protocol.handshake.S000VersionInfo;
import com.stabilise.network.protocol.login.IClientLogin;
import com.stabilise.util.concurrent.TrackableFuture;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.World.WorldBundle;
import com.stabilise.world.provider.ClientProvider;


public class GameClient extends Client implements IClientHandshake, IClientLogin {
	
	private boolean firstTimeConnecting = true;
	
	private ClientProvider provider;
	private ClientWorld world;
	private CharacterData player;
	
	private TrackableFuture<WorldBundle> loader;
	
	public GameClient(InetAddress address, int port) {
		super(address, port);
	}
	
	@Override
	protected void onConnect() {
		if(firstTimeConnecting) {
			firstTimeConnecting = false;
			
			
		}
	}
	
	@Override
	protected void doUpdate() {
		
	}
	
	public void login(CharacterData player) {
		this.player = player;
	}
	
	@Override
	public void handleVersionInfo(S000VersionInfo packet) {
		
	}
	
}
