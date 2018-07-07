package com.stabilise.util;

import java.lang.reflect.Field;

/**
 * Defines a convenience method, {@link #print()}, with a default
 * implementation, all in the name of making life easier.
 */
public interface Printable {
    
    /**
     * Prints this object to standard output, as if by
     * {@code System.out.println(this.toString())}.
     */
    default void print() {
        System.out.println(toString());
    }
    
    /**
     * Prints a debug statement of this object to standard output. Prints
     * this object's class name, and all fields of this object in the format
     * {@code "fieldName": field.toString()}.
     */
    default void debugPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("Object of class ");
        sb.append(getClass().getName()).append(": {");
        for(Field f : getClass().getDeclaredFields()) {
            f.setAccessible(true);
            sb.append("\n\t\"");
            sb.append(f.getName()).append("\": ");
            try {
                sb.append(f.get(this));
            } catch(IllegalArgumentException | IllegalAccessException e) {
                sb.append("[UNPRINTABLE]");
            }
        }
        sb.append("\n}");
        System.out.println(sb.toString());
    }
    
}
