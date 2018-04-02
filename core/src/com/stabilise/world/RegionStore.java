package com.stabilise.world;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.stabilise.util.Checks;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadSafeMethod;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.Striper;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Point;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.loader.WorldLoader;

/**
 * Stores and manages regions. Every {@link HostWorld} owns a {@code
 * RegionStore}.
 * 
 * <p>A {@code RegionStore} provides two layers of region storage for a world -
 * primary and cache storage.
 * 
 * <h3>Primary Storage</h3>
 * 
 * <p>Primary storage holds all regions currently being used by the world.
 * Regions are loaded into primary storage using {@link
 * #loadRegion(int, int, boolean) loadRegion()}, and can be retrieved through
 * {@link #getRegion(int, int) getRegionAt()}. Every Region in primary
 * storage is {@link Region#update(HostWorld, RegionStore) updated} by {@link
 * #update()}, and is only removed from primary storage if {@code
 * update} returns {@code true}.
 * 
 * <p>Primary storage is not thread-safe and should only be interacted with on
 * the main thread.
 * 
 * <h3>Cache Storage</h3>
 * 
 * <p>Cache storage can overlap with primary storage, and it holds any Regions
 * which are loaded for anything other than gameplay.
 * 
 * <p>A Region may be obtained from the cache through {@link #cache(int, int)},
 * which is the cache analogue of {@code loadRegion()}. Since this class only
 * cares about the number of threads using a particular region, a thread may
 * invoke {@code cache()} as many times as it pleases. There is no method to
 * relinquish a particular region; a thread <i>must</i> invoke {@link
 * #uncacheAll()} to release each region it has cached.
 * 
 * <p>Cache storage is thread-safe, and interacts with primary storage in a
 * thread-safe way to ensure complete consistency.
 * 
 * <h3>Other Details</h3>
 * 
 * <p>This class's constructor requires a {@link Consumer} to act as an unload
 * handler. It is invoked in {@code updateRegions()} when a region is about to
 * be unloaded - immediately before it is {@link #saveRegion(Region) saved} and
 * removed from primary storage.
 */
public class RegionStore {
    
    /** Wait time in seconds for {@link #waitUntilDone()}. */
    private static final int WAIT_TIME = 5;
    
    /** The number of locks to stripe {@link #cacheLocks} and {@link
     * #storeLocks} into. */
    private static final int STRIPE_FACTOR = 8;
    private static final IntBinaryOperator STRIPE_HASHER =
            Maths.genHashFunction(STRIPE_FACTOR, false);
    
    
    // References to the world, loader, and generator.
    private final HostWorld world;
    private WorldLoader loader = null;
    private WorldGenerator generator = null;
    
    /** Consumer which is invoked right before a region is unloaded, to perform
     * any unload logic as required by the world. */
    private final Box<Consumer<Region>> unloadHandler = Boxes.emptyMut();
    
    /** Contains all loaded regions. Maps region.loc -> region. */
    private final ConcurrentMap<Point, Region> regions =
            new ConcurrentHashMap<>();
    /** Tracks the number of loaded regions. */
    private final AtomicInteger numRegions = new AtomicInteger();
    
    /** The map of cached regions. Maps region.loc -> region. */
    private final ConcurrentMap<Point, CachedRegion> cache =
            new ConcurrentHashMap<>();
    
    /** Regions which have been cached by the current worker thread. */
    private final ThreadLocal<Map<Point, Region>> localCachedRegions =
            ThreadLocal.withInitial(HashMap::new);
    
    /** Locks for everything. The locks are Point objects for convenience in
     * {@link #moveToPrimary(Region)}. */
    private final Striper2D<Point> locks = new Striper2D<>(Region::createMutableLoc);
    
    /** Dummy key for {@link #regions} whose mutability may be abused for
     * convenience, but ONLY on the main thread. */
    private final Point unguardedDummyLoc = Region.createMutableLoc();
    
    // A Lock and its associated Condition to wait on in waitUntilDone().
    private final Lock doneLock = new ReentrantLock();
    private final Condition emptyCondition = doneLock.newCondition();
    
    private final Log log;
    
    
    /**
     * Creates a new region store for the given world.
     */
    RegionStore(HostWorld world) {
        this.world = world;
        
        log = Log.getAgent(world.getDimensionName() + "_RegionStore");
    }
    
