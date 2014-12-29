package com.stabilise.util.maths.physics;

/**
 * A complex number matrix entry.
 */
public class ComplexNumber {
	
	/** The real component. */
	public final float re;
	/** The imaginary component. */
	public final float im;
	
	
	/** creates a new compelx number with a 0 imaginary component */
	public ComplexNumber(float re) {
		this.re = re;
		this.im = 0f;
	}
	
	/**
	 * Creates a new complex number with the specified real and imaginary
	 * components.
	 */
	public ComplexNumber(float re, float im) {
		this.re = re;
		this.im = im;
	}
	
	/**
	 * Multiplies the other entry by this one, and returns the result.
	 */
	public ComplexNumber mul(ComplexNumber o) {
		return new ComplexNumber(re*o.re - im*o.im, re*o.im + im*o.re);
	}
	
	/** returns this + o */
	public ComplexNumber sum(ComplexNumber o) {
		return new ComplexNumber(re + o.re, im + o.im);
	}
	
	/** returns this - o */
	public ComplexNumber sub(ComplexNumber o) {
		return new ComplexNumber(re - o.re, im - o.im);
	}
	
	public ComplexNumber conjugate() {
		return new ComplexNumber(re, -im);
	}
	
	public boolean isReal() {
		return re != 0f && im == 0f;
	}
	
	public boolean isImaginary() {
		return re == 0f && im != 0f;
	}
	
	@Override
	public String toString() {
		if(re == 0f) {
			if(im == 0f) return "0";
			else return Float.toString(im).replace("f", "") + "i";
		} else {
			if(im == 0f) return Float.toString(re).replace("f", "");
			else return Float.toString(re).replace("f", "") + " " + Float.toString(im).replace("f", "") + "i";
		}
	}
	
	public static ComplexNumber sum(ComplexNumber... nums) {
		float re = 0f;
		float im = 0f;
		for(ComplexNumber n : nums) {
			re += n.re;
			im += n.im;
		}
		return new ComplexNumber(re, im);
	}
	
}
