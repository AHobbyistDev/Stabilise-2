package com.stabilise.util.collect.test;

import java.util.Collection;
import java.util.Set;

import com.badlogic.gdx.math.MathUtils;


public class IntHashMap<V> {
    
    public static class Entry<V> {
        
        final int key;
        V val;
        Entry<V> next = null;
        
        Entry(int key, V val) {
            this.key = key;
            this.val = val;
        }
        
        public int getKey() {
            return key;
        }
        
        public V getValue() {
            return val;
        }
        
    }
    
    private final V defaultVal;
    private Entry<V>[] table;
    private int mask;
    private int size = 0;
    private int buckets = 0;
    @SuppressWarnings("unused")
    private final float loadFactor;
    private int resizeTrigger;
    
    public IntHashMap(int defaultSize, float loadFactor, V defaultValue) {
        if(defaultSize < 1)
            throw new IllegalArgumentException("defaultSize < 0");
        if(loadFactor <= 0 || loadFactor > 1)
            throw new IllegalArgumentException("loadFactor must be between 0 and 1");
        
        this.loadFactor = loadFactor;
        defaultSize = tableSizeFor(defaultSize);
        mask = defaultSize - 1;
        recalcResize(defaultSize);
        
        defaultVal = defaultValue;
        
        table = createTable(defaultSize);
    }
    
    private void recalcResize(int size) {
        resizeTrigger = (int)(size * resizeTrigger);
    }
    
    private int tableSizeFor(int size) {
        return MathUtils.nextPowerOfTwo(size);
    }
    
    @SuppressWarnings("unchecked")
    private Entry<V>[] createTable(int size) {
        return (Entry<V>[])(new Object[size]);
    }
    
    private int hash(int key) {
        return key & mask;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean containsKey(int key) {
        Entry<V> n = table[hash(key)];
        while(n != null) {
            if(n.key == key)
                return true;
            n = n.next;
        }
        return false;
    }
    
    public boolean containsValue(V value) {
        return false;
    }
    
    public V get(int key) {
        Entry<V> n = table[hash(key)];
        while(n != null) {
            if(n.key == key)
                return n.val;
            n = n.next;
        }
        return defaultVal;
    }
    
    public V put(int key, V value) {
        int h = hash(key);
        Entry<V> e = table[h];
        if(e == null) {
            table[h] = newNode(key, value);
            size++;
            buckets++;
            tryResize();
            return defaultVal;
        } else {
            do {
                if(e.key == key) {
                    V old = e.val;
                    e.val = value;
                    return old;
                } else if(e.next == null) {
                    e.next = newNode(key, value);
                    size++;
                    tryResize();
                    return defaultVal;
                } else
                    e = e.next;
            } while(true);
        }
    }
    
    private Entry<V> newNode(int key, V val) {
        return new Entry<>(key, val);
    }
    
    private void tryResize() {
        
    }
    
    public V remove(int key) {
        return null;
    }
    
    public void clear() {
        
    }
    
    public Set<Integer> keySet() {
        return null;
    }
    
    public Collection<V> values() {
        return null;
    }
    
    public Set<Entry<V>> entrySet() {
        return null;
    }
    
}
