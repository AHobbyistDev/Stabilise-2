package com.stabilise.world;

import static com.stabilise.core.Constants.REGION_UNLOAD_TICK_BUFFER;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;

import com.stabilise.util.Checks;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;


/**
 * This class contains and deals with everything related to the load state of
 * a region. 
 * 
 * <p>This entire class could be made a part of the Region class, but for
 * clarity of mind I've thought it best to separate out into its own thing.
 */
public class RegionState {
    
    /* Alright, let's plan this out.
     * 
     * A region should be considered active if it is ANCHORED and PREPARED, and
     * all 8 of its neighbours are PREPARED.
     * 
     * A region should be loaded when it is ANCHORED, or any of its neighbours
     * are ANCHORED. Region generation should be performed as part of the
     * loading process. A region is called PREPARED if it is LOADED and
     * GENERATED.
     * 
     * When a region is NOT ANCHORED, and none of its neighbours are ANCHORED,
     * it is eligible to be UNLOADED. A region is generally unloaded after a
     * time delay as to provide a buffer time.
     * 
     * From an outside perspective, regions are never anchored directly. All
     * anchoring takes place through HostWorld.loadSlice() and
     * HostWorld.unloadSlice(). For now at least we don't care about the slices
     * and instead stick the anchor on the region in which each requested slice
     * lies. (QUESTION: In the future, might we care to distinguish which
     * slices have anchors?)
     * 
     * A region is eligible to be SAVED if it is PREPARED.
     * 
     * When a region is UNLOADED, it is SAVED first.
     * 
     * 
     * 
     * 
     * Unfortunate as it may be, implementation of the above functionality is
     * spread throughout this class, RegionStore, WorldLoader, and
     * WorldGenerator.
     */
    
    /** The state of this region. See the documentation for {@link #State.NEW}
     * and all other states. */
    private final AtomicReference<State> state = new AtomicReference<>(State.NEW);
    /** Keeps track of whether the region is being saved. */
    @GuardedBy("this")
    private SaveState saveState = SaveState.NOT_SAVING;
    /** Whether or not this region has been generated. Note that this is
     * distinct from {@link State#PREPARED}, since a region can be generated at
     * time of loading, but still in need of preparation to implant structures. */
    private boolean generated = false;
    
    
    /** The number of slices anchored due to having been loaded by a client
     * within the region. Used to determine whether the region should begin the
     * 'unload countdown'.
     * 
     * <p>A region can only be anchored on the main thread, so we don't need to
     * be careful with synchronisation. */
    private int anchors = 0;
    /** Number of adjacent regions which are anchored. We do not unload a
     * region unless it has no anchored neighbours.
     * 
     * <p>A region (and its neighbours) can only be anchored on the main
     * thread, so we don't need to be careful with synchronisation. */
    private int anchoredNeighbours = 0;
    /** Number of adjacent regions which are 'prepared'. A region is considered
     * active if it is anchored and all its neighbours are prepared. */
    private AtomicInteger preparedNeighbours = new AtomicInteger();
    /** true if all this region's neighbours are prepared. A region is
     * considered active if it is anchored and all its neighbours are prepared.
     * 
     * <p>This value is cached as to avoid reading from an AtomicInteger on a
     * per-tick basis. Since this is not volatile, the correct value is updated
     * in the main thread by establishing a happens-before using the
     * ConcurrentMap in RegionStore. (TODO: not actually airtight -- will 
     * backfire on me when I use this to assume all neighbouring regions are
     * definitely present in the region store. Ah well, I'll deal with it when
     * I get the first NPE.) */
    private boolean allNeighboursPrepared = false;
    
    /** Whether a region's contents have been imported into the world.
     * Importing is done on the first tick after a region is added into the
     * world. */
    private boolean imported = false;
    
