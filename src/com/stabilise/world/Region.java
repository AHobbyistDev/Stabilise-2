package com.stabilise.world;

import static com.stabilise.core.Constants.REGION_UNLOAD_TICK_BUFFER;

import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.ClearOnIterateLinkedList;
import com.stabilise.util.maths.HashPoint;

/**
 * This class represents a region of the world, which contains 16x16 slices,
 * or 256x256 tiles.
 * 
 * <p>Regions are to slices as slices are to tiles; they provide a means of
 * storage and management.
 */
public class Region {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The length of an edge of the square of slices in a region. */
	public static final int REGION_SIZE = 16;
	/** {@link REGION_SIZE} - 1; minor optimisation purposes. */
	public static final int REGION_SIZE_MINUS_ONE = 15;
	/** The power of 2 of {@link REGION_SIZE}; minor optimisation purposes. */
	public static final int REGION_SIZE_SHIFT = 4;
	/** The length of an edge of the square of tiles in a region. */
	public static final int REGION_SIZE_IN_TILES = 256;//Slice.SLICE_SIZE * REGION_SIZE;
	/** {@link REGION_SIZE_IN_TILES} - 1; minor optimisation purposes. */
	public static final int REGION_SIZE_IN_TILES_MINUS_ONE = 255;
	/** The power of 2 of {@link REGION_SIZE_IN_TILES}; minor optimisation
	 * purposes. */
	public static final int REGION_SIZE_IN_TILES_SHIFT = 8;
	
	/** A dummy Region object to use when a Region object is required for API
	 * reasons but isn't actually used. This region's {@link #loc} member will
	 * return {@code false} for all {@code equals()}. */
	public static final Region DUMMY_REGION = new Region();
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The number of ticks until this region should be unloaded. {@code -1}
	 * indicates that this region is still considered anchored and the unload
	 * countdown is not active. */
	private int ticksToUnload = -1;
	/** Whether or not the region should be unloaded from the world. */
	public boolean unload = false;
	/** The number of slices anchored due to having been loaded by a client
	 * within the region. Used to determine whether the region should begin the
	 * 'unload countdown'. */
	private final AtomicInteger anchoredSlices = new AtomicInteger(0);
	
	/** The slices contained by this region.
	 * <i>Note slices are indexed in the form <b>[y][x]</b>; {@link
	 * #getSliceAt(int, int)} provides such an accessor.</i> */
	public final Slice[][] slices = new Slice[REGION_SIZE][REGION_SIZE];
	
	/** The region's location, whose components are in region-lengths. This
	 * should be used as this region's key in any sort of Map implementation.
	 * 
	 * <p>An identical {@code HashPoint} object (for which {@code equals()}
	 * would return {@code true}) may be generated by {@link
	 * #getKey(int, int) Region.getKey(x, y)}, where {@code x} and {@code y}
	 * are equal to this {@code HashPoint}'s {@code x} and {@code y} members. */
	public final HashPoint loc;
	
	/** The coordinate offsets on the x and y-axes due to the coordinates of
	 * the region, in slice-lengths. */
	public final int offsetX, offsetY;
	
	/** Whether or not the region has been loaded. This is volatile. */
	public volatile boolean loaded = false;
	/** Whether or not the region is being loaded. This is volatile. */
	public volatile boolean loading = false;
	
	/** Whether or not the region has unsaved changes. */
	public boolean unsavedChanges = false;
	/** The time the region was last saved, in terms of the world age. */
	public long lastSaved;
	/** Whether or not the region is pending a save. This is volatile. */
	public volatile boolean pendingSave = false;
	/** Whether or not the region is currently saving. This is volatile. */
	public volatile boolean saving = false;
	
	/** Whether or not the region has been generated. This is volatile. */
	public volatile boolean generated = false;
	/** The slices to send to clients once the region has finished generating. */
	//private List<QueuedSlice> queuedSlices;
	
	/** The structures queued to be added to the region. This is a final
	 * ArrayList. Access to this is usually performed while synchronised on
	 * itself.
	 * <p>For world generator use only.
	 * <p><i>Design specifications:</i> This is a ClearOnIterateLinkedList;
	 * add() is O(1) and toArray() is O(n) and clears the list. */
	public final ClearOnIterateLinkedList<QueuedSchematic> queuedSchematics =
			new ClearOnIterateLinkedList<>();
	
