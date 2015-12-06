package com.stabilise.world.dimension;

import java.io.IOException;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.character.CharacterData;
import com.stabilise.util.collect.registry.DuplicatePolicy;
import com.stabilise.util.collect.registry.Registry;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.GeneralTypeFactory.ReflectiveFactory;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.world.AbstractWorld;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multiverse.Multiverse;

/**
 * A Dimension is a distinct 'world' within the game. Each multiverse may
 * have multiple dimensions/worlds.
 */
public abstract class Dimension {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** Registry of dimensions. */
    private static final Registry<String, Class<? extends Dimension>> DIMENSIONS =
            new Registry<>(new RegistryParams("Dimensions", 2, DuplicatePolicy.THROW_EXCEPTION));
    
    /** The default dimension name. This is {@code null} until set by {@link
     * #registerDimensions()}. */
    private static String defaultDim = null;
    /** The internal name for the private dimension. This is {@code null} until
     * set by {@link #registerDimensions()}. */
    private static String privateDim = null;
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** This dimension's info. Never null. */
    public final Info info;
    
    
    /**
     * Every subclass of Dimension should have this constructor.
     */
    protected Dimension(Info info) {
        this.info = info;
    }
    
    /**
     * Creates the HostWorld object upon which this dimension will be hosted.
     * Subclasses may override this to return a custom implementation of
     * HostWorld to implement dimension-specific logic.
     * 
     * @param multiverse The multiverse.
     * 
     * @throws NullPointerException if {@code multiverse} is {@code null}.
     */
    public HostWorld createHost(Multiverse<? extends AbstractWorld> multiverse) {
        return new HostWorld(multiverse, this);
    }
    
    /**
     * Creates the ClientWorld object upon which this dimension will be used.
     * Subclasses may override this to return a custom implementation of
     * ClientWorld to implement dimension-specific features.
     * 
     * @param multiverse The multiverse.
     * 
     * @throws NullPointerException if {@code multiverse} is {@code null}.
     */
    public ClientWorld createClient(Multiverse<AbstractWorld> multiverse) {
        return new ClientWorld(multiverse, this);
    }
    
    /**
     * Creates the world generator to use for generating this dimension.
     * 
     * @param multiverse The multiverse.
     * @param world This dimension's host world.
     * 
     * @return The world generator.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public WorldGenerator generatorFor(Multiverse<?> multiverse,
            HostWorld world) {
        return new WorldGenerator(multiverse, world);
    }
    
    /**
     * Returns {@code true} if this dimension should have perpetually-loaded
     * spawn regions; {@code false} otherwise.
     */
    public boolean hasSpawnRegions() {
        // For now, only the default dimension has spawn regions
        return info.name.equals(defaultDimensionName());
    }
    
    /**
     * Loads this dimension's info, if it exists on the filesystem.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public final void loadData() throws IOException {
        if(info.getFile().exists())
            loadExtraData(info.load());
    }
    
    /**
     * This method is invoked by {@link #loadData()} and does nothing in the
     * default implementation.
     * 
     * <p>Subclasses may optionally implement this to read additional data from
     * a dimension's info file.
     * 
     * @param tag The NBT compound tag from which to read the data.
     */
    protected void loadExtraData(DataCompound tag) {
        // nothing to see here, move along
    }
    
    /**
     * Saves this dimension's info.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public final void saveData() throws IOException {
        DataCompound tag = Format.NBT.newCompound(true);
        saveExtraData(tag);
        info.save(tag);
    }
    
    /**
     * This method is invoked by {@link #saveData()} and does nothing in the
     * default implementation.
     * 
     * <p>Subclasses may optionally implement this to write additional data to
     * a dimension's info file.
     * 
     * @param tag The NBT compound tag to which to write the data.
     */
    protected void saveExtraData(DataCompound tag) {
        // nothing to see here, move along
    }
    