    /**
     * Sets this store's unload handler. The given consumer will be invoked
     * when a region is unloaded - immediately before it is removed from
     * primary storage.
     */
    void setUnloadHandler(@Nullable Consumer<Region> action) {
        unloadHandler.set(action);
    }
    
    /**
     * Passes this RegionStore a reference to the world's loader and generator.
     */
    void passReferences(WorldLoader loader, WorldGenerator generator) {
        this.loader = loader;
        this.generator = generator;
    }
    
    /**
     * Gets a region at the given coordinates, which are in region-lengths.
     * 
     * @return The region, or {@code null} if no such region exists.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    Region getRegion(int x, int y) {
        return regions.get(unguardedDummyLoc.set(x, y));
    }
    
    /**
     * Gets the region at the specified location.
     * 
     * @param point The region's location, whose coordinates are in
     * region-lengths.
     * 
     * @return The region, or {@code null} if no such region is loaded.
     */
    @ThreadSafeMethod
    private Region getRegion(Point point) {
        return regions.get(point);
    }
    
    /**
     * Gets a region at the given coordinates, which are in region-lengths.
     * Tries the cache if the region is not in primary storage.
     * 
     * <p>IMPORTANT NOTE: This method does not synchronise properly nor stick
     * a mark on the cached region to keep it cached, so the caller should be
     * absolutely certain that nothing is going to blow up.
     * 
     * @return The region, or {@code null} if the region is in neither primary
     * storage nor the cache.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    private Region getRegionTryCache(int x, int y) {
        Region r = regions.get(unguardedDummyLoc.set(x, y));
        if(r != null)
            return r;
        
        // No sync nor mark -- be careful!
        CachedRegion cr = cache.get(unguardedDummyLoc);
        return cr == null ? null : cr.region;
    }
    
    /**
     * Loads and generates a region, and adds it to primary storage when done.
     * If the region already exists in this store, it is returned; otherwise,
     * it will live in the cache until it is done being prepared. <b>Note:</b>
     * the returned region might not be loaded nor generated, so be careful
     * with what you do with it.
     * 
     * @return The region. Never null.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    private Region loadRegion(int x, int y) {
        // Get the region if it is already in primary storage.
        Region r = regions.get(unguardedDummyLoc.set(x, y));
        if(r != null)
            return r;
        
        // Unfortunately due to checking the cache and trying prepareRegion()
        // this method has a fair bit of overhead if it's not in primary
        // storage. Ah well.
        
        // Try the cache next. Synchronised to make the put-if-absent atomic.
        synchronized(locks.get(x, y)) {
            // To avoid a race condition we'll want to try primary storage
            // again while synchronised on the lock just to be safe (see e.g.,
            // moveToPrimary()).
            r = regions.get(unguardedDummyLoc);
            if(r != null)
                return r;
            
            CachedRegion c = cache.get(unguardedDummyLoc);
            if(c == null) {
                r = new Region(x, y);
                c = new CachedRegion(r);
                cache.put(r.loc, c);
            } else
                r = c.region;
            
            c.prepareForPrimary = true;
            
            // Since we'll be going into prepareRegion() regardless of whether
            // the region was cached beforehand or not, grab a mark just to be
            // safe.
            c.mark();
        }
        
        prepareRegion(r, true);
        
        return r;
    }
    
    /**
     * Loads and optionally generates a region. Nothing is done if the region
     * is already loaded/generated, or if loading/generating is already taking
     * place on another thread.
     */
    @UserThread("Any")
    private void prepareRegion(Region r, boolean generate) {
        // Even if we don't get the load permit, if the region has already been
        // loaded due to being cached for some other purpose, we try getting
        // the gen permit.
        
        if(r.state.getLoadPermit())
            loader.loadRegion(r, generate, this::finishLoad);
        else if(generate && r.state.getGenerationPermit())
            generator.generate(r, false, this::finishGenerate);
        else
            finishGeneric(r); // get rid of our mark
    }
    
