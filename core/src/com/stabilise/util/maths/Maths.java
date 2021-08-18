package com.stabilise.util.maths;

import java.util.function.IntBinaryOperator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * This class provides some utility methods and fields with mathematical
 * purpose.
 */
// Named "Maths" because "Math" and "MathUtils" are already taken.
public class Maths {
    
    // non-instantiable
    private Maths() {}
    
    /** Holds the value given by Math.sqrt(2). */
    public static final double SQRT_2 = Math.sqrt(2);
    /** Holds the value given by Math.PI * 2. */
    public static final double TAU = 2 * Math.PI;
    /** Holds the value given by Math.PI / 2. */
    public static final double PI_OVER_2 = Math.PI / 2;
    
    /** Holds the value given by Math.sqrt(2). */
    public static final float SQRT_2f = (float)SQRT_2;
    /** Holds the value given by 1/Math.sqrt(2). */
    public static final float INV_SQRT_2f = 1f/SQRT_2f;
    /** Holds the value given by Math.PI * 2. */
    public static final float TAUf = (float)TAU;
    /** Holds the value given by Math.PI / 2. */
    public static final float PI_OVER_2f = (float)PI_OVER_2;
    
    /** Holds the value given by Math.PI. */
    public static final float PIf = (float)Math.PI;
    
    /** The x-axis unit vector with components (1,0). */
    public static final Vector2 VEC_X = new Vector2(1f, 0f);
    /** The y-axis unit vector with components (0,1). */
    public static final Vector2 VEC_Y = new Vector2(0f, 1f);
    /** A vector with components (1, 1). */
    public static final Vector2 VEC_1_1 = new Vector2(1f, 1f);
    /** Zero-vector. */
    public static final Vector2 VEC_ZERO = new Vector2();
    
    /** The maximum value which can be held by an unsigned byte
     * (<tt>2<sup><font size=-1>8</font></sup>-1</tt>). */
    public static final int UBYTE_MAX_VALUE = 0xFF;
    /** The maximum value which can be held by an unsigned short
     * (<tt>2<sup><font size=-1>16</font></sup>-1</tt>). */
    public static final int USHORT_MAX_VALUE = 0xFFFF;
    /** The maximum value which can be held by an unsigned int
     * (<tt>2<sup><font size=-1>32</font></sup>-1</tt>). (Note: since this is
     * itself an int, the value reads <tt>-1</tt>.) */
    public static final int UINT_MAX_VALUE = 0xFFFFFFFF;
    
    
    /**
     * Calculates whether a number is a power of two. Note that this method will
     * erroneously report {@code 0} as a power of two.
     * 
     * @return {@code true} if {@code n} is a power of two; {@code false}
     * otherwise.
     */
    public static boolean isPowerOfTwo(int n) {
        return (n & -n) == n; // alternatively, return (n & (n-1)) == 0;
    }
    
    /**
     * Calculates the remainder of a division operation; negative remainders
     * are wrapped by adding {@code div} to such remainders.
     * 
     * @param num The numerator.
     * @param div The divisor.
     * 
     * @return The wrapped remainder of {@code num % div}.
     * @throws ArithmeticException if {@code div == 0}.
     */
    public static int remainder(int num, int div) {
        num %= div;
        return num >= 0 ? num : num + div;
    }
    
    /**
     * Calculates the remainder of a division operation; negative remainders
     * are wrapped by adding {@code div} to such remainders.
     * 
     * @param num The numerator.
     * @param div The divisor.
     * 
     * @return The wrapped remainder of {@code num % div}.
     * @throws ArithmeticException if {@code div == 0}.
     */
    public static long remainder(long num, long div) {
        num %= div;
        return num >= 0 ? num : num + div;
    }
    
    /**
     * Calculates the remainder of a division operation; negative remainders
     * are wrapped by adding {@code div} to such remainders.
     * 
     * <p>IMPORTANT NOTE: Ordinarily the returned value should strictly lie in
     * the range: {@code 0 <= remainder < div}. However, if {@code num} is a
     * sufficiently small negative number, then due to machine rounding errors,
     * the returned number will in fact be {@code div}. This function makes no
     * attempt to remedy this; if you require that the returned value be
     * strictly smaller than {@code div}, then be careful to handle that
     * special case yourself.
     * 
     * @param num The numerator.
     * @param div The divisor.
     * 
     * @return The wrapped remainder of {@code num % div}.
     * @throws ArithmeticException if {@code div == 0}.
     */
    public static double remainder(double num, double div) {
        num %= div;
        return num >= 0 ? num : num + div;
    }
    