    @Override
    public int hashCode() {
        return info.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o == this;
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Gets an instance of a Dimension. This method should be used to get the
     * Dimension object for a host world. For a non-host (i.e. client-only
     * variant), use {@link #getDimension(String)} instead.
     * 
     * @param worldInfo The WorldInfo object of the dimension's parent world.
     * @param dimName The name of the dimension.
     * 
     * @return The Dimension, or {@code null} if there is no such dimension
     * with the specified name.
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IllegalStateException if the dimensions have not yet been
     * {@link #registerDimensions() registered}.
     * @throws RuntimeException if the Dimension object could not be
     * instantiated.
     */
    public static Dimension getDimension(WorldInfo worldInfo, String dimName) {
        return getDimension(new Info(worldInfo, dimName));
    }
    
    /**
     * Gets an instance of a Dimension. This method should be used to get the
     * Dimension object for a non-host (i.e. client-only) world. For a host
     * variant, use {@link #getDimension(WorldInfo, String)} instead.
     * 
     * @param dimName The name of the dimension.
     * 
     * @return The Dimension, or {@code null} if there is no such dimension
     * with the specified name.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws IllegalStateException if the dimensions have not yet been
     * {@link #registerDimensions() registered}.
     * @throws RuntimeException if the Dimension object could not be
     * instantiated.
     */
    public static Dimension getDimension(String dimName) {
        return getDimension(new Info(dimName));
    }
    
    /**
     * Gets an instance of a Dimension.
     * 
     * @param info The dimension info.
     * 
     * @return The Dimension, or {@code null} if there is no such dimension
     * with the specified name.
     * @throws NullPointerException if {@code info} is {@code null}.
     * @throws IllegalStateException if the dimensions have not yet been
     * {@link #registerDimensions() registered}.
     * @throws RuntimeException if the Dimension object could not be
     * instantiated.
     */
    private static Dimension getDimension(Info info) {
        if(!DIMENSIONS.isLocked())
            throw new IllegalStateException("Dimensions have not yet been registered!");
        Class<? extends Dimension> dimClass = DIMENSIONS.get(info.name);
        if(dimClass == null)
            return null;
        // Hijack ReflectiveFactory from InstantiationRegistry to avoid needing
        // to deal with reflection
        return new ReflectiveFactory<>(dimClass, Info.class).create(info);
    }
    
    /**
     * Gets an instance of the private Dimension.
     * 
     * @param character The character data of the character's dimension.
     * 
     * @return The Dimension.
     * @throws NullPointerException if {@code character} is {@code null}.
     * @throws IllegalStateException if the dimensions have not yet been
     * {@link #registerDimensions() registered}.
     * @throws RuntimeException if the Dimension object could not be
     * instantiated.
     */
    public static Dimension getPrivateDimension(CharacterData character) {
        return getDimension(new Info(character)); // should never be null
    }
    
    /**
     * @return The name of the default dimension, or {@code null} if the
     * the dimensions have not yet been {@link #registerDimensions()
     * registered}.
     */
    public static String defaultDimensionName() {
        return defaultDim;
    }
    
    /**
     * Registers all the dimensions.
     * 
     * @throws IllegalStateException if the dimensions have already been
     * registered.
     */
    public static void registerDimensions() {
        registerDimension(true, "overworld", DimOverworld.class);
        registerPrivateDimension("privateDim", DimPrivate.class);
        
        DIMENSIONS.lock();
        if(defaultDim == null)
            throw new IllegalStateException("A default dimension must be set!");
        if(privateDim == null)
            throw new IllegalStateException("The private dimension must be set!");
    }
    
    /**
     * Registers a dimension. Note that it is legal to map multiple dimension
     * names to the same class if all such dimensions are to have the same
     * functionality.
     * 
     * <p>This should only be invoked from {@link #registerDimensions()}.
     * 
     * @param isDefault Whether or not this is the default dimension.
     * @param name The name of the dimension.
     * @param dimClass The dimension's class.
     * 
     * @throws IllegalStateException if {@code isDefault} is {@code true}, but
     * the default dimension has already been set.
     * @throws RuntimeException see {@link Registry#register(Object, Object)}.
     */
    private static void registerDimension(boolean isDefault, String name,
            Class<? extends Dimension> dimClass) {
        if(isDefault) {
            if(defaultDim != null)
                throw new IllegalStateException("Default dimension had already been set!");
            else
                defaultDim = name;
        }
        DIMENSIONS.register(name, dimClass);
    }
    
    /**
     * Registers the private dimension.
     * 
     * <p>This should only be invoked from {@link #registerDimensions()}.
     * 
     * @param name The name of the dimension.
     * @param dimClass The dimension's class.
     * 
     * @throws IllegalStateException if the private dimension has already been
     * set.
     * @throws RuntimeException see {@link Registry#register(Object, Object)}.
     */
    private static void registerPrivateDimension(String name, Class<? extends Dimension> dimClass) {
        if(privateDim != null)
            throw new IllegalStateException("Private dimension had already been set!");
        else
            privateDim = name;
        DIMENSIONS.register(name, dimClass);
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Contains information specific to a dimension.
     */
    public static class Info {
        
        /** This dimension's directory. */
        private final FileHandle dir;
        
        /** {@code true} if this dimension is a player-local client-only
         * private dimension; {@code false} otherwise. For all standard
         * dimensions this is {@code false}. */
        public final boolean privateDimension;
        
        /** The name of the dimension. Never null. */
        public final String name;
        /** The age of this dimension. This is volatile. */
        // see WorldLoader.saveRegion() for why this is volatile
        public volatile long age = 0L;
        
        /** The coordinates of the slice in which players should initially spawn,
         * in slice-lengths. */
        public int spawnSliceX = 0, spawnSliceY = 0;
        
        
        /**
         * Dimension info for some dimension
         * @throws NullPointerException if either arg is null
         */
        private Info(WorldInfo worldInfo, String name) {
            this.name = Objects.requireNonNull(name);
            privateDimension = false;
            dir = worldInfo.getWorldDir().child(World.DIR_DIMENSIONS).child(name + "/");
        }
        
        /**
         * Dimension info for a client view of some dimension
         * @throws NullPointerException if name is null
         */
        private Info(String name) {
            this.name = Objects.requireNonNull(name);
            privateDimension = false;
            dir = null;
        }
        
        /**
         * Dimension info for a character's private dimension
         * @throws NullPointerException if either arg is null
         */
        private Info(CharacterData character) {
            name = privateDim;
            privateDimension = true;
            dir = character.getDimensionDir();
        }
        
        /**
         * Loads the dimension info.
         * 
         * @throws IOException if the info file does not exist or an I/O error
         * otherwise occurs.
         */
        private DataCompound load() throws IOException {
            DataCompound tag = IOUtil.read(Format.NBT, Compression.GZIP, getFile());
            
            if(!name.equals(tag.getString("dimName")))
                throw new IOException("Dimension name does not match stored name \""
                        + tag.getString("dimName") + "\"");
            
            age = tag.getLong("age");
            
            spawnSliceX = tag.getInt("spawnX");
            spawnSliceY = tag.getInt("spawnY");
            
            return tag;
        }
        
        /**
         * Saves the dimension info.
         * 
         * @throws IOException if an I/O error occurs.
         */
        private void save(DataCompound tag) throws IOException {
            tag.put("dimName", name);
            tag.put("age", age);
            
            tag.put("spawnX", spawnSliceX);
            tag.put("spawnY", spawnSliceY);
            
            IOUtil.writeSafe(tag, Format.NBT, Compression.GZIP, getFile());
        }
        
        /**
         * @return This dimension's filesystem directory.
         */
        public FileHandle getDimensionDir() {
            return dir;
        }
        
        /**
         * @return This DimensionInfo's filesystem location.
         */
        public FileHandle getFile() {
            if(dir == null)
                throw new IllegalStateException("Cannot get the file for a "
                        + "client's view of a dimension!");
            return dir.child(World.FILE_INFO);
        }
        
        /**
         * @return {@code true} if this DimensionInfo file exists on the
         * filesystem; {@code false} otherwise.
         */
        public boolean fileExists() {
            return getFile().exists();
        }
        
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            return o == this;
        }
        
    }
    
}
