package com.stabilise.util.nbt.export;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import com.stabilise.util.Log;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.util.nbt.NBTTagList;

/**
 * Provides static methods to automatically export an object to and import an
 * object from an NBT compound tag.
 * 
 * <p>These utility methods are provided to avoid the need to manually write
 * such import and export code for every desired field of an object. Simply
 * pass an object to {@link #exportCompletely(Object)} or use it's inverse,
 * {@link #importCompletely(Object, NBTTagCompound, boolean)}. (Alternatively,
 * if you wish to be more selective about which fields are exported and which
 * aren't, annotate each desired field with {@link ExportToNBT}, and use {@link
 * #exportObj(Object)} and {@link #importObj(Object, NBTTagCompound, boolean)}
 * instead.)
 * 
 * <p>Note that, of course, as these methods utilise reflection, exporting an
 * object to NBT via these utility methods is much slower than manually writing
 * import and export methods.
 * 
 * <p>Also note that many field types (e.g. Collections classes) can not be
 * exported, and in some cases it is necessary to manually write or append to
 * an object's representative compound tag.
 * 
 * @see ExportToNBT
 * @see Exportable
 */
public class NBTExporter {
	
	private static final Log LOG_EXP = Log.getAgent("NBTExporter");
	private static final Log LOG_IMP = Log.getAgent("NBTImporter");
	
	private static final Predicate<Field> PRED_ALL = (f) -> { return true; };
	private static final Predicate<Field> PRED_MARKED =
			(f) -> { return f.getAnnotation(ExportToNBT.class) != null; };
	
