package com.stabilise.screen.menu.component;

import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.MutablePoint;

/**
 * This class allows for one to define the position of a MenuItem in terms of
 * two components:
 * 
 * <ul>
 * <li>A relative point which scales linearly with the screen dimensions (refer
 *     to the constructor parameters {@code relativeX} and {@code relativeY}).
 * <li>A constant displacement.
 * </ul>
 * 
 * <p>This enables the position of a MenuItem to be defined in such a way that
 * it automatically repositions itself accordingly with the screen size.
 * 
 * <p>Note that a Position object is immutable, and may hence be shared by
 * multiple MenuItems.
 */
public final class Position {
	
	/** The x-coordinate relative to the width of the screen, from {@code 0.0}
	 * to {@code 1.0}. */
	final float relativeX;
	/** The y-coordinate relative to the width of the screen, from {@code 0.0}
	 * to {@code 1.0}. */
	final float relativeY;
	/** The offset of the x-coordinate, in pixels. */
	final int offsetX;
	/** The offset of the y-coordinate, in pixels. */
	final int offsetY;
	
	
	/**
	 * Defines a new position.
	 * 
	 * @param relativeX The x-coordinate relative to the width of the screen,
	 * from {@code 0.0} to {@code 1.0}.
	 * @param relativeY The x-coordinate relative to the width of the screen,
	 * from {@code 0.0} to {@code 1.0}.
	 */
	public Position(float relativeX, float relativeY) {
		this(relativeX, relativeY, 0, 0);
	}
	
	/**
	 * Defines a new position.
	 * 
	 * @param x The x-coordinate, in pixels.
	 * @param y The y-coordinate, in pixels.
	 */
	public Position(int x, int y) {
		this(0f, 0f, x, y);
	}
	
	/**
	 * Defines a new position.
	 * 
	 * @param relativeX The x-coordinate relative to the width of the screen,
	 * from {@code 0.0} to {@code 1.0}.
	 * @param relativeY The x-coordinate relative to the width of the screen,
	 * from {@code 0.0} to {@code 1.0}.
	 * @param offsetX The offset of the x-coordinate, in pixels.
	 * @param offsetY The offset of the y-coordinate, in pixels.
	 */
	public Position(float relativeX, float relativeY, int offsetX, int offsetY) {
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	/**
	 * Defines a new position, linearly positioned between two other positions
	 * as dictated by {@code ratio}. If, for example:
	 * 
	 * <ul>
	 * <li>{@code ratio} is {@code 0}, this position will be equivalent to
	 *     {@code pos1}.
	 * <li>{@code ratio} is {@code 0.5}, this position will be midway between
	 *     {@code pos1} and {@code pos2}.
	 * <li>{@code ratio} is {@code 1}, this position will be equivalent to
	 *     {@code pos2}.
	 * </ul>
	 * 
	 * @param pos1 The first position.
	 * @param pos2 The second position.
	 * @param ratio The ratio by which to determine where this position is.
	 */
	public Position(Position pos1, Position pos2, float ratio) {
		this.relativeX = Interpolation.lerp(pos1.relativeX, pos2.relativeX, ratio);
		this.relativeY = Interpolation.lerp(pos1.relativeY, pos2.relativeY, ratio);
		this.offsetX = (int)Interpolation.lerp(pos1.offsetX, pos2.offsetX, ratio);
		this.offsetY = (int)Interpolation.lerp(pos1.offsetY, pos2.offsetY, ratio);
	}
	
	/**
	 * Gets the location defined by this position.
	 * 
	 * @return The location, the components of which are in pixels.
	 */
	public MutablePoint getLocation() {
		return null;
		//return getLocation(Screen.get());
	}
	
	/**
	 * Gets the location defined by this position.
	 * 
	 * @param screen A reference to the screen.
	 * 
	 * @return The location, the components of which are in pixels.
	 * @throws NullPointerException if {@code screen} is {@code null}.
	 */
	/*
	public MutablePoint getLocation(Screen screen) {
		return getLocation(screen.getWidth(), screen.getHeight());
	}
	*/
	
	/**
	 * Gets the location defined by this position.
	 * 
	 * @param width The width of the screen.
	 * @param height The height of the screen.
	 * 
	 * @return The location, the components of which are in pixels.
	 */
	public MutablePoint getLocation(int width, int height) {
		return new MutablePoint(getX(width), getY(height));
	}
	
	/**
	 * Gets the x-coordinate defined by this position.
	 * 
	 * @return The x-coordinate, in pixels.
	 */
	public int getX() {
		return 0;
		//return getX(Screen.get());
	}
	
	/**
	 * Gets the x-coordinate defined by this position.
	 * 
	 * @param screen A reference to the screen.
	 * 
	 * @return The x-coordinate, in pixels.
	 */
	/*
	public int getX(Screen screen) {
		return getX(screen.getWidth());
	}
	*/
	
	/**
	 * Gets the x-coordinate defined by this position.
	 * 
	 * @param width The width of the screen.
	 * 
	 * @return The x-coordinate, in pixels.
	 */
	public int getX(int width) {
		return (int)(width * relativeX) + offsetX;
	}
	
	/**
	 * Gets the y-coordinate defined by this position.
	 * 
	 * @return The y-coordinate, in pixels.
	 */
	public int getY() {
		return 0;
		//return getY(Screen.get());
	}
	
	/**
	 * Gets the y-coordinate defined by this position.
	 * 
	 * @param screen A reference to the screen.
	 * 
	 * @return The y-coordinate, in pixels.
	 */
	/*
	public int getY(Screen screen) {
		return getY(screen.getHeight());
	}
	*/
	
	/**
	 * Gets the y-coordinate defined by this position.
	 * 
	 * @param height The height of the screen.
	 * 
	 * @return The y-coordinate, in pixels.
	 */
	public int getY(int height) {
		return (int)(height * relativeY) + offsetY;
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof Position && equals((Position)o);
	}
	
	/**
	 * Checks for whether or not another position is equivalent to this one.
	 * 
	 * @param p The position.
	 * 
	 * @return {@code true} if the positions are equivalent; {@code false}
	 * otherwise.
	 */
	private boolean equals(Position p) {
		return p.relativeX == relativeX && p.relativeY == relativeY && p.offsetX == offsetX && p.offsetY == offsetY;
	}
	
}
