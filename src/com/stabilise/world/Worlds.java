package com.stabilise.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.core.GameClient;
import com.stabilise.core.Resources;
import com.stabilise.core.GameClient.WorldLoadHandle;
import com.stabilise.entity.Entity;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.task.ReturnTask;
import com.stabilise.util.concurrent.task.Task;
import com.stabilise.util.concurrent.task.TaskCallable;
import com.stabilise.util.concurrent.task.TaskHandle;
import com.stabilise.util.io.IOUtil;
import com.stabilise.world.multiverse.ClientMultiverse;
import com.stabilise.world.multiverse.HostMultiverse;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.multiverse.HostMultiverse.PlayerBundle;
import com.stabilise.world.multiverse.HostMultiverse.PlayerData;

@SuppressWarnings("deprecation")
public class Worlds {
    
    private Worlds() {}
    
    // WORLD MANAGEMENT STUFF -------------------------------------------------
    
    /**
     * Creates a new world with a random seed.
     * 
     * <p>Note that this does NOT check for whether or not a world by the same
     * name already exists. Such a check should be performed earlier.
     * 
     * @param worldName The world's name.
     * 
     * @return The WorldInfo object for the created world, or {@code null} if
     * the world could not be created.
     */
    public static WorldInfo createWorld(String worldName) {
        return createWorld(worldName, ThreadLocalRandom.current().nextLong());
    }
    
    /**
     * Creates a new world.
     * 
     * <p>Note that this does NOT check for whether or not a world by the same
     * name already exists. Such a check should be performed earlier.
     * 
     * @param worldName The world's name.
     * @param worldSeed The world's seed.
     * 
     * @return The WorldInfo object for the created world, or {@code null} if
     * the world could not be created.
     */
    public static WorldInfo createWorld(String worldName, long worldSeed) {
        // Handles the delegation of duplicate world names
        String originalWorldName = worldName;
        int iteration = 0;
        while(getWorldDir(worldName).exists()) {
            iteration++;
            worldName = originalWorldName + " - " + iteration;
        }
        
        WorldInfo info = new WorldInfo(worldName);
        
        info.name = originalWorldName;
        info.age = 0;
        info.seed = worldSeed;
        info.worldFormatVersion = -1;            // TODO: temporary value
        info.sliceFormatVersion = -1;            // TODO: temporary value
        info.creationDate = System.currentTimeMillis();
        info.lastPlayedDate = info.creationDate;
        
        try {
            info.save();
        } catch(IOException e) {
            Log.get().postSevere("Could not save world info during creation process!", e);
            return null;
        }
        
        return info;
    }
    
    /**
     * Gets a world's directory, given its name.
     * 
     * @param worldName The world's filesystem name.
     * 
     * @return The file representing the world's directory.
     * @throws NullPointerException if {@code worldName} is {@code null}.
     * @throws IllegalArgumentException if {@code worldName} is empty.
     */
    public static FileHandle getWorldDir(String worldName) {
        if(worldName.length() == 0)
            throw new IllegalArgumentException("The world name must not be empty!");
        return Resources.DIR_WORLDS.child(IOUtil.getLegalString(worldName) + "/");
    }
    
    /**
     * Gets the list of created worlds.
     * 
     * @return An array of created worlds.
     */
    public static WorldInfo[] getWorldsList() {
        FileHandle[] worldDirs = Resources.DIR_WORLDS.list();
        
        List<WorldInfo> worlds = new ArrayList<>(worldDirs.length);
        
        // Check all folders in the worlds directory and determine their
        // validity as worlds.
        for(int i = 0; i < worldDirs.length; i++) {
            try {
                WorldInfo info = new WorldInfo(worldDirs[i].name());
                info.load(); // throws IOE
                worlds.add(info);
            } catch(IOException e) {
                Log.get().postWarning("Could not load world info for world \""
                        + worldDirs[i].name() + "\"!" + ": "
                        + e.getClass().getSimpleName() + ": " + e.getMessage());
                continue;
            }
        }
        
        // Now, we convert the ArrayList to a conventional array
        WorldInfo[] worldArr = worlds.toArray(new WorldInfo[worlds.size()]);
        Arrays.sort(worldArr); // WorldInfo implements Comparable
        return worldArr;
    }
    
