package com.stabilise.world;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.entity.Position;
import com.stabilise.util.annotation.ThreadSafeMethod;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Point;
import com.stabilise.util.maths.PointFactory;
import com.stabilise.world.gen.action.Action;

/**
 * This class represents a region of the world, which contains 16x16 slices,
 * or 256x256 tiles.
 * 
 * <p>Regions are to slices as slices are to tiles; they provide a means of
 * storage and management.
 * 
 * <p>For implementation details on how regions are managed (saved, loaded,
 * generated, etc.) see {@link RegionStore}.
 */
public class Region {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The length of an edge of the square of slices in a region. */
    public static final int REGION_SIZE = 16; // must be a power of two
    /** {@link REGION_SIZE} - 1; minor optimisation purposes. */
    public static final int REGION_SIZE_MINUS_ONE = REGION_SIZE - 1;
    /** The power of 2 of {@link REGION_SIZE}; minor optimisation purposes. */
    public static final int REGION_SIZE_SHIFT = Maths.log2(REGION_SIZE);
    /** The length of an edge of the square of tiles in a region. */
    public static final int REGION_SIZE_IN_TILES = Slice.SLICE_SIZE * REGION_SIZE;
    /** {@link REGION_SIZE_IN_TILES} - 1; minor optimisation purposes. */
    public static final int REGION_SIZE_IN_TILES_MINUS_ONE = REGION_SIZE_IN_TILES - 1;
    /** The power of 2 of {@link REGION_SIZE_IN_TILES}; minor optimisation
     * purposes. */
    public static final int REGION_SIZE_IN_TILES_SHIFT = Maths.log2(REGION_SIZE_IN_TILES);
    
    /** A dummy Region object to use when a Region object is required for API
     * reasons but isn't actually used. This region's {@link #loc} member will
     * return {@code false} for all {@code equals()}. */
    //public static final Region DUMMY_REGION = new Region();
    
    /** The function to use to hash region coordinates for keys in a hash map. */
    // This method of hashing eliminates higher-order bits, but nearby regions
    // will never collide.
    private static final IntBinaryOperator COORD_HASHER = (x,y) ->
        (x << 16) | (y & 0xFFFF);
    
    /** The factory with which to generate a region's {@link #loc} member. */
    private static final PointFactory LOC_FACTORY = new PointFactory(16, true);
    
