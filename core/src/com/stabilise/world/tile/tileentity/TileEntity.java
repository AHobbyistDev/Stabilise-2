package com.stabilise.world.tile.tileentity;

import com.stabilise.entity.GameObject;
import com.stabilise.entity.Position;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * A tile entity contains additional data associated with a tile, and in
 * addition is able to be updated each tick.
 * 
 * <p>Tile entities may not be added to the world by another tile entity, and
 * and a queue for tile entities is hence not required.
 */
public abstract class TileEntity extends GameObject {
    
    /** The tile entity registry. */
    private static final TypeFactory<TileEntity> TILE_ENTITIES =
            new TypeFactory<>(new RegistryParams("TileEntities", 4));
    
    // Register all tile entity types.
    static {
        TILE_ENTITIES.register(0, TileEntityChest.class, TileEntityChest::new);
        TILE_ENTITIES.register(1, TileEntityMobSpawner.class, TileEntityMobSpawner::new);
        
        TILE_ENTITIES.lock();
    }
    
    public static void poke() {}
    
    
    /**
     * Creates a new tile entity.
     * 
     * <p>Subclasses should implement a constructor with identical arguments to
     * this one (that is to say, no arguments) for factory construction.
     */
    protected TileEntity() {
        // nothing to see here, move along
    }
    
    
    /**
     * @return {@code true} if this tile entity requires updates; {@code false}
     * otherwise.
     */
    public boolean requiresUpdates() {
        return this instanceof Updated;
    }
    
    /** Implement this to make {@link #requiresUpdates()} return true. */
    static interface Updated {}
    
    /**
     * Updates this tile entity iff {@link #isDestroyed()} returns {@code
     * false}, then returns {@code isDestroyed()}.
     * 
     * <p>If this method returns {@code true}, the {@code destroyed} flag is
     * cleared such that subsequent invocations of this method will return
     * {@code false} (unless, of course, this tile entity is {@link #destroy()
     * destroyed} again).
     */
    @Override
    public boolean updateAndCheck(World world) {
    	boolean ret = super.updateAndCheck(world);
    	// We reset the destroyed flag as to permit behaviour wherein a
    	// tile entity may add and remove itself from the update list
    	// repeatedly as it wills.
    	destroyed = false;
        return ret;
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        // nothing in the default implementation
    }
    
    /**
     * Handles being added to the world.
     * 
     * @param world The world.
     * @param pos The position of the tile entity.
     */
    public abstract void handleAdd(World world, Position pos);
    
    /**
     * Handles being removed from the world.
     * 
     * @param world The world.
     * @param pos The position of the tile entity.
     */
    public abstract void handleRemove(World world, Position pos);
    
    /**
     * @return The ID of this tile entity's type.
     */
    public final int getID() {
        return TILE_ENTITIES.getID(getClass());
    }
    
    /**
     * Writes the tile entity to an NBT tag. The returned tag compound contains
     * an integer tag "id" containing the value returned by {@link #getID()}
     * (the value for the {@code id} parameter of
     * {@link #createTileEntity(int, int, int)} required to produce a tile
     * entity of the same class), the "x" and "y" integer tags, and other tags
     * which are dependent on subclass implementations.
     */
    public final DataCompound toNBT() {
        DataCompound tag = DataCompound.create();
        tag.put("id", getID());
        pos.exportToCompound(tag);
        writeNBT(tag);
        return tag;
    }
    
    /**
     * Writes this tile entity's data to the specified compound NBT tag.
     */
    protected abstract void writeNBT(DataCompound tag);
    
    /**
     * Reads the tile entity from the specified compound NBT tag.
     */
    public abstract void fromNBT(DataCompound tag);
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Instantiates a tile entity with the specified ID.
     * 
     * @return A new tile entity, or {@code null} if the specified ID is
     * invalid.
     */
    public static TileEntity createTileEntity(int id) {
        return TILE_ENTITIES.create(id);
    }
    
    /**
     * Creates a tile entity object from its NBT representation. The given tag
     * compound should at least contain "id", "x" and "y" integer tags.
     * 
     * @param tag The compound tag from which to read the tile entity.
     * 
     * @return The tile entity, or {@code null} if it could not be constructed
     * for whatever reason.
     * @throws NullPointerException if {@code tag} is {@code null}.
     * @throws RuntimeException if tile entity creation failed.
     */
    public static TileEntity createTileEntityFromNBT(DataCompound tag) {
        TileEntity t = createTileEntity(tag.getInt("id"));
        if(t == null)
            return null;
        t.pos.importFromCompound(tag);
        t.fromNBT(tag);
        return t;
    }
    
}
