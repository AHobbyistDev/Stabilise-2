package com.stabilise.world.tile.tileentity;

import static com.stabilise.util.collect.Registry.DuplicatePolicy.*;

import java.lang.reflect.Constructor;

import com.stabilise.entity.FixedGameObject;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.collect.InstantiationRegistry;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.World;

/**
 * A tile entity contains additional data associated with a tile, and in
 * addition is able to be updated each tick.
 * 
 * <p>Tile entities may not be added to the world by another tile entity, and
 * and a queue for tile entities is hence not required.
 */
public abstract class TileEntity extends FixedGameObject {
	
	/** The tile entity registry. */
	private static final InstantiationRegistry<TileEntity> TILE_ENTITY_REGISTRY =
			new InstantiationRegistry<TileEntity>("tile entities", "stabilise", 4, THROW_EXCEPTION);
	
	
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
	 * @return The ID of this tile entity's type.
	 */
	public final int getID() {
		return TILE_ENTITY_REGISTRY.getID(getClass());
	}
	
	/**
	 * @return The name of this tile entity's type.
	 */
	public final String getName() {
		return TILE_ENTITY_REGISTRY.getName(getClass());
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
		return TILE_ENTITY_REGISTRY.instantiate(id, x, y);
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
	
	// Register all tile entity types.
	static {
		TILE_ENTITY_REGISTRY.register(0, "Chest", TileEntityChest.class);
		TILE_ENTITY_REGISTRY.register(1, "Mob Spawner", TileEntityMobSpawner.class);
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
