package com.stabilise.world;

import static com.stabilise.entity.Position.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.util.annotation.ForTestingPurposes;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.loader.WorldLoader;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.multiverse.HostMultiverse.PlayerData;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world as viewed by its host (i.e. the client in singleplayer, or the
 * server (slash hosting client) in multiplayer).
 * 
 * <!--
 * TODO: Implementation details for everything. Details are very important when
 * it comes to documenting interactions between the world, the world loader,
 * and the world generator
 * -->
 */
public class HostWorld extends AbstractWorld {
    
    /** This world's region store, which as the name suggests, stores and
     * manages all the regions. */
    public final RegionStore regions;
    
    /** The world loader. */
    private final WorldLoader loader;
    /** The world generator. */
    private final WorldGenerator generator;
    private final WorldLoadTracker loadTracker = new WorldLoadTracker();
    
    /** Holds all player slice maps. */
    private final List<SliceMap> sliceMaps = new UnorderedArrayList<>(4, 2);
    
    /** Whether or not the world has been {@link #prepare() prepared}. */
    private volatile boolean prepared = false;
    
    public final WorldStatistics stats = new WorldStatistics();
    
    
    /**
     * Creates a new HostWorld.
     * 
     * @param multiverse The multiverse.
     * @param dimension The dimension of this world.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public HostWorld(Multiverse<?> multiverse, Dimension dimension) {
        super(multiverse, dimension);
        
        // We instantiate the loader, generator and cache, and then safely hand
        // them references to each other as required.
        
        loader = new WorldLoader(this);
        dimension.addLoaders(loader, multiverse.info);
        
        generator = new WorldGenerator(multiverse, this);
        dimension.addGenerators(generator);
        
        regions = new RegionStore(this);
        regions.setUnloadHandler(this::unloadRegion);
        
        //loader.passReferences(generator);
        generator.passReferences(regions);
        regions.passReferences(loader, generator);
    }
    
    @UserThread("PoolThread")
    public void preload() {
        try {
            dimension.loadData();
        } catch(IOException e) {
            throw new RuntimeException("Could not load dimension info! (dim: " +
                    dimension.info.name + ") (" + e.getMessage() + ")" , e);
        }
        
        spawnSliceX = dimension.info.spawnSliceX;
        spawnSliceY = dimension.info.spawnSliceY;
        
        prepare();
    }
    
    @ThreadUnsafeMethod
    @Override
    public void prepare() {
        if(prepared)
            throw new IllegalStateException("World has already been prepared!");
        
        // Load the spawn regions if this is the default dimension
        if(dimension.hasSpawnRegions()) {
            // Ensure the 'spawn regions' are generated, and anchor them such that
            // they're always loaded
            // The spawn regions extend for -256 <= x,y <= 256 (this is arbitrary)
            for(int x = -1; x < 1; x++) {
                for(int y = -1; y < 1; y++) {
                    // This will induce a permanent anchorage imbalance which
                    // should never be rectified; the region will remain
                    // perpetually loaded
                    regions.anchorRegion(x, y);
                }
            }
        }
        
        prepared = true;
    }
    
    /**
     * Adds a player to the world.
     * 
     * @param data The data of the player to add.
     * 
     * @return The added player entity.
     * @throws NullPointerException if {@code data} is {@code null}.
     */
    public Entity addPlayer(PlayerData data) {
        Entity p = Entities.player();
        data.playerMob = p;
        if(data.newToWorld) {
            data.newToWorld = false;
            // TODO: For now I'm placing the character at (0,0) of the spawn
            // slice. In practice, we'll need to check to see whether or not
            // this location is valid, and keep searching until a valid
            // location is found.
            data.lastPos.set(spawnSliceX, spawnSliceY, 0f, 0f);
        }
        
        p.pos.set(data.lastPos);
        addEntity(p);
        setPlayer(p);
        sliceMaps.add(new SliceMap(this, p));
        return p;
    }
    
    /**
     * Checks for whether or not the spawn area about a player has been
     * loaded.
     * 
     * @param player The player.
     * 
     * @return {@code true} if the area is loaded; {@code false} otherwise.
     */
    /*
    public boolean spawnAreaLoaded(EntityMob player) {
        return true; // TODO
    }
    */
    
    /**
     * {@inheritDoc}
     * 
     * <p>In the HostWorld implementation, this returns {@code true} iff all
     * regions have been loaded and generated.
     */
    @Override
    public boolean isLoaded() {
        return regions.isLoaded() && prepared;
    }
    
    /**
     * Returns this world's load tracker.
     */
    public WorldLoadTracker loadTracker() {
        return loadTracker;
    }
    
    @Override
    public boolean update() {
        if(!prepared)
            return false;
        doUpdate();
        return regions.numRegions() == 0;
    }
    