	/** The object to use for locking purposes restricted to the WorldGenerator
	 * and WorldLoader. */
	private final Object lock = new Object();
	
	
	/**
	 * Private since this creates an ordinarily-invalid region.
	 */
	private Region() {
		loc = new HashPoint() {
			public boolean equals(Object o) { return false; }
			public boolean equals(int x, int y) { return false; }
		};
		offsetX = 0;
		offsetY = 0;
	}
	
	/**
	 * Creates a new region.
	 * 
	 * @param x The region's x-coordinate, in region lengths.
	 * @param y The region's y-coordinate, in region lengths.
	 * @param worldAge The age of the world.
	 * 
	 * @throws NullPointerException if {@code world} is {@code null}.
	 */
	public Region(int x, int y, long worldAge) {
		this(getKey(x, y), worldAge);
	}
	
	/**
	 * Creates a new region.
	 * 
	 * @param loc The region's location, whose coordinates are in
	 * region-lengths.
	 * @param worldAge The age of the world.
	 * 
	 * @throws NullPointerException if {@code loc} is {@code null}.
	 */
	public Region(HashPoint loc, long worldAge) {
		this.loc = loc;
		
		offsetX = loc.x * REGION_SIZE;
		offsetY = loc.y * REGION_SIZE;
		
		lastSaved = worldAge;
	}
	
	/**
	 * Updates the region.
	 * 
	 * @param world This region's parent world.
	 */
	public void update(HostWorld world) {
		// If the region is not generated ignore updates
		if(!loaded)
			return;
		
		if(anchoredSlices.get() == 0) {
			if(ticksToUnload > 0)
				ticksToUnload--;
			else if(ticksToUnload == -1)
				ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
			else
				unload = true;
		} else {
			ticksToUnload = -1;
			
			// Tick any number of random tiles in the region each tick
			tickTile(world);
			//tickTile(world);
			//tickTile(world);
			//tickTile(world);
			
			// Save at 30 second intervals if possible
			// TODO: Potential future problems in the fact that regions which have
			// not been saved for 30 seconds will save the very moment they are
			// modified, and as such it may become the case that every loaded region
			// rests on the brink of triggering a save, which may possibly be
			// problematic if something causes at least one tile in each region to
			// be updated simultaneously
			if(unsavedChanges && world.getAge() - lastSaved > 1800)
				world.saveRegion(this);
		}
	}
	
	/**
	 * Updates a random tile within the region.
	 * 
	 * <p>Given there are 65536 tiles in a region, a tile will, on average, be
	 * updated once every 18 minutes if this is invoked once per tick.
	 */
	private void tickTile(HostWorld world) {
		int sx = world.rng.nextInt(REGION_SIZE);
		int sy = world.rng.nextInt(REGION_SIZE);
		int tx = world.rng.nextInt(Slice.SLICE_SIZE);
		int ty = world.rng.nextInt(Slice.SLICE_SIZE);
		getSliceAt(sx, sy).getTileAt(tx, ty).update(world, (offsetX + sx) * Slice.SLICE_SIZE + tx, (offsetY + sy) * Slice.SLICE_SIZE + ty);
	}
	
	/** 
	 * Gets a slice at the specified coordinates.
	 * 
	 * @param x The x-coordinate of the slice relative to the region, in slice
	 * lengths.
	 * @param y The y-coordinate of the slice relative to the region, in slice
	 * lengths.
	 * 
	 * @return The slice.
	 * @throws ArrayIndexOutOfBoundsException if either {@code x} or {@code y}
	 * are less than 0 or greater than 15.
	 */
	public Slice getSliceAt(int x, int y) {
		return slices[y][x];
	}
	
	/**
	 * Anchors a slice as 'loaded'. A region which has anchored slices should
	 * not be unloaded under standard circumstances.
	 * 
	 * <p>Anchored slices will not be reset when a region is loaded or
	 * generated.
	 * 
	 * <p>This method is thread-safe.
	 */
	@UserThread({"MainThread", "WorldGenThread"})
	public void anchorSlice() {
		anchoredSlices.getAndIncrement();
	}
	
