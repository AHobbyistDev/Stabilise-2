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
	 * Returns a string representation of the given float in decimal form,
	 * with the fractional part, or <i>mantissa</i>, culled to the specified
	 * number of digits. If the mantissa does not possess that many digits,
	 * the character '0' will be appended until it is of the specified length.
	 * 
	 * <p>Note that this method rounds toward negative infinity, so, for
	 * example, {@code cullFP(1.19D, 1)} will return {@code "1.1"}, not {@code
	 * "1.2"}.
	 * 
	 * @param f The float to cull.
	 * @param n The number of digits to cull the mantissa to.
	 * 
	 * @return The culled number.
	 */
	public static String cullFP(float f, int n) {
		return fpToString(Float.toString(f), n);
	}
	
	/**
	 * Returns a string representation of the given double in decimal form,
	 * with the fractional part, or <i>mantissa</i>, culled to the specified
	 * number of digits. If the mantissa does not possess that many digits,
	 * the character '0' will be appended until it is of the specified length.
	 * 
	 * <p>Note that this method rounds toward negative infinity, so, for
	 * example, {@code cullFP(1.19D, 1)} will return {@code "1.1"}, not {@code
	 * "1.2"}.
	 * 
	 * @param d The double to cull.
	 * @param n The number of digits to cull the mantissa to.
	 * 
	 * @return The culled number.
	 */
	public static String cullFP(double d, int n) {
		return fpToString(Double.toString(d), n);
	}
	
	private static String fpToString(String s, int n) {
		int dotIndex = s.indexOf('.');
		if(dotIndex == -1) // NaN, Infinity
			return s;
		
		// We need to deal with scientific notation here, which is annoying
		int exponentIndex = s.lastIndexOf('E');
		if(exponentIndex != -1) {
			// Some points:
			// parseInt() never throws a NumberFormatException
			// exponent will never be exactly 0
			// exponentIndex is henceforth treated as s.length() as to ignore
			//     the exponent part
			int exponent = Integer.parseInt(s.substring(exponentIndex+1));
			if(exponent < 0) {
				// A negative exponent means every sig fig is after the point,
				// and the point must be shifted left.
				
				if(exponent + n < 0) { // all sig figs will be culled
					// This block is effectively equivalent to the else{} block
					// when the rest of this method is taken into account, but
					// this avoids bothering with stuff that would be culled
					// regardless.
					s = "0.000";
					dotIndex = 1;
				} else { // there are some sig figs that won't be culled
					// We prepend |exponent| many 0s to the start and relocate
					// the point.
					StringBuilder sb = new StringBuilder(exponentIndex - exponent);
					sb.append("0."); // first zero
					while(++exponent < 0)
						sb.append('0');
					sb.append(s.substring(0, dotIndex)); // everything before the point
					sb.append(s.substring(dotIndex+1, exponentIndex)); // everything after the point
					s = sb.toString();
					dotIndex = 1;
				}
			} else {
				// A positive exponent means the point must be shifted right
				
				StringBuilder sb = new StringBuilder(Math.max(s.length(), dotIndex + exponent + 1));
				sb.append(s.substring(0, dotIndex));
				if(s.length() > dotIndex + exponent) { // there's enough space to shift the point
					sb.append(s.substring(dotIndex+1, dotIndex + exponent + 1));
					sb.append('.');
					sb.append(s.substring(dotIndex + exponent + 1, exponentIndex));
				} else { // we need intermediate zeroes!
					sb.append(s.substring(dotIndex+1, exponentIndex));
					int e = exponent;
					while(--e >= exponentIndex - dotIndex - 1)
						sb.append('0');
					sb.append('.');
				}
				s = sb.toString();
				dotIndex += exponent;
			}
		}
		
		if(n <= 0)
			return s.substring(0, dotIndex);
		
		int mantissaChars = s.length() - dotIndex;
		if(mantissaChars > n) {
			return s.substring(0, dotIndex + n + 1);
		} else {
			StringBuilder sb = new StringBuilder(s);
			for(; mantissaChars <= n; mantissaChars++)
				sb.append('0');
			return sb.toString();
		}
	}
	
}
