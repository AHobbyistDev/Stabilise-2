package com.stabilise.entity;

import static com.stabilise.util.collect.DuplicatePolicy.THROW_EXCEPTION;

import com.stabilise.util.Direction;
import com.stabilise.util.collect.InstantiationRegistry;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AxisAlignedBoundingBox;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;

/**
 * An entity is an object which exists in the world that is subject to
 * physical laws.
 */
public abstract class Entity extends FreeGameObject {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The base value for air friction. */
	protected static final float AIR_FRICTION = 0.001f;
	
	/** The entity registry. */
	private static final InstantiationRegistry<Entity> ENTITIES =
			new InstantiationRegistry<Entity>(8, THROW_EXCEPTION, Entity.class);
	
	// Register all entity types.
	static {
		ENTITIES.register(0, EntityItem.class);
		ENTITIES.register(1, EntityFireball.class);
		ENTITIES.register(2, EntityBigFireball.class);
		ENTITIES.register(3, EntityEnemy.class);
		ENTITIES.register(4, EntityPerson.class);
		
		ENTITIES.lock();
	}
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The ID of this entity. */
	public int id;
	
	/** The Entity's age, in ticks. */
	public int age = 0;
	
	/** Whether or not the entity is invulnerable. */
	public boolean invulnerable = false;
	
	/** The entity's hitbox/bounding volume - used for collision detection. */
	public AxisAlignedBoundingBox boundingBox;
	
	/** Whether or not the Entity's physics are enabled. */
	protected boolean physicsEnabled = true;
	
	/** The entity's velocity values, in tiles per second. */
	public float dx = 0, dy = 0;
	/** dx, dy integral. The entity's velocity values, in tiles per 60th of a
	 * second. */
	private float dxi = 0, dyi = 0;
	
	/** True if dx and dy are positive; false otherwise (n.b. dxp = "dx
	 * positive") */
	private boolean dxp = false, dyp = false;
	
	/** Whether or not the entity is facing right. */
	public boolean facingRight = true;
	
	/** The entity's mass. TODO: Unused */
	public float mass;
	
	/** Whether or not the entity is on the ground. */
	public boolean onGround = false;
	/** The ID of the tile on which the entity is standing. TODO: Temporary? */
	public int floorTile = 0;
	
	
	/**
	 * Creates a new Entity.
	 */
	public Entity() {
		// temporary initialisation of variables
		mass = 20;
		
		boundingBox = getAABB();
	}
	
	/**
	 * Gets the Entity's axis-aligned bounding box. This is invoked upon entity
	 * construction.
	 * 
	 * @return The entity's AABB.
	 */
	protected AxisAlignedBoundingBox getAABB() {
		//boundingBox = new AxisAlignedBoundingBox(-0.25f, 0, 0.5f, 0.5f);
		//boundingBox = new AxisAlignedBoundingBox(-0.48f, 0, 0.96f, 0.96f);
		//boundingBox = new AxisAlignedBoundingBox(-0.49f, 0, 0.98f, 0.98f);
		return new AxisAlignedBoundingBox(-0.5f, 0, 1, 1);
		//boundingBox = new AxisAlignedBoundingBox(-0.51f, 0, 1.02f, 1.02f);
		//boundingBox = new AxisAlignedBoundingBox(-0.55f, 0, 1.1f, 1.1f);
		//boundingBox = new AxisAlignedBoundingBox(-0.75f, 0, 1.5f, 1.5f);
			
		//boundingBox = new AxisAlignedBoundingBox(-1, 0, 2f, 2f);
		
		//boundingBox = new AxisAlignedBoundingBox(-0.5f, 0, 1, 2);
	}
	
