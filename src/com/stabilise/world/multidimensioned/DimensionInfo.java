package com.stabilise.world.multidimensioned;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.google.common.base.Preconditions;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.IWorld;
import com.stabilise.world.WorldInfo;

/**
 * Contains information specific to a dimension.
 */
public class DimensionInfo {
	
	/** The info of the world this dimension belongs to. */
	public final WorldInfo info;
	
	/** The name of the dimension. */
	public final String name;
	/** The age of this dimension. */
	public volatile long age = 0L;
	
	/** The coordinates of the slice in which players should initially spawn,
	 * in slice-lengths. */
	public int spawnSliceX = 0, spawnSliceY = 0;
	
	
	/**
	 * Creates a new DimensionInfo.
	 * 
	 * @param worldInfo The WorldInfo object of this dimension's parent world.
	 * @param name The name of the dimension.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public DimensionInfo(WorldInfo worldInfo, String name) {
		this.info = Preconditions.checkNotNull(worldInfo);
		this.name = Preconditions.checkNotNull(name);
	}
	
	/**
	 * Loads the dimension info.
	 * 
	 * @throws IOException if the info file does not exist or an I/O error
	 * otherwise occurs.
	 */
	public void load() throws IOException {
		NBTTagCompound tag = NBTIO.readCompressed(getFile());
		
		if(name != tag.getString("dimName"))
			throw new IOException("Dimension name does not match stored name!");
		
		age = tag.getLongUnsafe("age");
		
		spawnSliceX = tag.getIntUnsafe("spawnX");
		spawnSliceY = tag.getIntUnsafe("spawnY");
	}
	
	/**
	 * Saves the dimension info.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void save() throws IOException {
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.addString("dimName", name);
		tag.addLong("age", age);
		
		tag.addInt("spawnX", spawnSliceX);
		tag.addInt("spawnY", spawnSliceY);
		
		NBTIO.safeWriteCompressed(getFile(), tag);
	}
	
	/**
	 * @return This dimension's filesystem directory.
	 */
	public FileHandle getDimensionDir() {
		return info.getWorldDir().child(IWorld.DIR_DIMENSIONS);
	}
	
	/**
	 * @return This DimensionInfo's filesystem location.
	 */
	public FileHandle getFile() {
		return getDimensionDir().child(IWorld.FILE_INFO);
	}
	
}
