package com.stabilise.tests;


public class ModArith {
	
	public ModArith() {
		// TODO Auto-generated constructor stub
	}
	
	private static void enumerate(int m) {
		StringBuilder sb = new StringBuilder();
		sb.append(m);
		sb.append(": ");
		if(sb.length() == 3)
			sb.append(' ');
		for(int i = 1; i < m; i++) {
			String n = Integer.toString((i*i)%m);
			sb.append(n);
			sb.append(' ');
			if(n.length() == 1)
				sb.append(' ');
		}
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args) {
		final int n = 30;
		for(int i = 1; i <= n; i++)
			enumerate(i);
	}
	
}
