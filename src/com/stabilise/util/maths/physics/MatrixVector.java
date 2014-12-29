package com.stabilise.util.maths.physics;

public class MatrixVector {
	
	public final ComplexNumber[] elements;
	
	
	/**
	 * Creates a new MatrixVector with the specified elements.
	 */
	public MatrixVector(ComplexNumber... elements) {
		this.elements = elements;
	}
	
	public MatrixVector(float... components) {
		elements = new ComplexNumber[components.length];
		for(int i = 0; i < elements.length; i++)
			elements[i] = new ComplexNumber(components[i]);
	}
	
	/**
	 * Creates a new MatrixVector with the elements instantiated to an array of
	 * the specified length.
	 */
	public MatrixVector(int numElements) {
		elements = new ComplexNumber[numElements];
	}
	
	/** returns this.v. In bra-ket notation returns 〈this|v〉*/
	public ComplexNumber dot(MatrixVector v) {
		// Dot product is the sum of the products of corresponding elements
		ComplexNumber sum = elements[0].conjugate().mul(v.elements[0]);
		for(int i = 1; i < elements.length; i++)
			sum = sum.sum(elements[i].conjugate().mul(v.elements[i]));
		return sum;
	}
	
	/** return this x v 
	 * <p>currently only returns the proper result if this is a 3-vector */
	public MatrixVector cross(MatrixVector v) {
		MatrixVector result = new MatrixVector(elements.length);
		for(int i = 0; i < elements.length; i++) {
			result.elements[i] = atIndexMod(i+1).mul(v.atIndexMod(i+2)).sub(
					atIndexMod(i+2).mul(v.atIndexMod(i+1))
			);
		}
		return result;
	}
	
	private ComplexNumber atIndexMod(int index) {
		return elements[index % elements.length];
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < elements.length; i++) {
			sb.append(elements[i].toString());
			if(i < elements.length - 1)
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
	
}
