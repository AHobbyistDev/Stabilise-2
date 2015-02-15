package com.stabilise.world;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;

/**
 * Contains important information about a world, common to all dimensions.
 */
public class WorldInfo implements Comparable<WorldInfo> {
	
	/** The name of the world. This is aesthetic. */
	public String name;
	/** The name of the world on the file system. */
	public String fileSystemName;
	
	/** The world seed upon which to base procedural generation. */
	public long seed = 0L;
	
	/** The age of the world in ticks. */
	public long age = 0L;
	
	/** The date the world was created at. */
	public long creationDate = 0L;
	/** The date the world was last accessed at. */
	public long lastPlayedDate = 0L;
	
	/** The version of the format in which the world has been saved.
	 * This is used to determine whether or not the game will need to convert the 
	 * world data from an older format a current one. */
	public int worldFormatVersion = 0;
	/** The version of the format in which the world slices have been saved.
	 * This will be used to determine whether or not the game will need to convert
	 * the world from an older format to a current one. */
	public int sliceFormatVersion = 0;
	
	
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
		NBTTagCompound infoTag = NBTIO.readCompressed(getFile());
		
		name = infoTag.getStringUnsafe("worldName");
		seed = infoTag.getLongUnsafe("seed");
		age = infoTag.getLongUnsafe("age");
		
		creationDate = infoTag.getLongUnsafe("creationDate");
		lastPlayedDate = infoTag.getLongUnsafe("lastPlayed");
		
		worldFormatVersion = infoTag.getIntUnsafe("formatVersion");
		sliceFormatVersion = infoTag.getIntUnsafe("sliceFormatVersion");
	}
	
	/**
	 * Saves the world info.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void save() throws IOException {
		lastPlayedDate = System.currentTimeMillis();
		
		NBTTagCompound infoTag = new NBTTagCompound();
		
		infoTag.addString("worldName", name);
		infoTag.addLong("seed", seed);
		infoTag.addLong("age", age);
		
		infoTag.addLong("creationDate", creationDate);
		infoTag.addLong("lastPlayed", lastPlayedDate);
		
		infoTag.addInt("formatVersion", worldFormatVersion);
		infoTag.addInt("sliceFormatVersion", sliceFormatVersion);
		
		NBTIO.safeWriteCompressed(getFile(), infoTag);
	}
	
	/**
	 * @return This world's filesystem directory.
	 */
	public FileHandle getWorldDir() {
		return IWorld.getWorldDir(fileSystemName);
	}
	
	/**
	 * Gets this WorldInfo's filesystem location.
	 */
	public FileHandle getFile() {
		return getWorldDir().child(IWorld.FILE_INFO);
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
