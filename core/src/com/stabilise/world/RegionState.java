package com.stabilise.world;

import static com.stabilise.core.Constants.REGION_UNLOAD_TICK_BUFFER;

import java.util.concurrent.atomic.AtomicReference;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.Tasks;


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
	 * A region should be unloaded when it it NOT ANCHORED, and none of its
	 * neighbours are ANCHORED.
	 * 
	 * A region is called PREPARED if it is LOADED and GENERATED. A region is
	 * made to prepare itself if either it, or a neighbouring region is
	 * ANCHORED.
	 * 
	 * A region is called ACTIVE if it is ANCHORED and PREPARED, and all its
	 * neighbours are PREPARED.
	 * 
	 * From an outside perspective, regions are never anchored directly. All
	 * anchoring takes place through HostWorld.loadSlice() and
	 * HostWorld.unloadSlice(). For now at least we don't care about the slices
	 * and instead stick the anchor on the region in which each requested slice
	 * lies. (QUESTION: In the future, might we care to distinguish which
	 * slices have anchors?)
	 * 
	 * When a region is ANCHORED for the first time, we initiate a PREPARE if
	 * it is not already PREPARED, and do the same for all its neighbours.
	 * 
	 * When a region is NOT ANCHORED, and none of its neighbours are ANCHORED,
	 * it is eligible to be UNLOADED. A region is generally unloaded after a
	 * time delay as to provide a buffer time.
	 * 
	 * A region is eligible to be SAVED if it is PREPARED.
	 * 
	 * When a region is UNLOADED, it is SAVED first.
	 */
	
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
    
    
    /** The number of slices anchored due to having been loaded by a client
     * within the region. Used to determine whether the region should begin the
     * 'unload countdown'.
     * 
     * <p>A region can only be anchored on the main thread, so we don't need to
     * be careful with synchronisation. */
    private int anchors = 0;
    /** Number of adjacent regions which are anchored. We do not unload a
     * region unless it has no anchored neighbours. A region counts itself as a
     * neighbour as being anchored itself must prevent it from being unloaded.
     * 
     * <p>A region (and its neighbours) can only be anchored on the main
     * thread, so we don't need to be careful with synchronisation. */
    private int anchoredNeighbours = 0;
    
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
     * it is {@link #isPrepared() prepared} and all its neighbours (including
     * itself) are {@link #isAnchored() anchored}.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public boolean isActive() {
    	return isPrepared() && anchoredNeighbours == 9;
    }
    
    /**
     * Informs this region that is has an anchored neighbour. Invoked by
     * RegionStore.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void addNeighbour() {
        anchoredNeighbours++;
    }
    
    /**
     * Informs this region that one of its neighbours has been de-anchored.
     * Invoked by RegionStore.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void removeNeighbour() {
        if(--anchoredNeighbours == 0)
            ticksToUnload = REGION_UNLOAD_TICK_BUFFER;
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
            //if(!hasQueuedStructures())
                state.compareAndSet(State.LOADING, State.PREPARED);
        } else if(s == State.GENERATING)
            state.compareAndSet(State.GENERATING, State.PREPARED);
        else
            Log.get().postWarning("Invalid state " + s + " on setGenerated for "
                    + this);
    }
    
    /**
     * Attempts to obtain the permit to load this region. If this returns
     * {@code true}, the caller may load the region.
     * 
     * <p>This method is provided for WorldLoader use only.
     */
    public boolean getLoadPermit() {
        // We can load only when this region is newly-created, so the only
        // valid state transition is from State.NEW to State.LOADING.
        return state.compareAndSet(State.NEW, State.LOADING);
    }
    
    /**
     * Attempts to obtain the permit to generate this region. If this returns
     * {@code true}, the caller may generate this region.
     * 
     * <p>This method is provided for WorldGenerator use only.
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
    public synchronized boolean getSavePermit() {
        // ^^^^^^^^^^^^ We synchronise on ourselves to make this atomic. This
    	// is much less painful than trying to do fancy stuff with an atomic
    	// variable; see finishSaving().
        
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
        
        throw new AssertionError(); // impossible
    }
    
    /**
     * Finalises a save operation by inducing an appropriate state change and
     * notifying relevant threads.
     */
    @UserThread("WorldLoaderThread")
    public synchronized void finishSaving() {
    	// ^^^^^^^^^^^^ synchronize on ourself; see getSavePermit().
    	
        saveState.set(saveState.get() == SaveState.WAITING
                ? SaveState.IDLE_WAITER
                : SaveState.IDLE);
        saveState.notifyAll();
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
	
}