    /**
     * Callback from the WorldLoader which is invoked when a region finishes
     * loading.
     */
    @UserThread("WorkerThread")
    private void finishLoad(Region r, boolean success) {
        if(!success && handleFailure())
            return;
        
        boolean generate;
        boolean save = false;
        
        synchronized(locks.get(r.x(), r.y())) {
            CachedRegion cr = cache.get(r.loc);
            generate = cr.prepareForPrimary; // = "please also generate me"
            
            if(generate) {
                if(!r.state.getGenerationPermit()) {
                    // It's already generated (or some other thread snuck in
                    // and stole generating rights from us in-between the load
                    // finishing and now).
                    generate = false;
                    if(cr.unmark())
                        // The region is no longer needed in the cache for
                        // anything else!
                        moveToPrimary(r);
                }
            } else
            // If the region doesn't need generating, then very well, remove
            // the mark for the load operation.
            // 
            // But, if whoever cached this region finished what they were doing
            // before the region finished loading, then unmark() will return
            // true here (since the load operating was the last thing keeping
            // the region cached), which means disposal is now our
            // responsibility. So we will need to save before unloading.
            if(!generate && cr.unmark()) { // remove the "load" mark
                if(r.state.getSavePermit()) {
                    cr.mark(); // add the "save" mark
                    save = true;
                } else
                    Checks.badAssert(r + " unmarked, yet couldn't get save permit?");
            }
        }
        
        // In both cases we continue on using the same thread, outside of the
        // synchronised block.
        
        if(generate)
            generator.generate(r, true, this::finishGenerate);
        else if(save)
            loader.saveRegion(r, true, this::finishSave);
        else
            notifyWaiters();
    }
    
    /**
     * Callback from the WorldGenerator when a region finishes generating.
     */
    @UserThread("WorkerThread")
    private void finishGenerate(Region r, boolean success) {
        if(!success && handleFailure())
            return;
        
        boolean save = false;
        
        synchronized(locks.get(r.x(), r.y())) {
            CachedRegion cr = cache.get(r.loc);
            
            // Save if nothing else has the region cached for other purposes
            if(cr.unmark()) { // remove the "generate" mark
                if(r.state.getSavePermit()) {
                    cr.mark(); // add the "save" mark
                    save = true;
                } else
                    Checks.badAssert(r + " unmarked, yet couldn't get save permit?");
            }
            // There is no else; some other thread will have to deal with the
            // saving and adding to the world in this case.
        }
        
        // Save on the same thread, outside of the synchronised block
        
        if(save)
            loader.saveRegion(r, true, this::finishSave);
        else
            notifyWaiters();
    }
    
    /**
     * Callback from the WorldLoader when a region finishes saving.
     */
    @UserThread("WorkerThread")
    private void finishSave(Region r, boolean success) {
        if(!success && handleFailure())
            return;
        
        synchronized(locks.get(r.x(), r.y())) {
            CachedRegion cr = cache.get(r.loc);
            
            if(cr.unmark()) {
                // If the save occurred right after a generate (in the load ->
                // generate -> save -> add to primary chain), the next step is
                // to move the region to primary storage.
                if(cr.prepareForPrimary && r.state.isPrepared())
                    moveToPrimary(r);
                // Otherwise, the save occurred after the region was cached for
                // some other purpose, we simply remove it from the cache.
                else
                    cache.remove(r.loc);
            }
        }
        
        notifyWaiters();
    }
    
    /**
     * Called by {@link #uncacheAll()} for each region cached by a thread, and
     * by {@link #prepareRegion(Region, boolean)} if it is unable to secure a
     * loading or generation permit. Performs all the necessary uncaching
     * cleanup.
     */
    @UserThread("Any")
    private void finishGeneric(Region r) {
        boolean save = false;
        
        synchronized(locks.get(r.x(), r.y())) {
            CachedRegion cr = cache.get(r.loc);
            
            if(cr.unmark()) { // remove the mark keeping us in here
                // If the region is in primary storage, simply remove it from
                // the cache; it'll be saved in due course. If not, then save
                // it.
                if(regions.containsKey(r.loc))
                    cache.remove(r.loc);
                else if(r.state.getSavePermit()) {
                    cr.mark(); // add the "save" mark
                    save = true;
                } else
                    Checks.badAssert("Couldn't get save permit even though " +
                            "nothing else had marked the region in the cache?");
            }
        }
        
        if(save)
            loader.saveRegion(r, false, this::finishSave);
        else
            notifyWaiters();
    }
    
