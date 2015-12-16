package com.stabilise.core;

import javaslang.control.Try;

import javax.annotation.concurrent.Immutable;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.nbt.NBTCompound;
import com.stabilise.world.Slice;

/**
 * This class contains all of the important universal game constants.
 */
public class Constants {
    
    // non-instantiable
    private Constants() {}
    
    // Primary Application Data -----------------------------------------------
    
    public static final String GAME_PACKAGE = "com.stabilise";
    
    /** Indicates whether or not this copy of the game is a developer version.
     * <p>This should be {@code false} for all public releases. */
    public static final boolean DEV_VERSION = true;
    
    /** The current game version. */
    public static final Version VERSION = new Version(0,0,0,0);
    /** The oldest game version which is compatible with the current version. */
    public static final Version BACKWARDS_VERSION = new Version(0,0,0);
    
    static {
        if(VERSION.compareTo(BACKWARDS_VERSION) == -1)
            throw new AssertionError("The oldest game version we're "
                    + "backwards-compatible with (" + BACKWARDS_VERSION
                    + ") is somehow newer than our current version ("
                    + VERSION + ")!");
    }
    
    /** The number of game ticks each second. */
    public static final int TICKS_PER_SECOND = 60;
    
    // Gameplay ---------------------------------------------------------------
    
    /** The port which the game will be hosted on by default. Note it is
     * completely arbitrary. */
    public static final int DEFAULT_PORT = 8224;
    
    /** True if different dimensions of the world should be hosted on different
     * threads if possible. */ // This may be temporary
    public static final boolean CONCURRENT_DIMENSIONS = true;
    
    public static final int LOADED_TILE_RADIUS = 6 * 16;
    public static final int LOADED_TILE_BUFFER = 3 * 16;
    /** The half-length of an edge of the square of loaded slices around the
     * player. */
    public static final int LOADED_SLICE_RADIUS = LOADED_TILE_RADIUS / Slice.SLICE_SIZE;
    /** The buffer length of loaded slices. */
    public static final int LOADED_SLICE_BUFFER = LOADED_TILE_BUFFER / Slice.SLICE_SIZE;
    /** The minimum number of slices which can be considered 'loaded' about a
     * player at any given time. */
    public static final int MIN_LOADED_SLICES =
            // Manually square this to make it a compile-time constant.
            (1 + 2 * LOADED_SLICE_RADIUS) * (1 + 2 * LOADED_SLICE_RADIUS);
    /** The maximum number of slices which should theoretically be able to be
     * loaded about a player at any given time. */
    public static final int MAX_LOADED_SLICES =
            // Manually square this to make it a compile-time constant.
            (1 + 2 * (LOADED_SLICE_RADIUS + LOADED_SLICE_BUFFER)) *
            (1 + 2 * (LOADED_SLICE_RADIUS + LOADED_SLICE_BUFFER));
    /** How many ticks after coming out of use that a region should unload. */
    public static final int REGION_UNLOAD_TICK_BUFFER = 10 * TICKS_PER_SECOND;
    
    /** How large a character's inventory is. */
    public static final int INVENTORY_CAPACITY = 36;
    /** The number of items on the hotbar. */
    public static final int HOTBAR_SIZE = 9;
    /** Maximum item stack size. */
    public static final int MAX_STACK_SIZE = 999;
    
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A {@code Version} object may be used to store the version data of an
     * application. A version takes the form {@code
     * release.major_patch.minor_patch} - e.g., <i>0.1.3</i>, <i>1.0.0</i>,
     * <i>2.10.172</i>.
     */
    @Immutable
    public static final class Version implements Comparable<Version> {
        
        private static final String[] tags = { "rel", "pMjr", "pMnr", "bld", "rev" };
        
        /** release, patchMajor, patchMinor, build, revision */
        private final int[] data = new int[5];
        private String str; // lazy-initialised toString value
        
        public Version(int release, int patchMajor, int patchMinor) {
            data[0] = release;
            data[1] = patchMajor;
            data[2] = patchMinor;
            data[3] = data[4] = -1;
        }
        