	private NBTExporter() {}
	
	
	/**
	 * Exports an object to an NBT compound tag.
	 * 
	 * <p>Fields of the provided object annotated with {@link ExportToNBT}
	 * will be exported to the compound if they're of an exportable type.
	 * Object fields will only be exported if the declared type of said fields
	 * have the {@link Exportable} annotation.
	 * 
	 * @param o The object.
	 * 
	 * @return The object's representative compound tag.
	 * @throws NullPointerException if {@code o} is {@code null}.
	 * @throws RuntimeException if the object could not be properly exported.
	 * @see #importObj(Object, NBTTagCompound)
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public static NBTTagCompound exportObj(Object o) {
		return exportObj(o, new NBTTagCompound());
	}
	
	/**
	 * Exports an object to an NBT compound tag.
	 * 
	 * <p>Fields of the provided object annotated with {@link ExportToNBT}
	 * will be exported to the compound if they're of an exportable type.
	 * Object fields will only be exported if the declared type of said fields
	 * have the {@link Exportable} annotation.
	 * 
	 * @param o The object.
	 * @param tag The tag into which to export the object.
	 * 
	 * @return {@code tag}, for chaining operations.
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws RuntimeException if the object could not be properly exported.
	 * @see #importObj(Object, NBTTagCompound)
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public static NBTTagCompound exportObj(Object o, NBTTagCompound tag) {
		return doExport(o, tag, PRED_MARKED);
	}
	
	/**
	 * Exports an object to an NBT compound tag.
	 * 
	 * <p>All fields of the provided object will be exported to the compound if
	 * they're of an exportable type. Object fields will only be exported if
	 * the declared type of said fields have the {@link Exportable} annotation.
	 * 
	 * @param o The object.
	 * 
	 * @return The object's representative compound tag.
	 * @throws NullPointerException if {@code o} is {@code null}.
	 * @throws RuntimeException if the object could not be properly exported.
	 * @see #importObj(Object, NBTTagCompound)
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public static NBTTagCompound exportCompletely(Object o) {
		return exportCompletely(o, new NBTTagCompound());
	}
	
	/**
	 * Exports an object to an NBT compound tag.
	 * 
	 * <p>All fields of the provided object will be exported to the compound if
	 * they're of an exportable type. Object fields will only be exported if
	 * the declared type of said fields have the {@link Exportable} annotation.
	 * 
	 * @param o The object.
	 * @param tag The tag into which to export the object.
	 * 
	 * @return {@code tag}, for chaining operations.
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws RuntimeException if the object could not be properly exported.
	 * @see #importObj(Object, NBTTagCompound)
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public static NBTTagCompound exportCompletely(Object o, NBTTagCompound tag) {
		return doExport(o, tag, PRED_ALL);
	}
	
	private static NBTTagCompound doExport(Object o, NBTTagCompound tag, Predicate<Field> pred) {
		for(Field f : o.getClass().getDeclaredFields()) {
			try {
				if(pred.test(f)) {
					Class<?> c = f.getType();
					String n = f.getName();
					f.setAccessible(true);
					// Test each possible option in general order of commonality
					// First, try common variable types: int, long, float,
					// double, String.
					// Next, check if it's an array.
					// If it's either an int or byte array, add it.
					// If it's an object array of a type with the Exportable
					// annotation, export each member via this member into an
					// NBTTagList and add the list to the compound.
					// Next, if the field is an object, basically do the same
					// thing as with an object array but add it to the compound
					// directly.
					// Finally, if it's a byte or short, add it.
					if(c.equals(Integer.TYPE)) tag.addInt(n, f.getInt(o));
					else if(c.equals(Long.TYPE)) tag.addLong(n, f.getLong(o));
					else if(c.equals(Float.TYPE)) tag.addFloat(n, f.getFloat(o));
					else if(c.equals(Double.TYPE)) tag.addDouble(n, f.getDouble(o));
					else if(c.equals(String.class)) tag.addString(n, (String)f.get(o));
					else if(c.isArray()) {
						Class<?> t = c.getComponentType();
						if(t.equals(Integer.TYPE)) tag.addIntArray(n, (int[])f.get(o));
						else if(t.equals(Byte.TYPE)) tag.addByteArray(n, (byte[])f.get(o));
						else if(t.getAnnotation(Exportable.class) != null) {
							Object[] arr = (Object[])f.get(o);
							if(arr != null) {
								NBTTagList list = new NBTTagList();
								for(Object obj : arr)
									if(obj != null)
										list.appendTag(doExport(obj, new NBTTagCompound(), pred));
								tag.addList(n, list);
							}
						}
					} else if(c.getAnnotation(Exportable.class) != null) {
						Object obj = f.get(o);
						if(obj != null)
							tag.addCompound(n, doExport(obj, new NBTTagCompound(), pred));
					} else if(c.equals(Byte.TYPE)) tag.addByte(n, f.getByte(o));
					else if(c.equals(Short.TYPE)) tag.addShort(n, f.getShort(o));
					else
						LOG_EXP.postWarning("Invalid field type " + f.getType().getSimpleName() +
								" of field \"" + n + "\"");
				}
			} catch(Exception e) {
				throw new RuntimeException("Could not export object!", e);
			}
		}
		return tag;
	}
	
	/**
	 * Imports an object from an NBT compound tag.
	 * 
	 * <p>Fields of the provided object annotated with {@link ExportToNBT}
	 * will be imported from the compound if they're of an exportable type.
	 * Object fields will only be imported if the declared type of said fields
	 * have the {@link Exportable} annotation.
	 * 
	 * <p>Note that if Object fields are {@code null}, or elements of Object
	 * arrays are {@code null}, their data will not be imported, as it is out
	 * of the scope of this method to instantiate objects. As such, to ensure
	 * data is properly imported, remember to instantiate {@code o}'s object
	 * fields before invoking this method.
	 * 
	 * @param o The object.
	 * @param tag The tag from which to read the object's fields.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws RuntimeException if the object could not be properly imported.
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public static void importObj(Object o, NBTTagCompound tag) {
		doImport(o, tag, false, PRED_MARKED);
	}
	
	/**
	 * Imports an object from an NBT compound tag.
	 * 
	 * <p>Fields of the provided object annotated with {@link ExportToNBT}
	 * will be imported from the compound if they're of an exportable type.
	 * Object fields will only be imported if the declared type of said fields
	 * have the {@link Exportable} annotation.
	 * 
	 * <p>Note that if Object fields are {@code null}, or elements of Object
	 * arrays are {@code null}, their data will not be imported, as it is out
	 * of the scope of this method to instantiate objects. As such, to ensure
	 * data is properly imported, remember to instantiate {@code o}'s object
	 * fields before invoking this method.
	 * 
	 * <p>In contrast to {@link #importObj(Object, NBTTagCompound)}, this
	 * method uses the unsafe variants of the getters from {@code
	 * NBTTagCompound} will be used instead of the ordinary variants (e.g.
	 * {@link NBTTagCompound#getIntUnsafe(String) getIntUnsafe()} instead of
	 * {@link NBTTagCompound#getInt(String) getInt()}).
	 * 
	 * @param o The object.
	 * @param tag The tag from which to read the object's fields.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if any of the object's fields are not present in
	 * {@code tag}, or present in incorrect data formats.
	 * @throws RuntimeException if the object could not be properly imported.
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public static void importObjUnsafe(Object o, NBTTagCompound tag) throws IOException {
		doImport(o, tag, true, PRED_MARKED);
	}
	
	/**
	 * Imports an object from an NBT compound tag.
	 * 
	 * <p>All fields of the provided object will be imported from the compound
	 * if they're of an exportable type. Object fields will only be imported if
	 * the declared type of said fields have the {@link Exportable} annotation.
	 * 
	 * <p>Note that if Object fields are {@code null}, or elements of Object
	 * arrays are {@code null}, their data will not be imported, as it is out
	 * of the scope of this method to instantiate objects. As such, to ensure
	 * data is properly imported, remember to instantiate {@code o}'s object
	 * fields before invoking this method.
	 * 
	 * @param o The object.
	 * @param tag The tag from which to read the object's fields.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws RuntimeException if the object could not be properly imported.
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public void importCompletely(Object o, NBTTagCompound tag) {
		doImport(o, tag, false, PRED_ALL);
	}
	
	/**
	 * Imports an object from an NBT compound tag.
	 * 
	 * <p>All fields of the provided object will be imported from the compound
	 * if they're of an exportable type. Object fields will only be imported if
	 * the declared type of said fields have the {@link Exportable} annotation.
	 * 
	 * <p>Note that if Object fields are {@code null}, or elements of Object
	 * arrays are {@code null}, their data will not be imported, as it is out
	 * of the scope of this method to instantiate objects. As such, to ensure
	 * data is properly imported, remember to instantiate {@code o}'s object
	 * fields before invoking this method.
	 * 
	 * <p>In contrast to {@link #importObjCompletely(Object, NBTTagCompound)},
	 * this method uses the unsafe variants of the getters from {@code
	 * NBTTagCompound} will be used instead of the ordinary variants (e.g.
	 * {@link NBTTagCompound#getIntUnsafe(String) getIntUnsafe()} instead of
	 * {@link NBTTagCompound#getInt(String) getInt()}).
	 * 
	 * @param o The object.
	 * @param tag The tag from which to read the object's fields.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IOException if any of the object's fields are not present in
	 * {@code tag}, or present in incorrect data formats.
	 * @throws RuntimeException if the object could not be properly imported.
	 * @see ExportToNBT
	 * @see Exportable
	 */
	public void importCompletelyUnsafe(Object o, NBTTagCompound tag) throws IOException {
		doImport(o, tag, true, PRED_ALL);
	}
	
