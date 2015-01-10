package com.stabilise.world;

import java.io.File;
import java.util.Collection;

import com.stabilise.character.CharacterData;
import com.stabilise.core.Constants;
import com.stabilise.core.GameClient;
import com.stabilise.entity.EntityPlayer;
import com.stabilise.entity.GameCamera;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.Log;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world as viewed and manipulated by a client.
 * 
 * @deprecated Due to the removal of networking architecture.
 */
public class WorldClientOld extends BaseWorld {
	
	/** A reference to the client running the game. */
	public GameClient client;
	
	/** The client's camera. */
	public GameCamera camera;
	
	/** The client's player. */
	public EntityPlayer player;
	
	/** The map of loaded slices. */
	public SliceMapOld slices;
	/** The total number of slices loaded. */
	private int loadedSlices = 0;
	/** Whether or not a sufficient number of slices have been loaded for the
	 * world to be playable. */
	private boolean worldLoaded = false;
	
	
	/**
	 * Creates a new WorldClient instance.
	 * 
	 * @param client The client object to link the world to.
	 */
	public WorldClientOld(GameClient client) {
		super();
		
		this.client = client;
		
		//slices = new BufferedSliceMap(this);
		//player = new EntityPlayer(this, InputManager.get());
		
		log = Log.getAgent("CLIENT");
	}
	
	
	@Override
	public void update() {
		if(!worldLoaded) return;
		
		super.update();
		
		//player.update();
		
		slices.update();
		
		//camera.update();
	}
	
	/**
	 * Adds the player to the world.
	 * 
	 * @param id The player's entity ID.
	 * @param x The x-coordinate of the player, in tile-lengths.
	 * @param y The y-coordinate of the player, in tile-lengths.
	 */
	public void addPlayer(int id, double x, double y) {
		//----EntityPlayer p = new EntityPlayer(this, new Controller(null));
		//----p.id = id;
		//----p.name = "Player";
		//----player = p;		// TODO: temporary assigning of the player
		//----addPlayer(p, x, y);
	}
	
	/**
	 * Adds a slice to the world.
	 * 
	 * @param slice The slice to add to the world.
	 */
	public void addSlice(Slice slice) {
		slices.addSlice(slice);
		loadedSlices++;
		if(loadedSlices >= (Constants.LOADED_SLICE_RADIUS * 2 + 1) * (Constants.LOADED_SLICE_RADIUS * 2 + 1))
			worldLoaded = true;
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		return slices.getSliceAt(x, y);
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		return slices.getSliceAt(x, y);
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		Slice slice = getSliceAt(x, y);
		if(slice != null) {
			return slice.getTileAt(
					(int)Math.floor(MathsUtil.wrappedRem(x, Slice.SLICE_SIZE)),
					(int)Math.floor(MathsUtil.wrappedRem(y, Slice.SLICE_SIZE))
			);
		} else {
			// If a slice hasn't yet been loaded from a server, the client will
			// treat it as if it's filled with invisible bedrock.
			return Tiles.BEDROCK_INVISIBLE;
		}
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		// nothing to see here, move along
	}
	
	@Override
	public void blowUpTile(int x, int y, float explosionPower) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public TileEntity getTileEntityAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setTileEntityAt(int x, int y, TileEntity tileEntity) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void removeTileEntityAt(int x, int y) {
		// TODO Auto-generated method stub
	}


	@Override
	public Collection<Particle> getParticles() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void breakTileAt(int x, int y) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public File getDir() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void addPlayer(CharacterData player) {
		// TODO Auto-generated method stub
		
	}
	
}
