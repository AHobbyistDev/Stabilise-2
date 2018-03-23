package com.stabilise.util.io.data;

import java.util.Objects;

/**
 * This class wraps a DataCompound to allow method chaining in a builder style.
 */
public class CompoundBuilder {
	
	private final DataCompound c;
	
	
	/**
	 * Creates a new CompoundBuilder wrapping a compound of the default format.
	 * 
	 * @see DataCompound#create()
	 */
	public CompoundBuilder() {
		this(DataCompound.create());
	}
	
	/**
	 * Creates a new CompoundBuilder wrapping the given compound.
	 */
	public CompoundBuilder(DataCompound c) {
		this.c = Objects.requireNonNull(c);
	}
	
    public CompoundBuilder put(String name, DataCompound data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, DataList     data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, boolean data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, byte    data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, char    data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, double  data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, float   data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, int     data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, long    data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, short   data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, String  data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, byte[]  data) { c.put(name,data); return this; }
    public CompoundBuilder put(String name, int[]   data) { c.put(name,data); return this; }
    
    /**
     * Returns the underlying DataCompound object.
     */
    public DataCompound get() {
    	return c;
    }
	
}
