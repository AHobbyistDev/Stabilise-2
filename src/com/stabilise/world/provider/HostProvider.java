package com.stabilise.world.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.entity.EntityMob;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.export.ExportToNBT;
import com.stabilise.util.nbt.export.NBTExporter;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.dimension.Dimension;


/**
 * A HostProvider is a provider of a world for the host of that world. There
 * are three types of hosts:
 * 
 * <ul>
 * <li><b>Singleplayer</b>: This is the simplest case.
 * <li><b>Multiplayer</b>: Hosts the world via internet to multiple clients.
 * <li><b>Multiplayer with Integrated Player</b>: A combination of the above
 *     two; the world is hosted to multiple clients, but there is also an
 *     integrated player who does not require a connection.
 * </ul>
 */
public class HostProvider extends WorldProvider<HostWorld> {
	
	/** Dimensions should treat this as read-only. */
	public final WorldInfo info;
	
	/** Stores players using this world. Maps player hash -> PlayerData. */
	private final Map<String, PlayerData> players = new HashMap<>(2);
	
	
	/**
	 * Creates a new HostProvider.
	 * 
	 * @param info The world info.
	 * @param profiler The profiler to use to profile this world provider and
	 * its worlds. If {@code null}, a default disabled profiler is instead set.
	 * 
	 * @throws NullPointerException if {@code info} is {@code null}.
	 */
	public HostProvider(WorldInfo info, Profiler profiler) {
		super(profiler);
		this.info = Objects.requireNonNull(info);
	}
	
	@Override
	public void update() {
		info.age++;
		super.update();
	}

	@Override
	@NotThreadSafe
	public HostWorld loadDimension(String name) {
		HostWorld world = getDimension(name);
		if(world != null)
			return world;
		
		Dimension dim = Dimension.getDimension(info, name);
		if(dim == null)
			throw new IllegalArgumentException("Invalid dimension \"" + name + "\"");
		
		try {
			dim.loadData();
		} catch(IOException e) {
			throw new RuntimeException("Could not load dimension info! (dim: " +
					name + ") (" + e.getMessage() + ")" , e);
		}
		
		world = dim.createHost(this);
		world.prepare();
		
		dimensions.put(name, world);
		
		return world;
	}
	
	/**
	 * Adds a player to a world.
	 * 
	 * @param player The CharacterData for the player to add.
	 * @param integrated {@code true} if this player should become that of the
	 * integrated client.
	 * 
	 * @return The PlayerBundle holding the player entity, data, and world the
	 * player was added to, or {@code null} if the player data could not be
	 * loaded, or the dimension could not be loaded.
	 * @throws NullPointerException if {@code player} is {@code null}.
	 */
	@NotThreadSafe
	public PlayerBundle addPlayer(CharacterData player, boolean integrated) {
		PlayerData data = players.get(player.hash);
		if(data == null)
			data = new PlayerData(player);
		try {
			data.load();
		} catch(IOException e) {
			log.postSevere("Could not load data for " + player + "!");
			return null;
		}
		players.putIfAbsent(player.hash, data);
		HostWorld world = loadDimension(data.dimension);
		if(world == null)
			return null;
		EntityMob playerEntity = world.addPlayer(data);
		if(integrated) {
			integratedClient = true;
			integratedCharacter = player;
			integratedPlayer = playerEntity;
		}
		return new PlayerBundle(world, playerEntity, data);
	}
	
	/**
	 * doesn't do anything yet
	 * @param player
	 */
	public void removePlayer(CharacterData player) {
		// TODO
	}
	
	@Override
	public long getSeed() {
		return info.seed;
	}
	
	/**
	 * Saves the worlds.
	 * 
	 * @throws RuntimeException if an I/O error occurred while saving.
	 */
	@Override
	public void save() {
		try {
			info.save();
		} catch(IOException e) {
			throw new RuntimeException("Could not save world info!", e);
		}
		
		for(HostWorld dim : dimensions.values())
			dim.save();
	}
	
	@Override
	protected void closeExtra() {
		for(PlayerData p : players.values()) {
			try {
				p.save();
			} catch(IOException e) {
				throw new RuntimeException("Could not save " + p, e);
			}
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Stores the world-local data of a player.
	 * 
	 * <p>An instance of this class should be {@link PlayerData#dispose()
	 * disposed} of when it is no longer needed.
	 */
	@NotThreadSafe
	public class PlayerData {
		
		/** The player's global data. */
		public final CharacterData data;
		
		private final FileHandle file;
		
		/** Whether or not the character is new to the world. */
		public boolean newToWorld;
		/** The dimension the player is in. */
		@ExportToNBT
		public String dimension;
		/** The coordinates of the player's last known location, in
		 * tile-lengths. */
		@ExportToNBT
		public double lastX, lastY;
		
		
		private PlayerData(CharacterData data) {
			this.data = data;
			file = info.getWorldDir().child(World.DIR_PLAYERS + data.hash + World.EXT_PLAYERS);
		}
		
		/**
		 * Initialises the player data to the default values.
		 */
		private void defaultData() {
			newToWorld = true;
			dimension = Dimension.defaultDimensionName();
			lastX = lastY = 0D; // TODO: let the default dimension initialise this
		}
		
		/**
		 * @throws IOException if the file exists but could not be read.
		 */
		public void load() throws IOException {
			if(file.exists()) {
				NBTExporter.importObj(this, NBTIO.readCompressed(file));
				newToWorld = false;
			} else {
				defaultData();
			}
		}
		
		/**
		 * Saves the player data.
		 */
		public void save() throws IOException {
			NBTIO.safeWriteCompressed(file, NBTExporter.exportObj(this));
		}
		
	}
	
	/**
	 * A convenience class which bundles the objects to be returned when a
	 * player is added to the world.
	 */
	public static class PlayerBundle {
		
		/** The world the player has been added to. */
		public final HostWorld world;
		/** The player entity. */
		public final EntityMob playerEntity;
		/** The player's data. */
		public final PlayerData playerData;
		
		private PlayerBundle(HostWorld world, EntityMob mob, PlayerData data) {
			this.world = world;
			this.playerEntity = mob;
			this.playerData = data;
		}
		
	}
	
}
