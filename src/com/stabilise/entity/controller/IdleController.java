package com.stabilise.entity.controller;

/**
 * Mobs with an IdleController have no defined behaviour; they do nothing.
 */
public class IdleController extends MobController {
	
	/** The global IdleController instance. Since an IdleController does
	 * nothing, this may be shared between multiple mobs. */
	public static final IdleController INSTANCE = new IdleController();
	
	/**
	 * Creates a new IdleController. Private as to make the global instance the
	 * only usable instance.
	 */
	private IdleController() {
		super();
	}
	
	@Override
	public void update() {
		// do nothing
	}
	
}
