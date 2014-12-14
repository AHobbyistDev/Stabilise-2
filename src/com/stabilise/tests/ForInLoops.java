package com.stabilise.tests;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ForInLoops {
	
	private HashMap<Integer, Integer> hashMap = new LinkedHashMap<Integer, Integer>();
	
	private ForInLoops() {
		hashMap.put(1, 10);
		hashMap.put(2, 20);
		hashMap.put(3, 30);
		hashMap.put(4, 40);
		
		for(Integer i : getMapValues()) {
			System.out.println(i);
		}
	}
	
	private Collection<Integer> getMapValues() {
		System.out.println("Getting values!");
		return hashMap.values();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ForInLoops();
	}
	
}
