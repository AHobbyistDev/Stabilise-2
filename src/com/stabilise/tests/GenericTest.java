package com.stabilise.tests;

public class GenericTest {
	
	static class A<V> {
		A(Class<? extends V> clazz) {}
		void put(V obj) {}
		void test(Class<? extends V> clazz) {}
	}
	static class B<V> {}


	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		A<B<?>> a = new A<B<?>>((Class<? extends B<?>>) B.class);
		B<Object> b = new B<Object>();
		a.put(b);
		a.test((Class<? extends B<?>>) b.getClass());
	}

}
