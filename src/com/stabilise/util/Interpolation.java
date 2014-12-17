package com.stabilise.util;

import com.stabilise.util.maths.MathsUtil;

/**
 * This class provides a variety of functions for achieving three modes of
 * interpolation between two values: ease in, ease out, and ease in-out.
 * 
 * <p>Each interpolation type accomplishes interpolating between two defined
 * values {@code start} and {@code end} by performing a linear interpolation
 * at a point {@code x}, which is transformed using a transformation function
 * {@code f(x)}. That is, the interpolation is performed as if by
 * {@link #interpolateLinear(float, float, float)
 * interpolateLinear(start, end, f(x))}.
 * 
 * <p>There are three typical modes of interpolation, {@link EaseIn ease in},
 * {@link EaseOut ease out}, and {@link EaseInOut ease in-out}. Each of these
 * types may be mathematically defined in terms of a single function: the
 * "standard transformation function", which for convenience is labelled {@code
 * f(x)}, and is used directly for ease-in interpolation. Hence, a standard
 * transformation function and ease-in interpolation function for any type
 * of interpolation are equivalent, and all three types of interpolation may
 * be defined in terms of this function as such:
 * 
 * <ul>
 * <li><b>{@link EaseIn Ease in}:</b>
 *     <ul>
 *     <li>{@code f(x)}
 *     </ul>
 * <li><b>{@link EaseOut Ease out}:</b>
 *     <ul>
 *     <li>{@code g(x) = 1 - f(1-x)}
 *     </ul>
 * <li><b>{@link EaseInOut Ease in-out}:</b> (Note that ease in-out is a piecewise function
 *     composed of two functions in such a way that it is continuous and
 *     differentiable at all points.
 *     <!--, provided {@code f(x)} is continuous and differentiable for
 *     {@code 0 <= x <= 1}.-->)
 *     <ul>
 *     <li><b>Ease in portion ({@code 0 <= x < 0.5})</b>
 *         <ul>
 *         <li>{@code h(x) = f(2x) / 2}
 *         </ul>
 *     <li><b>Ease out portion ({@code 0.5 <= x <= 1})</b>
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
 * 
 * <p>Instances of this class are immutable and hence may be shared between
 * objects and threads.
 * 
 * <p>Some code, namely related to the less conventional functions, is borrowed
 * from
 * <a href=http://hosted.zeh.com.br/tweener/docs/en-us/misc/transitions.html>
 * here</a> and <a href=http://www.robertpenner.com/easing/>here</a>.
 */
public abstract class Interpolation {
	
	/**
	 * The different interpolation types.
	 */
	public static enum Type {
		/** Ease in interpolation, as defined by the contract of
		 * {@link Interpolation#easeIn(float, float, float)}. */
		EASE_IN,
		/** Ease out interpolation, as defined by the contract of
		 * {@link Interpolation#easeOut(float, float, float)}. */
		EASE_OUT,
		/** Ease in-out interpolation, as defined by the contract of
		 * {@link Interpolation#easeInOut(float, float, float)}. */
		EASE_IN_OUT;
	}
	
	/**
	 * Linear interpolation provides straightforward {@code start} to
	 * {@code end} transitions with no smoothing. Note that the first
	 * derivative of all linear interpolation functions is always {@code 1},
	 * and hence all methods are equivalent.
	 * 
	 * <p>The standard function for linear interpolation is:
	 * <pre>f(x) = x</pre>
	 */
	public static final All LINEAR = new All() {
		
		// f(x) = x for everything
		
		@Override
		public float easeInTransform(float x) {
			return x;
		}
		
		@Override
		public float easeOutTransform(float x) {
			return x;
		}
		
		@Override
		public float easeInOutTransform(float x) {
			return x;
		}
		
	};
	
