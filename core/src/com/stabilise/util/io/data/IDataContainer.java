package com.stabilise.util.io.data;

/**
 * Shared parent interface for {@link DataCompound} and {@link DataList}.
 *
 * <p>This interface doesn't really have a real reason to exist other than
 * listing methods common to both interfaces, as one will almost certainly
 * never use this interface directly in practice.
 */
public interface IDataContainer<T extends IDataContainer<T>> extends IData {
    
    /**
     * Sets this compound/list to either <tt>strict</tt> or <tt>relaxed</tt>
     * mode.
     *
     * <p>In <tt>strict</tt> mode:
     * <ul>
     *     <li>{@code contains()} methods will return {@code true} only if there
     *         is data contained under the specified name of the queried type.
     *     <li>{@code get()} methods will return the contained value if the
     *         corresponding {@code contains()} method would return {@code
     *         true}, or otherwise return a suitable default value.
     *     <li>{@code opt()} methods will return an {@code Option} containing
     *         the data if the corresponding {@code contains()} method would
     *         return {@code}, and will otherwise return a {@code None}.
     * </ul>
     *
     * <p>In <tt>relaxed</tt> mode:
     * <ul>
     *     <li>{@code contains()} methods will return {@code true} if there is
     *         data contained under the specified name, and it is either of the
     *         queried type or a type that can be converted to the queried type.
     *     <li>{@code get()} methods will return the contained value if the
     *         corresponding {@code contains()} method would return {@code
     *         true} and it is of the queried type; if it is of a type that can
     *         be converted to the queried type, it will be converted and
     *         returned; otherwise a suitable default value is returned.
     *     <li>{@code opt()} methods will return an {@code Option} containing
     *         the data if the corresponding {@code contains()} method would
     *         return {@code} and the data is of the queried type; if it is of a
     *         type that can be converted to the queried type it will be
     *         converted and returned; otherwise a {@code None} is returned.
     * </ul>
     *
     * All compounds & lists (except for JSON compounds/lists) are in
     * <tt>strict</tt> mode by default, and do <em>not</em> inherit the
     * strictness level of their parent.
     *
     * @param strict {@code true} for <tt>strict</tt>, {@code false} for
     * <tt>relaxed</tt>.
     *
     * @return This compound/list, mainly for convenience in
     * declarations/initialisations.
     */
    T setStrict(boolean strict);
    
    /**
     * Returns {@code true} if this compound/list is in <tt>strict</tt> mode,
     * and <tt>false</tt> if it is in <tt>relaxed</tt> mode.
     *
     * @see #setStrict(boolean)
     */
    boolean isStrict();
    
    /**
     * Returns the format of this compound/list.
     *
     * @see Format
     */
    Format format();
    
    /**
     * Converts this compound/list to the specified format. If this
     * compound/list is already in the specified format, this compound/list is
     * returned, otherwise a new compound/list is created and has this
     * compound/list's contents copied over.
     *
     * @throws UnsupportedOperationException if this compound/list is of a
     * format which cannot be converted for whatever reason.
     */
    T convert(Format format);
    
    /**
     * Returns a clone of this compound/list. The clone is a deep copy, so
     * later modifications to this compound/list will not affect the clone, and
     * vice versa.
     */
    @Override // overriding so it returns a good type
    default T duplicate() {
        return duplicate(format());
    }
    
    /**
     * Returns a clone of this compound/list. The clone is a deep copy, so
     * later modifications to this compound/list will not affect the clone, and
     * vice versa.
     *
     * @param format The desired format of the clone.
     */
    T duplicate(Format format);
    
    /**
     * Copies all the data from this compound/list into the given compound/list,
     * using {@link IData#duplicate()} whenever applicable.
     */
    void copyInto(T other);
    
    /**
     * Wraps this compound/list in an immutable wrapper, or returns this
     * compound/list if it is already immutable.
     *
     * @see ImmutableCompound
     * @see ImmutableList
     */
    T immutable();
    
}
