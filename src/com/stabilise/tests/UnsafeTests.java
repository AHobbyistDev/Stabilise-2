package com.stabilise.tests;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class UnsafeTests {
	
	private static final Unsafe unsafe;
	
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
		} catch(Exception e) {
			throw new AssertionError(e);
		}
	}
	
	public UnsafeTests() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		long value = 12345;
		byte size = 8; // 8 bytes
		long allocateMemory = unsafe.allocateMemory(size);
		unsafe.putLong(allocateMemory, value);
		long readValue = unsafe.getLong(allocateMemory);
		System.out.println("read value : " + readValue);
	}
	
}
