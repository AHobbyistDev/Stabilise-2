package com.stabilise.world.provider;

import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.world.old.MultiplayerClientWorld;

@Incomplete
public class ClientProvider extends WorldProvider<MultiplayerClientWorld> {
	
	public ClientProvider(Profiler profiler) {
		super(profiler);
	}
	
	@Override
	public MultiplayerClientWorld loadDimension(String name) {
		return null;
	}
	
	@Override
	public long getSeed() {
		return 0L;
	}
	
	@Override
	public void save() {
		// nothing to save for a client world
	}
	
	@Override
	protected void closeExtra() {
		
	}
	
}