    /**
     * Calculates the remainder of a division operation; negative remainders
     * are wrapped by adding {@code div} to such remainders.
     * 
     * <p>IMPORTANT NOTE: Ordinarily the returned value should strictly lie in
     * the range: {@code 0 <= remainder < div}. However, if {@code num} is a
     * sufficiently small negative number, then due to machine rounding errors,
     * the returned number will in fact be {@code div}. This function makes no
     * attempt to remedy this; if you require that the returned value be
     * strictly smaller than {@code div}, then be careful to handle that
     * special case yourself.
     * 
     * @param num The numerator.
     * @param div The divisor.
     * 
     * @return The wrapped remainder of {@code num % div}.
     * @throws ArithmeticException if {@code div == 0}.
     */
    public static float remainder(float num, float div) {
        num %= div;
        return num >= 0 ? num : num + div;
    }
    
    /**
     * Calculates the remainder of a division operation; negative remainders
     * are wrapped as if by adding {@code div} to such remainders.
     * 
     * <p><b>Note</b>: This method is faster than {@link
     * #remainder(int, int)}, but this only works if {@code div} is a
     * positive power of 2. As such, this method functions as a faster
     * alternative to the modulus operator for valid divisors.
     * 
     * @param num The numerator.
     * @param div The divisor, which should be a power of 2.
     * 
     * @return The wrapped remainder of {@code num % div}. If {@code div} is
     * not a power of 2, the result may be incorrect.
     */
    public static int remainder2(int num, int div) {
        // This is a bit-level hack which uses a bitmask to get the wrapped
        // remainder. Remember: this only works with powers of 2 since
        // decrementing a Po2 results in a consistent set of 1s to the right
        // of the single 1 in div
        return num & (div - 1);
        // Equivalently (though with what looks like one more instruction):
        //return num & -(~div);
    }
    
    /**
     * Calculates the remainder of a division operation; negative remainders
     * are wrapped as if by adding {@code div} to such remainders.
     * 
     * <p><b>Note</b>: This method is faster than {@link
     * #remainder(long, long)}, but this only works if {@code div} is a
     * positive power of 2. As such, this method functions as a faster
     * alternative to the modulus operator for valid divisors.
     * 
     * @param num The numerator.
     * @param div The divisor, which should be a power of 2.
     * 
     * @return The wrapped remainder of {@code num % div}. If {@code div} is
     * not a power of 2, the result may be incorrect.
     */
    public static long remainder2(long num, long div) {
        return num & (div - 1);
    }
    
    /**
     * Gets the whole number nearest to {@code x}, in integer form. This
     * method is equivalent to, but faster than {@code (int)Math.round(x)}.
     * 
     * @param x The number.
     * 
     * @return The rounded value of x.
     * @see Math#round(double)
     */
    public static int round(double x) {
        return floor(x + 0.5D);
    }
    
    /**
     * Gets the whole number nearest to {@code x}, in integer form. This
     * method is equivalent to, but faster than {@code (int)Math.round(x)}.
     * 
     * @param x The number.
     * 
     * @return The rounded value of x.
     * @see Math#round(double)
     */
    public static int round(float x) {
        // The fact that duplicating for floats is necessary annoys me
        return floor(x + 0.5f);
    }
    
    /**
     * Gets the greatest whole number less than or equal to {@code x}, in
     * integer form. This method is equivalent to, but faster than
     * {@code (int)Math.floor(x)}.
     * 
     * @param x The number.
     * 
     * @return The floored value of x.
     * @see Math#floor(double)
     */
    public static int floor(double x) {
        int xi = (int)x;
        return x < xi ? xi-1 : xi;
    }
    
    /**
     * Gets the greatest whole number less than or equal to {@code x}, in
     * integer form. This method is equivalent to, but faster than
     * {@code (int)Math.floor(x)}.
     * 
     * @param x The number.
     * 
     * @return The floored value of x.
     * @see Math#floor(double)
     */
    public static int floor(float x) {
        int xi = (int)x;
        return x < xi ? xi-1 : xi;
    }
    
    /**
     * Gets the smallest whole number greater than or equal to {@code x}, in
     * integer form. This method is equivalent to, but faster than
     * {@code (int)Math.ceil(x)}.
     * 
     * @param x The number.
     * 
     * @return The ceil'd value of x.
     * @see Math#ceil(double)
     */
    public static int ceil(double x) {
        int xi = (int)x;
        return x > xi ? xi+1 : xi;
    }
    