    /**
     * Moves a region from the cache to primary storage. This is invoked while
     * the appropriate lock is held (see {@link #locks}).
     */
    @UserThread("WorkerThread")
    private void moveToPrimary(Region r) {
        if(!r.state.isPrepared())
            Checks.badAssert("Tried to move " + r.toStringDebug() + " from "
                    + "cache to primary storage, but it was not prepared?");
        
        // Notify neighbouring regions that this region is prepared and
        // vice-versa.
        int x = r.x(), y = r.y();
        Point loc = locks.get(x, y);
        for(int u = x-1; u <= x+1; u++) {
            for(int v = y-1; v <= y+1; v++) {
                if(u == x && v == y) continue; // don't do it if other == r
                
                Region other = regions.get(loc.set(u, v));
                if(other != null) {
                    r.state.addPreparedNeighbour();
                    other.state.addPreparedNeighbour();
                }
            }
        }
        
        // Put the region in primary after sharing preparedness to establish a
        // happens-before.
        
        regions.putIfAbsent(r.loc, r); // into primary
        //     ^ just .put() would work too, but putIfAbsent() probably avoids some work
        cache.remove(r.loc); // out of cache
        
        numRegions.getAndIncrement();
    }
    
    /**
     * Removes a region from primary storage (doesn't touch its position in the
     * cache). This is invoked while the appropriate lock is held (see {@link
     * #locks}).
     */
    @UserThread("MainThread")
    private void removeFromPrimary(Region r) {
        // Notify neighbouring regions that this region is being removed
        int x = r.x(), y = r.y();
        Point loc = locks.get(x, y);
        for(int u = x-1; u <= x+1; u++) {
            for(int v = y-1; v <= y+1; v++) {
                if(u == x && v == y) continue; // don't do it if other == r
                
                Region other = regions.get(loc.set(u, v));
                if(other != null) {
                    r.state.removePreparedNeighbour(); // could do all at once, but I cbf
                    other.state.removePreparedNeighbour();
                }
            }
        }
        
        // Remove the region after sharing preparedness to establish a
        // happens-before.
        
        regions.remove(r.loc);
        
        numRegions.getAndDecrement();
    }
    
    /**
     * Handles a region failing to either load or generate.
     * 
     * @return true if the current operation should be aborted in favour of
     * everything shutting down.
     */
    private boolean handleFailure() {
        log.postSevere("Region failed to load or generate? TODO: crash the game or something");
        return false;
    }
    
    /**
     * Updates all regions.
     */
    @UserThread("MainThread")
    void update() {
        regions.values().forEach(r -> {
            RegionState s = r.state;
            
            if(s.tryImport())
                r.importContents(world);
            
            if(s.isActive()) {
                r.update(world);
                r.implantStructures(this); // implant structures
            } else if(!s.isAnchored() && !s.hasAnchoredNeighbours() && s.tickDown()) {
                // Perform any operations needed for proper unloading first.
                unloadHandler.ifPresent(a -> a.accept(r));
                
                // We save the region and remove it from primary storage.
                // It's probably poor style to have the region removed while
                // we're iterating over them (normally this would give a
                // ConcurrentModificationException), but since regions is a
                // ConcurrentHashMap I'm not /too/ concerned.
                saveRegion(r, true);
            } else
                r.implantStructures(this); // implant structures even if not active
        });
    }
    
    /**
     * Runs the given action for every region in primary storage.
     * 
     * @throws NullPointerException if {@code action} is {@code null}.
     */
    @UserThread("MainThread")
    public void forEach(Consumer<Region> action) {
        regions.values().forEach(action);
    }
    
    /**
     * Anchors a region and loads its surrounding neighbours appropriately. If
     * any of the regions are not already loaded, they are loaded and
     * generated.
     * 
     * <p>This method is the main point of entry for the world to load regions.
     * 
     * @param x The x-coordinate of the region, in region-lengths.
     * @param y The y-coordinate of the region, in region-lengths.
     */
    @UserThread("MainThread")
    void anchorRegion(int x, int y) {
        Region r = loadRegion(x, y);
        
        if(r.state.anchor()) {
            // Load and notify all regions adjacent to r
            for(int u = x-1; u <= x+1; u++) {
                for(int v = y-1; v <= y+1; v++) {
                    if(u != x || v != y)
                        loadRegion(u, v).state.addAnchoredNeighbour();
                }
            }
        }
    }
    
    /**
     * De-anchors a region.
     * 
     * <p>This method is the main point of entry for the world to unload
     * regions (somewhat indirectly).
     * 
     * @param x The x-coordinate of the region, in region-lengths.
     * @param y The y-coordinate of the region, in region-lengths.
     */
    @UserThread("MainThread")
    void deAnchorRegion(int x, int y) {
        // In here we use getRegionTryCache() to get the region and its
        // neighbours. We assume all the regions are present in either primary
        // storage of the cache since they should've all been put in by
        // anchorRegion() above. So hopefully we don't get given any nulls
        // and we don't get any unhappy concurrency problems.
        
        Region r = getRegionTryCache(x, y); // shouldn't be null
        
        if(r.state.deAnchor()) {
            // Notify all regions adjacent to r
            for(int u = x-1; u <= x+1; u++) {
                for(int v = y-1; v <= y+1; v++) {
                    if(u != x || v != y)
                        getRegionTryCache(u, v).state.removeAnchoredNeighbour();
                }
            }
        }
    }
    
