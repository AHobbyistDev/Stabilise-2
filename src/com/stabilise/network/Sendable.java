package com.stabilise.network;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The {@code Sendable} interface provides an object with methods to export it
 * as and import it from a byte array.
 */
public interface Sendable {
	
	/**
	 * Requests for the object's data in the form of a byte array.
	 * <p>This should be such that the object's {@code fromByteArray} method
	 * is capable of completely reconstructing the object from the given array.
	 * </p>
	 * 
	 * @return An array of bytes representing the object's properties.
	 */
	void write(OutputStream dos);
	
	/**
	 * Sets the object's properties based on the given byte array.
	 * <p>This should be such that the properties set by this method should be
	 * the same as those of the object from which an invocation of {@code
	 * toByteArray} generated the byte array.</p>
	 * 
	 * @param data An array of bytes representing the object's properties.
	 */
	void read(InputStream in);
	
	/**
	 * Requests for the object's data in the form of a byte array.
	 * <p>This should be such that the object's {@code fromByteArray} method
	 * is capable of completely reconstructing the object from the given array.
	 * </p>
	 * 
	 * @return An array of bytes representing the object's properties.
	 */
	//byte[] toByteArray();
	
	/**
	 * Sets the object's properties based on the given byte array.
	 * <p>This should be such that the properties set by this method should be
	 * the same as those of the object from which an invocation of {@code
	 * toByteArray} generated the byte array.</p>
	 * 
	 * @param data An array of bytes representing the object's properties.
	 */
	//void fromByteArray(byte[] data);
	
}
