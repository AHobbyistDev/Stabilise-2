package com.stabilise.world;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;

import javax.annotation.concurrent.GuardedBy;

import com.stabilise.util.Checks;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.concurrent.Striper;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Point;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.loader.WorldLoader;

/**
 * Stores and manages regions. Delegates loading and generating where necessary
 * to the {@link WorldLoader} and {@link WorldGenerator} respectively. Every
 * {@link HostWorld} owns a {@code RegionStore}.
 * 
 * <p>A {@code RegionStore} provides two layers of region storage for a world -
 * <b>primary</b> and <b>cache</b> storage.
 * 
 * <h3>Primary Storage</h3>
 * 
 * <p>Primary storage holds all the regions being used by the world. All the
 * regions in primary storage have been loaded and generated, and are safe to
 * interact with. Regions in primary storage should only be interacted with on
 * the main thread.
 * 
 * <p>A region lives in primary storage if it is either "anchored", or any of
 * its neighbours (the 8 adjacent regions) are anchored. Regions with with no
 * anchors nor anchored neighbours will be removed from primary storage
 * (AKA unloaded) during an {@link #update() update tick} after a short delay.
 * 
 * <p>Regions are (somewhat indirectly) added to primary storage via {@link
 * #anchorRegion(int, int)}. Regions are similarly indirectly removed from
 * primary storage by {@link #deAnchorRegion(int, int)}. When a region (or one
 * of its neighbours) is anchored for the first time, it is placed in the
 * cache to be prepared, and then is moved to primary storage once it has
 * finished loading and generating. When a region is unloaded, it is moved
 * from primary storage to the cache, saved, and then removed from the cache.
 * 
 * <h3>Cache Storage</h3>
 * 
 * <p>Cache storage is a secondary storage which holds regions which are loaded
 * for anything other than gameplay. The cache may overlap with primary
 * storage; a region in primary may also live in the cache for some other
 * reason. Regions are temporarily placed in the cache as a part of their
 * natural lifecycle, but they may also be manually cached for other purposes.
 * 
 * <p>A region being prepared for primary storage will initially live in the
 * cache while it is being loaded and generated; once done, it will be moved to
 * primary storage. A region is also temporarily moved into the cache while is
 * being saved as part of the unload process.
 * 
 * <p>Regions may also be manually cached. To cache a region (or retrieve a
 * cached region), one uses {@link #cache(int, int)}. This class does track how
 * many times {@link #cache(int, int)} is invoked for a particular region, so
 * different parts of the code may invoke it as many times as they so please.
 * There is as such no associated {@code uncache()} method for a single region.
 * Instead, this class tracks cached regions on a per-thread basis. Therefore,
 * a thread <em>must</em> invoke {@link #uncacheAll()} when it is done with all
 * the regions it has cached. Failing to do so will leave them perpetually in
 * the cache.
 * 
 * <p>Cache storage is thread-safe, and interacts with primary storage in a
 * consistent thread-safe manner.
 * 
 * <h3>Save Policy</h3>
 * 
 * As part of their natural lifecycle, regions are saved immediately after
 * being generated, and also right before being unloaded. Both these saves
 * occur while a region is <em>not</em> in primary storage, so there is minimal
 * risk of it being concurrently modified while it is being saved.
 * 
 * <p>A region can also be manually saved (rather, currently, all regions in
 * primary storage may be saved via {@link #saveAll()}) - however, the onus
 * lies on the caller to ensure that concurrency issues won't arise (e.g., by
 * saving only while the game is paused).
 * 
 * <h3>Region Lifecycle</h3>
 * 
 * TODO: Write in more detail about the lifecycle of a region
 * 
 * <pre>
 * (On anchor): PUT IN CACHE -> LOAD -> GENERATE -> SAVE -> MOVE TO PRIMARY
 * (On unload): MOVE TO CACHE -> SAVE -> REMOVE FROM CACHE
 * (Cached region): PUT IN CACHE -> (LOAD IF NECESSARY) -> UNCACHE
 * </pre>
 * 
 * I have, to the best of my ability, tried to account for all possibilities
 * wherein a region is being loaded, generated, anchored, cached, etc. by
 * multiple threads at once. 
 */
public class RegionStore {
    
    /** Wait time in seconds for {@link #waitUntilDone()}. */
    private static final int WAIT_TIME = 1;
    
    /** The number of locks to stripe {@link #cacheLocks} and {@link
     * #storeLocks} into. */
    private static final int STRIPE_FACTOR = 8;
    private static final IntBinaryOperator STRIPE_HASHER =
            Maths.genHashFunction(STRIPE_FACTOR, false);
    
    
    /** Reference to the world. */
    private final HostWorld world;
    
