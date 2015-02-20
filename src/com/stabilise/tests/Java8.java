package com.stabilise.tests;

import java.util.List;

/**
 * testing java 8 features
 */
public class Java8 {
	
	public Java8() {
		// TODO Auto-generated constructor stub
	}
	
	public static class User {
		private final boolean active;
		private final int id;
		private final String name;
		public User(boolean active, int id, String name) {
			this.active = active;
			this.id = id;
			this.name = name;
		}
		public boolean getActive() { return active; }
		public int getID() { return id; }
		public String getName() { return name; }
	}
	
	public static List<String> activeById(List<User> us) {
		return null;
	    //return us.stream().filter(User::getActive).sorted(comparing(User::getId)).map(User::getLastName).collect(toList());
	}
	
}