        /**
         * Constructor to be used solely by the Constants class. Defines an
         * additional {@code build} value, which is used (in developer versions
         * only) to distinguish different builds of the same minor patch. A
         * further degree of granularity - {@code revision} - is offered, which
         * simply automatically counts the quantity of times the current build
         * has been compiled.
         */
        private Version(int release, int patchMajor, int patchMinor, int build) {
            data[0] = release;
            data[1] = patchMajor;
            data[2] = patchMinor;
            data[3] = build;
            data[4] = getRevision();
        }
        
        public  int release()    { return data[0]; }
        public  int patchMajor() { return data[1]; }
        public  int patchMinor() { return data[2]; }
        private int build()      { return data[3]; }
        private int revision()   { return data[4]; }
        
        /**
         * Returns 1 if this is a newer version than v, 0 if this and v are the
         * same version, and -1 if this is an older version than v.
         */
        @Override
        public int compareTo(Version v) {
            if(release() > v.release()) return 1;
            if(release() < v.release()) return -1;
            if(patchMajor() > v.patchMajor()) return 1;
            if(patchMajor() < v.patchMajor()) return -1;
            if(patchMinor() > v.patchMinor()) return 1;
            if(patchMinor() < v.patchMinor()) return -1;
            // We don't care about build and revision numbers unless we're
            // comparing strictly dev versions.
            if(build() == -1 || v.build() == -1) return 0;
            if(build() > v.build()) return 1;
            if(build() < v.build()) return -1;
            if(revision() > v.revision()) return 1;
            if(revision() < v.revision()) return -1;
            return 0;
        }
        
        /**
         * Returns {@code true} if this version precedes {@code v} - i.e. if
         * this is an earlier version than v (alternatively, if {@link
         * #compareTo(Version) compareTo(v) == -1}).
         */
        public boolean precedes(Version v) {
            return compareTo(v) == -1;
        }
        
        private String genStr() {
            return release() + "." + patchMajor() + "." + patchMinor()
                    + (DEV_VERSION
                       ? " (build " + build() + ", rev " + revision() + ")"
                       : "");
        }
        
        @Override
        public String toString() {
            return str == null ? str = genStr() : str;
        }
        
        @Override
        public int hashCode() {
            throw new AssertionError("hashCode not designed");
        }
        
        @Override
        public boolean equals(Object o) {
            if(!(o instanceof Version)) return false;
            return compareTo((Version)o) == 0;
        }
        
        // --------------------------------------------------------------------
        
        /**
         * Gets the build revision number, which represents the number of times
         * the current build has been run with {@link #DEV_VERSION} set to
         * {@code true} on the current system. This is for dev versions only.
         * 
         * @return {@code 0} if {@code DEV_VERSION} is {@code false}; otherwise,
         * the build revision number.
         */
        private int getRevision() {
            if(DEV_VERSION) {
                FileHandle file = Resources.DIR_APP.child("revision");
                try {
                    DataCompound tag = null;
                    
                    if(file.exists()) {
                        tag = Try.of(() -> IOUtil.read(Format.NBT, Compression.UNCOMPRESSED, file))
                                .recover(e -> {
                            Log.get().postWarning("Couldn't load revision file", e);
                            return new NBTCompound();
                        }).get();
                    } else {
                        tag = new NBTCompound();
                    }
                    
                    tag.put("comp", tag.getInt("comp") + 1);
                    int buildCompilations = buildTags(tag, 0);
                    
                    IOUtil.writeSafe(tag, Format.NBT, Compression.UNCOMPRESSED, file);
                    
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
        private int buildTags(DataCompound parent, int i) {
            DataCompound tag = parent.createCompound(tags[i] + data[i]);
            tag.put("comp", tag.getInt("comp") + 1);
            if(++i < data.length) // we are not build
                return buildTags(tag, i); // recursively get children
            else // we are build!
                return tag.getInt("comp");
        }
        
    }
    
}
