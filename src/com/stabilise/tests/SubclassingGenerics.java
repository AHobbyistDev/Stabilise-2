package com.stabilise.tests;

public class SubclassingGenerics {
	
	private SubclassingGenerics() {}
	
	/*
	public static abstract class A<T extends A<T>> {
		public T transform() {
			return newInstance();
		}
		public abstract T newInstance();
	}
	
	public static class B<T extends B<T>> extends A<T> {
		public B<T> newInstance() {
			return new B<T>();
		}
	}
	
	public static class C<T extends C<T>> extends B<T> {
		public C<T> newInstance() {
			return new C<T>();
		}
	}
	
	public static class D extends C<D> {
		public D newInstance() {
			return new D();
		}
	}
	
	public static void main(String[] args) {
		B<?> b = new B<>();
		C<?> c = new C<>();
		D d = new D();
		
		B<?> b1 = b.transform();
		B<?> c1 = c.transform();
	}
	*/
	
}
