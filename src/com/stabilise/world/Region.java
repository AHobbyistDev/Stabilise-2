package com.stabilise.world;

import static com.stabilise.core.Constants.REGION_UNLOAD_TICK_BUFFER;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Constants;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadSafeMethod;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.util.concurrent.Tasks;
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
 * <h3>Implementation Details</h3>
 * 
 * <h4>Saving</h4>
 * <p>We take a very loose approach to saving regions.
 * 
 * <!--wow such detail-->
 * <!--seriously though this section was wiped when I changed things up and
 * now needs rewriting-->
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
    
    /** The number of ticks until this region should be unloaded.  */
    private int ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
    /** The number of slices anchored due to having been loaded by a client
     * within the region. Used to determine whether the region should begin the
     * 'unload countdown'. */
    private int anchors = 0;
    
    /** Number of adjacent regions which are active. We do not unload a region
     * unless it has no active neighbours. */
    private int activeNeighbours = 0;
    
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
    
    /** The state of this region. See the documentation for {@link #State.NEW}
     * and all other states. */
    private final AtomicReference<State> state = new AtomicReference<>(State.NEW);
    /** Save state. Encapsulated in a Box so it can be safely passed off to
     * the lambda in getSavePermit(). */
    private final Box<SaveState> saveState = Boxes.box(SaveState.IDLE);
    /** Whether or not this region has been generated. */
    private boolean generated = false;
    /** Whether or not the entities & tile entities stored in this region have
     * been loaded into the world. This should be {@code false} until
     * isPrepared() returns true in the main thread, from which we set this to
     * true and add all the stuff in this region to the world. */
    private boolean imported = false;
    
    /** The time the region was last saved, in terms of the world age. */
    public long lastSaved;
    
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
     * @param x The region's x-coordinate, in region lengths.
     * @param y The region's y-coordinate, in region lengths.
     * @param worldAge The age of the world.
     */
    Region(int x, int y, long worldAge) {
        loc = createImmutableLoc(x, y);
        
        offsetX = x * REGION_SIZE;
        offsetY = y * REGION_SIZE;
        
        lastSaved = worldAge;
        
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
     * Updates the region.
     * 
     * @param world This region's parent world.
     * 
     * @return {@code true} if this region should be unloaded; {@code false}
     * if it should remain in memory.
     */
    public boolean update(HostWorld world, RegionStore store) {
        if(!isPrepared())
            return false;
        
        tryImport(world);
        
        if(activeNeighbours == 0) {
            // We unload the region if there's no neighbours keeping us around
            if(ticksToUnload-- == 0)
                return true;
        } else if(isAnchored()) {
            // Tick any number of random tiles in the region each tick
            tickSlice(world, 4);
            
            // Save the region at 64-second intervals.
            // Regions whose x and y coordinates are congruent modulo 8 are
            // saved simultaneously, but nearby regions are saved sequentially,
            // which distributes the IO overhead nicely.
            // N.B. loc.y & 7 == loc.y % 8
            if(world.getAge() % (8*8 * Constants.TICKS_PER_SECOND) ==
                    (((y() & 7) * 8 + (x() & 7)) * Constants.TICKS_PER_SECOND))
                world.saveRegion(this);
        }
        
        implantStructures(store);
        
        return false;
    }
    
    /**
     * Updates a random tile within the region.
     * 
     * <p>Given there are 65536 tiles in a region, a tile will, on average, be
     * updated once every 18 minutes if this is invoked once per tick.
     */
    @SuppressWarnings("unused")
    private void tickTile(HostWorld world) {
        int sx = world.rnd.nextInt(REGION_SIZE);
        int sy = world.rnd.nextInt(REGION_SIZE);
        int tx = world.rnd.nextInt(Slice.SLICE_SIZE);
        int ty = world.rnd.nextInt(Slice.SLICE_SIZE);
        getSliceAt(sx, sy).getTileAt(tx, ty).update(world,
                (offsetX + sx) * Slice.SLICE_SIZE + tx,
                (offsetY + sy) * Slice.SLICE_SIZE + ty);
    }
    
    /**
     * Ticks {@code tiles}-many tiles in a random slice.
     */
    private void tickSlice(HostWorld world, int tiles) {
        int sx = world.rnd.nextInt(REGION_SIZE);
        int sy = world.rnd.nextInt(REGION_SIZE);
        while(tiles-- > 0) {
            int tx = world.rnd.nextInt(Slice.SLICE_SIZE);
            int ty = world.rnd.nextInt(Slice.SLICE_SIZE);
            getSliceAt(sx, sy).getTileAt(tx, ty).update(world,
                    (offsetX + sx) * Slice.SLICE_SIZE + tx,
                    (offsetY + sy) * Slice.SLICE_SIZE + ty);
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
     * @param task The task.
     * 
     * @throws NullPointerException if {@code task} is {@code null}.
     */
    public void forEachSlice(Consumer<Slice> task) {
        for(int y = 0; y < REGION_SIZE; y++)
            for(int x = 0; x < REGION_SIZE; x++)
                task.accept(slices[y][x]);
    }
    
    /**
     * Anchors this region. An anchored region remains loaded, and additionally
     * loads its neighbours.
     * 
     * <p>Anchors will not be reset when a region is loaded or generated.
     * 
     * @return {@code true} if this region is newly-anchored.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    boolean anchor() {
        return anchors++ == 0;
    }
    
    /**
     * Removes an anchor from this region. This method is the inverse of {@link
     * #anchor()}, and invocations of these methods should be paired to ensure
     * an eventual equilibrium.
     * 
     * @return {@code true} if this region is no longer anchored as of this
     * method call.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    boolean deAnchor() {
        return --anchors == 0;
    }
    
    /**
     * Returns {@code true} if this region is anchored/active, and may be
     * updated.
     */
    private boolean isAnchored() {
        return anchors > 0;
    }
    
    /**
     * Informs this region that is has an anchored neighbour.
     */
    void addNeighbour() {
        activeNeighbours++;
    }
    
    /**
     * Informs this region that one of its neighbours has been de-anchored.
     */
    void removeNeighbour() {
        if(--activeNeighbours == 0)
            ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
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
     * Queues a structure for generation in this region.
     * 
     * @throws NullPointerException if {@code struct} is {@code null}.
     */
    @ThreadSafeMethod
    public void addStructure(QueuedStructure struct) {
        structures.add(Objects.requireNonNull(struct));
    }
    
    private void doAddStructure(QueuedStructure s, RegionStore regionCache) {
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
        for(QueuedStructure s : structures) // clears the queue
            doAddStructure(s, cache);
    }
    
    /**
     * Returns {@code true} if this region has been prepared and may be safely
     * used.
     */
    public boolean isPrepared() {
        return state.get().equals(State.PREPARED);
    }
    
    /**
     * Checks for whether or not this region has been generated.
     */
    public boolean isGenerated() {
        return generated;
    }
    
    /**
     * Marks this region as generated, and induces an appropriate state change.
     * This is invoked in two scenarios:
     * 
     * <ul>
     * <li>When the WorldLoader finishes loading this region and finds it to be
     *     generated.
     * <li>When the WorldGenerator finishes generating this region.
     * </ul>
     */
    public void setGenerated() {
        generated = true;
        
        // This method is invoked in two scenarios:
        // 
        // 1: When this region is loaded by the WorldLoader, and it finds that
        //    this region has already been generated. From here, there are two
        //    options:
        // 
        //    a: There are queued structures. We remain in State.LOADING so
        //       that getGenerationPermit() returns true so that the world
        //       generator can generate those structures concurrently.
        //    b: There are no queued structures. We change to State.ACTIVE as
        //       this region is now usable.
        // 
        // 2: The WorldGenerator just finished generating this region. We
        //    change to State.ACTIVE as this region is now usable.
        
        State s = state.get();
        if(s.equals(State.LOADING)) {
            if(!hasQueuedStructures())
                state.compareAndSet(State.LOADING, State.PREPARED);
        } else if(s == State.GENERATING)
            state.compareAndSet(State.GENERATING, State.PREPARED);
        else
            Log.get().postWarning("Invalid state " + s + " on setGenerated for "
                    + this);
    }
    
    /**
     * Attempts to obtain the permit to load this region. If this returns
     * {@code true}, the caller may load the region. This method is provided
     * for WorldLoader use only.
     */
    public boolean getLoadPermit() {
        // We can load only when this region is newly-created, so the only
        // valid state transition is from State.NEW to State.LOADING.
        return state.compareAndSet(State.NEW, State.LOADING);
    }
    
    /**
     * Attempts to obtain the permit to generate this region. If this returns
     * {@code true}, the caller may generate this region. This method is
     * provided for WorldGenerator use only.
     */
    public boolean getGenerationPermit() {
        return state.compareAndSet(State.LOADING, State.GENERATING);
    }
    
    /**
     * Attempts to obtain a permit to save this region. If this returns {@code
     * true}, the caller may save this region.
     * 
     * <p>Note that this method may block for a while if this region is
     * currently being saved.
     */
    public boolean getSavePermit() {
        synchronized(saveState) {
            // We synchronise on this region to make this atomic. This is much
            // less painful than trying to work with an atomic variable.
            
            switch(saveState.get()) {
                case IDLE:
                    // If we're in IDLE, we switch to SAVING and save.
                    saveState.set(SaveState.SAVING);
                    return true;
                case SAVING:
                    // If we're in SAVING, this means another thread is
                    // currently saving this region. However, since we have no
                    // guarantee that it is saving up-to-date data, we wait for
                    // it to finish and then save again on this thread.
                    saveState.set(SaveState.WAITING);
                    Tasks.waitUntil(saveState, () -> saveState.get() == SaveState.IDLE
                                    || saveState.get() == SaveState.IDLE_WAITER);
                    saveState.set(SaveState.SAVING);
                    return true;
                case WAITING:
                case IDLE_WAITER:
                    // As above, except another thread is waiting to save the
                    // updated state. We abort and let that thread do it.
                    // 
                    // As an added bonus, since we just grabbed the sync lock,
                    // we've established a happens-before with the waiter and thus
                    // provided it with a more recent batch of region state. Yay!
                    return false;
            }
        }
        
        throw new AssertionError(); // impossible
    }
    
    /**
     * Finalises a save operation by inducing an appropriate state change and
     * notifying relevant threads.
     */
    @UserThread("WorldLoaderThread")
    public void finishSaving() {
        synchronized(saveState) {
            saveState.set(saveState.get() == SaveState.WAITING
                    ? SaveState.IDLE_WAITER
                    : SaveState.IDLE);
            saveState.notifyAll();
        }
    }
    
    /**
     * Blocks the current thread until this region has finished saving. If the
     * current thread was interrupted while waiting, the interrupt flag will be
     * set when this method returns.
     */
    @SuppressWarnings("unused")
    private void waitUntilSaved() {
        Tasks.waitUntil(saveState, () -> saveState.get() == SaveState.IDLE);
    }
    
    public void tryImport(HostWorld world) {
        if(!imported) {
            imported = true;
            forEachSlice(s -> {
                //s.buildLight();
                s.importEntities(world);
                s.importTileEntities(world);
            });
            if(queuedActions != null) {
                for(Action a : queuedActions) {
                    a.apply(world, this);
                }
                queuedActions = null;
            }
        }
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
     * Returns {@code true} if this region's coords match the specified coords.
     */
    public boolean isAt(int x, int y) {
        return loc.equals(x, y);
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
        sb.append(loc.x());
        sb.append(',');
        sb.append(loc.y());
        sb.append(": ");
        sb.append(stateToString());
        sb.append('/');
        sb.append(saveStateToString());
        sb.append("]");
        return sb.toString();
    }
    
    private String stateToString() {
        return state.get().toString();
    }
    
    private String saveStateToString() {
        synchronized(saveState) {
            return saveState.toString();
        }
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
     * Values for a region's state.
     */
    private static enum State {
        /** A region is newly-instantiated and is not ready to be used.
         * Transitions to {@code LOADING} via {@link Region#getLoadPermit()}. */
        NEW,
        /** A region is currently being loaded by the world loader. This state
         * is also occupied by a cached region which has not yet been
         * generated. This may transition to GENERATING via {@link
         * Region#getGenerationPermit()}, but may also transition to PREPARED
         * via {@link Region#setGenerated()} if a region does not need
         * generating. */
        LOADING,
        /** A region is currently being generated by the world generator.
         * Transitions to ACTIVE via {@link Region#setGenerated()}. */
        GENERATING,
        /** A region is prepared - that is, loaded and generated, and may be
         * used. */
        PREPARED;
    }
    
    /**
     * Values for a region's "save state". These are separate from the main
     * state in an effort to reduce complexity, as each save state can overlap
     * with multiple different region states.
     * 
     * <p>All save state control is localised to {@link Region#getSavePermit()}
     * and {@link Region#finishSaving()}.
     */
    private static enum SaveState {
        /** A region is not currently being saved. */
        IDLE,
        /** A region is currently being saved. */
        SAVING,
        /** A region is currently being saved, and another thread is waiting in
         * line to save the region again. */
        WAITING,
        /** A region just finished saving, but another thread is waiting to
         * save the region again. */
        IDLE_WAITER;
    }
    
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
            super(0, 0, 0);
        }
        
        @Override
        public boolean update(HostWorld world, RegionStore store) {
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
