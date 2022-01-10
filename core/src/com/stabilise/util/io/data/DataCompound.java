package com.stabilise.util.io.data;

import javaslang.control.Option;

import com.stabilise.util.box.*;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A {@code DataCompound} is the basic unifying building block for this package.
 * A {@code DataCompound} is essentially equivalent to any old object - it
 * encapsulates data, and may be saved in a variety of {@link Format}{@code s}.
 * 
 * <p>There are four primary data-interaction methods for a {@code
 * DataCompound}, duplicated for each type of data:
 *  
 * <ul>
 * <li>{@code put()} methods. Each of these methods inserts data into a
 *     compound; if data with the specified name already exists, it will be
 *     overwritten.
 * <li>{@code contains()} methods. These return {@code true} if data with the
 *     specified name is present in a compound either of the specified type,
 *     or, if the compound is in <tt>relaxed</tt> mode, a type that can be
 *     converted to the queried type.
 * <li>{@code get()} methods. Each of these methods get data from a  compound.
 *     If the data is not present, or is of a different format and the compound
 *     is in <tt>strict</tt> mode, a suitable default will be returned. If data
 *     with the queried name is present but of a different format, but that
 *     format may be converted to the queried type, then in <tt>relaxed</tt>
 *     mode the data will be converted and returned.
 * <li>{@code opt()} methods. These methods behave like the {@code get()}
 *     methods, but return an {@code Option} instead of a default value.
 * </ul>
 */