	/**
	 * Quadratic interpolation uses a degree-2 polynomial for its interpolative
	 * methods.
	 * 
	 * <p>The standard function for quadratic interpolation is:
	 * <pre>f(x) = x<font size=-1><sup>2</sup></font></pre>
	 */
	public static final All QUADRATIC = new All() {
		
		@Override
		public float easeInTransform(float x) {
			// f(x) = x^2
			return x*x;
		}
		
		@Override
		public float easeOutTransform(float x) {
			// f(x) = 1 - (x-1)^2
			//      = x(2-x)
			return x*(2-x);
		}
		
		@Override
		public float easeInOutTransform(float x) {
			if(x < 0.5f) {
				// f(x) = 2x^2
				return 2*x*x;
			} else {
				// f(x) = 1 - (2-2x)^2 / 2
				//      = 1 - 2(x-1)^2
				x--;
				return 1 - 2*x*x;
			}
		}
	};
	
	/**
	 * Cubic interpolation uses a degree-3 polynomial for its interpolative
	 * methods.
	 * 
	 * <p>The standard function for cubic interpolation is:
	 * <pre>f(x) = x<font size=-1><sup>3</sup></font></pre>
	 */
	public static final All CUBIC = new All() {
		
		@Override
		public float easeInTransform(float x) {
			// f(x) = x^3
			return x*x*x;
		}
		
		@Override
		public float easeOutTransform(float x) {
			// f(x) = 1 + (x-1)^3
			x--;
			return 1 + x*x*x;
		}
		
		@Override
		public float easeInOutTransform(float x) {
			if(x < 0.5f) {
				// f(x) = 4x^3
				return 4*x*x*x;
			} else {
				// f(x) = 1 - 4(x-1)^3
				x--;
				return 1 - 4*x*x*x;
			}
		}
	};
	
	/**
	 * Quartic interpolation uses a degree-4 polynomial for its interpolative
	 * methods.
	 * 
	 * <p>The standard function for quartic interpolation is:
	 * <pre>f(x) = x<font size=-1><sup>4</sup></font></pre>
	 */
	public static final All QUARTIC = new All() {
		
		// N.B. A benchmarking test has indicated that for degree-4 exponentiation,
		// x*x*x*x is faster than:
		// x *= x;
		// x*x
		
		@Override
		public float easeInTransform(float x) {
			// f(x) = x^4
			x *= x;
			return x*x;
		}
		
		@Override
		public float easeOutTransform(float x) {
			// f(x) = 1 - (x-1)^4
			x--;
			return 1 - x*x*x*x;
		}
		
		@Override
		public float easeInOutTransform(float x) {
			if(x < 0.5f) {
				// f(x) = 8x^4
				return 8*x*x*x*x;
			} else {
				// f(x) = 1 - 8(x-1)^4
				x--;
				return 1 - 8*x*x*x*x;
			}
		}
	};
	
	/**
	 * Quintic interpolation uses a degree-5 polynomial for its interpolative
	 * methods.
	 * 
	 * <p>The standard function for quintic interpolation is:
	 * <pre>f(x) = x<font size=-1><sup>5</sup></font></pre>
	 */
	public static final All QUINTIC = new All() {
		
		// N.B. A benchmarking test has indicated that for degree-5 exponentiation,
		// float xSquared = x*x;
		// xSquared*xSquared*x is faster than:
		// x*x*x*x*x
		
		@Override
		public float easeInTransform(float x) {
			// f(x) = x^5
			float xSquared = x*x;
			return xSquared * xSquared * x;
		}
		
		@Override
		public float easeOutTransform(float x) {
			// f(x) = 1 + (x-1)^5
			x--;
			float xSquared = x*x;
			return 1 - xSquared*xSquared*x;
		}
		
		@Override
		public float easeInOutTransform(float x) {
			if(x < 0.5f) {
				// f(x) = 16x^5
				float xSquared = x*x;
				return 16*xSquared*xSquared*x;
			} else {
				// f(x) = 1 + 16(x-1)^5
				x--;
				float xSquared = x*x;
				return 1 + 16*xSquared*xSquared*x;
			}
		}
	};
	
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
	public static final All SINUSOIDAL = new All() {
		
		@Override
		public float easeInTransform(float x) {
			// f(x) = 1 - cos(x*pi/2)
			return 1 - (float)Math.cos(x * MathsUtil.PI_OVER_2);
		}
		
		@Override
		public float easeOutTransform(float x) {
			// f(x) = sin(x*pi/2)
			return (float)Math.sin(x * MathsUtil.PI_OVER_2);
		}
		
		@Override
		public float easeInOutTransform(float x) {
			// f(x) = (1 - cos(x*pi))/2
			return (1 - (float)Math.cos(x * Math.PI)) / 2;
		}
	};
	
