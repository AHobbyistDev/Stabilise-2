package com.stabilise.tests;

import java.util.stream.*;

public class Streams {
	
	private Streams() {}
	
	public static void main(String[] args) {
		long i = LongStream.rangeClosed(1, 10000000).filter(l -> (l & 1) == 1).sum();
		System.out.println(i);
	}
	
}