    public final WorldLoader loader;
    public final WorldGenerator generator;
    
    
    /** Primary storage. Contains all prepared regions. Maps region.loc ->
     * region. */
    private final ConcurrentMap<Point, Region> regions =
            new ConcurrentHashMap<>();
    
    /** The map of cached regions. Maps region.loc -> region. */
    private final ConcurrentMap<Point, CachedRegion> cache =
            new ConcurrentHashMap<>();
    
    /** Regions which have been cached by the current thread. */
    private final ThreadLocal<Map<Point, Region>> localCachedRegions =
            ThreadLocal.withInitial(HashMap::new);
    
    /** Locks for everything. The locks are Point objects for convenience in
     * {@link #moveToPrimary(Region)} and {@link #removeFromPrimary(Region)}. */
    private final Striper<Point> locks = 
            new Striper<>(STRIPE_FACTOR, Region::createMutableLoc);
    
    /** Dummy key for {@link #regions} whose mutability may be abused for
     * convenience, but ONLY on the main thread. */
    private final Point unguardedDummyLoc = Region.createMutableLoc();
    
    // Tracker for determining when all the regions are loaded.
    public final WorldLoadTracker loadTracker = new WorldLoadTracker();
    
    // A Lock and its associated Condition to wait on in waitUntilDone().
    private final Lock doneLock = new ReentrantLock();
    private final Condition emptyCondition = doneLock.newCondition();
    
    
    private final Log log;
    
    
    /**
     * Creates a new region store for the given world.
     */
    RegionStore(HostWorld world) {
        this.world = world;
        
        this.loader = new WorldLoader(world);
        this.generator = new WorldGenerator(world, this);
        
        log = Log.getAgent(world.getDimensionName() + "_RegionStore");
    }
    
