package com.stabilise.util.collect;

import com.stabilise.util.Log;

/**
 * Skeletal registry implementation.
 */
abstract class AbstractRegistry {
	
	public final String name;
	protected final DuplicatePolicy dupePolicy;
	protected final Log log;
	
	/** The number of entries in this registry. */
	protected int size = 0; 
	
	/** If {@code true}, attempts to register new entries should be rejected. */
	private boolean locked = false;
	
	
	/**
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	protected AbstractRegistry(String name, DuplicatePolicy dupePolicy) {
		if(name == null)
			throw new NullPointerException("name is null");
		if(dupePolicy == null)
			throw new NullPointerException("dupePolicy is null");
		this.name = name;
		this.dupePolicy = dupePolicy;
		
		log = Log.getAgent(name);
	}
	
	/**
	 * @return The number of entries in this registry.
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Locks this registry. Once this is done, attempting to register anything
	 * else will result in an {@code IllegalStateException} being thrown.
	 */
	public final void lock() {
		locked = true;
	}
	
	/**
	 * This should be invoked before something is registered, to ensure it's
	 * allowed.
	 * 
	 * @throws IllegalStateException if this registry is locked.
	 */
	protected final void checkLock() {
		if(locked)
			throw new IllegalStateException("Registry \"" + name + "\" is locked;"
					+ " cannot register more entries!");
	}
	
	@Override
	public String toString() {
		return name + "[" + size + " entries]";
	}
	
}
