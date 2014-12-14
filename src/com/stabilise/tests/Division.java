package com.stabilise.tests;

public class Division {

	public Division() {
		// TODO Auto-generated constructor stub
	}
	
	public static void divide(int dividend, int divisor) {
		int originalDivisor = divisor;
		int min = Integer.MIN_VALUE >>> 1;
		while((divisor & min) != min) {
			divisor <<= 1;
		}
		int quotient = 0;
		int remainder = dividend;
		while(divisor >= originalDivisor) {
			quotient <<= 1;
			if(remainder >= divisor) {
				remainder -= divisor;
				quotient += 1;
			}
			divisor >>= 1;
		}
		System.out.println(dividend + "/" + originalDivisor + " = " + quotient + " r" + remainder);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		divide(35, 9);
		divide(100, 6);
		divide(1000000, 1000);
	}

}
