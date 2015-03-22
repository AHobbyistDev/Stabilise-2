package com.stabilise.world.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.entity.EntityMob;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.collect.LightArrayList;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;
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
	
	/** Stores players using this world. Maps player names -> PlayerDataFiles. */
	private final Map<String, PlayerDataFile> players = new HashMap<>(1);
	
	
	/**
	 * Creates a new HostProvider.
	 * 
	 * @param info The world info.
	 * 
	 * @throws NullPointerException if {@code info} is {@code null}.
	 */
	public HostProvider(WorldInfo info) {
		super();
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
	 * loaded, or the world could not be loaded.
	 * @throws NullPointerException if {@code player} is {@code null}.
	 */
	@NotThreadSafe
	public PlayerBundle addPlayer(CharacterData player, boolean integrated) {
		PlayerDataFile file = players.get(player.name);
		if(file == null)
			file = new PlayerDataFile(player.name);
		PlayerData data = null;
		try {
			data = file.getData(player);
		} catch(IOException e) {
			log.postSevere("Could not load data for " + player + "!");
			return null;
		}
		players.putIfAbsent(player.name, file);
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
		for(PlayerDataFile p : players.values()) {
			try {
				p.dispose();
			} catch(IOException e) {
				throw new RuntimeException("Could not save " + p, e);
			}
		}
		
		for(HostWorld dim : dimensions.values())
			dim.blockUntilClosed();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A world maintains data of each player to visit it so that it may place
	 * each player in the same location of the same dimension they were in when
	 * they last played (as well as load any additional data and such).
	 * 
	 * <p>Player data files are saved with the name of the player they're
	 * representing (e.g. a player named "Steve" would have a "Steve.player"
	 * file located in a world's player directory). However, if multiple player
	 * characters have the same name, both characters have their data saved in
	 * the same player data file, and they would be distinguished by their
	 * {@link CharacterData#hash hash} (if two players have the same hash, gg).
	 * 
	 * <p>The data for individual players is represented by the {@link
	 * PlayerData} class, but each {@code PlayerData} object answers to an 
	 * instance of this class to which to save its data.
	 * 
	 * <p>The saving and loading strategy utilised by this class is that of
	 * loading the data on-demand. Namely:
	 * 
	 * <ul>
	 * <li>When a PlayerData object is requested via {@link
	 *     PlayerDataFile#getData(CharacterData) getData()}, the file is loaded
	 *     on the spot and the necessary data extracted; the rest is discarded.
	 * <li>When a PlayerData object is saved via {@link
	 *     PlayerDataFile#save(PlayerData) putData()}, the file is loaded,
	 *     the PlayerData's data is added to the NBT, and the file is saved.
	 * </ul>
	 * 
	 * <p>This strategy lends itself to single-threaded used only.
	 * 
	 * <!-- TODO: A caching strategy may be preferable if PlayerDataFiles:
	 * a) are saved frequently
	 * b) become large in size (though this adds to data redundancy)
	 * c) contain lots (e.g. 5+, or 10+) of characters, however unlikely
	 * -->
	 */
	@NotThreadSafe
	private class PlayerDataFile {
		
		private final String name;
		private final FileHandle file;
		private final LightArrayList<PlayerData> chars =
				LightArrayList.unordered(1, 1.0f); // def. cap 1, increase by 1
		
		
		/**
		 * Creates a new player data file. This method may block while this
		 * file's NBT data is loaded.
		 * 
		 * @param The name of the player(s).
		 * @param provider The world provider.
		 */
		public PlayerDataFile(String name) {
			this.name = name;
			file = info.getWorldDir().child(World.DIR_PLAYERS + name + World.EXT_PLAYERS);
		}
		
		/**
		 * Loads this file's NBT data, or returns an empty compound tag if the
		 * file does not exist.
		 */
		private NBTTagCompound loadData() throws IOException {
			return file.exists()
					? NBTIO.readCompressed(file)
					: new NBTTagCompound();
		}
		
		/**
		 * Saves the player data into the file.
		 */
		private void saveData(NBTTagCompound nbt) throws IOException {
			NBTIO.safeWriteCompressed(file, nbt);
		}
		
		/**
		 * Gets the PlayerData object for the specified player.
		 * 
		 * @throws IOException if an I/O error occurs while loading the file.
		 */
		public PlayerData getData(CharacterData player) throws IOException {
			NBTTagCompound tag = loadData().getCompound(player.hash);
			PlayerData p;
			if(tag.isEmpty())
				p = new PlayerData(this, player);
			else
				p = new PlayerData(this, player, tag);
			chars.add(p);
			return p;
		}
		
		/**
		 * Updates this PlayerDataFile's save of the specified player data, and
		 * then {@link #save() saves} this player data file.
		 */
		public void save(PlayerData data) throws IOException {
			NBTTagCompound nbt = loadData();
			nbt.addCompound(data.data.hash, data.toNBT());
			saveData(nbt);
		}
		
		/**
		 * Treats {@code p} is disposed (removes it from the list).
		 */
		public void disposeData(PlayerData p) {
			chars.remove(p);
		}
		
		/**
		 * Disposes of this PlayerDataFile, and every attached PlayerData
		 * object as if by {@link PlayerData#dispose()}.
		 * 
		 * <p>This operation involves both loading and saving this file.
		 * 
		 * @throws IOException if an I/O error occurs.
		 */
		public void dispose() throws IOException {
			NBTTagCompound nbt = loadData();
			for(PlayerData p : chars)
				nbt.addCompound(p.data.hash, p.toNBT());
			chars.clear();
			saveData(nbt);
		}
		
		@Override
		public String toString() {
			return "PlayerDataFile[" + name + "]";
		}
		
	}
	
	/**
	 * Stores the world-local data of a player.
	 * 
	 * <p>An instance of this class should be {@link PlayerData#dispose()
	 * disposed} of when it is no longer needed.
	 */
	@NotThreadSafe
	public static class PlayerData {
		
		/** The world-local player data file. */
		private final PlayerDataFile file;
		/** The player's global data. */
		private final CharacterData data;
		
		/** Whether or not the character is new to the world. */
		public boolean newToWorld;
		/** The dimension the player is in. */
		@ExportToNBT
		public String dimension;
		/** The coordinates of the player's last known location, in
		 * tile-lengths. */
		@ExportToNBT
		public double lastX, lastY;
		
		
		/**
		 * Creates a PlayerData object initialised to the default values.
		 */
		private PlayerData(PlayerDataFile file, CharacterData data) {
			this.file = file;
			this.data = data;
			defaultData();
		}
		
		/**
		 * Creates a new PlayerData object and imports into it the data from
		 * the given NBT compound tag.
		 */
		private PlayerData(PlayerDataFile file, CharacterData data, NBTTagCompound tag) {
			this.file = file;
			this.data = data;
			fromNBT(tag);
		}
		
		/**
		 * Initialises the player data to the default values.
		 */
		private void defaultData() {
			newToWorld = true;
			dimension = Dimension.defaultDimensionName();
			lastX = lastY = 0D; // TODO: let the default dimension intialise this
		}
		
		/**
		 * Exports this PlayerData object to an NBT compound tag and returns
		 * it.
		 * 
		 * @throws RuntimeException if something went wrong.
		 */
		private NBTTagCompound toNBT() {
			NBTTagCompound tag = NBTExporter.exportObj(this);
			return tag;
		}
		
		/**
		 * Imports this PlayerData from an NBT compound tag.
		 * 
		 * @throws RuntimeException if something went wrong.
		 */
		private void fromNBT(NBTTagCompound tag) {
			NBTExporter.importObj(this, tag);
			newToWorld = false;
		}
		
		/**
		 * Saves this PlayerData.
		 * 
		 * @throws IOException if an I/O error occurred.
		 */
		public void save() throws IOException {
			file.save(this);
		}
		
		/**
		 * Saves and then disposes this PlayerData object.
		 * 
		 * @throws IOException if an I/O error occurred.
		 */
		public void dispose() throws IOException {
			save();
			file.disposeData(this);
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
