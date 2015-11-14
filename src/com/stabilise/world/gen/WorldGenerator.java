package com.stabilise.world.gen;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Slice.SLICE_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.stabilise.util.Log;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.RegionStore;
import com.stabilise.world.Slice;
import com.stabilise.world.loader.WorldLoader.DimensionLoader;
import com.stabilise.world.multiverse.Multiverse;

/**
 * The {@code WorldGenerator} class provides the mechanism for generating the
 * terrain of a world.
 * 
 * <h3>Usage Guidelines</h3>
 * 
 * <p>Firstly, a {@code WorldGenerator} must be prepared via {@link
 * #prepare(DimensionLoader, RegionStore) prepare()} before it can be used.
 * 
 * <p>A {@code WorldGenerator} should be used exclusively by the {@code
 * WorldLoader}, as region generation happens immediately after loading.
 * 
 * <p>To generate a region, invoke {@link #generate(Region)}. When {@link
 * Region#isPrepared()} returns {@code true}, generation of the region is
 * complete, and it is safe to interact with it. {@link
 * #generateSynchronously(Region)} is offered as a convenience alternative
 * for all threads but the main thread.
 * 
 * <p>To shut down the generator, invoke {@link #shutdown()}.
 */
@ThreadSafe
public final class WorldGenerator {
    
    /** The world for which the generator is generating. */
    private final HostWorld world;
    private DimensionLoader loader;
    private final long seed;
    
    /** The executor which delegates threads. */
    private final Executor executor;
    /** Whether or not the generator has been shut down. This is volatile. */
    private volatile boolean isShutdown = false;
    
    private RegionStore regionStore;
    
    private final List<IWorldGenerator> generators = new ArrayList<>(1);
    
    private final Log log = Log.getAgent("GENERATOR");
    
    
    /**
     * Creates a new WorldGenerator.
     * 
     * @param multiverse The multiverse.
     * @param world The world.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public WorldGenerator(Multiverse<?> multiverse, HostWorld world) {
        this.world = Objects.requireNonNull(world);
        this.executor = multiverse.getExecutor();
        
        seed = multiverse.getSeed();
        
        // TODO: de-hardcodify
        generators.add(new PerlinNoiseGenerator());
    }
    
    /**
     * Prepares this WorldGenerator by providing it with references to the
     * world loader and the world's region store.
     * 
     * @throws IllegalStateException if this generator has already been
     * prepared.
     * @throws NullPointerException if either argument is null.
     */
    public void prepare(DimensionLoader loader, RegionStore regions) {
        if(this.regionStore != null || this.loader != null)
            throw new IllegalStateException("Already prepared");
        this.loader = Objects.requireNonNull(loader);
        this.regionStore = Objects.requireNonNull(regions);
    }
    
    /**
     * Instructs the WorldGenerator to generate the given region.
     * 
     * <p>Regions which are ineligible for generation will ignored. That is,
     * the given region will not be generated if {@link
     * Region#getGenerationPermit()} returns false.
     * 
     * @param region The region to generate.
     * 
     * @throws NullPointerException if {@code region} is {@code null}.
     */
    @UserThread("MainThread")
    public final void generate(final Region region) {
        Objects.requireNonNull(region);
        
        world.stats.gen.requests.increment();
        if(!isShutdown && region.getGenerationPermit())
            executor.execute(() -> genRegion(region));
        else
            world.stats.gen.rejected.increment();
    }
    
    /**
     * Instructs the WorldGenerator to generate the given region on the current
     * thread. This method does nothing if {@link Region#getGenerationPermit()}
     * returns false.
     * 
     * @param region The region to generate.
     * 
     * @throws NullPointerException if {@code region} is {@code null}.
     */
    @UserThread("WorkerThread")
    public final void generateSynchronously(Region region) {
        Objects.requireNonNull(region);
        
        world.stats.gen.requests.increment();
        if(!isShutdown && region.getGenerationPermit())
            genRegion(region);
        else
            world.stats.gen.rejected.increment();
    }
    
