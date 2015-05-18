package com.stabilise.core;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.Immutable;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;

/**
 * This class contains all of the important universal game constants.
 */
public class Constants {
	
	// non-instantiable
	private Constants() {}
	
	// Primary Application Data -----------------------------------------------
	
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
	
	/** true if different dimensions of the world should be hosted on different
	 * threads. */ // This may be temporary
	public static final boolean CONCURRENT_DIMENSIONS = true;
	
	/** The half-length of an edge of the square of loaded slices around the
	 * player. */
	public static final int LOADED_SLICE_RADIUS = 6;
	/** The buffer length of loaded slices. Unused. */
	public static final int LOADED_SLICE_BUFFER = 3;
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
	public static final int MAX_STACK_SIZE = 99;
	
	
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
	public static class Version implements Comparable<Version> {
		
		public final int release, patchMajor, patchMinor;
		private final int build, revision;
		private String str; // lazy-initialised toString value
		
		public Version(int release, int patchMajor, int patchMinor) {
			this.release = release;
			this.patchMajor = patchMajor;
			this.patchMinor = patchMinor;
			build = revision = -1;
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
			this.release = release;
			this.patchMajor = patchMajor;
			this.patchMinor = patchMinor;
			this.build = build;
			this.revision = getRevision();
		}
		
		/**
		 * Returns 1 if this is a newer version than v, 0 if this and v are the
		 * same version, and -1 if this is an older version than v.
		 */
		@Override
		public int compareTo(Version v) {
			if(release > v.release) return 1;
			if(release < v.release) return -1;
			if(patchMajor > v.patchMajor) return 1;
			if(patchMajor < v.patchMajor) return -1;
			if(patchMinor > v.patchMinor) return 1;
			if(patchMinor < v.patchMinor) return -1;
			// We don't care about build and revision numbers unless we're
			// comparing strictly dev versions.
			if(build == -1 || v.build == -1) return 0;
			if(build > v.build) return 1;
			if(build < v.build) return -1;
			if(revision > v.revision) return 1;
			if(revision < v.revision) return -1;
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
			return release + "." + patchMajor + "." + patchMinor + (DEV_VERSION ?
					" (build " + build + ", rev " + revision + ")" : "");
		}
		
		@Override
		public String toString() {
			return str == null ? str = genStr() : str;
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
				FileHandle file = Resources.APP_DIR.child("revision");
				try {
					NBTTagCompound tag = null;
					
					try {
						tag = NBTIO.read(file);
					} catch(IOException e) {
						// File doesn't appear to exist - we'll have to create it.
						tag = new NBTTagCompound("versions");
						tag.addInt("compilations", 1);
						tag.addCompound("Release " + release, createReleaseTag(new NBTTagCompound()));
						NBTIO.write(file, tag);
						
						// Throw an exception so we skip the to the end of the method body
						throw new Exception("Revision tracker file not found - generated new one. ("
						+ e.getClass().getSimpleName() + ")");
					}
					
					NBTTagCompound releaseTag, patchMajorTag, patchMinorTag, buildTag;
					
					//System.out.println(tag.toString());
					
					tag.addInt("compilations", tag.getInt("compilations") + 1);
					
					int buildCompilations = 1;
					
					releaseTag = tag.getCompound("Release " + release);
					if(!releaseTag.isEmpty()) {
						releaseTag.addInt("compilations", releaseTag.getInt("compilations") + 1);
						patchMajorTag = releaseTag.getCompound("Major Patch " + patchMajor);
						if(!patchMajorTag.isEmpty()) {
							patchMajorTag.addInt("compilations", patchMajorTag.getInt("compilations") + 1);
							patchMinorTag = patchMajorTag.getCompound("Minor Patch " + patchMinor);
							if(!patchMinorTag.isEmpty()) {
								patchMinorTag.addInt("compilations", patchMinorTag.getInt("compilations") + 1);
								buildTag = patchMinorTag.getCompound("Build " + build);
								if(!buildTag.isEmpty()) {
									buildCompilations = buildTag.getInt("compilations") + 1;
									buildTag.addInt("compilations", buildCompilations);
								} else {
									buildTag = createBuildTag(new NBTTagCompound());
									patchMinorTag.addCompound(buildTag.getName(), buildTag);
								}
							} else {
								patchMinorTag = createPatchMinorTag(new NBTTagCompound());
								patchMajorTag.addCompound(patchMinorTag.getName(), patchMinorTag);
							}
						} else {
							patchMajorTag = createPatchMajorTag(new NBTTagCompound());
							releaseTag.addCompound(patchMajorTag.getName(), patchMajorTag);
						}
					} else {
						releaseTag = createReleaseTag(new NBTTagCompound());
						tag.addCompound(releaseTag.getName(), releaseTag);
					}
					
					NBTIO.safeWrite(file, tag);
					
					return buildCompilations;
				} catch(Exception e) {
					Log.get().postWarning("Could not get game revision: " + e.getMessage());
					return 1;
				}
			}
			
			return 0;
		}
		
		/**
		 * Creates an NBT tag compound for the current release for the revision
		 * tracker file.
		 */
		private NBTTagCompound createReleaseTag(NBTTagCompound releaseTag) {
			releaseTag.setName("Release " + release);
			releaseTag.addInt("compilations", 1);
			NBTTagCompound patchMajorTag = createPatchMajorTag(new NBTTagCompound());
			releaseTag.addCompound(patchMajorTag.getName(), patchMajorTag);
			return releaseTag;
		}
		
		/**
		 * Creates an NBT tag compound for the current major patch for the revision
		 * tracker file.
		 */
		private NBTTagCompound createPatchMajorTag(NBTTagCompound patchMajorTag) {
			patchMajorTag.setName("Major Patch " + patchMajor);
			patchMajorTag.addInt("compilations", 1);
			NBTTagCompound patchMinorTag = createPatchMinorTag(new NBTTagCompound());
			patchMajorTag.addCompound(patchMinorTag.getName(), patchMinorTag);
			return patchMajorTag;
		}
		
		/**
		 * Creates an NBT tag compound for the current minor patch for the revision
		 * tracker file.
		 */
		private NBTTagCompound createPatchMinorTag(NBTTagCompound patchMinorTag) {
			patchMinorTag.setName("Minor Patch " + patchMinor);
			patchMinorTag.addInt("compilations", 1);
			NBTTagCompound buildTag = createBuildTag(new NBTTagCompound());
			patchMinorTag.addCompound(buildTag.getName(), buildTag);
			return patchMinorTag;
		}
		
		/**
		 * Creates an NBT tag compound for the current build for the revision
		 * tracker file.
		 */
		private NBTTagCompound createBuildTag(NBTTagCompound buildTag) {
			buildTag.setName("Build " + build);
			buildTag.addInt("compilations", 1);
			return buildTag;
		}
		
	}
	
}
