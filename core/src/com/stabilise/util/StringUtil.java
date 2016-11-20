package com.stabilise.util;

/**
 * Utility functions involving string manipulation.
 */
public class StringUtil {
    
    // non-instantiable
    private StringUtil() {}
    
    
    /**
     * Removes the time zone portion of a date in string form. That is, the
     * string returned will be of the form:
     * <blockquote><tt>
     * dow mon dd hh:mm:ss zzz yyyy
     * </tt></blockquote>
     * 
     * @param date The date, as returned by
     * {@link java.util.Date#toString() Date.toString()}.
     * 
     * @return The date string.
     */
    public static String getDateWithoutTimeZone(String date) {
        return date.substring(0, 20) + date.substring(24,28);
    }
    
    /**
     * Converts the given byte array to a hex string.
     * 
     * @throws NullPointerException if {@code bytes} is {@code null}.
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(2*bytes.length);
        for(byte b : bytes) {
            // Adding the 0x100 and cutting it off ensures leading zeroes
            // aren't ignored.
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    
}