    /**
     * Caches a region for usage by the current thread. If the region is not
     * already loaded into memory, this method initiates a load, but does <b>
     * not</b> generate the region. Since the region may either be a) loading
     * or generating on another thread, or b) acting as part of the world in
     * primary storage, no guarantees are made as to the state of the returned
     * region. One should assumed that any interactions with it are not
     * thread-safe. 
     * 
     * <p>The caller does not need to keep track of each cached region so to
     * later uncache them. Instead, you should call {@link #uncacheAll()} once
     * you are done working with any cached regions to dispose of all of them.
     * 
     * @param x The x-coordinate of the region, in region-lengths.
     * @param y The y-coordinate of the region, in region-lengths.
     * 
     * @return The region. Never null.
     */
    @UserThread("Any")
    public Region cache(int x, int y) {
        // If the region is already locally cached by this thread, use it.
        Map<Point, Region> localMap = localCachedRegions.get();
        Point loc = Region.createImmutableLoc(x, y);
        Region r = localMap.get(loc);
        if(r != null)
            return r;
        
        // Otherwise, we cache the region (unless it is already cached) and
        // then locally cache it.
        
        boolean needsLoad = false;
        
        synchronized(locks.get(x, y)) {
            CachedRegion cr = cache.get(loc);
            
            // If the region isn't in the cache, we check primary storage
            if(cr == null) {
                // If it's in primary storage, put it in the cache. If it
                // isn't, we create the region in the cache. If the region is
                // in primary storage, it must already be loaded; if not, we'll
                // have to load it ourselves.
                r = regions.get(loc);
                if(r == null) {
                    r = new Region(x, y);
                    needsLoad = true;
                }
                cr = new CachedRegion(r);
                cache.put(r.loc, cr);
            } else {
                r = cr.region;
                // Even if the region isn't loaded yet, whoever cached it
                // before us will take care of that for us, so we don't need to
                // set needsLoad to true.
            }
            
            cr.mark();
            // If the region needs loading, we slap another mark on cr, which
            // will be removed when the loading completes. This is probably the
            // easiest way of ensuring our mark placed above isn't erroneously
            // removed.
            if(needsLoad)
                cr.mark();
        }
        
        localMap.put(loc, r);
        
        if(needsLoad)
            prepareRegion(r, false);
        
        return r;
    }
    
    /**
     * Uncaches any regions which have been cached by this thread. It is okay
     * to defensively invoke this method, as this does nothing if no regions
     * have been cached.
     */
    @UserThread("Any")
    public void uncacheAll() {
        Map<Point, Region> localRegions = localCachedRegions.get();
        localRegions.values().forEach(this::finishGeneric);
        localRegions.clear();
    }
    
    /**
     * Safely saves a region.
     * 
     * @param removeFromPrimary true if the region should also be removed from
     * primary storage -- that is, if it should be ported over to the cache
     * during saving.
     */
    @UserThread("MainThread")
    private void saveRegion(Region r, boolean removeFromPrimary) {
        // Notes: since this is invoked by the main thread, this means the
        // region has already been prepared, so we don't have to worry about
        // whether the save will overlap with any load or generation tasks.
        // Furthermore, since the save is explicitly requested by the main
        // thread, we don't worry about whether we might get concurrency issues
        // from the region being modified while we save -- the onus to be
        // careful now rests on whoever called this method.
        
        // Note that even if we don't ourselves get the permit, requesting it
        // will ensure it gets carried through by another thread.
        boolean hasPermit = r.state.getSavePermit();
        if(!hasPermit && !removeFromPrimary) // early check to avoid synchronising
            return;
        
        synchronized(locks.get(r.x(), r.y())) {
            if(removeFromPrimary)
                removeFromPrimary(r); // unfortunate name collision, but eclipse doesn't mind
            if(!hasPermit)
                return;
            
            CachedRegion cr = cache.get(r.loc);
            // If the region isn't already in the cache, stick it in.
            if(cr == null) {
                cr = new CachedRegion(r);
                cache.put(r.loc, cr);
            }
            
            cr.mark();
        }
        
        loader.saveRegion(r, false, this::finishSave);
    }
    
