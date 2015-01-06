package com.stabilise.tests;

import com.stabilise.util.collect.InstantiationRegistry;

public class RegistryTest {

	public static class MyClass {
		// generic superclass
	}
 	
	public static class MyOtherClass extends MyClass {
		int x, y;
	    public MyOtherClass(int x, int y) {
	    	this.x = x; this.y = y;
	    }
	    public String toString() { return "[" + x + "," + y + "]"; }
	}
	public static class YetAnotherClass extends MyClass {
	    String name;
	    public YetAnotherClass(String name) { // switch to private and see this break
	        this.name = name;
	    }
	    public String toString() { return name; }
	}

	public static void main(String[] args) {
		InstantiationRegistry<MyClass> registry =
			    new InstantiationRegistry<MyClass>("objects", "", 2, com.stabilise.util.collect.Registry.DuplicatePolicy.THROW_EXCEPTION);
			
		registry.register(0, "objType1", MyOtherClass.class, Integer.TYPE, Integer.TYPE);
		registry.register(1, "objType2", YetAnotherClass.class, new InstantiationRegistry.Factory<YetAnotherClass>() {
		    @Override
		    public YetAnotherClass create(Object... args) {
		        return new YetAnotherClass((String)args[0]);
		    }
		});
		registry.register(2, "objType3", YetAnotherClass.class, String.class);
		
		// Henceforth there following blocks of code are equivalent:
		
		MyClass obj1 = new MyOtherClass(0, 1);
		MyClass obj2 = new YetAnotherClass("Penguin");
		MyClass obj3 = new YetAnotherClass("Boppin");
		
		System.out.println(obj1.toString());
		System.out.println(obj2.toString());
		System.out.println(obj3.toString());
		
		obj1 = registry.instantiate(0, 0, 1);
		obj2 = registry.instantiate(1, "Penguin");
		obj3 = registry.instantiate(2, "Boppin");
		
		System.out.println(obj1.toString());
		System.out.println(obj2.toString());
		System.out.println(obj3.toString());
		
		obj1 = registry.instantiate("objType1", 0, 1);
		obj2 = registry.instantiate("objType2", "Penguin");
		obj3 = registry.instantiate("objType3", "Boppin");
		
		System.out.println(obj1.toString());
		System.out.println(obj2.toString());
		System.out.println(obj3.toString());
	}

}
