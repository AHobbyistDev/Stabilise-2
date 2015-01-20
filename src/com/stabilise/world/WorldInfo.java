package com.stabilise.world;

import java.io.IOException;
import java.util.Date;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;

/**
 * A WorldInfo object is used to contain the critical information of a world.
 */
public class WorldInfo implements Comparable<WorldInfo> {
	
	/** The name of the world. This is aesthetic. */
	public String name;
	/** The name of the world on the file system. */
	public String fileSystemName;
	
	/** The world seed upon which to base procedural generation. */
	public long seed = 0L;
	
	/** The age of the world in ticks. */
	public volatile long age = 0L; // see WorldLoader.saveRegion() as to why this is volatile
	
	/** The date the world was created at. */
	public long creationDate = 0L;
	/** The date the world was last accessed at. */
	public long lastPlayedDate = 0L;
	
	/** Whether or not to use the flatland world generator. */
	public boolean flatland = false;
	
	/** The version of the format in which the world has been saved.
	 * This is used to determine whether or not the game will need to convert the 
	 * world data from an older format a current one. */
	public int worldFormatVersion = 0;
	/** The version of the format in which the world slices have been saved.
	 * This will be used to determine whether or not the game will need to convert
	 * the world from an older format to a current one. */
	public int sliceFormatVersion = 0;
	
	/** The x-coordinate of the slice in which players should initially spawn,
	 * in slice-lengths. */
	public int spawnSliceX = 0;
	/** The y-coordinate of the slice in which players should initially spawn,
	 * in slice-lengths. */
	public int spawnSliceY = 0;
	
	
	/**
	 * Creates a new WorldInfo.
	 * 
	 * @param worldName The name of the world. This serves as both the file
	 * system name and the display name by default.
	 */
	public WorldInfo(String worldName) {
		fileSystemName = worldName;
		name = worldName;
	}
	
	/**
	 * Loads the world info. Note that {@code fileSystemName} must first be
	 * set.
	 * 
	 * @throws IOException if the info file does not exist or an I/O error
	 * otherwise occurs.
	 */
	public void load() throws IOException {
		FileHandle file = getFile();
		if(!file.exists())
			throw new IOException("Info file does not exist!");
		
		NBTTagCompound infoTag = NBTIO.readCompressed(file);
		
		name = infoTag.getStringUnsafe("worldName");
		seed = infoTag.getLongUnsafe("seed");
		age = infoTag.getLongUnsafe("age");
		
		creationDate = infoTag.getLongUnsafe("creationDate");
		lastPlayedDate = infoTag.getLongUnsafe("lastPlayed");
		
		flatland = infoTag.getBooleanUnsafe("flatland");
		worldFormatVersion = infoTag.getIntUnsafe("formatVersion");
		sliceFormatVersion = infoTag.getIntUnsafe("sliceFormatVersion");
		
		spawnSliceX = infoTag.getIntUnsafe("spawnX");
		spawnSliceY = infoTag.getIntUnsafe("spawnY");
	}
	
	/**
	 * Saves the world info.
	 * 
	 * @throws IOException Thrown if there is an I/O exception when saving the
	 * info file.
	 */
	public void save() throws IOException {
		lastPlayedDate = new Date().getTime();
		
		NBTTagCompound infoTag = new NBTTagCompound();
		
		infoTag.addString("worldName", name);
		infoTag.addLong("seed", seed);
		infoTag.addLong("age", age);
		
		infoTag.addLong("creationDate", creationDate);
		infoTag.addLong("lastPlayed", lastPlayedDate);
		
		infoTag.addBoolean("flatland", flatland);
		infoTag.addInt("formatVersion", worldFormatVersion);
		infoTag.addInt("sliceFormatVersion", sliceFormatVersion);
		
		infoTag.addInt("spawnX", spawnSliceX);
		infoTag.addInt("spawnY", spawnSliceY);
		
		NBTIO.safeWriteCompressed(getFile(), infoTag);
	}
	
	/**
	 * Gets this WorldInfo filesystem location.
	 */
	private FileHandle getFile() {
		return IWorld.getWorldDir(fileSystemName).child(IWorld.FILE_INFO);
	}
	
	/*
	 * Compares the WorldInfo object to another WorldInfo object, such that
	 * they may be ordered in an array based on the last time the worlds were
	 * accessed.
	 */
	@Override
	public int compareTo(WorldInfo world) {
		if(lastPlayedDate > world.lastPlayedDate) return -1;
		if(lastPlayedDate < world.lastPlayedDate) return 1;
		return 0;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the WorldInfo object for a world.
	 * 
	 * @param fileSystemName The name of the world on the file system.
	 * 
	 * @return The WorldInfo object, or {@code null} if the world info could
	 * not be loaded.
	 */
	public static WorldInfo loadInfo(String fileSystemName) {
		WorldInfo info = new WorldInfo(fileSystemName);
		try {
			info.load();
			return info;
		} catch(IOException e) {
			return null;
		}
	}
	
}