	private static void doImport(Object o, NBTTagCompound tag, boolean unsafe, Predicate<Field> pred) {
		for(Field f : o.getClass().getDeclaredFields()) {
			try {
				if(pred.test(f) && !Modifier.isFinal(f.getModifiers())) {
					Class<?> c = f.getType();
					String n = f.getName();
					f.setAccessible(true);
					// See comments for doExport()
					if(c.equals(Integer.TYPE)) f.setInt(o, unsafe ? tag.getIntUnsafe(n) : tag.getInt(n));
					else if(c.equals(Long.TYPE)) f.setLong(o, unsafe ? tag.getLongUnsafe(n) : tag.getLong(n));
					else if(c.equals(Float.TYPE)) f.setFloat(o, unsafe ? tag.getFloatUnsafe(n) : tag.getFloat(n));
					else if(c.equals(Double.TYPE)) f.setDouble(o, unsafe ? tag.getDoubleUnsafe(n) : tag.getDouble(n));
					else if(c.equals(String.class)) f.set(o, unsafe ? tag.getStringUnsafe(n) : tag.getString(n));
					else if(c.isArray()) {
						Class<?> t = c.getComponentType();
						if(t.equals(Integer.TYPE)) f.set(o, unsafe ? tag.getIntArrayUnsafe(n) : tag.getIntArray(n));
						else if(t.equals(Byte.TYPE)) f.set(o, unsafe ? tag.getByteArrayUnsafe(n) : tag.getByteArray(n));
						else if(t.getAnnotation(Exportable.class) != null) {
							Object[] arr = (Object[])f.get(o);
							if(arr != null) {
								NBTTagList list = unsafe ? tag.getListUnsafe(n) : tag.getList(n);
								for(int i = 0; i < Math.min(arr.length, list.size()); i++)
									doImport(arr[i], (NBTTagCompound)list.getTagAt(i), unsafe, pred);
							}
						}
					} else if(c.getAnnotation(Exportable.class) != null) {
						Object obj = f.get(o);
						if(obj != null)
							doImport(obj, tag.getCompound(n), unsafe, pred);
					} else if(c.equals(Byte.TYPE)) f.setByte(o, unsafe ? tag.getByteUnsafe(n) : tag.getByte(n));
					else if(c.equals(Short.TYPE)) f.setShort(o, unsafe ? tag.getShortUnsafe(n) : tag.getShort(n));
					else
						LOG_IMP.postWarning("Invalid field type " + f.getType().getSimpleName() +
								" of field \"" + n + "\"");
				}
			} catch(Exception e) {
				throw new RuntimeException("Could not import object!", e);
			}
		}
	}
	
}