public interface DataCompound extends IDataContainer<DataCompound>,
                                      Iterable<Map.Entry<String, IData>> {
    
    /**
     * Creates a DataCompound of the format determined the current thread's
     * default value.
     * 
     * @see Format#getDefaultFormat()
     * @see Format#setDefaultFormat(Format)
     */
    static DataCompound create() {
        return Format.getDefaultFormat().newCompound();
    }
    
    // DIRECT PUT/GET METHODS
    
    /**
     * Puts the given {@code IData} object <em>directly</em> into this
     * DataCompound (think of this as being equivalent to {@link
     * java.util.Map#put(Object, Object) Map.put()}). The {@code IData} object
     * should be henceforth considered to be "owned" by this DataCompound, and
     * so should not be interacted with any further (namely, the data should not
     * be mutated).
     *
     * <p><b>NOTE:</b> This method is provided only for convenience, and it
     * should rarely, if ever, be used directly -- use the other {@code put()}
     * methods instead!
     *
     * @param name The name to which the given data is to be associated.
     * @param data The data to insert.
     *
     * @throws NullPointerException if either parameter is {@code null}.
     */
    void putData(String name, IData data);
    
    /**
     * Gets the raw data value mapped to the given name. Obviously it is better
     * to use the proper get() or opt() family of methods for most cases, so
     * only use this if you know what you're doing.
     *
     * <p><b>NOTE:</b> This method is provided only for convenience; external
     * code should try to avoid using this if possible.
     *
     * @return null if a tag by the given name isn't present in this compound.
     */
    IData getData(String name);
    
    
    
    /** Returns true if this compound contains data with the specified name. */
    boolean contains(String name);
    
    /** Returns true if this compound contains a compound with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsCompound(String name);
    /** Returns true if this compound contains a list with the specified name;
     * false if either no such data is present or it is of another data
     * type. */
    boolean containsList    (String name);
    /** Returns true if this compound contains a boolean with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsBool    (String name);
    /** Returns true if this compound contains a byte with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI8      (String name);
    /** Returns true if this compound contains a short with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI16     (String name);
    /** Returns true if this compound contains an int with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI32     (String name);
    /** Returns true if this compound contains a long with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI64     (String name);
    /** Returns true if this compound contains a float with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsF32     (String name);
    /** Returns true if this compound contains a double with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsF64     (String name);
    /** Returns true if this compound contains a byte array with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI8Arr   (String name);
    /** Returns true if this compound contains an int array with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI32Arr  (String name);
    /** Returns true if this compound contains a long array with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsI64Arr  (String name);
    /** Returns true if this compound contains a float array with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsF32Arr  (String name);
    /** Returns true if this compound contains a double array with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsF64Arr  (String name);
    /** Returns true if this compound contains a string with the specified
     * name; false if either no such data is present or it is of another data
     * type. */
    boolean containsString  (String name);
    
    // <---------------------------- PUT METHODS ---------------------------->
    
    /** Inserts the given compound into this compound; if data with the
     * specified name already exists, it will be overwritten. If the given
     * compound is of a different format to this one, it will be {@link
     * #convert(Format) converted} before being added. */
    void put(String name, DataCompound data);
    
    /** Inserts the given list into this compound; if data with the
     * specified name already exists, it will be overwritten. If the given
     * list is of a different format to this compound, it will be {@link
     * DataList#convert(Format) converted} before being added. */
    void put(String name, DataList     data);
    /** Inserts a boolean into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, boolean  data);
    /** Inserts a byte into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, byte     data);
    /** Inserts a short into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, short    data);
    /** Inserts an int into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, int      data);
    /** Inserts a long into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, long     data);
    /** Inserts a float into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, float    data);
    /** Inserts a double into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, double   data);
    /** Inserts a byte array into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, byte[]   data);
    /** Inserts an int array into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, int[]    data);
    /** Inserts a long array into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, long[]   data);
    /** Inserts a float array into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, float[]  data);
    /** Inserts a double array into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, double[] data);
    /** Inserts a string into this compound; if data with the specified
     * name already exists, it will be overwritten. */
    void put(String name, String   data);
    
    /**
     * Convenient shorthand for {@code o.exportToCompound(this)}.
     * 
     * @see #getInto(Exportable)
     */
    default void put(Exportable o) {
        o.exportToCompound(this);
    }
    
    /**
     * Convenient shorthand for
     * <pre>
     * DataCompound c = this.createCompound(name);
     * o.exportToCompound(c);
     * </pre>
     * 
     * @see #getInto(String, Exportable)
     */
    default void put(String name, Exportable o) {
        DataCompound c = this.childCompound(name);
        o.exportToCompound(c);
    }
    
    // <---------------------------- GET METHODS ---------------------------->
    // Gets data from this compound. If data with the specified name is not
    // present or is of another type, suitable defaults are returned.
    
    /** Gets a compound from this compound. If {@link #containsCompound(String)}
     * were to return false, a new empty compound is created and returned
     * instead, as a default value. */
    DataCompound getCompound(String name);
    /** Gets a list from this compound. If {@link #containsList(String)}
     * were to return false, a new empty compound is created and returned
     * instead, as a default value. */
    DataList getList(String name);
    /** Gets a boolean from this compound. If {@link #containsBool(String)}
     * were to return false, a default value of {@link BoolBox#defaultValue()}
     * is returned instead. */
    boolean  getBool  (String name);
    /** Gets a byte from this compound. If {@link #containsI8(String)}
     * were to return false, a default value of {@link I8Box#defaultValue()}
     * is returned instead. */
    byte     getI8    (String name);
    /** Gets a short from this compound. If {@link #containsI16(String)}
     * were to return false, a default value of {@link I16Box#defaultValue()}
     * is returned instead. */
    short    getI16   (String name);
    /** Gets an int from this compound. If {@link #containsI32(String)}
     * were to return false, a default value of {@link I32Box#defaultValue()}
     * is returned instead. */
    int      getI32   (String name);
    /** Gets a long from this compound. If {@link #containsI64(String)}
     * were to return false, a default value of {@link I64Box#defaultValue()}
     * is returned instead. */
    long     getI64   (String name);
    /** Gets a float from this compound. If {@link #containsF32(String)}
     * were to return false, a default value of {@link F32Box#defaultValue()}
     * is returned instead. */
    float    getF32   (String name);
    /** Gets a double from this compound. If {@link #containsF64(String)}
     * were to return false, a default value of {@link F64Box#defaultValue()}
     * is returned instead. */
    double   getF64   (String name);
    /** Gets a byte array from this compound. If {@link #containsI8Arr(String)}
     * were to return false, a default value of {@link I8ArrBox#defaultValue()}
     * is returned instead. */
    byte[]   getI8Arr (String name);
    /** Gets an int array from this compound. If {@link #containsI32Arr(String)}
     * were to return false, a default value of {@link I32ArrBox#defaultValue()}
     * is returned instead. */
    int[]    getI32Arr(String name);
    /** Gets a long array from this compound. If {@link #containsI64Arr(String)}
     * were to return false, a default value of {@link I64ArrBox#defaultValue()}
     * is returned instead. */
    long[]   getI64Arr(String name);
    /** Gets a float array from this compound. If {@link #containsF32Arr(String)}
     * were to return false, a default value of {@link F32ArrBox#defaultValue()}
     * is returned instead. */
    float[]  getF32Arr(String name);
    /** Gets a double array from this compound. If {@link #containsF64Arr(String)}
     * were to return false, a default value of {@link F64ArrBox#defaultValue()}
     * is returned instead. */
    double[] getF64Arr(String name);
    /** Gets a string from this compound. If {@link #containsString(String)}
     * were to return false, a default value of {@link StringBox#defaultValue()}
     * is returned instead. */
    String   getString(String name);
    
    /**
     * Convenient shorthand for {@code o.importFromCompound(this)}.
     * 
     * @see #put(Exportable)
     */
    default void getInto(Exportable o) {
        o.importFromCompound(this);
    }
    
    /**
     * Convenient shorthand for
     * <pre>
     * DataCompound c = this.getCompound(name);
     * o.importFromCompound(c);
     * </pre>
     * 
     * @see #put(String, Exportable)
     */
    default void getInto(String name, Exportable o) {
        DataCompound c = this.getCompound(name);
        o.importFromCompound(c);
    }
    
    // <----- OPTION GETTERS ----->
    
    
    /** Gets an Option-wrapped compound from this compound. If {@link
     * #containsCompound(String)} were to return false, None is returned. */
    Option<DataCompound> optCompound (String name);
    /** Gets an Option-wrapped list from this compound. If {@link
     * #containsList(String)} were to return false, None is returned. */
    Option<DataList>     optList     (String name);
    /** Gets an Option-wrapped boolean from this compound. If {@link
     * #containsBool(String)} were to return false, None is returned. */
    Option<Boolean>      optBool     (String name);
    /** Gets an Option-wrapped byte from this compound. If {@link
     * #containsI8(String)} were to return false, None is returned. */
    Option<Byte>         optI8       (String name);
    /** Gets an Option-wrapped short from this compound. If {@link
     * #containsI16(String)} were to return false, None is returned. */
    Option<Short>        optI16      (String name);
    /** Gets an Option-wrapped int from this compound. If {@link
     * #containsI32(String)} were to return false, None is returned. */
    Option<Integer>      optI32      (String name);
    /** Gets an Option-wrapped long from this compound. If {@link
     * #containsI64(String)} were to return false, None is returned. */
    Option<Long>         optI64      (String name);
    /** Gets an Option-wrapped float from this compound. If {@link
     * #containsF32(String)} were to return false, None is returned. */
    Option<Float>        optF32      (String name);
    /** Gets an Option-wrapped double from this compound. If {@link
     * #containsF64(String)} were to return false, None is returned. */
    Option<Double>       optF64      (String name);
    /** Gets an Option-wrapped byte array from this compound. If {@link
     * #containsI8Arr(String)} were to return false, None is returned. */
    Option<byte[]>       optI8Arr    (String name);
    /** Gets an Option-wrapped int array from this compound. If {@link
     * #containsI32Arr(String)} were to return false, None is returned. */
    Option<int[]>        optI32Arr   (String name);
    /** Gets an Option-wrapped long array from this compound. If {@link
     * #containsI64Arr(String)} were to return false, None is returned. */
    Option<long[]>       optI64Arr   (String name);
    /** Gets an Option-wrapped float array from this compound. If {@link
     * #containsF32Arr(String)} were to return false, None is returned. */
    Option<float[]>      optF32Arr   (String name);
    /** Gets an Option-wrapped double array from this compound. If {@link
     * #containsF64Arr(String)} were to return false, None is returned. */
    Option<double[]>     optF64Arr   (String name);
    /** Gets an Option-wrapped string from this compound. If {@link
     * #containsString(String)} were to return false, None is returned. */
    Option<String>       optString   (String name);
    
    
    
    /**
     * Gets a compound which is a child of this one. If a compound by the
     * specified name already exists, it is returned, otherwise one is
     * created and added to this compound. If another data type under the
     * specified name is present, it will be overwritten.
     *
     * <p>The returned compound will be of the same format as this one.
     *
     * <p>This method is equivalent to (but possibly faster than):
     *
     * <pre>
     * if(this.containsCompound(name)) {
     *     return this.getCompound(name);
     * } else {
     *     DataCompound child = this.format().newCompound();
     *     this.put(name, child);
     *     return child;
     * }
     * </pre>
     */
    DataCompound childCompound(String name);
    
    /**
     * Gets a list which is a child of this compound. If a list by the
     * specified name already exists, it is returned, otherwise one is created
     * and added to this compound. If another data type under the specified
     * name is present, it will be overwritten.
     *
     * <p>The returned list will be of the same format as this compound.
     *
     * <p>This method is equivalent to (but possibly faster than):
     *
     * <pre>
     * if(this.containsList(name)) {
     *     return this.getList(name);
     * } else {
     *     DataCompound child = this.format().newList();
     *     this.put(name, child);
     *     return child;
     * }
     * </pre>
     */
    DataList childList(String name);
    
    /**
     * Clears this compound.
     */
    void clear();
    
    
    @Override
    default void read(String name, DataCompound c) {
        c = c.getCompound(name);
        if(c != null)
            c.copyInto(this);
    }
    
    @Override
    default void write(String name, DataCompound o) {
        o.put(name, this);
    }
    
    @Override
    default void read(DataList l) {
        DataCompound c = l.getCompound();
        if(c != null)
            c.copyInto(this);
    }
    
    @Override
    default void write(DataList l) {
        l.add(this);
    }
    
    @Override
    default DataType type() {
        return DataType.COMPOUND;
    }
    
    @Override
    default boolean canConvertToType(DataType type) {
        return type.equals(DataType.COMPOUND);
    }
    
    @Override
    default IData convertToType(DataType type) {
        if(type.equals(DataType.COMPOUND))
            return duplicate();
        else
            throw new RuntimeException("Illegal conversion: Compound --> " + type);
    }
    
    @Override
    default DataCompound convert(Format format) {
        if(this.format() == format)
            return this;
        else
            return duplicate(format);
    }
    
    // Even though this already has a default implementation in IDataContainer,
    // usages don't seem to register that it returns a DataCompound object (and
    // not just an Object), so to avoid dumb unnecessary typecasts.
    //@Override
    //default DataCompound clone() {
    //    return clone(format());
    //}
    
    @Override
    default DataCompound duplicate(Format format) {
        DataCompound c = format.newCompound();
        copyInto(c);
        c.setStrict(this.isStrict());
        return c;
    }
    
    @Override
    default void copyInto(DataCompound other) {
        // TODO: Possible unbounded recursion
        for(Map.Entry<String, IData> entry : this) {
            String name = entry.getKey();
            IData data = entry.getValue();
            // Make sure compounds and lists are cloned into the same format as
            // other.
            if(data instanceof DataCompound)
                other.putData(name, ((DataCompound)data).duplicate(other.format()));
            else if(data instanceof DataList)
                other.putData(name, ((DataList)data).duplicate(other.format()));
            else
                other.putData(name, data.duplicate());
        }
    }
    
    @Override
    default ImmutableCompound immutable() {
        return ImmutableCompound.wrap(this);
    }
    
    /**
     * Convenience alternative to {@link #forEach(Consumer)}, which takes a
     * {@code BiConsumer} rather than a {@code Consumer} of a {@code Map.Entry}
     * object.
     */
    void forEach(BiConsumer<String, IData> action);
    
}
