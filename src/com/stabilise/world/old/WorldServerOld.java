package com.stabilise.world.old;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Slice.SLICE_SIZE;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.old.GameServerOld;
import com.stabilise.entity.Entity;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.Log;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.BaseWorld;
import com.stabilise.world.IWorld;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world as viewed and manipulated by a server.
 * 
 * @deprecated Due to the removal of networking architecture.
 */
public class WorldServerOld extends BaseWorld {
	
	/** The game server to which the world belongs. */
	public GameServerOld server;
	
	/** The world's information. */
	public WorldInfo info;
	
	/** The world's directory. */
	public File worldDir;
	
	/** The world generator. */
	public WorldGenerator generator;
	
	/** The map of all loaded regions. */
	public HashMap<Integer, Region> regions = new HashMap<Integer, Region>();
	
	
	/**
	 * Creates a new WorldServer instance.
	 * 
	 * @param info The WorldInfo object to base the world on.
	 */
	public WorldServerOld(GameServerOld server, WorldInfo info) {
		super(null, null);
		this.server = server;
		this.info = info;
		//generator = WorldGenerator.getGenerator(this, info.seed);
	}
	
	@Override
	public void update() {
		if(!server.paused) {
			super.update();
			info.age++;
		}
		
		Iterator<Integer> i = regions.keySet().iterator();
		
		while(i.hasNext()) {
			Region r = regions.get(i.next());
			if(r.unload)
				// TODO: r.unload();
				i.remove();
			else
				;//r.update();			// TODO: Still update for unloading purposes even when the game is paused, but do not perform tile updates
		}
	}
	
	/**
	 * Adds a player to the world.
	 * 
	 * @param playerName The name of the player.
	 * @param x The x-coordinate of the player, in tile-lengths.
	 * @param y The y-coordinate of the player, in tile-lengths.
	 * 
	 * @return Returns the ID of the player entity.
	 */
	public int addPlayer(String playerName, double x, double y) {
		entityCount++;
		//TODO: addPlayer(new EntityPlayer(world, null));
		return entityCount;
	}
	
	/**
	 * Checks for whether or not a region is loaded by the server.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return Whether or not the region is loaded in memory.
	 */
	public boolean hasRegion(int x, int y) {
		return regions.containsKey(Region.getKey(x, y));
	}
	
	/**
	 * Returns a region at the given coordinates.
	 * If the region is not currently loaded into memory, this method attempts
	 * to load the region. 
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * @param generate Whether or not the region should be generated, if it was
	 * not already.
	 * 
	 * @return The region at the given coordinates.
	 */
	public Region getRegionAt(int x, int y, boolean generate) {
		//x = (int)Math.floor((float)x / REGION_SIZE);
		//y = (int)Math.floor((float)y / REGION_SIZE);
		int key = 0;//Region.getKey(x, y);
		if(regions.containsKey(key)) {
			return regions.get(key);
		} else {
			Region region = null;// = new Region(this, x, y);
			/*----
			if(region.fileExists()) {
				try {
					// TODO: This could potentially cause a delay for server
					// processing
					region.load();
				} catch (IOException e) {
					log.logCritical("Could not load region!", e);
				}
			}
			*/
			if(generate) {
				//if(!region.generated)			// No need - checked within generator.generate()
				generator.generate(region);
			}
			regions.put(key, region);
			return region;
		}
	}
	
	/**
	 * Marks a slice as loaded and returns it.
	 * 
	 * @param owner The ID of the client causing the load of the slice.
	 * @param x The x-coordinate of the slice, in slice lengths.
	 * @param y The y-coordinate of the slice, in slice lengths.
	 * 
	 * @return The loaded slice, or null if the region has not yet been
	 * generated, in which case the world will delegate the sending of the
	 * slice to the client once generation is complete.
	 */
	public Slice loadSlice(int owner, int x, int y) {
		Region region = getRegionAt((int)Math.floor((float)x / REGION_SIZE), (int)Math.floor((float)y / REGION_SIZE), true);
		//region.anchorSlice(x, y);
		region.anchorSlice();
		if(region.generated) {
			return region.getSliceAt(Maths.wrappedRem(x, REGION_SIZE), Maths.wrappedRem(y, REGION_SIZE));
		} else {
			//----region.queueSlice(owner, MathUtil.calcWrappedRemainder(x, REGION_SIZE), MathUtil.calcWrappedRemainder(y, REGION_SIZE));
			//if(!region.generating) generator.generate(region);
			return null;
		}
	}
	
	/**
	 * Marks a slice as having been unloaded.
	 * 
	 * @param x The x-coordinate of the slice, in slice lengths.
	 * @param y The y-coordinate of the slice, in slice lengths.
	 */
	public void unloadSlice(int x, int y) {
		getRegionAt((int)Math.floor((float)x / REGION_SIZE), (int)Math.floor((float)y / REGION_SIZE), false).deAnchorSlice();
	}
	
	@Override
	public Slice getSliceAtTile(int x, int y) {
		return getSliceAt((int)Math.floor((x / SLICE_SIZE)), (int)Math.floor((y / SLICE_SIZE)));
	}
	
	@Override
	public Slice getSliceAt(int x, int y) {
		return getRegionAt((int)Math.floor((float)x / REGION_SIZE), (int)Math.floor((float)y / REGION_SIZE), false).getSliceAt(
				Maths.wrappedRem(x, REGION_SIZE),
				Maths.wrappedRem(y, REGION_SIZE));
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		return getSliceAt(x, y).getTileAt(
				(int)Math.floor(Maths.wrappedRem(x, SLICE_SIZE)),
				(int)Math.floor(Maths.wrappedRem(y, SLICE_SIZE))
		);
	}
	
	@Override
	public void setTileAt(int x, int y, int id) {
		// nothing to see here, move along
	}
	
	/**
	 * Gets the world's directory.
	 * 
	 * @return The File representing the world's directory.
	 */
	public FileHandle getDir() {
		return IWorld.getWorldDir(info.fileSystemName); //new File(Resources.WORLDS_DIR, IO.getLegalString(info.nameOnDisk) + "/");
	}
	
	/**
	 * Saves the world.
	 */
	public void save() {
		log.postInfo("Saving world...");
		
		try {
			// TODO: Save regions too, not just the info.
			info.save();
		} catch (IOException e) {
			log.postSevere("Could not save world info!");
		}
	}
	
	/**
	 * Closes the world.
	 */
	public void close() {
		generator.shutdown();
		
		save();
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Loads a WorldServer instance.
	 * 
	 * @param server The server to which the world belongs.
	 * @param worldName The name of the world on disk.
	 * 
	 * @return The WorldServer instance, or null if the world could not be
	 * loaded.
	 */
	public static WorldServerOld loadWorld(GameServerOld server, String worldName) {
		WorldInfo info = WorldInfo.loadInfo(worldName);
		
		if(info != null)
			return new WorldServerOld(server, info);
		
		Log.get().postSevere("Could not load info file of world \"" + worldName + "\" during world loading!");
		return null;
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
	public void addParticle(Particle p) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isLoaded() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void prepare() {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendToDimension(String dimension, Entity e, double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean updateAndCheck() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getAge() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
