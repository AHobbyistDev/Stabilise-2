package com.stabilise.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Defines the two complementary methods {@code readData} and {@code writeData}
 * which allow an object to export its properties across a data stream, across
 * which it may be reconstructed.
 * 
 * <p>As a general guideline, if {@code obj1} is an object whose data is
 * exported through {@code writeData}, and {@code obj2} is an object which
 * reads in {@code obj1}'s exported data through {@code readData}, then {@code
 * obj1.equals(obj2)} should return {@code true}.
 */
public interface Sendable {
	
	/**
	 * Reads this object's data in from the given DataInputStream.
	 * 
	 * @throws NullPointerException if {@code in} is {@code null}.
	 * @throws IOException for standard reasons.
	 */
	void readData(DataInputStream in) throws IOException;
	
	/**
	 * Writes this object's data to the given DataOutputStream.
	 * 
	 * @throws NullPointerException if {@code out} is {@code null}.
	 * @throws IOException for standard reasons.
	 */
	void writeData(DataOutputStream out) throws IOException;
	
}
