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
		return date.subSequence(0, 20) + date.substring(24,28);
	}
	
	/**
	 * Returns a string representation of the given double in decimal form,
	 * with the fractional part, or <i>mantissa</i>, abbreviated to the
	 * specified number of digits.
	 * 
	 * <p>Note that this method rounds toward negative infinity, so, for
	 * example, {@code doubleToNPlaces(1.19D, 1)} will return {@code "1.1"},
	 * not {@code "1.2"}.
	 * 
	 * @param d The double to abbreviate.
	 * @param n The number of digits to abbreviate the mantissa to.
	 * 
	 * @return The abbreviated number.
	 */
	public static String doubleToNPlaces(double d, int n) {
		if(n <= 0)
			return Integer.toString((int)d);
		String s = Double.toString(d);
		int index = s.indexOf('.');
		if(index == -1)
			return s;
		int mantissaChars = s.length() - index;
		if(mantissaChars > n) {
			return s.substring(0, index + n + 1);
		} else {
			StringBuilder sb = new StringBuilder(s);
			for(; mantissaChars <= n; mantissaChars++)
				sb.append('0');
			return sb.toString();
		}
	}
	
}
