package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

/**
 * A DataList is a list which may be written to and read from sequentially.
 * Each of the {@code add()} methods append to the end of this list, and
 * each of the {@code get()} methods reads the next element from the list.
 */
public interface DataList extends IDataContainer<DataList>, Iterable<IData> {
    
    /**
     * Creates a DataCompound of the format determined the current thread's
     * default value.
     * 
     * @see Format#getDefaultFormat()
     * @see Format#setDefaultFormat(Format)
     */
    static DataList create() {
        return Format.getDefaultFormat().newList();
    }
    
    
    
    /**
     * Returns the number of elements in this list.
     */
    int size();
    
    /**
     * Returns {@code size() == 0}.
     */
    default boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * Returns if another invocation of a {@code get()} method is valid and will
     * return some data without throwing an {@code IndexOutOfBoundsException}.
     */
    boolean hasNext();
    
    /**
     * Gets the next piece of data in this list.
     *
     * <p><b>NOTE:</b> This method is provided only for convenience, and it
     * should rarely, if ever, be used directly -- use the other {@code get()}
     * methods instead!
     *
     * @throws IndexOutOfBoundsException if there isn't any more data. Make sure
     * to check {@code hasNext()} first!
     */
    IData getNext();
    
    /**
     * Gets a data object from this list. Never {@code null}.
     *
     * <p><b>NOTE:</b> This method is provided only for convenience, and it
     * should rarely, if ever, be used directly -- use the other {@code get()}
     * methods instead!
     *
     * @throws IndexOutOfBoundsException if {@code index} is out of range (i.e.
     * {@code index < 0 || index >= size()}.
     */
    IData getData(int index);
    
    /**
     * Puts the given {@code IData} object <em>directly</em> into this
     * list. The {@code IData} object should be henceforth considered to be
     * "owned" by this list, and so should not be interacted with any further
     * (namely, the data should not be mutated).
     *
     * <p><b>NOTE:</b> This method is provided only for convenience, and it
     * should rarely, if ever, be used directly -- use the other {@code add()}
     * methods instead!
     *
     * @param data The data to insert.
     *
     * @throws NullPointerException if {@code data} is {@code null}.
     */
    void addData(IData data);
    
    
    /**
     * Creates a new compound and adds it to this list.
     * 
     * @return The created compound.
     */
    default DataCompound childCompound() {
        DataCompound c = format().newCompound();
        addData(c);
        return c;
    }
    
    /**
     * Creates a new list and adds it to this list.
     * 
     * @return The created list.
     */
    default DataList childList() {
        DataList l = format().newList();
        addData(l);
        return l;
    }
    
    /** If {@code data} is of a different format to this list, it will be
     * converted first. */
    void add(DataCompound data);
    /** If {@code data} is of a different format to this list, it will be
     * converted first. */
    void add(DataList data);
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
    
    @Override
    default void read(String name, DataCompound o) {
        DataList l = o.getList(name);
        if(l != null)
            l.copyInto(this);
    }
    
    @Override
    default void write(String name, DataCompound o) {
        o.put(name, this);
    }
    
    @Override
    default void read(DataList l) {
        l = l.getList();
        if(l != null)
            l.copyInto(this);
    }
    
    @Override
    default void write(DataList l) {
        l.add(this);
    }
    
    @Override
    default DataType type() {
        return DataType.LIST;
    }
    
    @Override
    default boolean canConvertToType(DataType type) {
        return type == DataType.LIST;
    }
    
    @Override
    default IData convertToType(DataType type) {
        if(type == DataType.LIST)
            return this.duplicate();
        else
            throw new RuntimeException("Illegal conversion: List --> " + type);
    }
    
    @Override
    default DataList convert(Format format) {
        if(this.format() == format)
            return this;
        else
            return duplicate(format);
    }
    
    // Even though this already has a default implementation in IDataContainer,
    // usages don't seem to register that it returns a DataList object (and not
    // just an Object), so to avoid dumb unnecessary typecasts.
    @Override
    default DataList duplicate() {
        return duplicate(format());
    }
    
    /**
     * Clones this DataList.
     *
     * @param format The desired format of the clone.
     */
    default DataList duplicate(Format format) {
        DataList l = format.newList();
        copyInto(l);
        return l;
    }
    
    /**
     * Copies all the data from this list into the given list, using
     * {@link IData#duplicate()} whenever applicable.
     */
    default void copyInto(DataList other) {
        // TODO: Possible unbounded recursion
        for(IData data : this) {
            // Make sure compounds and lists are cloned into the same format as
            // other
            if(data instanceof DataCompound)
                other.add(((DataCompound)data).duplicate(other.format()));
            else if(data instanceof DataList)
                other.add(((DataList)data).duplicate(other.format()));
            else
                other.addData(data.duplicate());
        }
    }
    
    @Override
    default ImmutableList immutable() {
        return ImmutableList.wrap(this);
    }
    
}