    /**
     * Generates a region. The {@link Region#getGenerationPermit() generation
     * permit} for the region must be obtained before this method is invoked.
     */
    @UserThread("WorkerThread")
    private void genRegion(Region r) {
        world.stats.gen.started.increment();
        
        if(isShutdown) {
            world.stats.gen.aborted.increment();
            return;
        }
        
        //log.logMessage("Generating " + r);
        
        TaskTimer timer = new TaskTimer("Generating " + r);
        timer.start();
        
        try {
            // Don't generate a region if we don't need to!
            if(!r.isGenerated()) {
                // Set up the region's slices
                for(int y = 0; y < REGION_SIZE; y++) {
                    for(int x = 0; x < REGION_SIZE; x++) {
                        r.slices[y][x] = new Slice(
                                x + r.offsetX, y + r.offsetY,
                                // all values are 0 == Tiles.AIR
                                new int[SLICE_SIZE][SLICE_SIZE]
                        );
                    }
                }
                
                // Generate the region, as per the generators
                for(IWorldGenerator generator : generators)
                    generator.generate(r, seed);
            }
            
            // After normal generation processes have been completed, add any
            // queued schematics.
            r.implantStructures(regionStore);
            
            timer.stop();
            log.postDebug(timer.getResult(TimeUnit.MILLISECONDS));
            
            world.stats.gen.completed.increment();
            
            r.setGenerated();
            
            // Save the region once it's done generating
            loader.saveRegion(r, null);
        } catch(Throwable t) {
            // TODO: What do we do if worldgen fails? Do we retry?
            world.stats.gen.failed.increment();
            log.postSevere("Worldgen of " + r + " failed!", t);
            return;
        } finally {
            // We always clean up all regions cached during generation.
            regionStore.uncacheAll();
        }
    }
    
    /**
     * Adds a schematic to the world.
     * 
     * @param r The region to add the schematic to.
     * @param name The name of the schematic to add.
     * @param sliceX The x-coordinate of the slice in which to place the
     * schematic, relative to the region, in slice-lengths.
     * @param sliceY The y-coordinate of the slice in which to place the
     * schematic, relative to the region, in slice-lengths.
     * @param tileX The x-coordinate of the tile at which to place the
     * schematic, relative to the slice, in tile-lengths.
     * @param tileY The y-coordinate of the tile at which to place the
     * schematic, relative to the slice, in tile-lengths.
     * @param params The parameters to use for adding the schematic.
     */
    /*
    @UserThread("WorkerThread")
    protected final void addSchematicAt(Region r, String name, int sliceX, int sliceY, int tileX, int tileY, SchematicParams params) {
        addSchematicAt(r, getSchematic(name), sliceX, sliceY, tileX, tileY, params);
    }
    */
    
    /**
     * Adds a schematic to the world.
     * 
     * @param r The region to add the schematic to.
     * @param sc The name of the schematic to add.
     * @param sliceX The x-coordinate of the slice in which to place the
     * schematic, relative to the region, in slice-lengths.
     * @param sliceY The y-coordinate of the slice in which to place the
     * schematic, relative to the region, in slice-lengths.
     * @param tileX The x-coordinate of the tile at which to place the
     * schematic, relative to the slice, in tile-lengths.
     * @param tileY The y-coordinate of the tile at which to place the
     * schematic, relative to the slice, in tile-lengths.
     * @param params The parameters to use for adding the schematic.
     * 
     * @throws IllegalArgumentException Thrown if the schematic's origin has
     * negative coordinates.
     */
    /*
    @UserThread("WorkerThread")
    protected final void addSchematicAt(Region r, Structure sc, int sliceX, int sliceY, int tileX, int tileY, SchematicParams params) {
        if(sc == null)
            return;
        
        //log.logMessage("Adding " + sc + " to " + r);
        
        // Don't allow negative pivots; they'll mess up our algorithm
        if(sc.x < 0 || sc.y < 0)
            throw new IllegalArgumentException(sc + " has a pivot in an invalid location! (" + sc.x + "," + sc.y + ")");
        
        //int offsetX = ((tileX + sliceX * SLICE_SIZE - sc.x) % (REGION_SIZE_IN_TILES));
        
        // Inform neighbouring regions of the schematic if the schematic overlaps into them
        if(params.informRegions) {
            for(int regionY = r.loc.y + params.offsetY + ((tileY + sliceY * SLICE_SIZE - sc.y) / (REGION_SIZE_IN_TILES));
                    regionY <= r.loc.y + params.offsetY + ((tileY + sliceY * SLICE_SIZE - sc.y + sc.height) / (REGION_SIZE_IN_TILES));
                    regionY++) {
                for(int regionX = r.loc.x + params.offsetX + ((tileX + sliceX * SLICE_SIZE - sc.x) / (REGION_SIZE_IN_TILES));
                        regionX <= r.loc.x + params.offsetX + ((tileX + sliceX * SLICE_SIZE - sc.x + sc.width) / (REGION_SIZE_IN_TILES));
                        regionX++) {
                    if(regionY != r.loc.y || regionX != r.loc.x) {
                        Region cachedRegion = regionCache.cacheRegion(regionX, regionY);
                        cachedRegion.addStructure(
                                new Region.QueuedStructure(
                                        sc.name, sliceX, sliceY, tileX, tileY, r.loc.x - regionX, r.loc.y - regionY
                                )
                        );
                    }
                }
            }
        }
        
        // Get the bottom-left corner tile of the region from which the schematic will originate
        int initialX = sliceX * SLICE_SIZE + tileX - sc.x + params.offsetX * REGION_SIZE_IN_TILES;
        int initialY = sliceY * SLICE_SIZE + tileY - sc.y + params.offsetY * REGION_SIZE_IN_TILES;
        
        int x, y;
        
        // If the bottom-left corner tile is within the region, start from there
        // If not, we'll start generating from the bottom and/or left boundary of the region, and
        // skip to the first part of the schematic that is within the region
        if(initialX >= 0) {
            tileX = (initialX % REGION_SIZE_IN_TILES) % SLICE_SIZE;
            sliceX = Maths.floor(((initialX / SLICE_SIZE)) % REGION_SIZE);
            x = 0;
        } else {
            tileX = 0;
            sliceX = 0;
            x = -initialX;
        }
        
        if(initialY >= 0) {
            tileY = (initialY % REGION_SIZE_IN_TILES) % SLICE_SIZE;
            sliceY = Maths.floor(((initialY / SLICE_SIZE)) % REGION_SIZE);
            y = 0;
        } else {
            tileY = 0;
            sliceY = 0;
            y = -initialY;
        }
        
        // tileX, sliceX and x need resetting every time it loops to the next row
        int defaultTileX = tileX;
        int defaultSliceX = sliceX;
        initialX = x;
        
        Slice s = r.getSliceAt(sliceX, sliceY);
        
        for(; y < sc.height; y++) {
            for(; x < sc.width; x++) {
                
                int tile = sc.getTileAt(x, y);
                if(tile != -1 && (params.copyAir || tile != 0))
                    s.setTileAt(tileX, tileY, tile);
                
                tileX++;
                
                if(tileX == SLICE_SIZE) {
                    sliceX++;
                    tileX = 0;
                    if(sliceX == REGION_SIZE)
                        break;
                    s = r.getSliceAt(sliceX, sliceY);
                }
            }
            
            tileX = defaultTileX;
            sliceX = defaultSliceX;
            x = initialX;
            
            tileY++;
            
            if(tileY == SLICE_SIZE) {
                sliceY++;
                tileY = 0;
                if(sliceY == REGION_SIZE)
                    break;
            }
            
            // Reset the slice
            s = r.getSliceAt(sliceX, sliceY);
        }
    }
    */
    
