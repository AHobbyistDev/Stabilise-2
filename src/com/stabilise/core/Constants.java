package com.stabilise.core;

import java.io.File;
import java.io.IOException;

import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;

/**
 * This class contains all of the important universal game constants.
 */
public class Constants {
	
	/** This should be false if the application will be compiled into a public-
	 * release version. */
	public static final boolean DEV_VERSION = true;
	
	/** The port which the game will be hosted on by default. Note it is
	 * completely arbitrary. */
	public static final int DEFAULT_PORT = 8224;
	
	/** The release version. (X...)*/
	public static final int RELEASE = 0;
	/** The major patch version. (x.X...) */
	public static final int PATCH_MAJOR = 0;
	/** The minor patch version. (x.x.X...) */
	public static final int PATCH_MINOR = 0;
	/** The minor patch build. (x.x.x.X...) */
	public static final int BUILD = 0;
	/** The build revision. (x.x.x.x.X) For developer's use only. */
	public static final int REVISION = getRevision();
	/** The game version. It is the stitched-together form of
	 * RELEASE.PATCH_MAJOR.PATCH_MINOR.BUILD.REVISION.
	 * At most only the former 3 should be publicly visible. */
	public static final String VERSION = getVersion();
	
	/** The number of game ticks each second. */
	public static final int TICKS_PER_SECOND = 60;
	
	/** The half-length of an edge of the square of loaded slices around the
	 * player. */
	public static final int LOADED_SLICE_RADIUS = 4;
	/** The buffer length of loaded slices. Unused. */
	public static final int BUFFER_LENGTH = 2;
	/** How many ticks after coming out of use that a region should unload. */
	public static final int REGION_UNLOAD_TICK_BUFFER = 10 * TICKS_PER_SECOND;
	
	/** How large a character's inventory is. */
	public static final int INVENTORY_CAPACITY = 36;
	/** How large the player's hotbar is. */
	public static final int HOTBAR_SIZE = 9;
	/** Maximum item stack size. */
	public static final int MAX_STACK_SIZE = 99;
	
	
	/**
	 * Gets the build revision number, which represents the number of times
	 * the current build has been run with {@link #DEV_VERSION} set to
	 * {@code true} on the current system. This is for dev versions only.
	 * 
	 * @return {@code 0} if {@code DEV_VERSION} is {@code false}; otherwise,
	 * the build revision number.
	 */
	private static int getRevision() {
		if(DEV_VERSION) {
			IOUtil.createDirQuietly(Resources.APP_DIR);
			File file = new File(Resources.APP_DIR, "revision");
			try {
				NBTTagCompound tag = null;
				
				try {
					tag = NBTIO.read(file);
				} catch(IOException e) {
					// File doesn't appear to exist - we'll have to create it.
					tag = new NBTTagCompound("versions");
					tag.addInt("compilations", 1);
					tag.addCompound("Release " + RELEASE, createReleaseTag());
					NBTIO.write(file, tag);
					
					// Throw an exception so we skip the to the end of the method body
					throw new Exception("Revision tracker file not found - generated new one. (" + e.getClass().getSimpleName() + ")");
				}
				
				NBTTagCompound release, patchMajor, patchMinor, build;
				
				//System.out.println(tag.toString());
				
				tag.addInt("compilations", tag.getInt("compilations") + 1);
				
				int buildCompilations = 1;
				
				release = tag.getCompound("Release " + RELEASE);
				if(release != null) {
					release.addInt("compilations", release.getInt("compilations") + 1);
					patchMajor = release.getCompound("Major Patch " + PATCH_MAJOR);
					if(patchMajor != null) {
						patchMajor.addInt("compilations", patchMajor.getInt("compilations") + 1);
						patchMinor = patchMajor.getCompound("Minor Patch " + PATCH_MINOR);
						if(patchMinor != null) {
							patchMinor.addInt("compilations", patchMinor.getInt("compilations") + 1);
							build = patchMinor.getCompound("Build " + BUILD);
							if(build != null) {
								buildCompilations = build.getInt("compilations") + 1;
								build.addInt("compilations", buildCompilations);
							} else {
								build = createBuildTag();
								patchMinor.addCompound(build.getName(), build);
							}
						} else {
							patchMinor = createPatchMinorTag();
							patchMajor.addCompound(patchMinor.getName(), patchMinor);
						}
					} else {
						patchMajor = createPatchMajorTag();
						release.addCompound(patchMajor.getName(), patchMajor);
					}
				} else {
					release = createReleaseTag();
					tag.addCompound(release.getName(), release);
				}
				
				NBTIO.safeWrite(file, tag);
				
				return buildCompilations;
			} catch(Exception e) {
				Log.critical("Could not get game revision", e);
				return 1;
			}
		}
		
		return 0;
	}
	
	/**
	 * Creates an NBT tag compound for the current release for the revision
	 * tracker file.
	 * 
	 * @return The release tag compound.
	 */
	private static NBTTagCompound createReleaseTag() {
		NBTTagCompound release = new NBTTagCompound("Release " + RELEASE);
		release.addInt("compilations", 1);
		NBTTagCompound patchMajor = createPatchMajorTag();
		release.addCompound(patchMajor.getName(), patchMajor);
		return release;
	}
	
	/**
	 * Creates an NBT tag compound for the current major patch for the revision
	 * tracker file.
	 * 
	 * @return The major patch tag compound.
	 */
	private static NBTTagCompound createPatchMajorTag() {
		NBTTagCompound patchMajor = new NBTTagCompound("Major Patch " + PATCH_MAJOR);
		patchMajor.addInt("compilations", 1);
		NBTTagCompound patchMinor = createPatchMinorTag();
		patchMajor.addCompound(patchMinor.getName(), patchMinor);
		return patchMajor;
	}
	
	/**
	 * Creates an NBT tag compound for the current minor patch for the revision
	 * tracker file.
	 * 
	 * @return The minor patch tag compound.
	 */
	private static NBTTagCompound createPatchMinorTag() {
		NBTTagCompound patchMinor = new NBTTagCompound("Minor Patch " + PATCH_MINOR);
		patchMinor.addInt("compilations", 1);
		NBTTagCompound build = createBuildTag();
		patchMinor.addCompound(build.getName(), build);
		return patchMinor;
	}
	
	/**
	 * Creates an NBT tag compound for the current build for the revision
	 * tracker file.
	 * 
	 * @return The build tag compound.
	 */
	private static NBTTagCompound createBuildTag() {
		NBTTagCompound build = new NBTTagCompound("Build " + BUILD);
		build.addInt("compilations", 1);
		return build;
	}
	
	/**
	 * Gets the game version, in string form.
	 * 
	 * @return The game version.
	 */
	private static String getVersion() {
		if(DEV_VERSION)
			return RELEASE + "." + PATCH_MAJOR + "." + PATCH_MINOR + " (build " + BUILD + ", rev " + REVISION + ")";
		else
			return RELEASE + "." + PATCH_MAJOR + "." + PATCH_MINOR;
	}
	
	// non-instantiable
	private Constants() {}
	
}
