package com.stabilise.world;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

/**
 * Contains important information about a world, common to all dimensions.
 */
public class WorldInfo implements Comparable<WorldInfo> {
    
    private boolean loaded = false;
    
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
    
    /** Contains information about the format in which the world has been
     * saved. This is an open-ended DataCompound object so that the WorldLoader
     * can decide what to write and write to this. */
    public DataCompound worldFormat = DataCompound.create();
    
    
    
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
     * Loads this world info if it has not been previously loaded. Note that
     * {@code fileSystemName} must first be set.
     * 
     * <p>If this method has been previously invoked, this does nothing.
     * 
     * @throws IOException if the info file does not exist or an I/O error
     * otherwise occurs.
     */
    public void load() throws IOException {
        if(loaded)
            return;
        
        DataCompound infoTag = IOUtil.read(getFile(), Format.NBT, Compression.GZIP);
        
        name = infoTag.getString("worldName");
        seed = infoTag.getI64("seed");
        age = infoTag.getI64("age");
        
        creationDate = infoTag.getI64("creationDate");
        lastPlayedDate = infoTag.getI64("lastPlayed");
        
        // Overwriting our worldFormat object can't be helped here
        worldFormat = infoTag.getCompound("format");
        
        loaded = true;
    }
    
    /**
     * Saves the world info.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void save() throws IOException {
        lastPlayedDate = System.currentTimeMillis();
        
        DataCompound infoTag = Format.NBT.newCompound();
        
        infoTag.put("worldName", name);
        infoTag.put("seed", seed);
        infoTag.put("age", age);
        
        infoTag.put("creationDate", creationDate);
        infoTag.put("lastPlayed", lastPlayedDate);
        
        infoTag.put("format", worldFormat);
        
        IOUtil.writeSafe(getFile(), infoTag, Compression.GZIP);
    }
    
    /**
     * @return This world's filesystem directory.
     */
    public FileHandle getWorldDir() {
        return Worlds.getWorldDir(fileSystemName);
    }
    
    /**
     * Gets this WorldInfo's filesystem location.
     */
    public FileHandle getFile() {
        return getWorldDir().child(World.FILE_INFO);
    }
    
    @Override
    public int compareTo(WorldInfo other) {
        // More recently-played worlds should appear higher
        int comp = Long.compare(other.lastPlayedDate, lastPlayedDate);
        // Failing that, order alphabetically
        if(comp == 0)
            comp = fileSystemName.compareTo(other.fileSystemName);
        return comp;
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