    /**
     * Gets a schematic.
     * 
     * @param schematic The schematic's name.
     * 
     * @return The schematic, or {@code null} if a schematic with the given
     * name could not be found.
     */
    /*
    @UserThread("WorkerThread")
    private Structure getSchematic(String schematic) {
        // Weakly synchronised through use of a ConcurrentHashMap - this is
        // sufficient for now (it doesn't really matter if a map entry is
        // overwritten by an equivalent schematic)
        if(schematics.containsKey(schematic)) {
            return schematics.get(schematic);
        } else {
            Structure s = new Structure(schematic);
            try {
                s.load();
            } catch(IOException e) {
                log.postWarning("Could not load schematic \"" + schematic + "\"!", e);
                return null;
            }
            schematics.put(schematic, s);
            return s;
        }
    }
    */
    
    /**
     * Instructs the WorldGenerator to shut down.
     */
    @UserThread("MainThread")
    public final void shutdown() {
        isShutdown = true;
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A class containing parameters for placing schematics in the world.
     */
    protected static class SchematicParams {
        
        /** Whether or not to copy air tiles from the schematic. */
        protected final boolean copyAir;
        /** Whether or not to inform neighbouring regions of the schematic
         * needing to be generated on their sides of a region boundary. */
        protected final boolean informRegions;
        /** The x-offset of the schematic's origin, in region-lengths. */
        protected final int offsetX;
        /** The y-offset of the schematic's origin, in region-lengths. */
        protected final int offsetY;
        
        
        /**
         * Creates a new object containing parameters for schematic placement.
         * 
         * @param copyAir Whether or not to copy air tiles.
         * @param informRegions Whether or not to carry generation over to
         * neighbouring regions.
         */
        protected SchematicParams(boolean copyAir, boolean informRegions, int offsetX, int offsetY) {
            this.copyAir = copyAir;
            this.informRegions = informRegions;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
        
        // Static factories
        
        /**
         * Gets a SchematicParams object suitable for default cases.
         */
        protected static SchematicParams defaultParams() {
            return new SchematicParams(true, true, 0, 0);
        }
        
        /**
         * Gets a SchematicParams object suitable for scheduled generation.
         * 
         * @param offsetX The x-offset of the schematic's origin, in
         * region-lengths.
         * @param offsetY The y-offset of the schematic's origin, in
         * region-lengths.
         */
        protected static SchematicParams scheduledGenParams(int offsetX, int offsetY) {
            return new SchematicParams(true, false, offsetX, offsetY);
        }
        
    }
    
}
