package com.stabilise.world.multiverse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.entity.Entity;
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
 */
public class HostMultiverse extends Multiverse<HostWorld> {
    
    /** Dimensions should treat this as read-only. */
    public final WorldInfo info;
    
    /** Stores players using this world. Maps player hash -> PlayerData. */
    private final Map<String, PlayerData> players = new HashMap<>(2);
    
    
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
        super(profiler);
        this.info = Objects.requireNonNull(info);
    }
    
    @Override
    public void update() {
        info.age++;
        super.update();
    }
    
    @Override
    @ThreadUnsafeMethod
    public HostWorld loadDimension(String name) {
        HostWorld world = getDimension(name);
        if(world != null)
            return world;
        
        Dimension dim = Dimension.getDimension(info, name);
        if(dim == null)
            throw new IllegalArgumentException("Invalid dim: \"" + name + "\"");
        
        try {
            dim.loadData();
        } catch(IOException e) {
            throw new RuntimeException("Could not load dimension info! (dim: " +
                    name + ") (" + e.getMessage() + ")" , e);
        }
        
        world = dim.createHost(this);
        world.prepare();
        
        dimensions.put(name, world);
        
        return world;
    }
    
    /**
     * Adds a player to a world.
     * 
     * @param player The CharacterData for the player to add.
     * @param integrated {@code true} if this player should become that of the
     * integrated client.
     * 
     * @return The PlayerBundle holding the player entity, data, and world the
     * player was added to, or {@code null} if the player data could not be
     * loaded, or the dimension could not be loaded.
     * @throws NullPointerException if {@code player} is {@code null}.
     */
    @ThreadUnsafeMethod
    public PlayerBundle addPlayer(CharacterData player, boolean integrated) {
        PlayerData data = players.get(player.hash);
        if(data == null)
            data = new PlayerData(player);
        if(!data.load())
            return null;
        players.putIfAbsent(player.hash, data);
        HostWorld world = loadDimension(data.dimension);
        Entity playerEntity = world.addPlayer(data);
        if(integrated) {
            integratedClient = true;
            integratedCharacter = player;
            integratedPlayer = playerEntity;
        }
        return new PlayerBundle(world, playerEntity, data);
    }
    
    /**
     * doesn't do anything yet
     * @param player
     */
    public void removePlayer(CharacterData player) {
        // TODO
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
        try {
            info.save();
        } catch(IOException e) {
            throw new RuntimeException("Could not save world info!", e);
        }
        
        for(HostWorld dim : dimensions.values())
            dim.save();
        
        for(PlayerData p : players.values()) {
            p.lastX = p.playerMob.x;
            p.lastY = p.playerMob.y;
            try {
                p.save();
            } catch(IOException e) {
                throw new RuntimeException("Could not save player!", e);
            }
        }
    }
    
    @Override
    protected void closeExtra() {
        for(PlayerData p : players.values()) {
            p.lastX = p.playerMob.x;
            p.lastY = p.playerMob.y;
            try {
                p.save();
            } catch(IOException e) {
                throw new RuntimeException("Could not save " + p, e);
            }
        }
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Stores the world-local data of a player.
     * 
     * <p>TODO outdated docs
     * <p>An instance of this class should be {@link PlayerData#dispose()
     * disposed} of when it is no longer needed.
     */
    @NotThreadSafe
    public class PlayerData {
        
        /** The player's global data. */
        public final CharacterData data;
        
        public Entity playerMob;
        
        private final FileHandle file;
        
        /** Whether or not the character is new to the world. */
        public boolean newToWorld = true;
        /** The dimension the player is in. */
        public String dimension;
        /** The coordinates of the player's last known location, in
         * tile-lengths. */
        public double lastX, lastY;
        
        
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
            lastX = lastY = 0D; // TODO: let the default dimension initialise this
        }
        
        /**
         * @return true if loading was a success; false if an I/O error
         * occurred.
         */
        public boolean load() {
            if(file.exists()) {
                try {
                    DataCompound tag = IOUtil.read(Format.NBT, Compression.GZIP, file);
                    dimension = tag.getString("dimension");
                    lastX = tag.getDouble("lastX");
                    lastY = tag.getDouble("lastY");
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
            DataCompound tag = Format.NBT.create(true);
            tag.put("dimension", dimension);
            tag.put("lastX", lastX);
            tag.put("lastY", lastY);
            IOUtil.writeSafe(tag, Format.NBT, Compression.GZIP, file);
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
