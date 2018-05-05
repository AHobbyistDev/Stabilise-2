package com.stabilise.world;

import static com.stabilise.core.Constants.REGION_UNLOAD_TICK_BUFFER;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;

import com.stabilise.util.Checks;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;


/**
 * This class contains and deals with everything related to the load state of
 * a region. It is through this class that {@link RegionStore} manages a
 * region.
 * 
 * <p>This entire class could be made a part of the Region class, but for
 * clarity of mind I've thought it best to separate out into its own thing.
 * 
 * @see RegionStore
 */
public class RegionState {
    
    /** The main state (for want of a better name) of the region. See the
     * documentation for {@link #State.NEW} and all other states. */
    private final AtomicReference<State> state = new AtomicReference<>(State.NEW);
    /** Keeps track of whether the region is being saved. */
    @GuardedBy("this")
    private SaveState saveState = SaveState.NOT_SAVING;
    /** Whether or not the region has been generated. Note that this is
     * distinct from {@link State#PREPARED}, since a region can be generated at
     * time of loading, but still in need of preparation to implant structures. */
    private boolean generated = false;
    
    
    /** The number of times the region has been 'anchored'. If a region is
     * anchored, or any regions adjacent to it are anchored, it will remain
     * loaded in primary storage.
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
     * active if it is anchored and prepared, and all its neighbours are
     * prepared. */
    private AtomicInteger preparedNeighbours = new AtomicInteger();
    /** true if all this region's neighbours are prepared. A region is
     * considered active if it is anchored and prepared, and all its neighbours
     * are prepared.
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
     * Anchors the region. An anchored region remains loaded, and additionally
     * loads its neighbours.
     * 
     * @return {@code true} if this region is newly-anchored.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean anchor() {
        return anchors++ == 0;
    }
    
    /**
     * Removes an anchor from the region. This method is the inverse of {@link
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
     * Returns {@code true} if this region is anchored at least once.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean isAnchored() {
        return anchors > 0;
    }
    
    /**
     * Returns {@code true} if the region is considered 'active' -- that is, if
     * it is {@link #isAnchored() anchored}, and it, and all its neighbours,
     * are {@link #isPrepared() prepared}. A region is {@link
     * Region#update(HostWorld) updated} only if it is active.
     * 
     * <p>Implementation note: we don't actually check for whether this region
     * is itself prepared, since by assumption a region in the RegionStore's
     * primary storage should already be prepared.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean isActive() {
        return isAnchored() && allNeighboursPrepared;
    }
    
    /**
     * Informs this region that is has an anchored neighbour. Called by
     * RegionStore.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void addAnchoredNeighbour() {
        anchoredNeighbours++;
    }
    
    /**
     * Informs this region that one of its neighbours has been de-anchored.
     * Called by RegionStore.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void removeAnchoredNeighbour() {
        if(--anchoredNeighbours == 0)
            ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
    }
    
    /**
     * Returns true if the region has at least one anchored neighbour (doesn't
     * include itself).
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean hasAnchoredNeighbours() {
        return anchoredNeighbours > 0;
    }
    
    /**
     * Informs the region that is has a prepared neighbour. This is called by
     * RegionStore.
     */
    @UserThread("Any")
    public void addPreparedNeighbour() {
        if(preparedNeighbours.incrementAndGet() == 8) // 8 neighbours; don't count self
            allNeighboursPrepared = true;
    }
    
    /**
     * Informs the region that one of its neighbours has been removed from the
     * world. This is called by RegionStore.
     */
    @UserThread("Any")
    public void removePreparedNeighbour() {
        preparedNeighbours.getAndDecrement();
        allNeighboursPrepared = false;
    }
    
    /**
     * Returns {@code true} if the region has been prepared -- that is, if it
     * has been loaded and generated.
     */
    public boolean isPrepared() {
        return state.get().equals(State.PREPARED);
    }
    
    /**
     * Checks for whether or not this region has been generated. Note that this
     * is distinct from {@link #isPrepared()} -- a region which has already
     * been generated (as per here) might still need to have a run through the
     * WorldGenerator to implant structures and whatnot.
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
     * 
     * @param generated true if the loaded region has already been generated.
     * @param queuedStructures Whether or not the region has {@link
     * Region#hasQueuedStructures() has queued structures} that need adding.
     * Ignored if {@code generated} is {@code false}. (Having to be passed this
     * is a bit inelegant but I can't think of anything better.)
     */
    public void setLoaded(boolean generated, boolean queuedStructures) {
        if(generated) {
            this.generated = true;
            swapState(State.LOADING, queuedStructures ? State.LOADED : State.PREPARED);
        } else
            swapState(State.LOADING, State.LOADED);
    }
    
    /**
     * Attempts to obtain the permit to generate the region. If this returns
     * {@code true}, the caller may generate the region.
     */
    public boolean getGenerationPermit() {
        return state.compareAndSet(State.LOADED, State.GENERATING);
    }
    
    /**
     * Marks the region as generated (moreso, <em>prepared</em>), and induces
     * an appropriate state change. This is called by the WorldGenerator when
     * it finishes generating the region.
     */
    public void setGenerated() {
        generated = true;
        swapState(State.GENERATING, State.PREPARED);
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
     * @return true if another save was requested in the meantime and the
     * region should be saved again.
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
     * This should only be called if the region is not anchored and none of
     * its neighbours are anchored.
     * 
     * @return true if the region should be unloaded
     */
    public boolean tickDown() {
        return --ticksToUnload == 0;
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
    
    /**
     * Sets the region as imported, so that {@link #tryImport()} can return
     * true again. This is invoked when the region is unloaded and moved from
     * primary storage to the cache for saving.
     */
    public void setUnimported() {
        imported = false;
    }
    
    private void swapState(State expect, State update) {
        if(!state.compareAndSet(expect, update))
            throw new RuntimeException("Could not update region state to " + update
                    + "; expected " + " expect, (probably) was " + state.get());
    }
    
    @Override
    public String toString() {
        return "[" + stateToString() + "/" + saveStateToString() + "; " + 
                "anchors: " + anchors + ", adjAnchored: " + anchoredNeighbours +
                ", adjPrepped: " + preparedNeighbours + ", active: " + 
                isActive() + "]";
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
         * to either {@link #LOADED} or {@link #PREPARED} via {@link
         * RegionState#setLoaded(boolean, boolean)}. */
        LOADING,
        /** A region has finished loading, but has not been generated or begun
         * generating. Transitions to {@link #GENERATING} via {@link
         * RegionState#getGenerationPermit()}. */
        LOADED,
        /** A region is currently being generated by the world generator.
         * Transitions to {@link #PREPARED} via {@link Region#setGenerated()}. */
        GENERATING,
        /** A region is prepared - that is, loaded and generated. */
        PREPARED
    }
    
    /**
     * Values for a region's "save state". These are separate from the main
     * state, as the save state is for the most part independent of, and can
     * overlap with, multiple different states.
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