    /** Dummy region to indicate the lack of a region in preference to a null
     * pointer. */
    public static final Region DUMMY_REGION = new DummyRegion();
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The slices contained by this region.
     * <i>Note slices are indexed in the form <b>[y][x]</b>; {@link
     * #getSliceAt(int, int)} provides such an accessor.</i> */
    public final Slice[][] slices = new Slice[REGION_SIZE][REGION_SIZE];
    
    /** The region's location, whose components are in region-lengths. This
     * should be used as this region's key in any map implementation. This
     * object is always created by {@link #createImmutableLoc(int, int)}. */
    public final Point loc;
    
    /** The coordinate offsets on the x and y-axes due to the coordinates of
     * the region, in slice-lengths. */
    public final int offsetX, offsetY;
    
    
    public final RegionState state = new RegionState();
    
    
    /** Actions to perform when added to the world. */
    public List<Action> queuedActions = null;
    /** The slices to send to clients once the region has finished generating. */
    //private List<QueuedSlice> queuedSlices;
    
    /** When a structure is added to this region, it is placed in this queue.
     * structures may be added by both the main thread and the world generator. */
    private final ClearingQueue<QueuedStructure> structures =
            ClearingQueue.create();
    
    
    /**
     * Creates a new region.
     * 
     * @param x The region's x-coordinate, in region-lengths.
     * @param y The region's y-coordinate, in region-lengths.
     */
    Region(int x, int y) {
        loc = createImmutableLoc(x, y);
        
        offsetX = x * REGION_SIZE;
        offsetY = y * REGION_SIZE;
        
        initSlices();
    }
    
    /**
     * Initialises this slices in this region. Invoked on construction.
     */
    public void initSlices() {
        for(int y = 0; y < REGION_SIZE; y++) {
            for(int x = 0; x < REGION_SIZE; x++) {
                slices[y][x] = new Slice(x + offsetX, y + offsetY);
            }
        }
    }
    
    /**
     * Updates this region. This is invoked on a per-tick basis by the
     * {@link RegionStore} if this region is {@link RegionState#isActive()
     * active}.
     */
    public void update(HostWorld world) {
        // Tick any number of random tiles in the region each tick
        tickSlice(world, 4);
    }
    
    /**
     * Ticks {@code tiles}-many tiles in a random slice.
     */
    private void tickSlice(HostWorld world, int tiles) {
        int sx = world.rnd.nextInt(REGION_SIZE);
        int sy = world.rnd.nextInt(REGION_SIZE);
        Position tmp = Position.create();
        while(tiles-- > 0) {
            int tx = world.rnd.nextInt(Slice.SLICE_SIZE);
            int ty = world.rnd.nextInt(Slice.SLICE_SIZE);
            tmp.set(sx + offsetX, sy + offsetY, tx, ty);
            getSliceAt(sx, sy).getTileAt(tx, ty).update(world, tmp);
        }
    }
    
    /** 
     * Gets a slice at the specified coordinates.
     * 
     * @param x The x-coordinate of the slice relative to the region, in slice
     * lengths.
     * @param y The y-coordinate of the slice relative to the region, in slice
     * lengths.
     * 
     * @return The slice, or {@code null} if it has not been loaded yet.
     * @throws ArrayIndexOutOfBoundsException if either {@code x} or {@code y}
     * are less than 0 or greater than 15.
     */
    public Slice getSliceAt(int x, int y) {
        return slices[y][x];
    }
    
    /**
     * Performs the specified task on every slice in this region.
     * 
     * @throws NullPointerException if {@code task} is {@code null}.
     */
    public void forEachSlice(Consumer<Slice> task) {
        for(int y = 0; y < REGION_SIZE; y++)
            for(int x = 0; x < REGION_SIZE; x++)
                task.accept(slices[y][x]);
    }
    
    /**
     * Queues a structure for generation in this region.
     * 
     * @throws NullPointerException if {@code struct} is {@code null}.
     */
    @ThreadSafeMethod
    public void addStructure(QueuedStructure struct) {
        structures.add(Objects.requireNonNull(struct));
    }
    
    private void doAddStructure(QueuedStructure s, RegionStore regionStore) {
        //s.add(world);
    }
    
    /**
     * Returns {@code true} if this region has queued structures; {@code false}
     * otherwise.
     */
    @ThreadSafeMethod
    public boolean hasQueuedStructures() {
        return !structures.isEmpty();
    }
    
    /**
     * Gets the structures queued to be added to this region.
     */
    @ThreadSafeMethod
    public Iterable<QueuedStructure> getStructures() {
        return structures.asNonClearing();
    }
    
    /**
     * Implants all structures queued to be added to this region.
     */
    @ThreadUnsafeMethod
    public void implantStructures(RegionStore cache) {
        for(QueuedStructure s : structures) // clears the queue since ClearingQueue
            doAddStructure(s, cache);
    }
    
    /**
     * Imports this region's contents (e.g. entities, tile entities, lighting)
     * into the world, if this region hasn't been imported already.
     * 
     * <p>This is invoked during an update tick.
     */
    @UserThread("Main Thread")
    public void importToWorld(HostWorld world, RegionStore regions) {
    	if(!state.tryImport())
    		return;
    	
        forEachSlice(s -> {
            //s.buildLight();
            s.importEntities(world);
            s.importTileEntities(world);
        });
        if(queuedActions != null) {
            for(Action a : queuedActions)
                a.apply(world, this);
            queuedActions = null;
        }
        
        // Stitch this region to all adjacent loaded regions
        int x = x(), y = y();
        for(int u = x-1; u <= x+1; u++) {
            for(int v = y-1; v <= y+1; v++) {
                if(u == x && v == y) continue; // don't do it if other == r
                
                Region other = regions.getRegion(u, v);
                if(other != null)
                    stitch(other);
            }
        }
    }
    
    /**
     * "Stitches" this region to an adjacent prepared region, i.e., shares
     * lighting, etc.
     */
    private void stitch(Region r) {
        
    }
    
    /**
     * Exports this region's contents (e.g., entities, tile entities) from the
     * world, in preparation for this region being unloaded.
     */
    public void exportFromWorld(HostWorld world) {
    	state.setUnimported();
    	
    	// Unload entities in the region...
        int minX = x() * Region.REGION_SIZE_IN_TILES;
        int maxX = minX + Region.REGION_SIZE_IN_TILES;
        int minY = y() * Region.REGION_SIZE_IN_TILES;
        int maxY = minY + Region.REGION_SIZE_IN_TILES;
        
        world.getEntities().forEach(e -> {
            if(e.pos.gx() + e.aabb.maxX() >= minX
                    && e.pos.gx() + e.aabb.minX() <= maxX
                    && e.pos.gy() + e.aabb.maxY() >= minY
                    && e.pos.gy() + e.aabb.minY() <= maxY)
                e.destroy();
        });
    }
    
    /**
     * @return This region's x-coordinate, in region-lengths.
     */
    public int x() {
        return loc.x();
    }
    
    /**
     * @return This region's y-coordinate, in region-lengths.
     */
    public int y() {
        return loc.y();
    }
    
    /**
     * Returns {@code true} if this region's coords match the specified coords
     * (which are assumed to be given in region-lengths).
     */
    public boolean isAt(int x, int y) {
        return loc.equals(x, y);
    }
    
    /**
     * @param world This region's parent world.
     * 
     * @return This region's file.
     */
    public FileHandle getFile(HostWorld world) {
        return world.getWorldDir().child("r_" + x() + "_" + y() + ".region");
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
     * Gets this region's hash code.
     */
    @Override
    public int hashCode() {
        // Use the broader hash rather than the usual loc hash.
        return COORD_HASHER.applyAsInt(loc.x(), loc.y());
    }
    
    @Override
    public String toString() {
        return "Region[" + loc.x() + "," + loc.y() + "]";
    }
    
    /**
     * Returns a string representation of this Region, with some additional
     * debug information.
     */
    public String toStringDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Region[");
        sb.append(String.format("%3d", loc.x()));
        sb.append(',');
        sb.append(String.format("%3d", loc.y()));
        sb.append(": ");
        sb.append(state);
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Checks for whether or not this is a dummy region.
     */
    public boolean isDummy() {
        return false;
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates a {@code Point} object equivalent to a region with identical
     * coordinates' {@link #loc} member.
     */
    public static Point createImmutableLoc(int x, int y) {
        return LOC_FACTORY.newImmutablePoint(x, y);
    }
    
    /**
     * Creates a mutable variant of a point returned by {@link
     * #createImmutableLoc(int, int)}. This method should not be invoked
     * carelessly as the sole purpose of creating mutable points should be to
     * avoid needless object creation in scenarios where thread safety is
     * guaranteed.
     */
    public static Point createMutableLoc() {
        return LOC_FACTORY.newMutablePoint();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * The QueuedStructure class contains information about a structure queued
     * to be generated within the region.
     * 
     * <p>TODO: Namechange
     */
    public static class QueuedStructure {
        
        /** The name of the structure queued to be added. */
        public String structureName;
        /** The x/y-coordinates of the slice in which to place the structure,
         * relative to the region, in slice-lengths. */
        public int sliceX, sliceY;
        /** The x/y-coordinates of the tile in which to place the structure,
         * relative to the slice in which it is in, in tile-lengths. */
        public int tileX, tileY;
        /** The x/y-offset of the structure, in region-lengths. */
        public int offsetX, offsetY;
        
        
        /**
         * Creates a new Queuedstructure.
         */
        public QueuedStructure() {
            // nothing to see here, move along
        }
        
        /**
         * Creates a new Queuedstructure.
         */
        public QueuedStructure(String structureName, int sliceX, int sliceY, int tileX, int tileY, int offsetX, int offsetY) {
            this.structureName = structureName;
            this.sliceX = sliceX;
            this.sliceY = sliceY;
            this.tileX = tileX;
            this.tileY = tileY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
        
    }
    
    private static class DummyRegion extends Region {
        
        DummyRegion() {
            super(0, 0);
        }
        
        @Override
        public void update(HostWorld world) {
            throw new IllegalStateException("Dummy region is getting updated!");
        }
        
        @Override
        public Slice getSliceAt(int x, int y) {
            return Slice.DUMMY_SLICE;
        }
        
        @Override
        public boolean isDummy() {
            return true;
        }
        
    }
    
}