    /**
     * Gets the smallest whole number greater than or equal to {@code x}, in
     * integer form. This method is equivalent to, but faster than
     * {@code (int)Math.ceil(x)}.
     * 
     * @param x The number.
     * 
     * @return The ceil'd value of x.
     * @see Math#ceil(double)
     */
    public static int ceil(float x) {
        int xi = (int)x;
        return x > xi ? xi+1 : xi;
    }
    
    /**
     * Calculates the arithmetic mean of {@code a} and {@code b}, rounded
     * towards negative infinity.
     * 
     * @param a The first number.
     * @param b The second number.
     */
    public static int meanFloor(int a, int b) {
        // How this works:
        // Binary addition can be described as such:
        // a + b = ((a & b) << 1) + (a ^ b)
        // The arithmetic mean is defined as (a + b)/2
        // (a+b)/2 = (((a & b) << 1) + (a ^ b)) / 2
        //         = (((a & b) << 1) + (a ^ b)) >> 1 
        //         = (a & b) + ((a ^ b) >> 1)        [the shifts cancel out]
        return (a & b) + ((a ^ b) >> 1);
    }
    
    /**
     * Returns the arithmetic mean of {@code a} and {@code b}, rounded towards
     * positive infinity.
     */
    public static int meanCeil(int a, int b) {
        // I haven't quite figured out how this works yet. Taken from hacker's
        // delight
        return (a | b) - ((a ^ b) >> 1);
    }
    
    /**
     * Returns the lesser of the two provided numbers.
     * 
     * <p>Unlike {@link Math#min(float, float)}, this method does not perform
     * any 'unnecessary' computation (refer to Math.min() source to determine
     * which is preferable).
     *
     * @deprecated just use Math.min() aiya
     */
    //public static float min(float a, float b) {
    //    return a < b ? a : b;
    //}
    
    /**
     * Returns the greater of the two provided numbers.
     * 
     * <p>Unlike {@link Math#max(float, float)}, this method does not perform
     * any 'unnecessary' computation (refer to Math.max() source to determine
     * which is preferable).
     *
     * @deprecated Just use Math.max() aiya
     */
    //public static float max(float a, float b) {
    //    return a > b ? a : b;
    //}
    
    /*
     * Calculates the inverse square root of {@code x}, or
     * <tt>x<font size=-1><sup>-1/2</sup></font></tt>.
     * 
     * @param x The number.
     * @return <tt>x<font size=-1><sup>-1/2</sup></font></tt>
     */
    /*
    public static float invSqrt(float x) {
        float xhalf = x/2;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i>>1);
        x = Float.intBitsToFloat(i);
        x = x*(1.5f - xhalf*x*x);
        return x;
    }
    */
    
    /**
     * Interpolates between the four vertices of a unit square.
     * 
     * @param z00 The z value when x=0 and y=0.
     * @param z01 The z value when x=0 and y=1.
     * @param z10 The z value when x=1 and y=0.
     * @param z11 The z value when x=1 and y=1.
     * @param x The x-coordinate at which to find the value, from 0.0 to 1.0.
     * @param y The y-coordinate at which to find the value, from 0.0 to 1.0.
     * @param interp Interpolation type to use.
     * 
     * @return A value interpolated between all four vertices at the given
     * point.
     */
    public static float biInterp(float z00, float z01, float z10, float z11,
            float x, float y, Interpolation interp) {
        return interp.apply(interp.apply(z00, z10, x), interp.apply(z01, z11, x), y);
    }
    
    /**
     * Returns {@code x*x}.
     */
    public static int square(int x) {
        return x*x;
    }
    
    /**
     * Returns the floor of the log to base 2 of {@code x}, for positive x.
     */
    public static int log2(int x) {
        if(x == 0) return 0;
        return 31 - Integer.numberOfLeadingZeros(x);
    }
    
    /**
     * Returns the ceil of the log to base 2 of {@code x}, for positive x.
     */
    public static int log2Ceil(int x) {
        return 32 - Integer.numberOfLeadingZeros(x - 1);
    }
    
    /**
     * Converts radians to degrees.
     */
    public static float toDegrees(float rads) {
        return rads * MathUtils.radiansToDegrees;
    }
    
    /**
     * Converts degrees to radians.
     */
    public static float toRadians(float deg) {
        return deg * MathUtils.degreesToRadians;
    }
    
