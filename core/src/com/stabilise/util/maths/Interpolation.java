package com.stabilise.util.maths;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import com.badlogic.gdx.math.MathUtils;

/**
 * This class provides a variety of functions for achieving three modes of
 * interpolation between two values: ease in, ease out, and ease in-out.
 * 
 * <p>Each interpolation type accomplishes interpolating between two defined
 * values {@code start} and {@code end} by performing a linear interpolation
 * at a point {@code x}, which is transformed using a transformation function
 * {@code f(x)}. That is, the interpolation is performed as if by
 * {@link #lerp(float, float, float) lerp(start, end, f(x))}.
 * 
 * <p>There are three typical modes of interpolation, {@link All#in ease in},
 * {@link All#out ease out}, and {@link All#inOut ease in-out}. Each of these
 * types may be mathematically defined in terms of a single function: the
 * "standard transformation function", which for convenience is labelled {@code
 * f(x)}, and is used directly for ease-in interpolation. Hence, a standard
 * transformation function and ease-in interpolation function for any type
 * of interpolation are equivalent, and all three types of interpolation may
 * be defined in terms of this function as such:
 * 
 * <ul>
 * <li><b>Ease in:</b>
 *     <ul>
 *     <li>{@code f(x)}
 *     </ul>
 * <li><b>Ease out:</b>
 *     <ul>
 *     <li>{@code g(x) = 1 - f(1-x)}
 *     </ul>
 * <li><b>Ease in-out:</b> (Note that ease in-out is a piecewise function
 *     composed of two functions in such a way that it is continuous and
 *     differentiable at all points.)
 *     <ul>
 *     <li><b>Ease in portion (x ∈ [0,0.5))</b>
 *         <ul>
 *         <li>{@code h(x) = f(2x) / 2}
 *         </ul>
 *     <li><b>Ease out portion (x ∈ [0.5,1])</b>
 *         <ul>
 *         <li>{@code i(x) = (1 + g(2x-1)) / 2}
 *         <li>{@code i(x) = 1 - f(2-2x) / 2}
 *         </ul>
 *     </ul>
 * </ul>
 * 
 * <p>Note that if a function is said to smoothly tend towards either
 * {@code start} or {@code end}, then there is a stationary point (that is,
 * the first derivative of that function is zero) at {@code x = 0} or
 * {@code x = 1} respectively. Similarly, if a function is said to sharply
 * tend towards either {@code start} or {@code end}, then there is no
 * stationary point.
 * 
 * <p>Also note that though none of the methods of this class will throw an
 * exception if {@code x} is outside the range {@code 0 <= x <= 1},
 * the behaviour of the interpolative functions for such values is undefined.
 */
public interface Interpolation {
    
    /**
     * Linear interpolation provides straightforward {@code start} to
     * {@code end} transitions with no smoothing. Note that the first
     * derivative of all linear interpolation functions is always {@code 1},
     * and hence all methods are equivalent.
     * 
     * <p>The standard function for linear interpolation is:
     * <pre>f(x) = x</pre>
     */
    All LINEAR = newInterpolation(
            x -> x,
            x -> x,
            x -> x
    );
    
    /**
     * Quadratic interpolation uses a degree-2 polynomial for its interpolative
     * methods.
     * 
     * <p>The standard function for quadratic interpolation is:
     * <pre>f(x) = x<font size=-1><sup>2</sup></font></pre>
     */
    All QUADRATIC = newInterpolation(
            x -> x*x,
            x -> x*(2-x),
            x -> x < 0.5f ? 2*x*x : 1 - 2*(--x)*x
    );
    
    /**
     * Cubic interpolation uses a degree-3 polynomial for its interpolative
     * methods.
     * 
     * <p>The standard function for cubic interpolation is:
     * <pre>f(x) = x<font size=-1><sup>3</sup></font></pre>
     */
    All CUBIC = newInterpolation(
            x -> x*x*x,
            x -> 1 + (--x)*x*x,
            x -> x < 0.5f ? 4*x*x*x : 1 - 4*(--x)*x*x
    );
    
    /**
     * Quartic interpolation uses a degree-4 polynomial for its interpolative
     * methods.
     * 
     * <p>The standard function for quartic interpolation is:
     * <pre>f(x) = x<font size=-1><sup>4</sup></font></pre>
     */
    All QUARTIC = newInterpolation(
            x -> x*x*x*x,
            x -> 1 - x*x*x*x,
            x -> x < 0.5f ? 8*x*x*x*x : 1 - 8*(--x)*x*x*x
    );
    
