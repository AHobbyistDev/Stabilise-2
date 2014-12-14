package com.stabilise.tests;

public class ExceptionCast {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Exception e = new IllegalArgumentException("blah");
		System.out.println(e.toString());
	}

}