	/**
	 * Circular interpolation uses circular curves for its interpolative
	 * methods, such that the sharp ends of the interpolative curves have an
	 * undefined first derivative.
	 * 
	 * <p>The standard function for circular interpolation is:
	 * <pre>f(x) = 1 - sqrt(1 - x<font size=-1><sup>2</sup></font>)</pre>
	 */
	public static final All CIRCULAR = new All() {
		
		@Override
		public float easeInTransform(float x) {
			// f(x) = 1 - sqrt(1-x^2)
			return 1 - (float)Math.sqrt(1 - x*x);
		}
		
		@Override
		public float easeOutTransform(float x) {
			// f(x) = sqrt(1 - (x-1)^2)
			//      = sqrt(x(2-x))
			return (float)Math.sqrt(x*(2-x));
		}
		
		@Override
		public float easeInOutTransform(float x) {
			if(x < 0.5f) {
				// f(x) = (1 - sqrt(1-4x^2)) / 2
				return (1 - (float)Math.sqrt(1 - 4*x*x)) / 2;
			} else {
				// f(x) = (1 + sqrt(1-4(x-1)^2)) / 2
				x--;
				return (1 + (float)Math.sqrt(1 - 4*x*x)) / 2;
			}
		}
	};
	
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
	public static final All EXPONENTIAL = new All() {
		
		@Override
		public float easeInTransform(float x) {
			if(x == 0f)
				return 0f;
			else			// f(x) = 2^(10(x-1))
				return (float)Math.pow(2, 10*x - 10);
		}
		
		@Override
		public float easeOutTransform(float x) {
			if(x == 1f)
				return 1f;
			else			// f(x) = 1 - 2^(-10x)
				return 1 - (float)Math.pow(2, -10*x);
		}
		
		@Override
		public float easeInOutTransform(float x) {
			if(x < 0.5f) {
				if(x == 0f)
					return 0f;
				// f(x) = b^(n(2x-1)) / 2
				// f(x) = 2^(10(2x-1)) / 2
				//      = 2^(20x-11)
				return (float)Math.pow(2, 20*x - 11);
			} else {
				if(x == 1)
					return 1;
				// f(x) = 1 - b^(-n(2x - 1)) / 2
				// f(x) = 1 - 2^(-10(2x-1)) / 2
				//      = 1 - 2^(-20x+9)
				return 1 - (float)Math.pow(2, 9 - 20*x);
			}
		}
	};
	
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
	public static final All BACK = backInterpolation(1.70158f);
	
	
	// ------------------------------------------------------------------------
	
	// Not publicly instantiable
	private Interpolation() {}
	
	
	/**
	 * Transforms the given value using this Interpolation object's
	 * transformation function. This method may produce undefined results for
	 * {@code x} outside the range {@code 0 <= x <= 1}.
	 * 
	 * <p>The returned value is equivalent to:
	 * <pre>{@link #apply(float, float, float) apply(0f, 1f, x)}</pre>
	 * 
	 * @param x The value, between 0.0 and 1.0 (inclusive).
	 * @return The transformed value.
	 */
	public abstract float transform(float x);
	
