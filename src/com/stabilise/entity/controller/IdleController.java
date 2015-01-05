package com.stabilise.entity.controller;

/**
 * Mobs with an IdleController have no defined behaviour; they do nothing.
 */
public class IdleController extends MobController {
	
	/** The global IdleController instance. Since an IdleController does
	 * nothing, this may be shared between multiple mobs. */
	public static final IdleController INSTANCE = new IdleController();
	
	
	// Only privately instantiable
	private IdleController() {
		super();
	}
	
	@Override
	public void update() {
		// do nothing
	}
	
}