	@Override
	public void update(World world) {
		age++;
		
		if(physicsEnabled) {
			//if(dx != 0)
			//	dx *= (1-friction);
			
			// Thanks to calculus, we know that y at the next step (y_next) is
			// given by (where y' is dy at the end of the last step, and y'' is
			// gravitational accel).
			// y_next = y + dy = y + y't + (1/2)y''t^2
			// In other words, in this step the net effect is the following
			// (assuming we don't run into a wall or something):
			// y_next = y_old + dy_old*t + 2ndOrderGravityValue
			// dy_next = dy_old + gravity*t
			
			dxi = dx * world.getTimeIncrement();
			dyi = dy * world.getTimeIncrement() + world.getGravity2ndOrder();
			
			dxp = dxi > 0;
			dyp = dyi > 0;
			
			onGround = false;
			
			if(dxi > 1.0f || dxi < -1.0f || dyi > 1.0f || dyi < -1.0f) {
				// TODO: vertical wall offsetting for higher velocities
				// That is, currently this is pretty screwy and glitches out
				
				float divisor = Math.abs(dxi) > Math.abs(dyi) ? Math.abs(dxi) : Math.abs(dyi);
				float xInc = dxi / divisor;		// x increments
				float yInc = dyi / divisor;		// y increments
				double px = x + xInc;		// projected x
				double py = y + yInc;		// projected y
				boolean xCollided = false;
				boolean yCollided = false;
				
				for(int i = 0; i < Math.ceil(divisor); i++) {
					if(!yCollided)
						yCollided = verticalCollisions(world, px, py);
					if(!xCollided)
						xCollided = horizontalCollisions(world, px, py);
					px += xInc;
					py += yInc;
				}
			} else {
				//double xp = x + dx + (dx > 0 ? boundingBox.p11.x : boundingBox.p00.x);		// projected x
				//double yp = y + dy + (dy > 0 ? boundingBox.p11.y : boundingBox.p00.y);		// projected y
				
				double px = x + dxi;		// projected x
				double py = y + dyi;		// projected y
				
				verticalCollisions(world, px, py);
				//collideHorizontal(xp, yp);
				
				// TODO: This is broken now that I use dyi instead of dy
				// The following is necessary because otherwise gravity will offset the vertical
				// wall being checked for sideways collisions slightly when on the ground.
				horizontalCollisions(world, px, y + dyi);
			}
			
			x += dxi;
			y += dyi;
			
			dy += world.getGravityIncrement(); // apply after updating y
			
			dx *= getXFriction();
			dy *= getYFriction();
		}
	}
	
	/**
	 * Gets the frictive force acting on the entity.
	 * 
	 * @return The frictive force.
	 */
	protected float getXFriction() {
		return 0.98f;
		/*
		// TODO: may be overlapping with multiple tiles
		Tile tileIn = world.getTileAt(x, y);
		
		if(tileIn instanceof TileFluid) {
			return ((TileFluid)tileIn).getViscosity();
		} else {
			return getAirFriction() + getTileFriction();
		}
		*/
	}
	
	/**
	 * Gets the vertical frictive force acting on the entity.
	 * 
	 * @return The frictive force.
	 */
	protected float getYFriction() {
		return 1f;
	}
	
	/**
	 * Gets the air friction.
	 * 
	 * @return The air friction acting on the entity.
	 */
	protected final float getAirFriction() {
		// TODO: Possibly a temporary method
		return AIR_FRICTION;
	}
	
	/**
	 * Gets the tile friction.
	 * 
	 * @return The tile friction acting on the entity.
	 */
	protected float getTileFriction() {
		// TODO: Possibly a temporary method
		if(onGround)
			return Tile.getTile(floorTile).getFriction();
		else
			return 0;
	}
	
