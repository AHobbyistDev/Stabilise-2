package com.stabilise.world;

import static com.stabilise.entity.Position.*;

import java.io.IOException;
import java.util.function.Consumer;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.CSliceAnchorer;
import com.stabilise.util.annotation.ForTestingPurposes;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.concurrent.SingleBlockingJob;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.multiverse.HostMultiverse.PlayerData;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The world as viewed by its host (i.e. the client in singleplayer, or the
 * server (/hosting client) in multiplayer).
 */
public class HostWorld extends AbstractWorld {
    
    /** This world's region store, which as the name suggests, stores and
     * manages all the regions. */
    public final RegionStore regions;
    
    
    public final SingleBlockingJob preloadJob = new SingleBlockingJob(this::prepare);
    
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
        
        // Instantiate from within the constructor so that it can grab the
        // executor from the multiverse
        regions = new RegionStore(this);
    }
    
    /**
     * Prepares the world by performing any necessary preemptive loading
     * operations, such as preparing the spawn regions, etc. Polling {@link
     * #isLoaded()} allows one to check the status of this operation.
     */
    @UserThread("WorkerThread")
    public void prepare() {
    	try {
            dimension.loadData();
        } catch(IOException e) {
            throw new RuntimeException("Could not load dimension info! (dim: " +
                    dimension.info.name + ") (" + e.getMessage() + ")" , e);
        }
    	
    	dimension.addLoaders(regions.loader, multiverse.info);
    	dimension.addGenerators(regions.generator);
    	
        spawnSliceX = dimension.info.spawnSliceX;
        spawnSliceY = dimension.info.spawnSliceY;
        
        // No spawn reasons; there's actually no good reason I can think of at
        // the moment for why we might want spawn reasons.
        // 
        // Also, we were getting concurrency issues since we were invoking
        // anchorRegion(), which is optimised for being called only on the main
        // thread -- and in here we are in a worker thread.
        
        // Load the spawn regions if this is the default dimension
        /*
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
        */
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
        addEntity(p); // assigns ID
        setPlayer(p);
        
        // Anchor everything quickly
        p.getComponent(CSliceAnchorer.class).anchorAll(this, p);
        
        return p;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>In the HostWorld implementation, this returns {@code true} iff all
     * regions have been loaded and generated.
     */
    @Override
    public boolean isLoaded() {
        return regions.allRegionsLoaded();
    }
    
    @Override
    public boolean update() {
    	if(!preloadJob.isDone())
            return false;
        doUpdate();
        return regions.isEmpty();
    }
    
    @Override
    protected void doUpdate() {
        super.doUpdate();
        
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
     * Returns the region occupying the specified slice coord, or
     * {@link Region#DUMMY_REGION} if it is not loaded.
     */
    private Region getRegionFromSliceCoords(int x, int y) {
        return getRegionAt(
        		regionCoordFromSliceCoord(x),
                regionCoordFromSliceCoord(y)
        );
    }
    
    @Override
    public void anchorSlice(int x, int y) {
        regions.anchorRegion(
                regionCoordFromSliceCoord(x),
                regionCoordFromSliceCoord(y)
        );
    }
    
    @Override
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
     */
    @UserThread("MainThread")
    private void save(boolean unload) {
        log.postInfo("Saving dimension...");
        
        if(unload) // just in case
            regions.uncacheAll();
        
        regions.saveAll();
        
        multiverse.getExecutor().execute(() -> {
            try {
                dimension.saveData();
            } catch(IOException e) {
                log.postSevere("Could not save dimension info", e);
            }
        });
        
        //savePlayers();
    }
    
    @Override
    public void close() {
        regions.cancelLoads();
        save(true);
    }
    
    @Override
    public void blockUntilClosed() {
        regions.waitUntilDone();
        
        log.postDebug(stats.toString());
    }
    
    public WorldLoadTracker loadTracker() {
    	return regions.loadTracker;
    }
    
    @Override
    public int hashCode() {
        return dimension.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o == this;
    }
    
}