	/**
	 * De-anchors a slice; it is no longer loaded. This method is the reverse
	 * of {@link #anchorSlice()}, and invocations of these methods should be
	 * paired to ensure an equilibrium.
	 * 
	 * <p>This method is thread-safe.
	 */
	public void deAnchorSlice() {
		anchoredSlices.getAndDecrement();
	}
	
	/**
	 * Gets the number of slices anchored in the region. The returned result is
	 * equivalent to the number of times {@link #anchorSlice()} has been
	 * invoked minus the number of times {@link #deAnchorSlice()} has been
	 * invoked.
	 * 
	 * <p>This method is thread-safe.
	 * 
	 * @return The number of anchored slices.
	 */
	public int getAnchoredSlices() {
		return anchoredSlices.get();
	}
	
	/**
	 * @param world This region's parent world.
	 * 
	 * @return This region's file.
	 */
	public FileHandle getFile(HostWorld world) {
		return world.getWorldDir().child("r_" + loc.x + "_" + loc.y + ".region");
	}
	
	/**
	 * Checks for whether or not this region's file exists.
	 * 
	 * @param world This region's parent world.
	 * 
	 * @return {@code true} if this region has a saved file; {@code false}
	 * otherwise.
	 */
	public boolean fileExists(HostWorld world) {
		return getFile(world).exists();
	}
	
	/**
	 * Queues a slice to send to a client once world generation has been
	 * completed.
	 * 
	 * @param clientHash The hash of the client to send the slice to.
	 * @param sliceX The x-coordinate of the slice, in slice-lengths.
	 * @param sliceY The y-coordinate of the slice, in slice-lengths.
	 * 
	 * @deprecated Due to removal of networking architecture.
	 */
	/*
	public void queueSlice(int clientHash, int sliceX, int sliceY) {
		if(queuedSlices == null)
			queuedSlices = new ArrayList<QueuedSlice>();
		
		queuedSlices.add(new QueuedSlice(clientHash, sliceX, sliceY));
	}
	*/
	
	/**
	 * Queues a schematic for later generation.
	 * 
	 * @param schematicName The name of the schematic to queue.
	 * @param sliceX The x-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param sliceY The y-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param tileX The x-coordinate of the tile in which to place the 
	 * schematic, relative to the slice in which it is in, in tile-lengths.
	 * @param tileY The y-coordinate of the tile in which to place the
	 * schematic, relative to the slice in which it is in, in tile-lengths.
	 * @param offsetX The x-offset of the schematic, in region-lengths.
	 * @param offsetY The y-offset of the schematic, in region-lengths.
	 */
	@NotThreadSafe
	public void queueSchematic(String schematicName, int sliceX, int sliceY, int tileX, int tileY, int offsetX, int offsetY) {
		queuedSchematics.add(new QueuedSchematic(schematicName, sliceX, sliceY, tileX, tileY, offsetX, offsetY));
	}
	
	/**
	 * Returns {@code true} if this region has queued schematics; {@code false}
	 * otherwise.
	 */
	@NotThreadSafe
	public boolean hasQueuedSchematics() {
		return queuedSchematics.size() != 0;
	}
	
	/**
	 * Checks for whether or not this region is considered generated.
	 * 
	 * @return {@code true} if this region does not need to be generated;
	 * {@code false} if it does.
	 */
	public boolean isGenerated() {
		return generated && !hasQueuedSchematics();
	}
	
	/**
	 * Adds any entities and tile entities contained by the region to the
	 * world.
	 * 
	 * <p>Unused.
	 */
	public void addContainedEntitiesToWorld(HostWorld world) {
		for(int r = 0; r < REGION_SIZE; r++)
			for(int c = 0; c < REGION_SIZE; c++)
				slices[r][c].addContainedEntitiesToWorld(world);
	}
	
