package com.stabilise.world.multidimensioned;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.google.common.base.Preconditions;
import com.stabilise.character.CharacterData;
import com.stabilise.util.Checkable;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.concurrent.BoundedThreadPoolExecutor;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.util.nbt.export.ExportToNBT;
import com.stabilise.util.nbt.export.NBTExporter;
import com.stabilise.world.HostWorld;
import com.stabilise.world.IWorld;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.save.WorldLoader;

/**
 * A WorldProvider manages and 'provides' all the dimensions/worlds of a
 * world.<sup><font size=-1>1</font></sup>
 * 
 * <p>{@code 1.} The terminology is somewhat confusing here. From the user's
 * perspective, a <i>WorldProvider</i> is actually a <i>world</i>, and
 * different <i>Worlds</i> (e.g. {@code HostWorld}, etc.) are
 * <i>dimensions</i> of that world/WorldProvider. We largely refer to
 * 'dimensions' as 'worlds' in the code (e.g. GameObjects have a {@code world}
 * member through which they interact with the dimension they are in) for both
 * legacy and aesthetic purposes.
 */
public class WorldProvider {
	
	public final WorldInfo info;
	/** Maps dimension names -> dimensions. */
	private final Map<String, HostWorld> dimensions = new HashMap<>(2);
	/** Maps player names -> PlayerDataFiles. */
	private final Map<String, PlayerDataFile> players = new HashMap<>(1);
	
	/** The ExecutorService to use for delegating loader and generator threads. */
	public final ExecutorService executor;
	/** The global WorldLoader to use for loading regions. */
	public final WorldLoader loader;
	
	/** Profile any world's operation with this. */
	public final Profiler profiler;
	
	
	/**
	 * Creates a new WorldProvider.
	 * 
	 * @param info The world info.
	 * @param profiler The profiler to use to profile the world.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public WorldProvider(WorldInfo info, Profiler profiler) {
		this.info = Preconditions.checkNotNull(info);
		this.profiler = Preconditions.checkNotNull(profiler);
		
		// Start up the executor
		
		final int coreThreads = 2; // region loading typically happens in pairs
		final int maxThreads = Math.max(coreThreads, Runtime.getRuntime().availableProcessors());
		
		BoundedThreadPoolExecutor tpe = new BoundedThreadPoolExecutor(
				coreThreads, maxThreads,
				30L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new WorldThreadFactory()
		);
		tpe.setRejectedExecutionHandler(new BoundedThreadPoolExecutor.CallerRunsPolicy());
		executor = tpe;
		
		// Start up the world loader
		loader = WorldLoader.getLoader(this);
	}
	
	public void update() {
		info.age++;
		
		Checkable.updateCheckables(dimensions.values());
	}
	
	/**
	 * @param name The name of the dimension.
	 * 
	 * @return The dimension, or {@code null} if the specified dimension is not
	 * loaded.
	 */
	public HostWorld getDimension(String name) {
		return dimensions.get(name);
	}
	
	/**
	 * Loads a dimension into memory.
	 * 
	 * @param name The name of the dimension.
	 * 
	 * @return The dimension, or {@code null} if no such dimension has been
	 * registered.
	 * @throws RuntimeException if the dimension could not be prepared.
	 * @throws IllegalArgumentException if {@code name} is not the name of a
	 * valid dimension.
	 */
	public HostWorld loadDimension(String name) {
		HostWorld world = getDimension(name);
		if(world != null)
			return world;
		
		Dimension dim = Dimension.getDimension(new Dimension.Info(info, name));
		if(dim == null)
			throw new IllegalArgumentException("Invalid dimension \"" + name + "\"");
		
		// If the info file exists, this implies the dimension has already been
		// created.
		if(dim.info.fileExists()) {
			try {
				dim.loadData();
			} catch(IOException e) {
				Log.getAgent("WorldProvider").postSevere("Could not load dimension info! (dim: " + name + ")", e);
			}
		}
		
		world = dim.createHost(this);
		world.prepare();
		
		dimensions.put(name, world);
		
		return world;
	}
	
	/**
	 * Saves the worlds.
	 */
	public void save() {
		try {
			info.save();
		} catch(IOException e) {
			Log.getAgent("WorldProvider").postSevere("Could not save world info", e);
		}
		
		for(HostWorld dim : dimensions.values())
			dim.save();
	}
	
	/**
	 * Closes this world provider down. This method will block the current
	 * thread until shutdown procedures have completed.
	 */
	public void close() {
		loader.shutdown();
		
		for(HostWorld dim : dimensions.values())
			dim.close();
		
		for(HostWorld dim : dimensions.values())
			dim.blockUntilClosed();
		
		executor.shutdown();
		
		try {
			if(!executor.awaitTermination(10, TimeUnit.SECONDS))
				Log.get().postWarning("World executor took longer than 10 seconds to shutdown!");
		} catch(InterruptedException e) {
			Log.get().postWarning("Interrupted while waiting for world executor to terminate!");
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Thread factory implementation for world loader and world generator
	 * threads.
	 */
	private static class WorldThreadFactory implements ThreadFactory {
		
		/** The number of threads created with this factory. */
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		
		private final UncaughtExceptionHandler ripWorkerThread = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Log.get().postSevere("Worker thread \"" + t.toString() + "\" died!", e);
			}
		};
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "WorldThread" + threadNumber.getAndIncrement());
			if(t.isDaemon())
				t.setDaemon(false);
			if(t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			t.setUncaughtExceptionHandler(ripWorkerThread);
			return t;
		}
	}
	
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
	 *     PlayerDataFile#saveData(PlayerData) putData()}, the file is loaded,
	 *     the PlayerData's data is added to the NBT, and the file is saved.
	 * </ul>
	 * 
	 * <p>This strategy lends itself to single-threaded used only.
	 * 
	 * <!-- TODO: A caching strategy may be preferable if PlayerDataFiles:
	 * a) are saved frequently
	 * b) become large in size (though this adds to data redundancy)
	 * c) contain lots (e.g. 5, 10+) characters, however unlikely
	 * -->
	 */
	@NotThreadSafe
	private static class PlayerDataFile {
		
		private final FileHandle file;
		private final Array<PlayerData> chars = new Array<>(false, 2, PlayerData.class);
		
		
		/**
		 * Creates a new player data file. This method may block while this
		 * file's NBT data is loaded.
		 * 
		 * @param The name of the player(s).
		 * @param provider The world provider.
		 */
		public PlayerDataFile(String name, WorldProvider provider) {
			file = provider.info.getWorldDir().child(IWorld.DIR_PLAYERS + name + IWorld.EXT_PLAYERS);
		}
		
		/**
		 * Loads this file's NBT data, or returns an empty compound tag if the
		 * file does not exist.
		 */
		private NBTTagCompound loadData() throws IOException {
			if(file.exists())
				return NBTIO.readCompressed(file);
			else
				return new NBTTagCompound();
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
			if(tag == null)
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
		public void saveData(PlayerData data) throws IOException {
			NBTTagCompound nbt = loadData();
			nbt.addCompound(data.data.hash, data.toNBT());
			saveData(nbt);
		}
		
		/**
		 * Treats {@code p} is disposed (removes it from the list).
		 */
		public void disposeData(PlayerData p) {
			chars.removeValue(p, true);
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
			dimension = Dimension.defaultDimension();
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
			file.saveData(this);
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
	
}
