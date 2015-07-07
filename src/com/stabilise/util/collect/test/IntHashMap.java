package com.stabilise.util.collect.test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class IntHashMap<V> implements Map<Integer, V> {
	
	public IntHashMap() {
		
	}
	
	@Override
	public int size() {
		return 0;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return false;
	}
	
	@Override
	public boolean containsValue(Object value) {
		return false;
	}
	
	@Override
	public V get(Object key) {
		return null;
	}
	
	@Override
	public V put(Integer key, V value) {
		return null;
	}
	
	@Override
	public V remove(Object key) {
		return null;
	}
	
	@Override
	public void putAll(Map<? extends Integer, ? extends V> m) {
		
	}
	
	@Override
	public void clear() {
		
	}
	
	@Override
	public Set<Integer> keySet() {
		return null;
	}
	
	@Override
	public Collection<V> values() {
		return null;
	}
	
	@Override
	public Set<java.util.Map.Entry<Integer, V>> entrySet() {
		return null;
	}
	
}