	/**
	 * Blocks the current thread until this region has loaded. If this thread
	 * is interrupted while waiting, the interrupt status flag will be set when
	 * this method returns.
	 * 
	 * <p>This method is intended for WorldGenerator use only.
	 */
	@UserThread("WorldGenThread")
	public void waitUntilLoaded() {
		if(loaded)
			return;
		boolean interrupted = false;
		try {
			synchronized(lock) {
				while(!loaded) { // TODO: what if the world loader fails or aborts?
					try {
						lock.wait();
					} catch(InterruptedException e) {
						interrupted = true;
					}
				}
			}
		} finally {
			if(interrupted)
				Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Notifies other threads waiting on {@link #waitUntilLoaded()} that this
	 * region has been loaded.
	 * 
	 * <p>This method is intended for WorldLoader use only.
	 */
	@UserThread("WorldLoaderThread")
	public void notifyOfLoaded() {
		synchronized(lock) {
			loaded = true;
			lock.notifyAll();
		}
	}
	
	/**
	 * Gets this region's hash code. The returned value is given as if by
	 * {@link #loc}{@code .hashCode()}.
	 */
	@Override
	public int hashCode() {
		return loc.hashCode();
	}
	
	@Override
	public String toString() {
		return "Region[" + loc.x + "," + loc.y + "]";
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The QueuedSlice class contains information about a slice queued to be
	 * sent to a client while a region is generating.
	 * 
	 * @deprecated Due to the removal of networking architecture.
	 */
	@SuppressWarnings("unused")
	private static class QueuedSlice {
		
		/** The hash of the client to send the slice to. */
		private int clientHash;
		/** The x-coordinate of the slice, in slice-lengths. */
		private int sliceX;
		/** The y-coordinate of the slice, in slice-lengths. */
		private int sliceY;
		
		
		/**
		 * Creates a new queued slice.
		 * 
		 * @param clientHash The hash of the client to send the slice to.
		 * @param sliceX The x-coordinate of the slice, in slice-lengths.
		 * @param sliceY The y-coordinate of the slice, in slice-lengths.
		 */
		private QueuedSlice(int clientHash, int sliceX, int sliceY) {
			this.clientHash = clientHash;
			this.sliceX = sliceX;
			this.sliceY = sliceY;
		}
		
	}
	
	/**
	 * The QueuedSchematic class contains information about a schematic queued
	 * to be generated within the region.
	 */
	public static class QueuedSchematic {
		
		/** The name of the schematic queued to be added. */
		public String schematicName;
		/** The x-coordinate of the slice in which to place the schematic,
		 * relative to the region, in slice-lengths. */
		public int sliceX;
		/** The y-coordinate of the slice in which to place the schematic,
		 * relative to the region, in slice-lengths. */
		public int sliceY;
		/** The x-coordinate of the tile in which to place the schematic,
		 * relative to the slice in which it is in, in tile-lengths. */
		public int tileX;
		/** The y-coordinate of the tile in which to place the schematic,
		 * relative to the slice in which it is in, in tile-lengths. */
		public int tileY;
		/** The x-offset of the schematic, in region-lengths. */
		public int offsetX;
		/** The y-offset of the schematic, in region-lengths. */
		public int offsetY;
		
		
		/**
		 * Creates a new QueuedSchematic.
		 */
		public QueuedSchematic() {
			// nothing to see here, move along
		}
		
		/**
		 * Creates a new QueuedSchematic.
		 * 
		 * @param schematicName The name of the schematic queued to be added.
		 * @param sliceX The x-coordinate of the slice in which to place the
		 * schematic, relative to the region, in slice-lengths.
		 * @param sliceY The y-coordinate of the slice in which to place the
		 * schematic, relative to the region, in slice-lengths.
		 * @param tileX The x-coordinate of the tile in which to place the 
		 * schematic, relative to the slice in which it is in, in tile-lengths.
		 * @param tileY The y-coordinate of the tile in which to place the
		 * schematic, relative to the slice in which it is in, in tile-lengths.
		 * @param offsetX The x-offset of the schematic, in region-lengths.
		 * @param offsetY The y-offset of the schematic, in region-lengths.
		 */
		public QueuedSchematic(String schematicName, int sliceX, int sliceY, int tileX, int tileY, int offsetX, int offsetY) {
			this.schematicName = schematicName;
			this.sliceX = sliceX;
			this.sliceY = sliceY;
			this.tileX = tileX;
			this.tileY = tileY;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Returns a HashPoint to use for referencing a region. The returned point
	 * is equivalent to a region with the same coordinates' {@link Region#loc
	 * loc} member.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The key to use for a region of the given coordinates.
	 */
	public static HashPoint getKey(int x, int y) {
		return new HashPoint(x, y);
	}
	
}
