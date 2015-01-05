package com.stabilise.world.tile.tileentity;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.stabilise.util.collect.RegistryNamespaced;
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
	
	/** The tile entity registry. */
	private static final RegistryNamespaced<TEFactory> TILE_ENTITIES =
			new RegistryNamespaced<TEFactory>("tile entities", "stabilise", 4);
	/** The map of tile entity classes to their factory. */
	private static final Map<Class<? extends TileEntity>, TEFactory> CLASS_MAP =
			new HashMap<Class<? extends TileEntity>, TEFactory>(4);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** A reference to the world the tile entity is in. */
	public World world;
	/** The x/y coordinates of this tile entity, in tile-lengths. */
	public int x, y;
	
	
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
	 * @return The ID of this tile entity.
	 */
	public final int getID() {
		// Could throw an NPE if this TE wasn't registered properly
		return TILE_ENTITIES.getObjectID(CLASS_MAP.get(getClass()));
	}
	
	/**
	 * @return The name of this tile entity.
	 */
	public final String getName() {
		// Could throw an NPE if this TE wasn't registered properly
		return TILE_ENTITIES.getObjectName(CLASS_MAP.get(getClass()));
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
	 * @throws RuntimeException if the tile entity corresponding to the ID was
	 * registered incorrectly.
	 */
	public static TileEntity createTileEntity(int id, int x, int y) {
		TEFactory creator = TILE_ENTITIES.get(id);
		if(creator == null)
			return null;
		return creator.create(x, y);
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
	 */
	public static TileEntity createTileEntityFromNBT(NBTTagCompound tag) {
		TileEntity t = createTileEntity(tag.getInt("id"), tag.getInt("x"), tag.getInt("y"));
		if(t == null)
			return null;
		t.fromNBT(tag);
		return t;
	}
	
	// Register all tile entity types.
	static {
		registerTileEntity(0, "Chest", TileEntityChest.class);
		registerTileEntity(1, "Mob Spawner", TileEntityMobSpawner.class);
	}
	
	/**
	 * Registers a tile entity.
	 * 
	 * @param id The ID of the tile entity.
	 * @param name The name of the tile entity.
	 * @param teClass The tile entity's class.
	 * 
	 * @throws RuntimeException if the specified class does not have a
	 * constructor accepting only two integer parameters (i.e. it doesn't have
	 * a constructor corresponding to {@code new TileEntity(x, y)}).
	 * @throws IndexOufOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if either {@code name} or {@code teClass}
	 * are {@code null}.
	 */
	private static void registerTileEntity(int id, String name, Class<? extends TileEntity> teClass) {
		registerTileEntity(id, name, teClass, new ReflectiveTEFactory(teClass));
	}
	
	/**
	 * Registers a tile entity.
	 * 
	 * @param id The ID of the tile entity.
	 * @param name The name of the tile entity.
	 * @param teClass The tile entity's class.
	 * @param factory The factory object with which to create instances of the
	 * tile entity.
	 * 
	 * @throws IndexOufOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if either {@code name} or {@code factory}
	 * are {@code null}.
	 */
	private static void registerTileEntity(int id, String name, Class<? extends TileEntity> teClass,
			TEFactory factory) {
		TILE_ENTITIES.register(id, name, factory);
		CLASS_MAP.put(teClass, factory);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A tile entity factory object is used to instantiate a TileEntity.
	 */
	public static interface TEFactory {
		
		/**
		 * Creates the tile entity.
		 * 
		 * @param x The x-coordinate of the tile entity, in tile-lengths.
		 * @param y The y-coordinate of the tile entity, in tile-lengths.
		 * 
		 * @return The tile entity.
		 * @throws RuntimeException if this TEFactory is a derp.
		 */
		TileEntity create(int x, int y);
		
	}
	
	/**
	 * A tile entity factory which reflectively instantiates tile entities.
	 */
	static final class ReflectiveTEFactory implements TEFactory {
		
		/** The tile entity constructor. */
		private final Constructor<? extends TileEntity> constructor;
		
		
		/**
		 * Creates a new ReflectiveTileEntityCreator for tile entities of the
		 * specified class.
		 * 
		 * @param teClass The tile entity's class.
		 * 
		 * @throws NullPointerException if {@code teClass} is {@code null}.
		 * @throws RuntimeException if the specified class does not have a
		 * constructor accepting only two integer parameters.
		 */
		ReflectiveTEFactory(Class<? extends TileEntity> teClass) {
			try {
				constructor = teClass.getConstructor(Integer.TYPE, Integer.TYPE);
			} catch(Exception e) {
				throw new RuntimeException("Constructor for " + teClass.getCanonicalName() +
						" with x and y parameters does not exist!");
			}
		}
		
		@Override
		public TileEntity create(int x, int y) {
			try {
				return constructor.newInstance(x, y);
			} catch(Exception e) {
				throw new RuntimeException("Could not reflectively instantiate tile entity of class \""
						+ constructor.getDeclaringClass().getSimpleName() + "\"!");
			}
		}
		
	}
	
}