    @Override
    protected void doUpdate() {
        super.doUpdate();
        
        profiler.start("sliceMap"); // root.update.game.world.sliceMap
        for(SliceMap m : sliceMaps)
            m.update();
        
        profiler.next("regions"); // root.update.game.world.regions
        regions.update();
        
        // Uncache any regions which may have been cached during this tick.
        // TODO: Once a tick might be too often, since this can be expensive.
        regions.uncacheAll();
        
        profiler.end(); // root.update.game.world
    }
    
    @ForTestingPurposes
    public void forEachRegion(Consumer<Region> action) {
        regions.forEach(action);
    }
    
    /**
     * Gets a region at the given coordinates.
     * 
     * @param x The x-coordinate of the region, in region-lengths.
     * @param y The y-coordinate of the region, in region-lengths.
     * 
     * @return The region, or {@link Region#DUMMY_REGION} if no such region
     * exists.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public Region getRegionAt(int x, int y) {
        Region r = regions.getRegion(x, y);
        return r == null ? Region.DUMMY_REGION : r;
    }
    
    /**
     * Ports the state of all entities located in a region to that region, and
     * then moves the region to the cache so that it may be saved.
     * 
     * <p>The region should be removed from the map of regions immediately
     * after this method returns.
     * 
     * @param r The region.
     */
    private void unloadRegion(Region r) {
        // TODO
        
        // Unload entities in the region...
        int minX = r.x() * Region.REGION_SIZE_IN_TILES;
        int maxX = minX + Region.REGION_SIZE_IN_TILES;
        int minY = r.y() * Region.REGION_SIZE_IN_TILES;
        int maxY = minY + Region.REGION_SIZE_IN_TILES;
        
        getEntities().forEach(e -> {
            if(e.pos.getGlobalX() + e.aabb.maxX() >= minX
                    && e.pos.getGlobalX() + e.aabb.minX() <= maxX
                    && e.pos.getGlobalY() + e.aabb.maxY() >= minY
                    && e.pos.getGlobalY() + e.aabb.minY() <= maxY)
                e.destroy();
        });
    }
    
    /**
     * Saves a region.
     */
    public void saveRegion(Region r) {
        loader.saveRegion(r, false, null);
    }
    
    /**
     * Returns the region occupying the specified slice coord, or
     * {@link Region#DUMMY_REGION} if it is not loaded.
     */
    private Region getRegionFromSliceCoords(int x, int y) {
        return getRegionAt(regionCoordFromSliceCoord(x),
                regionCoordFromSliceCoord(y));
    }
    
    /**
     * Marks a slice as loaded. This will attempt to load and generate the
     * slice's parent region, if it is not already loaded.
     * 
     * @param x The x-coordinate of the slice, in slice lengths.
     * @param y The y-coordinate of the slice, in slice lengths.
     */
    @UserThread("MainThread")
    @ThreadUnsafeMethod
    public void anchorSlice(int x, int y) {
        regions.anchorRegion(
                regionCoordFromSliceCoord(x),
                regionCoordFromSliceCoord(y)
        );
    }
    
    /**
     * Removes an anchor from a slice.
     * 
     * @param x The x-coordinate of the slice, in slice lengths.
     * @param y The y-coordinate of the slice, in slice lengths.
     */
    public void deanchorSlice(int x, int y) {
        regions.deAnchorRegion(
                regionCoordFromSliceCoord(x),
                regionCoordFromSliceCoord(y)
        );
    }
    
    @Override
    public Slice getSliceAt(int x, int y) {
        return getRegionFromSliceCoords(x, y).getSliceAt(
                sliceCoordRelativeToRegionFromSliceCoord(x),
                sliceCoordRelativeToRegionFromSliceCoord(y)
        );
    }
    
    /*
    @Override
    public Slice getSliceAtTile(int x, int y) {
        return getRegionFromTileCoords(x, y).getSliceAt(
                sliceCoordRelativeToRegionFromTileCoord(x),
                sliceCoordRelativeToRegionFromTileCoord(y)
        );
    }
    */
    
    @Override
    public void setTileAt(Position pos, int id) {
        Slice s = getSliceAt(pos);
        
        if(!s.isDummy()) {
            int tx = pos.getLocalTileX();
            int ty = pos.getLocalTileY();
            
            // TODO: remove this when I make sure one can't set a tile over another
            if(id != s.getTileIDAt(tx, ty)) {
                s.getTileAt(tx, ty).handleRemove(this, pos);
                SingleplayerState.pop.play(1f, 0.75f, 0f);
                
                s.setTileIDAt(tx, ty, id);
                //recalcLightingAt(x, y, s.getLightAt(tx, ty));
                s.updateLight(tx, ty);
                Tile.getTile(id).handlePlace(this, pos);
            }
        }
    }
    
