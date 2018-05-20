package com.stabilise.util.io.data;

import com.stabilise.util.Checks;

/**
 * A DataList is a list which may be written to and read from sequentially.
 * Each of the {@code add()} methods append to the end of this list, and
 * each of the {@code get()} methods reads the next element from the list.
 */
public interface DataList extends ITag, IContainerTag<DataList> {
    
    /**
     * Creates a DataCompound of the format determined the current thread's
     * default value.
     * 
     * @see Format#getDefaultFormat()
     * @see Format#setDefaultFormat(Format)
     */
    public static DataList create() {
        return Format.getDefaultFormat().newList();
    }
    
    
    
    /**
     * Returns the number of elements in this list.
     */
    int size();
    
    /**
     * Returns if another invocation of a {@code get()} method is valid.
     */
    boolean hasNext();
    
    /**
     * Creates a new compound and adds it to this list.
     * 
     * @return The created compound.
     */
    DataCompound childCompound();
    
    /**
     * Creates a new list and adds it to this list.
     * 
     * @return The created list.
     */
    DataList childList();
    
    /** If {@code data} is of a different format to this list, it will be
     * converted first. */
    void add(DataCompound data);
    /** If {@code data} is of a different format to this list, it will be
     * converted first. */
    void add(DataList     data);
    void add(boolean  data);
    void add(byte     data);
    void add(double   data);
    void add(float    data);
    void add(int      data);
    void add(long     data);
    void add(short    data);
    void add(String   data);
    void add(byte[]   data);
    void add(int[]    data);
    void add(long[]   data);
    void add(float[]  data);
    void add(double[] data);
    
    /** Gets the {@code index-th} tag in this list. This method should
     * generally be ignored in favour of the specific getter methods. */
    ITag getTag(int index) throws IndexOutOfBoundsException;
    
    DataCompound getCompound(int index) throws IndexOutOfBoundsException;
    DataList     getList    (int index) throws IndexOutOfBoundsException;
    boolean  getBool  (int index) throws IndexOutOfBoundsException;
    byte     getI8    (int index) throws IndexOutOfBoundsException;
    short    getI16   (int index) throws IndexOutOfBoundsException;
    int      getI32   (int index) throws IndexOutOfBoundsException;
    long     getI64   (int index) throws IndexOutOfBoundsException;
    float    getF32   (int index) throws IndexOutOfBoundsException;
    double   getF64   (int index) throws IndexOutOfBoundsException;
    byte[]   getI8Arr (int index) throws IndexOutOfBoundsException;
    int[]    getI32Arr(int index) throws IndexOutOfBoundsException;
    long[]   getI64Arr(int index) throws IndexOutOfBoundsException;
    float[]  getF32Arr(int index) throws IndexOutOfBoundsException;
    double[] getF64Arr(int index) throws IndexOutOfBoundsException;
    String   getString(int index) throws IndexOutOfBoundsException;
    
    DataCompound getCompound();
    DataList     getList();
    boolean  getBool  ();
    byte     getI8    ();
    short    getI16   ();
    int      getI32   ();
    long     getI64   ();
    float    getF32   ();
    double   getF64   ();
    byte[]   getI8Arr ();
    int[]    getI32Arr();
    long[]   getI64Arr();
    float[]  getF32Arr();
    double[] getF64Arr();
    String   getString();
    
    /**
     * Wraps this {@code DataList} in an {@code ImmutableList}, or returns this
     * list if it is already immutable.
     */
    default ImmutableList immutable() {
        return ImmutableList.wrap(this);
    }
    
    @Override
    default ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        throw Checks.ISE("Can't convert " + other.getClass().getSimpleName() + " to list type.");
    }
    
    @Override default boolean isBoolean() { return false; }
    @Override default boolean isLong()    { return false; }
    @Override default boolean isDouble()  { return false; }
    @Override default boolean isString()  { return false; }
    
    @Override default boolean getAsBoolean() { throw Checks.ISE("Can't convert list to boolean"); }
    @Override default long    getAsLong()    { throw Checks.ISE("Can't convert list to long");    }
    @Override default double  getAsDouble()  { throw Checks.ISE("Can't convert list to double");  }
    @Override default String  getAsString()  { throw Checks.ISE("Can't convert list to string");  }
    
}