    /**
     * Saves all regions in primary storage.
     */
    void saveAll() {
        regions.values().forEach(r -> saveRegion(r, false));
    }
    
    /**
     * Returns true iff all regions in primary storage are {@link
     * Region#isPrepared() prepared}.
     */
    boolean isLoaded() {
        // TODO: pretty crude implementation
        for(Region r : regions.values()) {
            if(!r.state.isPrepared()) {
                //log.postInfo("Not all regions loaded (" + r + ": "
                //        + r.isLoaded() + ", " + r.isGenerated() + ")");
                return false;
            }
        }
        //log.postInfo("All regions loaded!");
        return true;
    }
    
    /**
     * Returns the number of regions in primary storage.
     */
    @UserThread("MainThread")
    int numRegions() {
        return numRegions.get();
    }
    
    /**
     * Blocks the current thread until all regions have finished saving and all
     * cached regions have been uncached.
     */
    void waitUntilDone() {
        try {
            doneLock.lock();
            
            if(cache.isEmpty())
                return;
            
            // We only wait for a certain number of seconds, since we shouldn't
            // risk deadlock if some bug causes some regions to not be managed
            // properly. Instead we'll just complain about it to the log.
            // TODO: Instead, we could perhaps take charge of unloading them
            // ourselves?
            if(!emptyCondition.await(WAIT_TIME, TimeUnit.SECONDS)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Regions too long to finish saving! "
                        + "Here are our offenders:");
                for(CachedRegion c : cache.values()) {
                    sb.append("\n    > ");
                    sb.append(c.region.toStringDebug());
                }
                log.postWarning(sb.toString());
            }
        } catch(InterruptedException e) {
            log.postWarning("Interrupted while waiting to finish.");
        } finally {
            doneLock.unlock();
        }
    }
    
    /**
     * If the cache is empty, notifies any threads that may be waiting in
     * {@link #waitUntilDone()}.
     */
    private void notifyWaiters() {
        if(cache.isEmpty()) {
            doneLock.lock();
            try {
                emptyCondition.signalAll();
            } finally {
                doneLock.unlock();
            }
        }
    }
    
    /**
     * Returns a string representation of this region store and all the regions
     * in it.
     */
    public String toStringDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Regions: {\n");
        sb.append("(note that the following printout may not be accurate due to");
        sb.append(" obvious concurrency reasons)\n");
        sb.append("    PRIMARY STORAGE: {\n");
        regions.values().forEach(r -> sb.append("        ").append(r.toStringDebug()).append('\n'));
        sb.append("    },\n");
        sb.append("    CACHE: {\n");
        cache.values().forEach(cr -> sb.append("        ").append(cr.region.toStringDebug()).append('\n'));
        sb.append("    }\n");
        sb.append("}");
        return sb.toString();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Container class for cached regions.
     * 
     * <p>All methods of this class should only be invoked while the monitor
     * lock on one of the {@link RegionStore#cacheLocks} is held.
     */
    public class CachedRegion {
        
        private final Region region;
        /** The number of times the region has been cached. */
        private int timesCached = 0;
        
        /** true if the region should be added to primary storage once it's
         * prepared. This also indicates that the region should be generated
         * after it's loaded. Default: false. */
        private boolean prepareForPrimary = false;
        
        
        private CachedRegion(Region region) {
            this.region = region;
        }
        
        /**
         * Marks the region as cached by the current thread.
         */
        private void mark() {
            timesCached++;
        }
        
        /**
         * Removes the current thread's marking, and returns {@code true} if
         * the region is no longer considered marked and should be saved.
         */
        private boolean unmark() {
            return --timesCached == 0;
        }
        
    }
    
    @FunctionalInterface
    public interface RegionCallback {
        void accept(Region r, boolean success);
    }
    
    /**
     * Convenience class which extends {@link Striper} to provide {@code
     * get(x, y)}.
     */
    private static class Striper2D<T> extends Striper<T> {
        
        public Striper2D(Supplier<T> supplier) {
            super(STRIPE_FACTOR, supplier);
        }
        
        /**
         * Gets the object corresponding to the specified x and y coordinates.
         */
        public T get(int x, int y) {
            return get(STRIPE_HASHER.applyAsInt(x, y));
        }
        
    }
    
}
