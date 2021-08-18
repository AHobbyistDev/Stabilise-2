package com.stabilise.core;

import static com.stabilise.util.io.data.Compression.*;
import static com.stabilise.util.io.data.Format.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javaslang.control.Try;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.Version;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.nbt.NBTCompound;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;

/**
 * This class contains all of the important universal game constants.
 */
public class Constants {
    
    private Constants() {} // non-instantiable
    
    
    // Primary Application Data -----------------------------------------------
    
    public static final String GAME_PACKAGE = "com.stabilise";
    
    /** Indicates whether this copy of the game is a developer version.
     * <p>This should be {@code false} for all public releases. */
    public static final boolean DEV_VERSION = true;
    
    /** The current game version, without the build metadata. */
    public static final Version VERSION_RAW = new Version(0, 0, 0, "alpha");
    /** The current game version. */
    public static final Version VERSION = genVersion();
    /** The oldest version we're backwards-compatible with. */
    public static final Version BACKWARDS_VERSION = VERSION.clone();
    
    static {
        if(VERSION.compareTo(BACKWARDS_VERSION) < 0)
            throw new AssertionError("The oldest game version we're "
                    + "backwards-compatible with (" + BACKWARDS_VERSION
                    + ") is somehow newer than our current version ("
                    + VERSION + ")!");
    }
    
    /** The number of game ticks each second. */
    public static final int TICKS_PER_SECOND = 60;
    
    // Gameplay ---------------------------------------------------------------
    
    public static final String LOCAL_BROADCAST_IP = "4.2.0.0";
    
    /** The port which the game will be hosted on by default. Note it is
     * completely arbitrary. */
    public static final int PORT_DEFAULT = 8224;
    /** The port on which the update server is hosted. */
    public static final int PORT_SERVER = 8225;
    /** Port for UDP broadcasts. */
    public static final int PORT_BROADCAST = 8226;
    public static final byte[] BROADCAST_MSG = "<<Stabilise II LAN search broadcast>>".getBytes();
    public static final byte[] BROADCAST_RESPONSE = "<<Stabilise II LAN server response>>".getBytes();
    public static final InetAddress BROADCAST_ADDRESS;
    
    static {
        try {
            // 255.255.255.255 is the default broadcast address
            BROADCAST_ADDRESS = InetAddress.getByName("255.255.255.255");
        } catch(UnknownHostException e) {
            throw new Error(e);
        }
    }
    
    
    public static final int SLICE_SIZE = Slice.SLICE_SIZE;
    public static final int REGION_SIZE = Region.REGION_SIZE;
    
    public static final int LOADED_TILE_RADIUS = 6 * SLICE_SIZE;
    /** The half-length of an edge of the square of loaded slices around the
     * player. */
    public static final int LOADED_SLICE_RADIUS = LOADED_TILE_RADIUS / Slice.SLICE_SIZE;
    /** The minimum number of slices which can be considered 'loaded' about a
     * player at any given time. */
    public static final int MIN_LOADED_SLICES =
            // Manually square this to make it a compile-time constant.
            (1 + 2 * LOADED_SLICE_RADIUS) * (1 + 2 * LOADED_SLICE_RADIUS);
    /** The maximum number of slices which should theoretically be able to be
     * loaded about a player at any given time. */
    public static final int MAX_LOADED_SLICES =
            // Manually square this to make it a compile-time constant.
            (1 + 2*(LOADED_SLICE_RADIUS)) * (1 + 2*(LOADED_SLICE_RADIUS));
    /** How many ticks after coming out of use that a region should unload. */
    public static final int REGION_UNLOAD_TICK_BUFFER = 5 * TICKS_PER_SECOND;
    
    /** How large a character's inventory is. */
    public static final int INVENTORY_CAPACITY = 36;
    /** The number of items on the hotbar. */
    public static final int HOTBAR_SIZE = 9;
    /** Maximum item stack size. */
    public static final int MAX_STACK_SIZE = 999;
    
    
    
    /**
     * Generates the game version.
     */
    private static Version genVersion() {
        Version v = VERSION_RAW;
        int build = getBuild(v, Resources.DIR_APP.child("build"));
        return new Version(v.major(), v.minor(), v.patch(), v.preRelease(),
                "build." + build);
    }
    
    /**
     * Gets the build number, which represents the number of times the current
     * version has been run with {@link #DEV_VERSION} set to {@code true} on
     * the current system. This is for dev versions only.
     * 
     * @return {@code 0} if {@code DEV_VERSION} is {@code false}; otherwise,
     * the build revision number.
     */
    private static int getBuild(Version version, FileHandle file) {
        if(DEV_VERSION) {
            try {
                DataCompound tag;
                
                if(file.exists()) {
                    tag = Try.of(() -> IOUtil.read(file, NBT, UNCOMPRESSED))
                            .recover(e -> {
                        Log.get().postWarning("Couldn't load revision file", e);
                        return new NBTCompound();
                    }).get();
                } else {
                    tag = new NBTCompound();
                }
                
                String[] fieldNames = { "major", "minor", "patch" };
                int[] fields = { version.major(), version.minor(), version.patch() };
                
                tag.put("builds", tag.getI32("builds") + 1);
                int buildCompilations = buildTags(tag, fieldNames, fields, 0);
                
                IOUtil.writeSafe(file, tag, UNCOMPRESSED);
                
                return buildCompilations;
            } catch(Exception e) {
                Log.get().postWarning("Could not get game revision: " + e.getMessage());
                return 1;
            }
        }
        
        return 0;
    }
    
    /**
     * Recursively builds the version compilation data tags.
     * 
     * @return The number of builds of the current version.
     */
    private static int buildTags(DataCompound parent, String[] names, int[] fields, int i) {
        DataCompound tag = parent.childCompound(names[i] + ": " + fields[i]);
        tag.put("builds", tag.getI32("builds") + 1);
        if(++i < fields.length) // we are not build
            return buildTags(tag, names, fields, i); // recursively get children
        else // we are build
            return tag.getI32("builds");
    }
    
}