    /**
     * Returns a hashing function designed to evenly compress two integers into
     * a hash for a hash table accommodating up to {@code maxElements}-many
     * elements.
     * 
     * <p>The returned hashing function works best when the table size is equal
     * to {@link #log2Ceil(int) log2Ceil(maxElements)}, and may weight the two
     * input numbers unevenly in terms of their higher bits if the table size
     * exceeds this.
     * 
     * @param maxElements The maximum number of elements which are expected to
     * appear in the table.
     * 
     * @throws IllegalArgumentException if {@code maxElements <= 0}.
     */
    public static IntBinaryOperator generateHashingFunction(int maxElements) {
        return genHashFunction(maxElements, false);
    }
    
    /**
     * Returns a hashing function designed to evenly compress two integers into
     * a hash for a hash table accommodating up to {@code maxElements}-many
     * elements.
     * 
     * <p>The returned hashing function works best when the average table size
     * is equal to {@code maxElements}, and may degrade if the table size
     * exceeds {@code maxElements}.
     * 
     * <p>The returned hashing function works best when the table size is equal
     * to {@link #log2Ceil(int) log2Ceil(maxElements)}, and may weight the two
     * input numbers unevenly in terms of their higher bits if the table size
     * exceeds this.
     * 
     * @param maxElements The maximum number of elements which are expected to
     * appear in the table.
     * @param negateHashMapShift Whether to negate a background shift performed
     * by certain map implementations. Doing so may result in less undesirable
     * hash collisions.
     * 
     * @throws IllegalArgumentException if {@code maxElements <= 0}.
     */
    public static IntBinaryOperator genHashFunction(int maxElements,
            boolean negateHashMapShift) {
        if(maxElements <= 0)
            throw new IllegalArgumentException("maxElements <= 0");
        
        // We create a hasher which combines the lowest bits of x and y into a
        // single hash code, aiming to fit as much hash data as we can into a
        // table which will hold up to maxElements many elements.
        // 
        // e.g., if maxElements is 999, the table size will be 1024, with 10
        // usable hash bits. So, we take the lowest 5 bits from x and y:
        //     x = xxxxxxxxxxxxxxxxxxxxxxxxxxxXXXXX
        //     y = yyyyyyyyyyyyyyyyyyyyyyyyyyyYYYYY
        // and combine them into the hash as such:
        //     hash = xxxxxxxxxxxxxxxxxxxxxxXXXXXYYYYY.
        // We keep x's higher bits for the hash rather than indiscriminately
        // setting the higher hash bits to 0, just in case the table expands.
        //
        // Since HashMap, ConcurrentHashMap and co. like to further transform a
        // hash by
        //     hash = hash ^ (hash >>> 16);
        // we offer the option to negate this in order to eliminate interference
        // which would be created by higher-order bits. If the user chooses to
        // do so, our hash in the above example would instead be:
        //     hash = xxxxxxXXXXX0000000000000000YYYYY,
        // which will be transformed into
        //     hash = xxxxxxXXXXX00000xxxxxxXXXXXYYYYY
        // by HashMap/ConcurrentHashMap, which is the same as our desired hash
        // for the lower 16 bits. However, if our table size ends up being
        // large enough, we lose information as the 0s become incorporated into
        // the hash, and so for larger tables we don't apply this shift.
        
        int sizeLog = log2Ceil(maxElements) / 2;
        // n.b. sizeLog <= 8 iff the hash uses less than 16 bits
        int xShift = sizeLog + (negateHashMapShift && sizeLog <= 8 ? 16 : 0);
        int yMask = (1 << sizeLog) - 1;
        
        return (x,y) -> (x << xShift) | (y & yMask);
    }
    
    /**
     * Returns {@code true} iff the points specified by (x1, y1) and (x2, y2)
     * are within the specified range of each other.
     */
    public static boolean pointsInRange(float x1, float y1, float x2, float y2,
            float range) {
        x2 -= x1;
        y2 -= y1;
        return x2*x2 + y2*y2 <= range*range;
    }
    
    /**
     * Returns {@code true} iff the points specified by (x1, y1) and (x2, y2)
     * are within the specified range of each other.
     */
    public static boolean pointsInRange(double x1, double y1, double x2,
            double y2, double range) {
        x2 -= x1;
        y2 -= y1;
        return x2*x2 + y2*y2 <= range*range;
    }
    
    /** What do you think this does? */
    public static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
    
    /**
     * Returns the maximum element in the given array, or throws an exception
     * if {@code b.length == 0}.
     */
    public static byte max(byte[] b) {
        byte max = b[0];
        for(int i = 1; i < b.length; i++) {
            if(b[i] > max)
                max = b[i];
        }
        return max;
    }
    
}
