package com.stabilise.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This packet contains pause and unpause requests from the client for
 * singleplayer modes.
 */
public class Packet253Pause extends Packet {
	
	/** True if the request is to pause; false if the request is to unpause. */
	public boolean pause;
	
	public Packet253Pause() {
		
	}
	
	public Packet253Pause(boolean pause) {
		this.pause = pause;
	}

	@Override
	public void readData(DataInputStream in) throws IOException {
		pause = in.readBoolean();
	}

	@Override
	public void writeData(DataOutputStream out) throws IOException {
		out.writeBoolean(pause);
	}

	@Override
	public int getBytes() {
		return 1;
	}

}
