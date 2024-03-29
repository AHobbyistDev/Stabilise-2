package com.stabilise.world.multiverse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.CEntityTracker;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.dimension.Dimension;


/**
 * A HostProvider is a provider of a world for the host of that world. There
 * are three types of hosts:
 * 
 * <ul>
 * <li><b>Singleplayer</b>: This is the simplest case.
 * <li><b>Multiplayer</b>: Hosts the world via internet to multiple clients.
 * <li><b>Multiplayer with Integrated Player</b>: A combination of the above
 *     two; the world is hosted to multiple clients, but there is also an
 *     integrated player who does not require a connection.
 * </ul>
 * 
 * <p>Since networking is NYI, however, Singleplayer is really the only case.
 */
public class HostMultiverse extends Multiverse<HostWorld> {
    
    /** Stores players using this world. Maps player hash -> PlayerData. */
    private final Map<String, PlayerData> players = new HashMap<>(1);
    
    /** The total number of entities which have existed during the lifetime of
     * all worlds. When a new entity is created this is incremented and set as
     * its ID. This is shared between dimensions since, of course, entities may
     * move between dimensions and it would suck to encounter an ID collision. */
    private long entityCount = 0;
    
    
    /**
     * Creates a new HostProvider.
     * 
     * @param info The world info.
     * @param profiler The profiler to use to profile this world provider and
     * its worlds. If {@code null}, a default disabled profiler is instead set.
     * 
     * @throws NullPointerException if {@code info} is {@code null}.
     */
    public HostMultiverse(WorldInfo info, Profiler profiler) {
        super(info, profiler);
    }
    
    @Override
    @ThreadUnsafeMethod
    public HostWorld loadDimension(String name) {
        HostWorld world = getDimension(name);
        if(world != null)
            return world;
        
        Dimension dim;
        if(Dimension.isPlayerDimension(name)) {
            String hash = Dimension.extractPlayerHash(name);
            PlayerData dat = players.get(hash);
            if(dat == null)
                throw new RuntimeException("No player present for this dimension!");
            if(!name.equals(dat.data.getDimensionName()))
                throw new RuntimeException("Given dimension name (" + name + 
                		") != expected name (" + dat.data.getDimensionName() + ")");
            dim = Dimension.getPlayerDimension(dat.data);
        } else
            dim = Dimension.getDimension(info, name);
        
        if(dim == null)
            throw new IllegalArgumentException("Invalid dim: \"" + name + "\"");
        
        world = dim.createHost(this);
        world.dimensionName = name;
        dimensions.put(name, world);
        
        executor.execute(world.preloadJob::run);
        
        return world;
    }
    
    /**
     * Adds a player to a world.
     * 
     * @param player The CharacterData for the player to add.
     * 
     * @return The PlayerBundle holding the player entity, data, and world the
     * player was added to, or {@code null} if the player data could not be
     * loaded, or the dimension could not be loaded.
     * @throws NullPointerException if {@code player} is {@code null}.
     */
    @ThreadUnsafeMethod
    public PlayerBundle addPlayer(CharacterData player) {
        PlayerData data = players.get(player.hash);
        if(data == null)
            data = new PlayerData(player);
        if(!data.load())
            return null;
        players.putIfAbsent(player.hash, data);
        
        HostWorld world = loadDimension(data.dimension);
        Entity playerEntity = world.addPlayer(data);
        
        // Stick on a tracker
        CEntityTracker tracker = new CEntityTracker();
        playerEntity.addComponent(tracker);
        tracker.world = world;
        data.tracker = tracker;
        
        return new PlayerBundle(world, playerEntity, data);
    }
    
    /**
     * doesn't do anything yet
     */
    public void removePlayer(CharacterData player) {
        // TODO
    }
    
    @Override
    public long getNextEntityID() {
        return ++entityCount;
    }
    
    @Override
    public long getTotalEntityCount() {
        return entityCount;
    }
    
    @Override
    public long getSeed() {
        return info.seed;
    }
    
    /**
     * Saves the worlds.
     * 
     * @throws RuntimeException if an I/O error occurred while saving.
     */
    @Override
    public void save() {
    	// TODO: pass everything off to the executor
    	
        getExecutor().execute(() -> {
            try {
                info.save();
            } catch(IOException e) {
                //throw new RuntimeException("Could not save world info!", e);
                log.postSevere("Could not save world info!", e);
            }
        });
        
        for(HostWorld dim : dimensions.values())
            dim.save();
        
        savePlayers();
    }
    
    private void savePlayers() {
        for(PlayerData p : players.values()) {
            p.lastPos.set(p.tracker.entity.pos);
            p.dimension = p.tracker.world.getDimensionName();
            
            this.getExecutor().execute(() -> {
                try {
                    p.save();
                } catch(IOException e) {
                    log.postSevere("Could not save player: " + p, e);
                }
            });
        }
    }
    
    @Override
    protected void closeExtra() {
        savePlayers();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Stores the world-local data of a player.
     * 
     * <p>TODO outdated docs
     */
    @NotThreadSafe
    public class PlayerData {
        
        /** The player's global data. */
        public final CharacterData data;
        /** Tracks the player so that we know what to save when save time
         * comes. */
        private CEntityTracker tracker;
        
        private final FileHandle file;
        
        
        /** Whether the character is new to the world. */
        public boolean newToWorld = true;
        /** The dimension the player is in. */
        public String dimension;
        /** The player's last known position. */
        public Position lastPos = Position.create();
        
        
        private PlayerData(CharacterData data) {
            this.data = data;
            file = info.getWorldDir().child(World.DIR_PLAYERS + data.hash + World.EXT_PLAYERS);
        }
        
        /**
         * Initialises the player data to the default values.
         */
        private void defaultData() {
            newToWorld = true;
            dimension = Dimension.defaultDimensionName();
            lastPos.set(0, 0, 0f, 0f); // TODO: let the default dimension initialise this
        }
        
        /**
         * @return true if loading was a success; false if an I/O error
         * occurred.
         */
        public boolean load() {
            if(file.exists()) {
                try {
                    DataCompound tag = IOUtil.read(file, Format.NBT, Compression.GZIP);
                    dimension = tag.getString("dimension");
                    lastPos.importFromCompound(tag.getCompound("lastPos"));
                    newToWorld = false;
                } catch(IOException e) {
                    log.postSevere("Could not load data for " + data + "!");
                    return false;
                }
            } else {
                defaultData();
            }
            return true;
        }
        
        /**
         * Saves the player data.
         */
        public void save() throws IOException {
            DataCompound tag = Format.NBT.newCompound();
            tag.put("dimension", dimension);
            lastPos.exportToCompound(tag.childCompound("lastPos"));
            IOUtil.writeSafe(file, tag, Compression.GZIP);
        }
        
    }
    
    /**
     * A convenience class which bundles the objects to be returned when a
     * player is added to the world.
     */
    public static class PlayerBundle {
        
        /** The world the player has been added to. */
        public final HostWorld world;
        /** The player entity. */
        public final Entity playerEntity;
        /** The player's data. */
        public final PlayerData playerData;
        
        private PlayerBundle(HostWorld world, Entity player, PlayerData data) {
            this.world = world;
            this.playerEntity = player;
            this.playerData = data;
        }
        
    }
    
}