    /**
     * Quintic interpolation uses a degree-5 polynomial for its interpolative
     * methods.
     * 
     * <p>The standard function for quintic interpolation is:
     * <pre>f(x) = x<font size=-1><sup>5</sup></font></pre>
     */
    All QUINTIC = newInterpolation(
            x -> x*x*x*x*x,
            x -> 1 - (--x)*x*x*x*x,
            x -> x < 0.5f ? 16*x*x*x*x*x : 1 + 16*(--x)*x*x*x*x
    );
    
    /**
     * Sinusoidal interpolation uses a sine curve for its interpolative
     * methods.
     * 
     * <p>The standard function for sinusoidal interpolation is:
     * <pre>f(x) = 1 - cos(x * π/2)</pre>
     * Note, however, rather than modifying this function for {@code easeOut}
     * and {@code easeInOut}, sine and cosine functions with varying arguments
     * are appropriately used instead.
     */
    All SINUSOIDAL = newInterpolation(
            // f(x) = 1 - cos(x*pi/2)
            x -> 1 - MathUtils.cos(x * Maths.PI_OVER_2f),
            x -> MathUtils.sin(x * Maths.PI_OVER_2f),
            x -> (1 - MathUtils.cos(x * Maths.PIf)) / 2
    );
    
    /**
     * Circular interpolation uses circular curves for its interpolative
     * methods, such that the sharp ends of the interpolative curves have an
     * undefined first derivative.
     * 
     * <p>The standard function for circular interpolation is:
     * <pre>f(x) = 1 - sqrt(1 - x<font size=-1><sup>2</sup></font>)</pre>
     */
    All CIRCULAR = newInterpolation(
            // f(x) = 1 - sqrt(1-x^2)
            x -> 1 - (float)Math.sqrt(1 - x*x),
            x -> (float)Math.sqrt(x*(2-x)),
            x -> x < 0.5f ? (1 - (float)Math.sqrt(1 - 4*x*x)) / 2
                    : (1 + (float)Math.sqrt(1 - 4*(--x)*x)) / 2
    );
    
    /**
     * Exponential interpolation uses exponential functions for its
     * interpolative methods, which creates smooth curves but can be costly to
     * calculate.
     * 
     * <p>Note that due to the nature of exponential functions, these functions
     * do not feature stationary points per-se, but rather approach {@code 0} 
     * and {@code 1} at {@code x = 0} and {@code 1} respectively such that the
     * difference is almost completely unnoticeable.
     * 
     * <p>The standard function for exponential interpolation is:
     * <pre>f(x) = 2<font size=-1><sup>10(x-1)</sup></font></pre>
     * 
     * <!-- TODO: Create a factory method for exponential interpolative objects
     * with customisable base and exponent skew. (This has base=2 and exponent
     * skew=10.) -->
     */
    All EXPONENTIAL = newInterpolation(
            // f(x) = 2^(10(x-1))
            x -> x == 0f ? 0f : (float)Math.pow(2, 10*x - 10),
            x -> x == 1f ? 1f : 1 - (float)Math.pow(2, -10*x),
            x -> x < 0.5f
                    // f(x) = b^(n(2x-1)) / 2
                    // f(x) = 2^(10(2x-1)) / 2
                    //      = 2^(20x-11)
                    ? (x == 0f ? 0f : (float)Math.pow(2, 20*x - 11))
                    // f(x) = 1 - b^(-n(2x - 1)) / 2
                    // f(x) = 1 - 2^(-10(2x-1)) / 2
                    //      = 1 - 2^(-20x+9)
                    : (x == 1 ? 1 : 1 - (float)Math.pow(2, 9 - 20*x))
    );
    
