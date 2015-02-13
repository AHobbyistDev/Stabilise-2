package com.stabilise.world.multidimensioned;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.files.FileHandle;
import com.google.common.base.Preconditions;
import com.stabilise.character.CharacterData;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
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
		
		Iterator<HostWorld> i = dimensions.values().iterator();
		while(i.hasNext())
			if(i.next().updateAndCheck())
				i.remove();
	}
	
	/**
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
	 * @throws RuntimeException if something derped.
	 */
	public HostWorld loadDimension(String name) {
		HostWorld world = getDimension(name);
		if(world != null)
			return world;
		
		// TODO: There is a very significant amount of overhead to doing this
		// on the main thread. Find a way to shove this off to a worker thread.
		
		Dimension dim = Dimension.getDimension(new Dimension.Info(info, name));
		if(dim == null)
			return null; // note: perhaps an exception might be more in order?
		
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
	 */
	public static class PlayerDataFile {
		
		/** The name of the player(s) held by this data file. */
		private final String name;
		/** The file. */
		private final FileHandle file;
		/** The root compound tag of the player's data file. */
		private final NBTTagCompound nbt;
		
		
		/**
		 * Creates a new player data file. This method may block while this
		 * file's NBT data is loaded.
		 * 
		 * @param The name of the player(s).
		 * @param provider The world provider.
		 * 
		 * @throws IOException if an I/O error occurs while loading the file.
		 */
		private PlayerDataFile(String name, WorldProvider provider) throws IOException {
			this.name = name;
			file = provider.info.getWorldDir().child(IWorld.DIR_PLAYERS + name + IWorld.EXTENSION_PLAYERS);
			
			if(file.exists())
				nbt = NBTIO.readCompressed(file);
			else
				nbt = new NBTTagCompound();
		}
		
		/**
		 * Gets the player data for 
		 * 
		 * @param player
		 * @return
		 */
		public PlayerData getData(CharacterData player) {
			NBTTagCompound tag = nbt.getCompound(player.hash);
			if(tag == null)
				return new PlayerData(this, player);
			else
				return new PlayerData(this, player, tag);
		}
		
		/**
		 * Updates this PlayerDataFile's save of the specified player data, and
		 * then {@link #save() saves} this player data file.
		 */
		public void putData(PlayerData data) {
			nbt.addCompound(data.data.hash, data.toNBT());
			save();
		}
		
		/**
		 * Saves the player data into the file.
		 */
		private void save() {
			try {
				NBTIO.safeWriteCompressed(file, nbt);
			} catch(IOException e) {
				Log.get().postSevere("Could not save the world's player data file for " + name, e);
			}
		}
		
	}
	
	/**
	 * Stores the world-local data of a player.
	 */
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
		 */
		public void save() {
			file.putData(this);
		}
		
	}
	
}