    /**
     * Deletes a world. All world files will be removed permanently from the
     * file system.
     * 
     * @param worldName The world's filesystem name.
     * 
     * @throws NullPointerException if {@code worldName} is {@code null}.
     * @throws IllegalArgumentException if {@code worldName} is empty.
     */
    public static void deleteWorld(String worldName) {
        getWorldDir(worldName).deleteDirectory();
    }
    
    /**
     * Creates and returns a new {@link WorldBuilder} to use to construct a
     * world.
     */
    public static WorldBuilder builder() {
        return new WorldBuilder();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A WorldBuilder is a builder used to construct and set up {@link
     * Multiverse} objects in an easy, concise, and consistent manner.
     */
    public static class WorldBuilder {
        
        /** The info of the world. Null if client-only. */
        private WorldInfo worldInfo = null;
        /** The data of the integrated player. Null if server only. */
        private CharacterData integratedPlayer = null;
        /** The GameClient through which to communicate to the host server.
         * Null unless multiplayer client. */
        private GameClient client = null;
        /** Profiler to use for the world. May be null. */
        private Profiler profiler = null;
        
        private boolean building = false;
        
        private WorldBuilder() {}
        
        private void checkState() {
            if(building)
                throw new IllegalStateException("Already building or built!");
        }
        
        /**
         * Sets the world. This will throw an exception if the client has
         * already been set via {@link #setClient(GameClient)}, as a client
         * does not know the info of the world it is visiting.
         * 
         * @param worldName The world's filesystem name.
         * 
         * @return This WorldBuilder.
         * @throws NullPointerException if {@code worldName} is {@code null}.
         * @throws IllegalStateException if this builder has already built the
         * multiverse, or the world has already been set, or the client has
         * already been set.
         */
        public WorldBuilder setWorld(String worldName) {
            return setWorld(new WorldInfo(Objects.requireNonNull(worldName)));
        }
        
        /**
         * Sets the world. This will throw an exception if the client has
         * already been set via {@link #setClient(GameClient)}, as a client
         * does not know the info of the world it is visiting.
         * 
         * @param worldInfo The world's info. This may or may not have already
         * been loaded.
         * 
         * @return This WorldBuilder.
         * @throws NullPointerException if {@code worldInfo} is {@code null}.
         * @throws IllegalStateException if this builder has already built the
         * multiverse, or the world has already been set, or the client has
         * already been set.
         */
        public WorldBuilder setWorld(WorldInfo worldInfo) {
            checkState();
            if(this.worldInfo != null)
                throw new IllegalStateException("World already set!");
            if(client != null)
                throw new IllegalStateException("Cannot set both client and world");
            this.worldInfo = Objects.requireNonNull(worldInfo);
            return this;
        }
        
        /**
         * Sets the integrated player.
         * 
         * @param characterName The name of the character.
         * 
         * @return This WorldBuilder.
         * @throws NullPointerException if {@code characterName} is {@code
         * null}.
         * @throws IllegalStateException if this builder has already built the
         * multiverse, or the integrated player has already been set.
         */
        public WorldBuilder setPlayer(String characterName) {
            return setPlayer(new CharacterData(Objects.requireNonNull(characterName)));
        }
        
        /**
         * Sets the integrated player.
         * 
         * @param character The character's data. This may or may not be
         * already loaded.
         * 
         * @return This WorldBuilder.
         * @throws NullPointerException if {@code character} is {@code null}.
         * @throws IllegalStateException if this builder has already built the
         * multiverse, or the integrated player has already been set.
         */
        public WorldBuilder setPlayer(CharacterData character) {
            checkState();
            if(integratedPlayer != null)
                throw new IllegalStateException("Player already set!");
            integratedPlayer = Objects.requireNonNull(character);
            return this;
        }
        
        /**
         * Sets the client with which to communicate with the server that is
         * hosting the world. This will throw an exception if the world has
         * already been set via either {@link #setWorld(String)} or {@link
         * #setWorld(WorldInfo)}, as a client does not know the info of the
         * world it is visiting.
         * 
         * @param client The client.
         * @param loadHandle The handle through which to detect world loading.
         * 
         * @return This WorldBuilder.
         * @throws NullPointerException if either argument is {@code null}.
         * @throws IllegalStateException if this builder has already built the
         * multiverse, or the client has already been set, or the world
         * has been set.
         */
        @Deprecated
        public WorldBuilder setClient(GameClient client, WorldLoadHandle loadHandle) {
            checkState();
            if(this.client != null)
                throw new IllegalStateException("Client already set!");
            if(worldInfo != null)
                throw new IllegalStateException("Cannot set both client and world"
                        + " info!");
            this.client = Objects.requireNonNull(client);
            //----this.loadHandle = Objects.requireNonNull(loadHandle);
            return this;
        }
        
        /**
         * Sets the profiler to use to profile each world.
         * 
         * @return This WorldBuilder.
         * @throws NullPointerException if {@code profiler} is {@code null}.
         * @throws IllegalStateException if this builder has already built the
         * multiverse, or the profiler has already been set.
         */
        public WorldBuilder setProfiler(Profiler profiler) {
            checkState();
            if(this.profiler != null)
                throw new IllegalStateException("Profiler already set!");
            this.profiler = Objects.requireNonNull(profiler);
            return this;
        }
        
        /**
         * Builds a HostMultiverse, and optionally, if an integrated player has
         * been set, that player's PlayerData, entity, and initial world.
         * 
         * @throws IllegalStateException if the world has already been built,
         * or the world has not been set.
         */
        public ReturnTask<WorldBundle> buildHost() {
            return build(true);
        }
        
        /**
         * Builds a ClientMultiverse, and the integrated player's entity and
         * initial world.
         * 
         * @throws IllegalStateException if the world has already been built,
         * or either the player or client have not been set.
         */
        public ReturnTask<WorldBundle> buildClient() {
            return build(false);
        }
        
        private ReturnTask<WorldBundle> build(final boolean buildHost) {
            checkState();
            building = true;
            
            // Make sure we've set the right parameters for the requested
            // build before we begin building.
            if(buildHost) {
                if(worldInfo == null)
                    throw new IllegalStateException("Cannot create a host "
                            + "world without setting the world!");
            } else {
                if(client == null)
                    throw new IllegalStateException("Cannot create a client "
                            + "world without setting the game client!");
                if(integratedPlayer == null)
                    throw new IllegalStateException("Cannot create a client "
                            + "world without setting the player!");
            }
            
            Box<HostMultiverse> bMulti    = Boxes.emptyMut();
            Box<PlayerBundle> bPlayer     = Boxes.emptyMut();
            Box<HostWorld> bWorld         = Boxes.emptyMut();
            Box<WorldLoadTracker> bStatus = Boxes.emptyMut();
            
            return Task.builder(Tasks.newThreadExecutor())
                .name("Loading world")
                .beginReturn(WorldBundle.class)
                .andThen(200, (t) -> {
                    t.setStatus("Loading player data");
                    if(integratedPlayer != null) // host or client
                        integratedPlayer.load();
                    
                    if(buildHost) {
                        t.next(50, "Loading world data");
                        worldInfo.load();
                        t.next(50, "Starting up the world");
                        bMulti.set(new HostMultiverse(worldInfo, profiler));
                        t.next(50, "Creating the player");
                        bPlayer.set(bMulti.get().addPlayer(integratedPlayer, true));
                        bWorld.set(bPlayer.get().world);
                        bStatus.set(bWorld.get().loadTracker());
                    } else {
                        throw new RuntimeException("Client is NYI");
                    }
                }).andThenReturn(1000, new TaskCallable<WorldBundle>() {
                    @Override
                    public WorldBundle run(TaskHandle t) throws Exception {
                        t.setTotal(bStatus.get().numTotal());
                        HostWorld w = bWorld.get();
                        t.setStatus("Loading dimension " + w.getDimensionName());
                        do {
                            t.set(bStatus.get().numDone());
                            bStatus.get().waitUntilNext();
                            w.forEachRegion(r -> { if(r.isPrepared()) r.tryImport(w); });
                        } while(!bStatus.get().isDone());
                        return new WorldBundle(bMulti.get(), w,
                                bPlayer.get().playerEntity, bPlayer.get().playerData);
                    }
                    
                })
                .start();
        }
        
    }
    
    /**
     * Encapsulates the items which may be built and returned by a {@link
     * World#builder() WorldBuilder}.
     */
    public static class WorldBundle {
        
        /** The multiverse. This should be cast to a {@link HostMultiverse}
         * if a host was built, or a {@link ClientMultiverse} if a client was
         * built. */
        private final Multiverse<?> multiverse;
        /** The world in which the integrated player has been placed. This
         * should be cast to a {@link HostWorld} if a host was built, and
         * to a {@link ClientWorld} if a client was built. This is {@code null}
         * if the world was built without setting an integrated player (i.e. if
         * a server was constructed). */
        private final AbstractWorld world;
        /** The player entity. This is {@code null} if no player was
         * specified. */
        private final Entity playerEntity;
        /** The world-specific player data. This is non-null iff a host was
         * built with an integrated player specified. */
        private final PlayerData playerData;
        
        private WorldBundle(Multiverse<?> multiverse, AbstractWorld world,
                Entity playerEntity, PlayerData playerData) {
            this.multiverse = multiverse;
            this.world = world;
            this.playerEntity = playerEntity;
            this.playerData = playerData;
        }
        
        /**
         * Gets this bundle's HostMultiverse.
         * 
         * @throws IllegalStateException if this is a client bundle.
         */
        public HostMultiverse getHostMultiverse() {
            if(!(multiverse instanceof HostMultiverse))
                throw new IllegalStateException("Not a host bundle!");
            return (HostMultiverse)multiverse;
        }
        
        /**
         * Gets this bundle's ClientMultiverse.
         * 
         * @throws IllegalStateException if this is a host bundle.
         */
        public ClientMultiverse getClientMultiverse() {
            if(!(multiverse instanceof ClientMultiverse))
                throw new IllegalStateException("Not a client bundle!");
            return (ClientMultiverse)multiverse;
        }
        
        /**
         * Gets the integrated player's initial world.
         * 
         * @throws IllegalStateException if there is no integrated player, or
         * this is a client bundle.
         */
        public HostWorld getHostWorld() {
            if(world == null)
                throw new IllegalStateException("No integrated player!");
            if(!(world instanceof HostWorld))
                throw new IllegalStateException("Not a host bundle!");
            return (HostWorld)world;
        }
        
        /**
         * Gets the integrated player's initial world.
         * 
         * @throws IllegalStateException if there is no integrated player, or
         * this is a host bundle.
         */
        public ClientWorld getClientWorld() {
            if(world == null)
                throw new IllegalStateException("No integrated player!");
            if(!(world instanceof ClientWorld))
                throw new IllegalStateException("Not a client bundle!");
            return (ClientWorld)world;
        }
        
        /**
         * Gets the integrated player's PlayerData, for use by a host.
         * 
         * @throws IllegalStateException if there is no integrated player, or
         * this is a client bundle.
         */
        public PlayerData getPlayerData() {
            if(playerData == null)
                throw new IllegalStateException("Either no integrated player or"
                        + " this is not a host bundle.");
            return playerData;
        }
        
        /**
         * Gets the integrated player's in-game entity.
         * 
         * @throws IllegalStateException if there is no integrated player.
         */
        public Entity getPlayerEntity() {
            if(playerEntity == null)
                throw new IllegalStateException("Not integrated player!");
            return playerEntity;
        }
        
    }
    
}