    /**
     * Back interpolation is a special type of interpolation which is
     * characterised by the fact that its functions are not constrained to the
     * range {@code 0 <= f(x) <= 1}, such that:
     * 
     * <ul>
     * <li>{@code easeIn} dips below {@code 0} near {@code x = 0}. In the
     *     interpolative sense, this means the {@code easeIn} function moves
     *     <i>away</i> from {@code end} near the start. It still obeys the
     *     general contract for {@code easeIn} in that it tends smoothly
     *     towards {@code start} and sharply towards {@code end}.
     * <li>{@code easeOut} dips above {@code 1} near {@code x = 1}. In the
     *     interpolative sense, this means the {@code easeOut} function moves
     *     <i>past</i> {@code end} before returning. It still obeys the general
     *     contract for {@code easeOut} in that it tends sharply towards
     *     {@code start} and smoothly towards {@code end}.
     * <li>{@code easeInOut} dips below {@code 0} near {@code x = 0} and above
     *     {@code 1} near {@code x = 1}. In the interpolative sense, this means
     *     the {@code easeInOut} function moves <i>away</i> from {@code end}
     *     near the start, and <i>past</i> {@code end} before returning to it.
     *     It still obeys the general contract for {@code easeInOut} in that it
     *     tends smoothly towards both {@code start} and {@code end}.
     * </ul>
     * 
     * <p>The standard function for back interpolation is:
     * <pre>f(x) = x<font size=-1><sup>2</sup></font>((s+1)x - s))</pre>
     * where {@code s} is {@code 1.70158}.
     * 
     * <p>This interpolation object is equivalent to what would be returned by
     * invoking {@link #backInterpolation(float) backInterpolation(1.70158)}.
     */
    All BACK = backInterpolation(1.70158f);
    
    
    // ------------------------------------------------------------------------
    
    
    /**
     * Transforms {@code x} using this object's transformation function. The
     * result is undefined if x ∉ [0,1].
     * 
     * <p>The returned value is equivalent to:
     * <pre>{@link #apply(float, float, float) apply(0f, 1f, x)}</pre>
     * 
     * @param x The value, in the range [0,1].
     * @return The transformed value.
     */
    float transform(float x);
    