	/**
	 * Interpolates between {@code start} and {@code end} using this
	 * Interpolation object's transformation function. This method may produce
	 * undefined results for {@code x} outside the range {@code 0 <= x <= 1}.
	 * 
	 * <p>The returned value is equivalent to:
	 * <pre>{@link #interpolateLinear(float, float, float)
	 * interpolateLinear(start, end, transform(x))}</pre>
	 * 
	 * @param start The start value (i.e. when {@code x == 0}).
	 * @param end The end value (i.e. when {@code x == 1}).
	 * @param x The position, between 0.0 and 1.0 (inclusive).
	 * 
	 * @return The interpolated value.
	 */
	public final float apply(float start, float end, float x) {
		return interpolateLinear(start, end, transform(x));
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
	 * @param x The position between the start and end values, between
	 * {@code 0.0} and {@code 1.0} (inclusive).
	 * 
	 * @return A value interpolated between {@code start} and {@code end}.
	 */
	public static float interpolateLinear(float start, float end, float x) {
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
	public static All polynomialInterpolation(final int degree) {
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
				
				return new All() {
					
					/** The degree. */
					private int n = degree;
					
					@Override
					public float easeInTransform(float x) {
						// f(x) = x^n
						return (float)Math.pow(x, n);
					}
					
					@Override
					public float easeOutTransform(float x) {
						// f(x) = 1 + (1-x)^n
						return 1 + (float)Math.pow(1-x, n);
					}
					
					@Override
					public float easeInOutTransform(float x) {
						if(x < 0.5f) {
							// f(x) = 2^(n-1) * x^n
							//      = x(2x)^(n-1)
							return (float)Math.pow(2*x, n-1);
						} else {
							// f(x) = 1 + (-2)^(n-1) * (x-1)^n		Note this would fail in the non-int implementation
							//      = 1 + (x-1)[2(1-x)]^(n-1)
							return 1 + (x-1)*(float)Math.pow(2-2*x, n-1);
						}
					}
				};
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
	public static All polynomialInterpolation(final double degree) {
		if((long)degree == degree)
			return polynomialInterpolation((int)degree);
		
		if(degree < 1)
			throw new IllegalArgumentException("degree < 1");
		
		return new All() {
			
			/** The polynomial degree. */
			private double n = degree;
			
			@Override
			public float easeInTransform(float x) {
				// f(x) = x^n
				return (float)Math.pow(x, n);
			}
			
			@Override
			public float easeOutTransform(float x) {
				// f(x) = 1 + (1-x)^n
				return 1 + (float)Math.pow(1-x, n);
			}
			
			@Override
			public float easeInOutTransform(float x) {
				if(x < 0.5f) {
					// f(x) = 2^(n-1) * x^n
					//      = x(2x)^(n-1)
					return x * (float)Math.pow(2*x, n-1);
				} else {
					// f(x) = 1 + (-2)^(n-1) * (x-1)^n				Note this form would fail as n is a non-integer
					//      = 1 + (x-1)[2(1-x)]^(n-1)
					return 1 + (x-1)*(float)Math.pow(2-2*x, n-1);
				}
			}
		};
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
	public static All backInterpolation(final float strength) {
		if(strength == 0f)
			return CUBIC;
		
		return new All() {
			
			/** Alias strength to s. */
			private final float s = strength;
			
			@Override
			public float easeInTransform(float x) {
				// f(x) = x^2 ((s+1)x - s)
				
				// Note: The point where this function is a minimum is:
				// (2s/3(s+1), -4s^3 / (27(s+1)^2)
				return x*x*((s+1)*x - s);
			}
			
			@Override
			public float easeOutTransform(float x) {
				// Provided in the reference code, but geogebra tells me this is incorrect
				// g(x) 1 + x*(x-1)*((s+1)*x + s)
				// Instead...
				
				// g(x) = 1 - f(1-x), where f(x) = x^2 ((s+1)x - s) [the ease-in formula]
				//      = 1 + (x-1)^2 ((s+1)(x-1) + s)		Note: can this be simplified?
				
				// Note: The point where this function is a maximum is:
				// ((s+3)/3(s+1), 1 + 4s^3 / (27(s+1)^2)
				x--;
				return 1 + x*x*((s+1)*x + s);
			}
			
			@Override
			public float easeInOutTransform(float x) {
				//float s2 = s * 1.525f;
				
				if(x < 0.5f) {
					// g(x) = f(2x) / 2
					//      = 4x^2 (2(s+1)x - s) / 2
					//      = 2x^2 (2(s+1)x - s)
					return 2*x*x*(2*(s+1)*x - s);
				} else {
					// g(x) = 1 - f(2-2x) / 2
					//      = 1 - (2-2x)^2 ((s+1)(2-2x) - s) / 2
					//      = 1 - 2(1-x)^2 (2(s+1)(1-x) - s)
					//      = 1 + 2(x-1)^2 (2(s+1)(x-1) + s)
					x--;
					return 1 + 2*x*x*(2*(s+1)*x + s);
				}
			}
		};
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
	 *     new Interpolation.EaseIn() {
	 *         &#64;Override
	 *         public float transform(float x) {
	 *             return x*x*x;
	 *         }
	 *     }
	 * );</pre>
	 * 
	 * @param function The transformation function {@code f(x)}.
	 * 
	 * @return An Interpolation object which uses the specified function as its
	 * transformation function.
	 */
	public static All newInterpolation(final EaseIn function) {
		return new All() {
			@Override
			public float easeInTransform(float x) {
				return function.transform(x);
			}
			@Override
			public float easeOutTransform(float x) {
				return 1 - function.transform(1-x);
			}
			@Override
			public float easeInOutTransform(float x) {
				if(x < 0.5f)
					return function.transform(2*x) / 2;
				else
					return 1 - function.transform(2 - 2*x) / 2;
			}
		};
	}
	
	/**
	 * Defines a single method - {@link #transform(float)} - which should be
	 * implemented to transform a value in accordance with a desired
	 * "transformation function" {@code f(x)} of which typical properties are
	 * outlined {@link Interpolation here}.
	 */
	public static interface TransformationFunction {
		/**
		 * Transforms a value.
		 * 
		 * @param x The value.
		 * 
		 * @return The transformed value.
		 */
		float transform(float x);
	}
	
	/**
	 * <p>Ease in interpolation is characterised by a first derivative less
	 * than {@code 1} as {@code x} tends to {@code 0}, and a first derivative
	 * greater than {@code 1} as {@code x} tends to {@code 1}. This results in
	 * an interpolative function which smoothly tends towards {@code start}
	 * and sharply tends toward {@code end}.
	 */
	public static abstract class EaseIn extends Interpolation {
		// nothing to see here, move along
	}
	
	/**
	 * <p>Ease out interpolation is characterised by a first derivative greater
	 * than {@code 1} as {@code x} tends to {@code 0}, and a first derivative
	 * less than {@code 1} as {@code x} tends to {@code 1}.  This results in
	 * an interpolative function which sharply tends towards {@code start} and
	 * and smoothly tends toward {@code end}.
	 */
	public static abstract class EaseOut extends Interpolation {
		// nothing to see here, move along
	}
	
	/**
	 * <p>Ease in-out interpolation is characterised by a reminiscence to
	 * ease-in interpolation for {@code 0 <= x <= 0.5}, and to ease-out
	 * interpolation for {@code 0.5 <= x <= 1}. This implies a first derivative
	 * less than {@code 1} as {@code x} tends to {@code 0} and {@code 1}, and a
	 * first derivative greater than {@code 1} as {@code x} tends to
	 * {@code 0.5}. This results in an interpolative function which smoothly
	 * tends towards both {@code start} and {@code end}.
	 */
	public static abstract class EaseInOut extends Interpolation {
		// nothing to see here, move along
	}
	
	/**
	 * An instance of this class provides all forms of interpolation. Unlike
	 * other subclasses of Interpolation, {@link #transform(float) transform}
	 * and {@link #apply(float, float, float) apply} are not the primary
	 * methods offered by such an instance; however, in the case that they are
	 * used, they will delegate to {@link All#easeInOutTransform(float)
	 * easeInOutTransform} and {@link All#easeInOut(float, float, float)
	 * easeInOut} respectively.
	 * 
	 * @see Interpolation#EaseIn
	 * @see Interpolation#EaseOut
	 * @see Interpolation#EaseInOut
	 */
	public static abstract class All extends Interpolation {
		
		// Ignoring naming conventions because these things should generally
		// be treated as static constants anyway (e.g.
		// Interpolation.CUBIC.EASE_IN). Besides, there'd be naming conflicts
		// if I opted to name them e.g. easeIn()
		
		/** Ease in interpolation. */
		public final EaseIn EASE_IN = new EaseIn() {
			@Override public float transform(float x) { return easeInTransform(x); }
		};
		/** Ease out interpolation. */
		public final EaseOut EASE_OUT = new EaseOut() {
			@Override public float transform(float x) { return easeOutTransform(x); }
		};
		/** Ease in-out interpolation. */
		public final EaseInOut EASE_IN_OUT = new EaseInOut() {
			@Override public float transform(float x) { return easeInOutTransform(x); }
		};
		
		@Override
		public float transform(float x) {
			return easeInOutTransform(x);
		}
		
		/**
		 * Interpolates between two values using the specified interpolation type.
		 * 
		 * @param start The start value.
		 * @param end The end value.
		 * @param x The position between the start and end values, between
		 * {@code 0.0} and {@code 1.0} (inclusive).
		 * @param type The type of interpolation to use.
		 * 
		 * @return A value interpolated between {@code start} and {@code end}.
		 */
		public final float apply(float start, float end, float x, Type type) {
			switch(type) {
				case EASE_IN:
					return easeIn(start, end, x);
				case EASE_OUT:
					return easeOut(start, end, x);
				case EASE_IN_OUT:
					return easeInOut(start, end, x);
			}
			throw new AssertionError();
		}
		
		/**
		 * Transforms the given position using the specified transformation
		 * function.
		 * 
		 * <p>Invoking this method is equivalent to invoking:
		 * <pre>{@link
		 * #apply(float, float, float, Type) apply(0f, 1f, x, type)}</pre>
		 * 
		 * @param x The position, between {@code 0.0} and {@code 1.0} (inclusive).
		 * @param type The type of interpolation to use.
		 *  
		 * @return The transformed position.
		 */
		public final float transform(float x, Type type) {
			switch(type) {
				case EASE_IN:
					return easeInTransform(x);
				case EASE_OUT:
					return easeOutTransform(x);
				case EASE_IN_OUT:
					return easeInOutTransform(x);
			}
			throw new AssertionError();
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
		 * @see {@link Interpolation.EaseIn} for implementation details.
		 */
		public final float easeIn(float start, float end, float x) {
			return interpolateLinear(start, end, easeInTransform(x));
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
		 * @see {@link Interpolation.EaseIn} for implementation details.
		 */
		public abstract float easeInTransform(float x);
		
		/**
		 * Interpolates between two values using ease out interpolation.
		 * 
		 * @param start The start value.
		 * @param end The end value.
		 * @param x The position between the start and end values, between
		 * {@code 0.0} and {@code 1.0} (inclusive).
		 *  
		 * @return A value interpolated between {@code start} and {@code end}.
		 * @see {@link Interpolation.EaseOut} for implementation details.
		 */
		public final float easeOut(float start, float end, float x) {
			return interpolateLinear(start, end, easeOutTransform(x));
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
		 * @see {@link Interpolation.EaseOut} for implementation details.
		 */
		public abstract float easeOutTransform(float x);
		
		/**
		 * Interpolates between two values using ease in-out interpolation.
		 * 
		 * @param start The start value.
		 * @param end The end value.
		 * @param x The position between the start and end values, between
		 * {@code 0.0} and {@code 1.0} (inclusive).
		 *  
		 * @return A value interpolated between {@code start} and {@code end}.
		 * {@link Interpolation.EaseInOut} for implementation details.
		 */
		public final float easeInOut(float start, float end, float x) {
			return interpolateLinear(start, end, easeInOutTransform(x));
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
		 * @see {@link Interpolation.EaseInOut} for implementation details.
		 */
		public abstract float easeInOutTransform(float x);
	}
	
}
