package com.stabilise.util.io.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/**
 * Provides static methods to automatically export an object to and import an
 * object from a compound tag.
 * 
 * <p>These utility methods are provided to avoid the need to manually write
 * such import and export code for every desired field of an object. Simply
 * pass an object to {@link #exportObj(Object, Format)} or use it's inverse,
 * {@link #importObj(Object, DataCompound)}.
 * 
 * <p>Note that, of course, as these methods utilise reflection, exporting an
 * object via these utility methods is much slower than manually writing
 * import and export methods.
 */
public class ObjectExporter {
    
    //private static final Log LOG_EXP = Log.getAgent("DataExporter");
    //private static final Log LOG_IMP = Log.getAgent("DataImporter");
    
    // No final fields and no transient fields
    private static final Predicate<Field> ALLOWED_TO_EXPORT = f ->
            !Modifier.isFinal(f.getModifiers()) && !Modifier.isTransient(f.getModifiers());
    
    private ObjectExporter() {} // non-instantiable
    
    
    /**
     * Exports an object.
     * 
     * @param o The object.
     * 
     * @return The object's representative compound tag.
     * @throws NullPointerException if either argument is {@code null}.
     * @throws RuntimeException if the object could not be properly exported.
     */
    public static DataCompound exportObj(Object o, Format f) {
        return doExport(o, f.newCompound());
    }
    
    /**
     * Imports an object from a compound tag.
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
     */
    public static void importObj(Object o, DataCompound tag) {
        doImport(o, tag);
    }
    
    private static DataCompound doExport(Object o, DataCompound tag) {
        for(Field f : o.getClass().getDeclaredFields()) {
            try {
                if(ALLOWED_TO_EXPORT.test(f)) {
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
                    if(c.equals(int.class)) tag.put(n, f.getInt(o));
                    else if(c.equals(long.class)) tag.put(n, f.getLong(o));
                    else if(c.equals(float.class)) tag.put(n, f.getFloat(o));
                    else if(c.equals(double.class)) tag.put(n, f.getDouble(o));
                    else if(c.equals(String.class)) tag.put(n, (String)f.get(o));
                    else if(c.isArray()) {
                        Class<?> t = c.getComponentType();
                        if(t.equals(int.class)) tag.put(n, (int[])f.get(o));
                        else if(t.equals(byte.class)) tag.put(n, (byte[])f.get(o));
                        else { //if(t.getAnnotation(Exportable.class) != null) {
                            Object[] arr = (Object[])f.get(o);
                            if(arr != null) {
                                DataList list = tag.childList(n);
                                for(Object obj : arr)
                                    if(obj != null)
                                        doExport(obj, list.childCompound());
                            }
                        }
                    } else if(c.equals(byte.class)) tag.put(n, f.getByte(o));
                    else if(c.equals(boolean.class)) tag.put(n, f.getBoolean(o));
                    else if(c.equals(short.class)) tag.put(n, f.getShort(o));
                    else { //if(c.getAnnotation(Exportable.class) != null) {
                        //LOG_EXP.postWarning("Invalid field type " + c.getSimpleName() +
                        //        " of field \"" + n + "\"");
                        Object obj = f.get(o);
                        if(obj != null)
                            doExport(obj, tag.childCompound(n));
                    }

                } else {
                    //System.out.println("Not exporting field \"" + f.getName() + "\"");
                }
            } catch(Exception e) {
                throw new RuntimeException("Could not export object!", e);
            }
        }
        //System.out.println("Exported " + o + " as:\n" + tag);
        return tag;
    }
    
    private static void doImport(Object o, DataCompound tag) {
        for(Field f : o.getClass().getDeclaredFields()) {
            try {
                if(ALLOWED_TO_EXPORT.test(f)) {
                    Class<?> c = f.getType();
                    String n = f.getName();
                    f.setAccessible(true);
                    // See comments for doExport()
                    if(c.equals(int.class)) f.setInt(o, tag.getI32(n));
                    else if(c.equals(long.class)) f.setLong(o, tag.getI64(n));
                    else if(c.equals(float.class)) f.setFloat(o, tag.getF32(n));
                    else if(c.equals(double.class)) f.setDouble(o, tag.getF64(n));
                    else if(c.equals(String.class)) f.set(o, tag.getString(n));
                    else if(c.isArray()) {
                        Class<?> t = c.getComponentType();
                        if(t.equals(int.class)) f.set(o, tag.getI32Arr(n));
                        else if(t.equals(byte.class)) f.set(o, tag.getI8Arr(n));
                        else { //if(t.getAnnotation(Exportable.class) != null) {
                            Object[] arr = (Object[])f.get(o);
                            if(arr != null) {
                                DataList list = tag.childList(n);
                                for(int i = 0; i < Math.min(arr.length, list.size()); i++)
                                    doImport(arr[i], list.getCompound());
                            }
                        }
                    } else if(c.equals(byte.class)) f.setByte(o, tag.getI8(n));
                    else if(c.equals(boolean.class)) f.setBoolean(o, tag.getBool(n));
                    else if(c.equals(short.class)) f.setShort(o, tag.getI16(n));
                    else { //if(c.getAnnotation(Exportable.class) != null) {
                        //LOG_IMP.postWarning("Invalid field type " + c.getSimpleName() +
                        //        " of field \"" + n + "\"");
                        Object obj = f.get(o);
                        if(obj != null)
                            doImport(obj, tag.childCompound(n));
                    }
                } else {
                    //System.out.println("Not importing field \"" + f.getName() + "\"");
                }
            } catch(Exception e) {
                throw new RuntimeException("Could not import object!", e);
            }
        }
        //System.out.println("Imported " + o + " from:\n" + tag);
    }
    
}
