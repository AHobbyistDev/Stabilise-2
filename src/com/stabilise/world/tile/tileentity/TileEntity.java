package com.stabilise.world.tile.tileentity;

import static com.stabilise.util.collect.DuplicatePolicy.*;

import com.stabilise.entity.FixedGameObject;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.collect.InstantiationRegistry;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.IWorld;

/**
 * A tile entity contains additional data associated with a tile, and in
 * addition is able to be updated each tick.
 * 
 * <p>Tile entities may not be added to the world by another tile entity, and
 * and a queue for tile entities is hence not required.
 */
public abstract class TileEntity extends FixedGameObject {
	
	/** The tile entity registry. */
	private static final InstantiationRegistry<TileEntity> TILE_ENTITIES =
			new InstantiationRegistry<TileEntity>(4, THROW_EXCEPTION, TileEntity.class,
					Integer.TYPE, Integer.TYPE);
	
	// Register all tile entity types.
	static {
		TILE_ENTITIES.registerDefaultArgs(0, TileEntityChest.class);
		TILE_ENTITIES.registerDefaultArgs(1, TileEntityMobSpawner.class);
		
		TILE_ENTITIES.lock();
	}
	
	
	/**
	 * Creates a new tile entity.
	 * 
	 * <p>Subclasses should implement a constructor with identical arguments
	 * to this one for factory construction.
	 * 
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	protected TileEntity(int x, int y) {
		this(null, x, y);
	}
	
	/**
	 * Creates a new tile entity.
	 * 
	 * @param world The world in which the tile entity is to be placed.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	protected TileEntity(IWorld world, int x, int y) {
		super(world);
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @return {@code true} if this tile entity requires updates; {@code false}
	 * otherwise.
	 */
	public abstract boolean isUpdated();
	
	/**
	 * Updates this TileEntity, and then returns {@link #isDestroyed()}.
	 * 
	 * <p>{@code update()} is not invoked if {@code isDestroyed()} already
	 * returns {@code true}. This is due to the fact that tile entities are
	 * stored in a List rather than a Map by the world for updating; it is
	 * vastly more efficient to remove a TileEntity while iterating than when
	 * not.
	 */
	@Override
	public boolean updateAndCheck() {
		if(isDestroyed())
			return true;
		update();
		return isDestroyed();
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		// nothing in the default implementation
	}
	
	/**
	 * Handles being added to the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public abstract void handleAdd(IWorld world, int x, int y);
	
	/**
	 * Handles being removed from the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public abstract void handleRemove(IWorld world, int x, int y);
	
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
	 * 
	 * @return The NBT tag.
	 */
	public final NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.addInt("id", getID());
		tag.addInt("x", x);
		tag.addInt("y", y);
		writeNBT(tag);
		return tag;
	}
	
	/**
	 * Writes this tile entity's data to the specified compound NBT tag.
	 */
	protected abstract void writeNBT(NBTTagCompound tag);
	
	/**
	 * Reads the tile entity from the specified compound NBT tag.
	 */
	public abstract void fromNBT(NBTTagCompound tag);
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Instantiates a tile entity with the specified ID.
	 * 
	 * @param id The ID of the tile entity type.
	 * @param args The constructor arguments.
	 * 
	 * @return A new tile entity, or {@code null} if the specified ID is
	 * invalid.
	 * @throws RuntimeException if tile entity creation failed.
	 */
	public static TileEntity createTileEntity(int id, int x, int y) {
		return TILE_ENTITIES.instantiate(id, x, y);
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
	public static TileEntity createTileEntityFromNBT(NBTTagCompound tag) {
		TileEntity t = createTileEntity(tag.getInt("id"), tag.getInt("x"), tag.getInt("y"));
		if(t == null)
			return null;
		t.fromNBT(tag);
		return t;
	}
	
}