    /** The number of ticks until the region should be unloaded.  */
    private int ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
    
    
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
    public boolean anchor() {
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
    public boolean deAnchor() {
        return --anchors == 0;
    }
    
    /**
     * Returns {@code true} if this region is anchored/active, and may be
     * updated.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean isAnchored() {
        return anchors > 0;
    }
    
    /**
     * Returns {@code true} if the region is considered 'active' -- that is, if
     * it is {@link #isAnchored() anchored} and all its neighbours (including
     * itself) are {@link #isPrepared() prepared}.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean isActive() {
        return isAnchored() && allNeighboursPrepared;
    }
    
    /**
     * Informs this region that is has an anchored neighbour. Invoked by
     * RegionStore.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void addAnchoredNeighbour() {
        anchoredNeighbours++;
    }
    
    /**
     * Informs this region that one of its neighbours has been de-anchored.
     * Invoked by RegionStore.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void removeAnchoredNeighbour() {
        if(--anchoredNeighbours == 0)
            ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
    }
    
    /**
     * Returns true if the region has anchored neighbours.
     */
    public boolean hasAnchoredNeighbours() {
        return anchoredNeighbours > 0;
    }
    
    /**
     * Informs this region that is has a prepared neighbour. This is called by
     * RegionStore.
     */
    @UserThread("Any")
    public void addPreparedNeighbour() {
        if(preparedNeighbours.incrementAndGet() == 8) // 8 neighbours; don't count self
            allNeighboursPrepared = true;
    }
    
    /**
     * Informs this region that one of its neighbours has been removed from the
     * world. This is called by RegionStore.
     */
    @UserThread("Any")
    public void removePreparedNeighbour() {
        preparedNeighbours.getAndDecrement();
        allNeighboursPrepared = false;
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
     * Attempts to obtain the permit to load the region. If this returns {@code
     * true}, the caller may load the region.
     */
    public boolean getLoadPermit() {
        return state.compareAndSet(State.NEW, State.LOADING);
    }
    
    /**
     * Declares that the region has been loaded. This should be called by the
     * WorldLoader once loading of the region is completed.
     */
    public void setLoaded() {
        if(!state.compareAndSet(State.LOADING, State.LOADED))
            throw new RuntimeException("Couldn't transition from loading to loaded?");
    }
    
    /**
     * Attempts to obtain the permit to generate this region. If this returns
     * {@code true}, the caller may generate this region.
     */
    public boolean getGenerationPermit() {
        return state.compareAndSet(State.LOADED, State.GENERATING);
    }
    
    /**
     * Marks the region as generated, and induces an appropriate state change.
     * This is invoked in two scenarios:
     * 
     * <ul>
     * <li>When the WorldLoader has finished loading this region and found it
     *     to be generated.
     * <li>When the WorldGenerator finishes generating this region.
     * </ul>
     * 
     * @param queuedStructures Whether or not the region has {@link
     * Region#hasQueuedStructures()} that need adding. (Having to be passed
     * this is a bit inelegant but I can't think of anything better.)
     */
    public void setGenerated(boolean queuedStructures) {
        generated = true;
        
        // This method is invoked in two scenarios:
        // 
        // 1: When this region is loaded by the WorldLoader, and it finds that
        //    this region has already been generated. From here, there are two
        //    options:
        // 
        //    a: There are queued structures. We remain in LOADED so that
        //       getGenerationPermit() returns true so that the world generator
        //       can generate those structures concurrently.
        //    b: There are no queued structures. We change to PREPARED as this
        //       region is now usable.
        // 
        // 2: The WorldGenerator just finished generating this region. We
        //    change to PREPARED as this region is now usable.
        
        State s = state.get();
        if(s == State.LOADING || s == State.LOADED) {
            if(!queuedStructures)
                state.set(State.PREPARED);
        } else if(s == State.GENERATING)
            state.set(State.PREPARED);
        else
            Log.get().postWarning("Invalid state " + s + " on setGenerated");
    }
    
    /**
     * Returns true if the region is currently being saved.
     */
    public synchronized boolean isSaving() {
        // ^^^^^^^^^^^^ Synchronised to make this atomic
        return saveState == SaveState.SAVING || saveState == SaveState.SAVE_QUEUED;
    }
    
    /**
     * Attempts to obtain a permit to save the region. If this returns {@code
     * true}, the caller may save this region. Even if this returns false, we
     * take note that a save was requested -- see {@link #finishSaving()}.
     */
    @UserThread("Any")
    public synchronized boolean getSavePermit() {
        // ^^^^^^^^^^^^ Synchronised to make this atomic
        
        switch(saveState) {
            case NOT_SAVING:
                // If we're in IDLE, we switch to SAVING and save.
                saveState = SaveState.SAVING;
                return true;
            case SAVING:
                // If we're in SAVING, this means another thread is
                // currently saving this region. However, since we have no
                // guarantee that it is saving up-to-date data, we queue up
                // another save. After completing the current save, the
                // WorldLoader will pick up on our new save request via
                // finishSaving() and save again.
                saveState = SaveState.SAVE_QUEUED;
                return false;
            case SAVE_QUEUED:
                // As above, except a save is already queued, so we don't do
                // anything.
                // 
                // As an added bonus, since we just grabbed the sync lock,
                // we've established a happens-before with the saver and
                // provided it with a more recent batch of region state. Yay!
                return false;
        }
        
        throw Checks.badAssert();
    }
    
    /**
     * This is called when a save operation is completed.
     *
     * @return true if another save was queued in the meantime and the region
     * should be saved again.
     */
    @UserThread("WorkerThread")
    public synchronized boolean finishSaving() {
        // ^^^^^^^^^^^^ Synchronised
        if(saveState == SaveState.SAVE_QUEUED) {
            saveState = SaveState.SAVING;
            return true;
        } else if(saveState == SaveState.SAVING) {
            saveState = SaveState.NOT_SAVING;
            return false;
        }
        
        throw Checks.badAssert();
    }
    
    /**
     * Counts down another tick until the region is scheduled to be unloaded.
     * 
     * @return true if the region should be unloaded
     */
    public boolean tickDown() {
        return --ticksToUnload == 0;
    }
    
    /**
     * This is called when the region is removed from the world.
     */
    public void removedFromWorld() {
        // Might need to be reimported if the region is added back before it is
        // flushed from the cache
        imported = false;
    }
    
    /**
     * Checks to see if the region should have its contents imported into the
     * world.
     */
    public boolean tryImport() {
        if(!imported) {
            imported = true;
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return stateToString() + "/" + saveStateToString();
    }
    
    private String stateToString() {
        return state.get().toString();
    }
    
    private synchronized String saveStateToString() {
        //  ^^^^^^^^^^^^ Synchronised
        return saveState.toString();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Values for a region's state.
     */
    private static enum State {
        /** A region is newly-instantiated and is not ready to be used.
         * Transitions to {@link #LOADING} via {@link Region#getLoadPermit()}. */
        NEW,
        /** A region is currently being loaded by the world loader. Transitions
         * to {@link #LOADED} via {@link RegionState#setLoaded()}. */
        LOADING,
        /** A region has finished loading, but has not been generated or begun
         * generating. May transition to {@link #GENERATING} via {@link
         * RegionState#getGenerationPermit()} or to {@link #PREPARED} via
         * {@link RegionState#setGenerated()}. */
        LOADED,
        /** A region is currently being generated by the world generator.
         * Transitions to {@link #PREPARED} via {@link Region#setGenerated()}. */
        GENERATING,
        /** A region is prepared - that is, loaded and generated, and may be
         * used. */
        PREPARED
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
        NOT_SAVING,
        /** A region is currently being saved. */
        SAVING,
        /** A region is currently being saved, and another save has been
         * requested. */
        SAVE_QUEUED
    }
    
}