    /**
     * Gets a region at the given coordinates, which are in region-lengths.
     * 
     * @return The region, or {@code null} if no such region exists in primary
     * storage.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public Region getRegion(int x, int y) {
        return regions.get(unguardedDummyLoc.set(x, y));
    }
    
    /**
     * Gets a region at the given coordinates, which are in region-lengths.
     * Tries the cache if the region is not in primary storage.
     * 
     * <p>IMPORTANT NOTE: This method does not synchronise properly nor stick
     * a mark on the cached region to keep it cached, so the caller should be
     * absolutely certain that nothing is going to go wrong.
     * 
     * @return The region, or {@code null} if the region is in neither primary
     * storage nor the cache.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    private Region getRegionTryCache(int x, int y) {
        // Try primary
        Region r = regions.get(unguardedDummyLoc.set(x, y));
        if(r != null)
            return r;
        
        // Try cache
        // No sync nor mark -- be careful!
        CachedRegion cr = cache.get(unguardedDummyLoc);
        return cr == null ? null : cr.region;
    }
    
    /**
     * Loads and generates a region, and adds it to primary storage when done.
     * If the region already lives in primary storage, it is returned;
     * otherwise, it will sit in the cache until it is done being prepared.
     * <b>Note:</b> the returned region might not be loaded nor generated, so
     * be careful with what you do with it.
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
        // this method has a fair bit of overhead if it's not yet in primary
        // storage. Ah well.
        
        boolean tryPrepare = false;
        
        // Try the cache next. Synchronised to make the put-if-absent atomic.
        synchronized(getLock(x, y)) {
            // To avoid a race condition we'll want to try primary storage
            // again while synchronised on the lock just to be safe (see e.g.,
            // moveToPrimary()).
            r = regions.get(unguardedDummyLoc);
            if(r != null)
                return r;
            
            CachedRegion cr = cache.get(unguardedDummyLoc);
            if(cr == null) {
                r = new Region(x, y);
                cr = new CachedRegion(r);
                cache.put(r.loc, cr);
            } else
                r = cr.region;
            
            // Unless someone else has already initiated a prepare, we do it
            if(!cr.prepareForPrimary) {
                cr.prepareForPrimary = true;
                tryPrepare = true;
                cr.mark();
            }
        }
        
        // Initiate the prepare outside of the synchronized block
        if(tryPrepare) {
        	loadTracker.startLoadOp(); // op is ended in moveToPrimary()
            prepareRegion(r, true);
        }
        
        return r;
    }
    
    /**
     * Loads and optionally generates a region. Nothing is done if the region
     * is already loaded/generated, or if loading/generating is already taking
     * place on another thread. This method cleans up a mark on the region in
     * the cache, so make sure it's marked first before invoking this.
     */
    @UserThread("Any")
    private void prepareRegion(Region r, boolean generate) {
        // Even if we don't get the load permit, if the region has already been
        // loaded due to being cached for some other purpose, we try getting
        // the gen permit. Note that this permits the possibility of us
        // stealing the gen permit from a thread that just finished loading and
        // was about to then gen the region; ah well, it works either way.
        
        if(r.state.getLoadPermit())
            loader.loadRegion(r, this::finishLoad);
        else if(generate && r.state.getGenerationPermit())
            generator.generate(r, false, this::finishGenerate);
        else
            finishGeneric(r, false); // Beaten to the punch! Get rid of our mark
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
        
        synchronized(getLock(r)) {
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
                // There is a subtle downside to what we've done here. If the
                // region has already been generated (so that we don't get the
                // permit above), but another thread concurrently queued a
                // structure (say, while generating another region), then we
                // don't implant it nor save the region. Instead, we just move
                // it to primary. If we're unlucky and the game abruptly closes
                // before the region is saved from within primary, then we'll
                // lose the queued structure. Ah well, I can't be bothered
                // dealing with that problem now.
            }
            // If the region doesn't need generating, then very well, remove
            // the mark for the load operation.
            // 
            // But, if whoever cached this region finished what they were doing
            // before the region finished loading, then unmark() will return
            // true here (since the load operating was the last thing keeping
            // the region cached), which means disposal is now our
            // responsibility. So we will need to save before unloading.
            // 
            // Note that since this is an "else if(...)" to the "if(generate)"
            // above, a region which was merely loaded for primary will not be
            // saved immediately after loading (which we what we want!).
            else if(cr.unmark()) { // remove the "load" mark
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
        
        synchronized(getLock(r)) {
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
        
        synchronized(getLock(r)) {
            CachedRegion cr = cache.get(r.loc);
            
            if(cr.unmark()) {
                // If the region is slated to be added to the world and it has
                // been prepared (e.g., if we're at the save part of the LOAD
                // -> GENERATE -> SAVE -> ADD TO PRIMARY chain (but not 
                // necessarily; this could just be part of a CACHE -> DO 
                // STUFF -> UNCACHE -> SAVE chain that ran in parallel)), then
                // the next step is to move it to primary storage.
            	// 
            	// Note: Checking isPrepared() shouldn't strictly be necessary
            	// since, because of the synchronized block in loadRegion(),
            	// prepareForPrimary should only be set -- with the last mark
            	// having just been removed -- when and only when the region is
            	// prepared.
                if(cr.prepareForPrimary) // && r.state.isPrepared())
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
     * 
     * @param saveIfAble true to save the region if it is not in primary and
     * calling this would remove it from the cache.
     */
    @UserThread("Any")
    private void finishGeneric(Region r, boolean saveIfAble) {
        boolean save = false;
        
        synchronized(getLock(r)) {
            CachedRegion cr = cache.get(r.loc);
            
            if(cr.unmark()) { // remove the mark keeping us in here
                // If the region is in primary storage, simply remove it from
                // the cache; it'll be saved in due course. If not, then save
                // it.
                if(regions.containsKey(r.loc))
                    cache.remove(r.loc);
                else if(saveIfAble && r.state.getSavePermit()) {
                    cr.mark(); // add the "save" mark
                    save = true;
                }
            }
        }
        
        if(save)
            loader.saveRegion(r, false, this::finishSave);
        else
            notifyWaiters();
    }
    
    /**
     * Moves a region from the cache to primary storage. This is invoked after
     * a region has been appropriately loaded/generated. This is invoked while
     * the appropriate lock is held (see {@link #locks}).
     */
    @UserThread("WorkerThread")
    @GuardedBy("getLock()")
    private void moveToPrimary(Region r) {
        if(!r.state.isPrepared())
            Checks.badAssert("Tried to move " + r.toStringDebug() + " from "
                    + "cache to primary storage, but it was not prepared?");
        
        cache.remove(r.loc); // out of cache
        
        // Synchronise over this RegionStore to make the "put into primary then
        // notify adjacent regions that were already in primary" atomic.
        // It's unfortunate that we can't use a striped lock here, so this may
        // cause some hitches.
        synchronized(this) {
            if(regions.put(r.loc, r) != null) { // into primary
                log.postWarning("Adding " + r + " to primary storage, but it's already there?");
                return;
            }
            
            // Notify neighbouring regions that this region is prepared and
            // vice-versa.
            int x = r.x(), y = r.y();
            Point loc = getLock(x, y); // safe to use since we're synced on it
            
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
        }
        
        loadTracker.endLoadOp(); // op is started in loadRegion()
    }
    
    /**
     * Removes a region from primary storage (doesn't touch its position in the
     * cache). This is invoked while the appropriate lock is held (see {@link
     * #locks}).
     */
    @UserThread("MainThread")
    @GuardedBy("getLock()")
    private void removeFromPrimary(Region r) {
        // Synchronise over this RegionStore to make the "remove from primary
        // then notify adjacent regions in primary" atomic.
        // It's unfortunate that we can't use a striped lock here, so this may
        // cause some hitches.
        synchronized(this) {
            regions.remove(r.loc); // out of primary
            
            // Notify neighbouring regions that this region is being removed
            int x = r.x(), y = r.y();
            Point loc = getLock(x, y); // safe to use since we're synced on it
            
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
        }
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
            
            r.importToWorld(world, this);
            
            if(s.isActive()) {
                r.update(world);
                r.implantStructures(this); // implant structures
            } else if(!s.isAnchored() && !s.hasAnchoredNeighbours() && s.tickDown()) {
                // Perform any operations needed for proper unloading first.
                r.exportFromWorld(world);
                
                // We save the region and remove it from primary storage.
                // It's probably poor style to have the region removed while
                // we're iterating over them (normally this would give a
                // ConcurrentModificationException), but since regions is a
                // ConcurrentHashMap I'm not *too* concerned.
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
        
        synchronized(getLock(x, y)) {
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
        localRegions.values().forEach(r -> finishGeneric(r, true));
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
        
        synchronized(getLock(r)) {
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
    
    private Point getLock(int x, int y) {
        return locks.get(STRIPE_HASHER.applyAsInt(x, y));
    }
    
    private Point getLock(Region r) {
    	return getLock(r.x(), r.y());
    }
    
    /**
     * Returns true if all regions have been loaded (this means there are no
     * regions in the cache).
     */
    boolean allRegionsLoaded() {
        return cache.isEmpty();
    }
    
    /**
     * Returns true if there are no regions in primary storage nor the cache.
     */
    public boolean isEmpty() {
    	// This isn't an atomic check, but it shouldn't matter, I don't think.
    	return regions.isEmpty() && cache.isEmpty();
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
     * Blocks the current thread until all regions have finished saving and all
     * cached regions have been uncached.
     */
    void waitUntilDone() {
    	doneLock.lock();
        try {
            // TODO: only use cache size for now. Upgrade to checking primary too
        	// when I get primary to be completely flushed when the world is
        	// forcefully unloaded (e.g., when the game is closed.)
            int prevNumRegions = cache.size();
            
            long waitTime = TimeUnit.SECONDS.toNanos(WAIT_TIME);
            
            while(prevNumRegions > 0) {
            	long nanosRemaining = emptyCondition.awaitNanos(waitTime);
            	if(nanosRemaining <= 0) { // timeout elapsed
            		int numRegions = cache.size();
            		if(numRegions < prevNumRegions) {
            			// Progress has been made, keep waiting.
            			prevNumRegions = numRegions;
            		} else {
            			// No progress! Just chuck a log message and abort.
            			StringBuilder sb = new StringBuilder();
                        sb.append("Regions too long to finish saving! "
                                + "Here are our offenders:");
                        for(CachedRegion c : cache.values()) {
                            sb.append("\n    > ");
                            sb.append(c.region.toStringDebug());
                        }
                        log.postWarning(sb.toString());
                        return;
            		}
            	} else
            		// If the await was broken before timeout, either we
            		// underwent a spurious wakeup or we we notified that
            		// the cache is empty. Either way, update
            		// prevNumRegions; we'll see next time around the while
            		// loop.
            		prevNumRegions = cache.size();
            }
        } catch(InterruptedException e) {
            log.postWarning("Interrupted while waiting to finish.", e);
        } finally {
            doneLock.unlock();
        }
    }
    
    /**
     * Cancels all queued load and generation operations, but allows save
     * operations to be queued and carried out. This is used to help make
     * closing the world a slighly speedier process.
     */
    public void cancelLoads() {
    	loader.shutdown();
    	generator.shutdown();
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
     * lock on one of the {@link RegionStore#getLock(int,int) locks} is held.
     */
    private static class CachedRegion {
        
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
         * Marks the region. A region will not be removed from the cache while
         * it is marked.
         */
        private void mark() {
            timesCached++;
        }
        
        /**
         * Removes a marking, and returns {@code true} if the region no longer
         * has any marks and should be saved/removed from the cache.
         */
        private boolean unmark() {
            return --timesCached == 0;
        }
        
    }
    
    /**
     * Signature of the callback function to be invoked by the WorldLoader/
     * WorldGenerator when a region has finished loading/generating.
     */
    @FunctionalInterface
    public interface RegionCallback {
        void accept(Region r, boolean success);
    }
    
}