    /**
     * Interpolates between {@code start} and {@code end} using this
     * object's transformation function. The result is undefined if x ∉ [0,1].
     * 
     * <p>The returned value is equivalent to:
     * <pre>{@link #lerp(float, float, float)
     * lerp(start, end, transform(x))}</pre>
     * 
     * @param start The start value (i.e. when {@code x == 0}).
     * @param end The end value (i.e. when {@code x == 1}).
     * @param x The position, between 0.0 and 1.0 (inclusive).
     * 
     * @return The interpolated value.
     */
    default float apply(float start, float end, float x) {
        return lerp(start, end, transform(x));
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Linearly interpolates between {@code start} and {@code end}, as if by:
     * <pre>start + (end - start) * x</pre>
     * 
     * @param start The start value.
     * @param end The end value.
     * @param x ∈ [0,1] - the position between the start and end values. Values
     * not in [0,1] are extrapolated to.
     * 
     * @return A value interpolated between {@code start} and {@code end}.
     */
    static float lerp(float start, float end, float x) {
        return start + (end - start) * x;
    }
    
    /**
     * Gets an interpolation object which uses polynomial interpolation of the
     * specified degree.
     * 
     * <p>The standard function for polynomial interpolation is:
     * <pre>f(x) = x<font size=-1><sup>n</sup></font></pre>
     * where {@code n} is defined by the {@code degree} parameter.
     * 
     * @param degree The polynomial degree.
     * 
     * @return The interpolation object.
     * @throws IllegalArgumentException Thrown if {@code degree < 1}.
     */
    static All polynomialInterpolation(final int degree) {
        switch(degree) {
            case 1:
                return LINEAR;
            case 2:
                return QUADRATIC;
            case 3:
                return CUBIC;
            case 4:
                return QUARTIC;
            case 5:
                return QUINTIC;
            default:
                if(degree < 1)
                    throw new IllegalArgumentException("degree < 1");
                final double n = degree;
                return newInterpolation(
                        x -> (float)Math.pow(x, n),
                        x -> 1 + (float)Math.pow(1-x, n),
                        x -> x < 0.5f ? (float)Math.pow(2*x, n-1)
                                : 1 + (x-1)*(float)Math.pow(2-2*x, n-1)
                );
        }
    }
    
    /**
     * Gets an interpolation object which uses polynomial interpolation of the
     * specified degree.
     * 
     * <p>The standard function for polynomial interpolation is:
     * <pre>f(x) = x<font size=-1><sup>n</sup></font></pre>
     * where {@code n} is defined by the {@code degree} parameter.
     * 
     * @param degree The polynomial degree.
     * 
     * @return The interpolation object.
     * @throws IllegalArgumentException Thrown if {@code degree < 1}.
     */
    static All polynomialInterpolation(final double degree) {
        if((long)degree == degree)
            return polynomialInterpolation((int)degree);
        
        if(degree < 1)
            throw new IllegalArgumentException("degree < 1");
        
        final double n = degree;
        return newInterpolation(
                x -> (float)Math.pow(x, n),
                x -> 1 + (float)Math.pow(1-x, n),
                x -> x < 0.5f ? (float)Math.pow(2*x, n-1)
                        : 1 + (x-1)*(float)Math.pow(2-2*x, n-1)
        );
    }
    
    /**
     * Gets an interpolation object which uses 'back' interpolation. Back
     * interpolation is characterised by the fact that its functions are not
     * constrained to the range {@code 0 <= f(x) <= 1}, such that typically:
     * 
     * <ul>
     * <li>{@code easeIn} dips below {@code 0} near {@code x = 0}. In the
     *     interpolative sense, this means the {@code easeIn} function moves
     *     <i>away</i> from {@code end} near the start. It still obeys the
     *     general contract for {@code easeIn} in that it tends smoothly
     *     towards {@code start} and sharply towards {@code end}.
     * <li>{@code easeOut} dips above {@code 1} near {@code x = 1}. In the
     *     interpolative sense, this means the {@code easeOut} function moves
     *     <i>past</i> {@code end} before returning. It still obeys the general
     *     contract for {@code easeOut} in that it tends sharply towards
     *     {@code start} and smoothly towards {@code end}.
     * <li>{@code easeInOut} dips below {@code 0} near {@code x = 0} and above
     *     {@code 1} near {@code x = 1}. In the interpolative sense, this means
     *     the {@code easeInOut} function moves <i>away</i> from {@code end}
     *     near the start, and <i>past</i> {@code end} before returning to it.
     *     It still obeys the general contract for {@code easeInOut} in that it
     *     tends smoothly towards both {@code start} and {@code end}.
     * </ul>
     * 
     * <p>In general, {@code strength} defines the strength of the 'backness' -
     * that is, a higher value of {@code strength} will result in the
     * interpolative function moving further from and past {@code end} in the
     * cases defined above.
     * 
     * <p>Note that while back interpolation works for all values of
     * {@code strength}, the standard contract defined above does not hold when
     * {@code strength <= 0}, and in addition, there are some unique cases for
     * special values of {@code strength}. In particular, for:
     * 
     * <ul>
     * <li><b>{@code strength = 0}</b>, back interpolation is equivalent to
     *     cubic interpolation, and {@link #CUBIC} is hence returned.
     * <li><b>{@code -1.5 <= strength < 0}</b>, {@code easeIn}, {@code easeOut}
     *     and {@code easeInOut} resemble their standard polynomial
     *     counterparts; as {@code strength} approaches {@code -1.5}, each
     *     function approaches the general shape of linear interpolation while
     *     still obeying their respective contracts in regards to smoothly
     *     approaching {@code start} and {@code end}. Also, {@code easeOut} and
     *     {@code easeIn} maintain constant concavity.
     * <li><b>{@code -3 < strength < -1.5}</b>,  {@code easeIn},
     *     {@code easeOut}, and {@code easeInOut} resemble their standard
     *     polynomial counterparts; however, as {@code strength} approaches
     *     {@code -3}, each of these functions approach its defined special
     *     cases.
     * <li><b>{@code strength = -3}</b>, {@code easeIn} and {@code easeOut}
     *     become identical functions defined by <tt>f(x) =
     *     3x<font size=-1><sup>2</sup></font> -
     *     2x<font size=-1><sup>3</sup></font></tt>, and {@code easeInOut}
     *     remains similar to its standard form, but gains a stationary point
     *     at {@code x = 0.5}. This is the only case where {@code easeIn} and
     *     {@code easeOut} smoothly approach both {@code start} and {@code end}
     *     - in all other cases, they obey the standard contract.
     * <li><b>{@code strength < -3}</b>,
     *     <ul>
     *     <li>{@code easeIn} dips above {@code 1} near {@code x = 1}, which
     *         mirrors the functionality of {@code easeOut} for
     *         {@code strength > 0}. Note that the dip is steeper than a
     *         standard {@code easeOut} interpolation.
     *     <li>{@code easeOut} dips below {@code 0} near {@code x = 0}, which
     *         mirrors the functionality of {@code easeIn} for
     *         {@code strength > 0}. Note that the dip is steeper than a
     *         standard {@code easeIn} interpolation.
     *     <li>{@code easeInOut} dips above and below {@code 0.5} as {@code x}
     *         approaches {@code 0.5} for {@code x < 0.5} and {@code x > 0.5}
     *         respectively. In the interpolative sense, this means the
     *         {@code easeInOut} function oscillates once about {@code 0.5}
     *         before reaching {@code end}.
     *     </ul>
     * </ul>
     * 
     * <p>The standard function for back interpolation is:
     * <pre>f(x) = x<font size=-1><sup>2</sup></font>((s+1)x - s))</pre>
     * where {@code s} is defined by the {@code strength} parameter.
     * 
     * @param strength The strength of the backing factor.
     * 
     * @return The interpolation object.
     */
    static All backInterpolation(final float strength) {
        if(strength == 0f)
            return CUBIC;
        
        final float s = strength;
        return newInterpolation(
                // f(x) = x^2 ((s+1)x - s)
                // 
                // Note: The point where this function is a minimum is:
                // (2s/3(s+1), -4s^3 / (27(s+1)^2)
                x -> x*x*((s+1)*x - s),
                // Provided in the reference code, but geogebra tells me this is incorrect
                // g(x) 1 + x*(x-1)*((s+1)*x + s)
                // Instead...
                // 
                // g(x) = 1 - f(1-x), where f(x) = x^2 ((s+1)x - s) [the ease-in formula]
                //      = 1 + (x-1)^2 ((s+1)(x-1) + s)        Note: can this be simplified?
                // 
                // Note: The point where this function is a maximum is:
                // ((s+3)/3(s+1), 1 + 4s^3 / (27(s+1)^2)
                x -> 1 + (--x)*x*((s+1)*x + s),
                //float s2 = s * 1.525f;
                x -> x < 0.5f
                        // g(x) = f(2x) / 2
                        //      = 4x^2 (2(s+1)x - s) / 2
                        //      = 2x^2 (2(s+1)x - s)
                        ? 2*x*x*(2*(s+1)*x - s)
                        // g(x) = 1 - f(2-2x) / 2
                        //      = 1 - (2-2x)^2 ((s+1)(2-2x) - s) / 2
                        //      = 1 - 2(1-x)^2 (2(s+1)(1-x) - s)
                        //      = 1 + 2(x-1)^2 (2(s+1)(x-1) + s)
                        : 1 + 2*(--x)*x*(2*(s+1)*x + s)
        );
    }
    
    /**
     * Gets a generic Interpolation object which uses the specified
     * "transformation function" upon which to base interpolation. Refer to
     * the {@link Interpolation Interpolation class overview} for information
     * as to how the transformation function is used.
     * 
     * <p>For example, the Interpolation object returned by the following
     * snippet is functionally equivalent to {@link #CUBIC}:
     * 
     * <pre>
     * Interpolation cubic = Interpolation.newInterpolation(
     *     x -> x*x*x
     * );</pre>
     * 
     * @param easeIn The transformation function {@code f(x)}.
     * 
     * @return An Interpolation object which uses the specified function as its
     * transformation function.
     * @throws NullPointerException if {@code easeIn} is {@code null}.
     */
    static All newInterpolation(Interpolation easeIn) {
        return newInterpolation(
                easeIn,
                x -> 1 - easeIn.transform(1-x),
                x -> x < 0.5f ? easeIn.transform(2*x) / 2
                        : 1 - easeIn.transform(2 - 2*x) / 2
        );
    }
    
    /**
     * Creates a new {@link All} encapsulating the three specified modes of
     * interpolation.
     * 
     * @throws NullPointerException if any argument is {@code null}.
     */
    static All newInterpolation(Interpolation easeIn, Interpolation easeOut,
            Interpolation easeInOut) {
        return new All(
            Objects.requireNonNull(easeIn),
            Objects.requireNonNull(easeOut),
            Objects.requireNonNull(easeInOut)
        );
    }
    
    /**
     * An instance of this class provides all forms of interpolation. Unlike
     * other subclasses of Interpolation, {@link #transform(float) transform}
     * and {@link #apply(float, float, float) apply} are not the primary
     * methods offered by such an instance; however, in the case that they are
     * used, they will delegate to {@link All#easeInOutTransform(float)
     * easeInOutTransform} and {@link All#easeInOut(float, float, float)
     * easeInOut} respectively.
     */
    @Immutable
    final class All implements Interpolation {
        
        /**
         * An ease-in interpolation function f(x) is broadly characterised by:
         * 
         * <ul>
         * <li>f'(x) < 1 nearby x = 0 (often f'(x) = 0 at x = 0).
         * <li>f'(x) > 1 nearby x = 1.
         * </ul>
         * 
         * <p>This results in an interpolative function which smoothly tends
         * towards {@code start} and sharply tends toward {@code end}.
         */
        public final Interpolation in;
        
        /**
         * An ease-out interpolation function f(x) is broadly characterised by:
         * 
         * <ul>
         * <li>f'(x) > 1 nearby x = 0.
         * <li>f'(x) < 1 nearby x = 1 (often f'(x) = 0 at x = 1).
         * </ul>
         * 
         * <p>This results in an interpolative function which sharply tends
         * towards {@code start} and smoothly tends toward {@code end}.
         */
        public final Interpolation out;
        
        /**
         * An ease-in-out interpolation function f(x) is broadly characterised
         * as identical to an ease-in function on [0,0.5) and an ease-out
         * function on (0.5,1]. That is:
         * 
         * <ul>
         * <li>f'(x) < 1 nearby x = 0 (often f'(x) = 0 at x = 0).
         * <li>f'(x) > 1 nearby x = 0.5.
         * <li>f'(x) < 1 nearby x = 1 (often f'(x) = 0 at x = 1).
         * </ul>
         * 
         * <p>This results in an interpolative function which smoothly tends
         * towards both {@code start} and {@code end}.
         */
        public final Interpolation inOut;
        
        
        public All(Interpolation in, Interpolation out, Interpolation inOut) {
            this.in = in;
            this.out = out;
            this.inOut = inOut;
        }
        
        @Override
        public float transform(float x) {
            return easeInOutTransform(x);
        }
        
        /**
         * Interpolates between two values using ease in interpolation.
         * 
         * @param start The start value.
         * @param end The end value.
         * @param x The position between the start and end values, between
         * {@code 0.0} and {@code 1.0} (inclusive).
         *  
         * @return A value interpolated between {@code start} and {@code end}.
         * @see {@link #in} for implementation details.
         */
        public float easeIn(float start, float end, float x) {
            return lerp(start, end, easeInTransform(x));
        }
        
        /**
         * Transforms the given position by the ease in transformation function.
         * 
         * <p>Invoking this method is equivalent to invoking:
         * <pre>{@link #easeIn(float, float, float) easeIn(0f, 1f, x)}</pre>
         * 
         * @param x The position, between {@code 0.0} and {@code 1.0} (inclusive).
         *  
         * @return The transformed position.
         * @see {@link #in} for implementation details.
         */
        public float easeInTransform(float x) {
            return in.transform(x);
        }
        
        /**
         * Interpolates between two values using ease out interpolation.
         * 
         * @param start The start value.
         * @param end The end value.
         * @param x The position between the start and end values, between
         * {@code 0.0} and {@code 1.0} (inclusive).
         *  
         * @return A value interpolated between {@code start} and {@code end}.
         * @see {@link #out} for implementation details.
         */
        public float easeOut(float start, float end, float x) {
            return lerp(start, end, easeOutTransform(x));
        }
        
        /**
         * Transforms the given position by the ease out transformation function.
         * 
         * <p>Invoking this method is equivalent to invoking:
         * <pre>{@link #easeOut(float, float, float) easeOut(0f, 1f, x)}</pre>
         * 
         * @param x The position, between {@code 0.0} and {@code 1.0} (inclusive).
         *  
         * @return The transformed position.
         * @see {@link #out} for implementation details.
         */
        public float easeOutTransform(float x) {
            return out.transform(x);
        }
        
        /**
         * Interpolates between two values using ease in-out interpolation.
         * 
         * @param start The start value.
         * @param end The end value.
         * @param x The position between the start and end values, between
         * {@code 0.0} and {@code 1.0} (inclusive).
         * 
         * @return A value interpolated between {@code start} and {@code end}.
         * {@link #inOut} for implementation details.
         */
        public float easeInOut(float start, float end, float x) {
            return lerp(start, end, easeInOutTransform(x));
        }
        
        /**
         * Transforms the given position by the ease in-out transformation
         * function.
         * 
         * <p>Invoking this method is equivalent to invoking:
         * <pre>{@link
         * #easeInOut(float, float, float) easeInOut(0f, 1f, x)}</pre>
         * 
         * @param x The position, between {@code 0.0} and {@code 1.0} (inclusive).
         * 
         * @return The transformed value.
         * @see {@link #inOut} for implementation details.
         */
        public float easeInOutTransform(float x) {
            return inOut.transform(x);
        }
        
    }
    
}
