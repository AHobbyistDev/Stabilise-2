package com.stabilise.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Container class for an instance of {@link sun.misc.Unsafe}.
 */
public class TheUnsafe {
	
	private TheUnsafe() {}
	
	private static final Unsafe UNSAFE;
	
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			UNSAFE = (Unsafe)field.get(null);
		} catch(Exception e) {
			throw new Error(e);
		}
	}
	
	/**
	 * Returns the unsafe instance.
	 */
	public static Unsafe get() {
		return UNSAFE;
	}
	
}