    /*
    @SuppressWarnings("unused")
    private byte recalcLightingAt(int x, int y, byte curLight) {
        Tile t = getTileAt(x, y);
        
        byte[] l = new byte[5]; // base, 4x neighbours
        l[0] = t.getLight();
        if(l[0] == curLight)
            return (byte)(curLight - t.getFalloff());
        setLightAt(x, y, l[0]);
        
        l[1] = getLightAt(x-1, y  );
        l[2] = getLightAt(x+1, y  );
        l[3] = getLightAt(x,   y-1);
        l[4] = getLightAt(x,   y+1);
        
        if(l[1] > l[0]) l[1] = recalcLightingAt(x-1, y  , l[1]);
        if(l[2] > l[0]) l[2] = recalcLightingAt(x+1, y  , l[2]);
        if(l[3] > l[0]) l[3] = recalcLightingAt(x  , y-1, l[3]);
        if(l[4] > l[0]) l[4] = recalcLightingAt(x  , y+1, l[4]);
        
        byte newLevel = Maths.max(l);
        if(newLevel > l[0])
            setLightAt(x, y, newLevel);
        
        return (byte)(newLevel - t.getFalloff());
    }
    */
    
    @Override
    public void breakTileAt(Position pos) {
        Slice s = getSliceAt(pos);
        
        if(!s.isDummy()) {
            int tx = pos.getLocalTileX();
            int ty = pos.getLocalTileY();
            
            Tile old = s.getTileAt(tx, ty);
            
            if(old != Tiles.air) {
                SingleplayerState.pop.play(1f, 1.7f, 0f);
                
                old.handleBreak(this, pos);
                s.setTileAt(tx, ty, Tiles.air);
                s.updateLight(tx, ty);
            }
        }
    }
    
    @Override
    public void setTileEntity(TileEntity t) {
        doSetTileEntity(t, t.pos);
    }
    
    @Override
    public void removeTileEntityAt(Position pos) {
        doSetTileEntity(null, pos);
    }
    
    /**
     * @param t may be null -- null means remove whatever TE is there
     * @param pos never null
     */
    private void doSetTileEntity(TileEntity t, Position pos) {
        Slice s = getSliceAt(pos);
        
        if(!s.isDummy()) {
            int tx = pos.getLocalTileX();
            int ty = pos.getLocalTileY();
            
            TileEntity t2 = s.getTileEntityAt(tx, ty);
            if(t2 != null) {
                t2.handleRemove(this, pos);
                removeTileEntity(t2);
            }
            
            s.setTileEntityAt(tx, ty, t);
            
            if(t != null)
                addTileEntity(t);
        }
    }
    
    @Override
    public void blowUpTile(Position pos, float explosionPower) {
        Slice s = getSliceAt(pos);
        
        if(!s.isDummy()) {
            int tx = pos.getLocalTileX();
            int ty = pos.getLocalTileY();
            
            if(s.getTileAt(tx, ty).getHardness() < explosionPower) {
                s.getTileAt(tx, ty).handleBreak(this, pos);
                
                s.setTileAt(tx, ty, Tiles.air);
                
                //Tiles.AIR.handlePlace(this, x, y);
            }
        }
    }
    
    /**
     * Gets this world's filesystem directory.
     */
    public FileHandle getWorldDir() {
        return dimension.info.getDimensionDir();
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws RuntimeException if an I/O error occurred while saving.
     */
    @Override
    public void save() {
        save(false);
    }
    
    /**
     * Saves this world.
     * 
     * @param unload Whether or not every region should be unloaded as well as
     * saved.
     * 
     * @throws RuntimeException if an I/O error occurred while saving.
     */
    private void save(boolean unload) {
        log.postInfo("Saving dimension...");
        
        if(unload) // just in case
            regions.uncacheAll();
        
        regions.saveAll();
        
        try {
            dimension.saveData();
        } catch(IOException e) {
            throw new RuntimeException("Could not save dimension info!", e);
        }
        
        //savePlayers();
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws RuntimeException if an I/O error occurred while saving.
     */
    @Override
    public void close() {
        loader.shutdown();
        save(true);
        generator.shutdown();
    }
    
    @Override
    public void blockUntilClosed() {
        regions.waitUntilDone();
        
        log.postDebug(stats.toString());
    }
    
    @Override
    public int hashCode() {
        return dimension.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o == this;
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates a new HostWorld as per
     * {@link #HostWorld(WorldInfo) new HostWorld(info)}, where {@code info} is
     * the WorldInfo object returned as if by
     * {@link WorldInfo#loadInfo(String) WorldInfo.loadInfo(worldName)}. If you
     * already have access to a world's WorldInfo object, it is preferable to
     * construct the GameWorld directly.
     * 
     * @param worldName The name of the world on the file system.
     * 
     * @return The HostWorld instance, or {@code null} if the world info could
     * not be loaded.
     */
    /*
    public static HostWorld loadWorld(String worldName) {
        WorldInfo info = WorldInfo.loadInfo(worldName);
        
        if(info != null)
            return new HostWorld(info);
        
        Log.get().postSevere("Could not load info file of world \"" + worldName + "\" during world loading!");
        return null;
    }
    */
    
}
