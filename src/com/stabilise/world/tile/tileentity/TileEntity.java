package com.stabilise.world.tile.tileentity;

import java.lang.reflect.Constructor;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.stabilise.util.Log;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.World;

/**
 * A tile entity contains additional data associated with a tile, and in
 * addition is able to be updated each tick.
 * 
 * <p>Tile entities may not be added to the world by another tile entity, and
 * and a queue for tile entities is hence not required.
 */
public abstract class TileEntity {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** A map of each tile entity's ID and class. */
	private static DualHashBidiMap<Integer, Class<? extends TileEntity>> tileEntityMap = new DualHashBidiMap<Integer, Class<? extends TileEntity>>();
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** A reference to the world the tile entity is in. */
	public World world;
	/** The x-coordinate of the tile entity, in tile-lengths. */
	public int x;
	/** The y-coordinate of the tile entity, in tile-lengths. */
	public int y;
	
	
	/**
	 * Creates a new tile entity.
	 * 
	 * <p>Note that is is <b>absolutely crucial</b> that subclasses of
	 * TileEntity implement a constructor with the same arguments as this for
	 * the purpose of reflective construction.
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
	protected TileEntity(World world, int x, int y) {
		this.world = world;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Updates the tile entity.
	 */
	public abstract void update();
	
	/**
	 * Handles being added to the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public abstract void handleAdd(World world, int x, int y);
	
	/**
	 * Handles being removed from the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public abstract void handleRemove(World world, int x, int y);
	
	/**
	 * Gets the tile entity's class ID.
	 * 
	 * @return The ID of the tile entity's class.
	 */
	public final int getID() {
		return tileEntityMap.getKey(getClass());
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
	 * Writes the tile entity's data to an NBT tag.
	 * 
	 * @param tag The tag to write the tile entity's data to.
	 */
	protected abstract void writeNBT(NBTTagCompound tag);
	
	/**
	 * Reads the tile entity from an NBT tag.
	 * 
	 * @param tag The NBT tag.
	 */
	public abstract void fromNBT(NBTTagCompound tag);
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Registers and lists a tile entity type so that it may be properly
	 * referenced.
	 * 
	 * @param id The tile entity's ID.
	 * @param entityClass The tile entity's class.
	 */
	private static void registerTileEntity(int id, Class<? extends TileEntity> entityClass) {
		if(tileEntityMap.containsKey(id)) {
			Log.critical("Could not register tile entity class " + entityClass.toString() + " with ID " + id +
					" - ID is already being used by tile entity class " + tileEntityMap.get(id).toString() + "!");
			return;
		} else if(tileEntityMap.containsValue(entityClass)) {
			Log.critical("Could not register tile entity class " + entityClass.toString() + " with ID " + id +
					" - class is already registed to ID " + tileEntityMap.getKey(entityClass) + "!");
			return;
		}
		
		tileEntityMap.put(id, entityClass);
	}
	
	/**
	 * Creates a TileEntity object.
	 * 
	 * @param id The ID of the tile entity, as would be given by its
	 * {@link #getID()} method. 
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 * 
	 * @return A TileEntity object of class determined by the {@code id}
	 * parameter, or {@code null} if the {@code id} parameter is invalid or
	 * the tile entity could not be constructed for whatever reason.
	 */
	public static TileEntity createTileEntity(int id, int x, int y) {
		Class<? extends TileEntity> teClass = tileEntityMap.get(id);
		if(teClass == null)
			return null;
		try {
			Constructor<? extends TileEntity> c = teClass.getConstructor(Integer.TYPE, Integer.TYPE);
			return c.newInstance(x, y);
		//} catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		} catch(Exception e) {
			Log.critical("Could not reflectively instantiate tile entity of id " + id + "!");
			//throw new RuntimeException(e);
		}
		return null;
	}
	
	/**
	 * Creates a tile entity object from its NBT representation. The given tag
	 * compound should at least contain "id", "x" and "y" integer tags.
	 * 
	 * @param tag The compound tag from which to read the tile entity.
	 * 
	 * @return The tile entity, or {@code null} if it could not be constructed
	 * for whatever reason.
	 * @throws IllegalArgumentException if {@code tag} is {@code null}.
	 */
	public static TileEntity createTileEntityFromNBT(NBTTagCompound tag) {
		if(tag == null)
			throw new IllegalArgumentException("NBT Tag is null!");
		TileEntity t = createTileEntity(tag.getInt("id"), tag.getInt("x"), tag.getInt("y"));
		if(t == null)
			return null;
		t.fromNBT(tag);
		return t;
	}
	
	// Register all tile entity types.
	static {
		registerTileEntity(0, TileEntityChest.class);
		registerTileEntity(1, TileEntityMobSpawner.class);
	}
	
}