	/**
	 * Tests for all horizontal collisions.
	 * 
	 * @param xp The entity's projected x-coordinate.
	 * @param yp The entity's projected y-coordinate.
	 * 
	 * @return {@code true} if a collision is detected.
	 */
	private boolean horizontalCollisions(World world, double xp, double yp) {
		if(dx == 0) return false;
		
		float leadingEdge = dxp ? boundingBox.getV11().x : boundingBox.getV00().x;
		
		xp += leadingEdge;
		
		// If the vertical wall is the same wall as the one the entity is
		// currently occupying, don't bother checking
		if(dxp ? Math.ceil(xp) == Math.ceil(x + leadingEdge) : Math.floor(xp) == Math.floor(x + leadingEdge))
			return false;
		
		// Check the vertical wall of tiles to the left/right of the entity
		
		//double max = dyp ? Math.ceil(yp + boundingBox.p11.y) : Math.ceil(yp + boundingBox.p11.y);
		double max = Math.ceil(yp + boundingBox.getV11().y);
		
		// TODO: < vs <= - watch out for this, it may cause problems in the future
		for(double v = yp + boundingBox.getV00().y; v < max; v++) {
			if(world.getTileAt(xp, v).isSolid() && rowValid(world, xp, v)) {
				//x = dxp ? Math.floor(xp) - boundingBox.p11.x : Math.ceil(xp) - boundingBox.p00.x;
				// Alternatively... (doesn't really matter though)
				//x = dxp ? Math.floor(xp) - leadingEdge : Math.ceil(xp) - leadingEdge;
				//dx = 0;
				collideHorizontal(world, xp, dxp ? Direction.RIGHT : Direction.LEFT);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests for all vertical collisions.
	 * 
	 * @param xp The entity's projected x-coordinate.
	 * @param yp The entity's projected y-coordinate.
	 * 
	 * @return {@code true} if a collision is detected.
	 */
	private boolean verticalCollisions(World world, double xp, double yp) {
		if(dyi == 0.0f) return false;
		
		float leadingEdge = dyp ? boundingBox.getV11().y : boundingBox.getV00().y;
		
		yp += leadingEdge;
		
		// If the horizontal wall is the same as the one the entity is
		// currently occupying, don't bother checking.
		if(dyp ? Math.ceil(yp) == Math.ceil(y + leadingEdge) : Math.floor(yp) == Math.floor(y + leadingEdge))
			return false;
		
		// Check the horizontal wall of tiles at the top/bottom of the entity
		
		//double max = dxp ? Math.ceil(xp + boundingBox.p11.x) : Math.ceil(xp + boundingBox.p11.x);
		double max = Math.ceil(xp + boundingBox.getV11().x);
		
		// TODO: < vs <= - watch out for this, it may cause problems in the future
		for(double h = xp + boundingBox.getV00().x; h < max; h++) {
			if(world.getTileAt(h, yp).isSolid() && columnValid(world, h, yp)) {
				//y = dyp ? Math.floor(yp) - boundingBox.p11.y : Math.ceil(yp) - boundingBox.p00.y;
				//onGround = dy < 0;
				//dy = 0;
				collideVertical(world, yp, dyp ? Direction.UP : Direction.DOWN);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if a column of tiles above or below (depending on the
	 * entity's vertical velocity) a given tile are valid tiles for the entity
	 * to move into (that is, are non-solid).
	 * 
	 * @param x The x-coordinate of the tile to check about.
	 * @param y The y-coordinate of the tile to check about.
	 * 
	 * @return {@code true} if and only if the entity is able to move into the
	 * column.
	 */
	private boolean columnValid(World world, double x, double y) {
		// Only check as many tiles above or below the tile in question that
		// the height of the entity's bounding box would require.
		int max = Maths.ceil(boundingBox.height);
		for(int i = 1; i <= max; i++) {
			if(world.getTileAt(x, y + (dyp ? -i : i)).isSolid())
				return false;
		}
		return true;
	}
	
	/**
	 * Returns true if a row of tiles to the left or right of (depending on the
	 * entity's horizontal velocity) a given tile are valid tiles for the
	 * entity to move into (that is, are non-solid).
	 * 
	 * @param x The x-coordinate of the tile to check about.
	 * @param y The y-coordinate of the tile to check about.
	 * 
	 * @return {@code true} if and only if the entity is able to move into the
	 * row.
	 */
	private boolean rowValid(World world, double x, double y) {
		// Only check as many tiles to the left or right of the tile in
		// question that the width of the entity's bounding box would require.
		int max = Maths.ceil(boundingBox.width);
		for(int i = 1; i <= max; i++) {
			if(world.getTileAt(x + (dxp ? -i : i), y).isSolid())
				return false;
		}
		return true;
	}
	
	/**
	 * Causes the entity to horizontally collide with a tile.
	 * 
	 * @param xp The x position at which the collision is to be made.
	 * @param direction The direction relative to the entity that the tile the
	 * entity has collided with is located.
	 */
	private void collideHorizontal(World world, double xp, Direction direction) {
		onHorizontalCollision();
		impact(world, dx, true);
		
		dx = dxi = 0;
		
		if(direction == Direction.RIGHT) {
			x = Math.floor(xp) - boundingBox.getV11().x;
		} else {
			x = Math.ceil(xp) - boundingBox.getV00().x;
		}
	}
	
	/**
	 * Causes the entity to horizontally collide with a tile.
	 * 
	 * @param yp The x position at which the collision is to be made.
	 * @param direction The direction relative to the entity that the tile the
	 * entity has collided with is located.
	 */
	private void collideVertical(World world, double yp, Direction direction) {
		onVerticalCollision();
		impact(world, dy, true);
		
		dy = dyi = 0;
		
		if(direction == Direction.UP) {
			y = Math.floor(yp) - boundingBox.getV11().y;
		} else {
			y = Math.ceil(yp) - boundingBox.getV00().y;
			
			// TODO: Find a better way of doing this
			int tx = Maths.floor(x);
			int ty = Maths.floor(y - 0.001D);
			Tile t = world.getTileAt(tx, ty);
			t.handleStep(world, tx, ty, this);
			floorTile = t.getID();
			onGround = true;
		}
	}
	
	/**
	 * Handles any resultant effects of a horizontal collision.
	 */
	protected void onHorizontalCollision() {
		// nothing to see here, implemented in subclasses
	}
	
	/**
	 * Handles any resultant effects of a vertical collision.
	 */
	protected void onVerticalCollision() {
		// nothing to see here, implemented in subclasses
	}
	
	/**
	 * Resolves an impact (a sudden change in velocity).
	 * 
	 * @param world the world
	 * @param dv The change in the entity's velocity.
	 * @param tileCollision Whether or not the impact is from a tile collision.
	 */
	protected void impact(World world, float dv, boolean tileCollision) {
		// TODO
	}
	
	/**
	 * Sets the direction in which the entity is facing.
	 * 
	 * @param facingRight Whether or not the entity is to face right.
	 */
	public void setFacingRight(boolean facingRight) {
		if(this.facingRight != facingRight) {
			this.facingRight = facingRight;
			changeFacing();
		}
	}
	
	/**
	 * Performs any logic to be executed when the Entity changes the direction
	 * in which it is facing.
	 */
	protected void changeFacing() {
		// subclasses are to implement this functionality
	}
	
	/**
	 * This is invoked when an entity is added to a world - immediately before
	 * it is added to the map of entities. The default implementation does
	 * nothing.
	 */
	public void onAdd() {
		// subclasses are to implement this functionality
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Creates an Entity object.
	 * 
	 * @param id The ID of the entity, as would be given by its
	 * {@link #getID()} method.
	 * 
	 * @return An Entity object of class determined by the {@code id}
	 * parameter, or {@code null} if the {@code id} parameter is invalid or
	 * the entity could not be constructed for whatever reason.
	 * @throws RuntimeException if the entity corresponding to the ID was
	 * registered incorrectly.
	 */
	public static Entity createEntity(int id) {
		return ENTITIES.instantiate(id);
	}
	
	/**
	 * Creates an entity object from its NBT representation. The given tag
	 * compound should at least contain "id", "x" and "y" integer tags.
	 * 
	 * @param tag The compound tag from which to read the tile entity.
	 * 
	 * @return The tile entity, or {@code null} if it could not be constructed
	 * for whatever reason.
	 * @throws NullPointerException if {@code tag} is {@code null}.
	 */
	/*
	public static Entity createEntityFromNBT(NBTTagCompound tag, World world) {
		Entity e = createEntity(tag.getInt("id"), world);
		if(e == null)
			return null;
		e.fromNBT(tag);
		return e;
	}
	*/
	
}
