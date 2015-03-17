package com.stabilise.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Container class for an instance of {@link sun.misc.Unsafe}.
 */
public class TheUnsafe {
	
	private TheUnsafe() {}
	
	/** The Unsafe instance. */
	public static final Unsafe unsafe;
	
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
		} catch(Exception e) {
			throw new Error(e);
		}
	}
	
}
