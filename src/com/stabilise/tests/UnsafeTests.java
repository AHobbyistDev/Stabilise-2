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
			throw new Error(e);
		}
	}
	
	static class HugeIntArray {
		
		final long INT_BYTES = 4;
		
		final long size;
		final long arrAddress;
		
		HugeIntArray(long size) {
			this.size = size;
			arrAddress = unsafe.allocateMemory(size * INT_BYTES);
			unsafe.setMemory(arrAddress, size * INT_BYTES, (byte)0); // initialise all values to 0
		}
		
		void set(long index, int value) {
			unsafe.putInt(memIndex(index), value);
		}
		
		int get(long index) {
			return unsafe.getInt(memIndex(index));
		}
		
		long memIndex(long index) {
			return arrAddress + index * INT_BYTES;
		}
		
		void deallocate() {
			unsafe.freeMemory(arrAddress);
		}
		
	}
	
	public static void main(String[] args) {
		HugeIntArray arr = new HugeIntArray(Integer.MAX_VALUE);
		System.out.println(arr.get(1));
		arr.set(1, 123456789);
		System.out.println(arr.get(1));
		
		try {
			Thread.sleep(2L);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		arr.deallocate();
	}
	
}
