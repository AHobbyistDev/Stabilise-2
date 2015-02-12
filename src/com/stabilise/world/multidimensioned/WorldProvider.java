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
import com.stabilise.world.HostWorld;
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
	private final Map<String, PlayerDataFile> players = new HashMap<>(2);
	
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
	 * A way of easily working with a world's data file for each
	 * player/character.
	 */
	public static class PlayerDataFile {
		
		/** The file. */
		private FileHandle file;
		/** Whether or not the file has been initially loaded in. */
		private boolean loaded;
		/** The root compound tag of the player's data file. */
		private NBTTagCompound nbt;
		/** The compound representing the character's tag compound, with a name
		 * which is that of the character's hash. */
		private NBTTagCompound tag;
		/** Whether or not the character's tag exists within the file and was
		 * loaded. */
		private boolean tagLoaded;
		/** The character data. */
		private CharacterData character;
		
		
		/**
		 * Creates a new player data file.
		 * 
		 * @param character The player data upon which to base the data file.
		 */
		private PlayerDataFile(CharacterData character) {
			this.character = character;
			character.dataFile = this;
			
			file = getFile();
			nbt = null;
			loaded = !file.exists();
			tagLoaded = false;
		}
		
		/**
		 * Loads the file's contents into the character data.
		 */
		private void load() {
			loadNBT();
			
			if(tagLoaded) {
				try {
					character.lastX = tag.getDoubleUnsafe("x");
					character.lastY = tag.getDoubleUnsafe("y");
					character.newToWorld = false;
					return;
				} catch(IOException ignored) {}
			}
			
			character.newToWorld = true;
		}
		
		/**
		 * Loads the NBT file.
		 */
		private void loadNBT() {
			if(file.exists()) {
				try {
					nbt = NBTIO.readCompressed(file);
					tag = nbt.getCompound(character.hash);
					if(tag.isEmpty())
						nbt.addCompound(tag.getName(), tag);
					else
						tagLoaded = true;
					loaded = true;
				} catch(IOException e) {
					log.postSevere("Could not load character data file for character " + character.name, e);
				}
			} else {
				nbt = new NBTTagCompound("");
				tag = new NBTTagCompound(character.hash);
				nbt.addCompound(tag.getName(), tag);
				loaded = true;
			}
		}
		
		/**
		 * Saves the character's data into the file.
		 */
		private void save() {
			// In case there are other characters with the same name but a
			// different hash, we don't want to completely overwrite their data
			// in the file, so load in the file's content if possible
			if(!loaded)
				loadNBT();
			
			tag.addDouble("x", character.lastX);
			tag.addDouble("y", character.lastY);
			
			try {
				NBTIO.writeCompressed(file, nbt);
			} catch(IOException e) {
				log.postSevere("Could not save character data file for character " + character.name, e);
			}
		}
		
		/**
		 * Gets the data file's file reference.
		 * 
		 * @return The world's local character file.
		 */
		private FileHandle getFile() {
			return getDir().child(DIR_PLAYERS + character.name + EXTENSION_PLAYERS);
		}
	}
	
}
