package com.stabilise.util;

/**
 * A functional interface which consumes two integers and produces one.
 */
@FunctionalInterface
public interface BiIntFunction {
	
	int apply(int x, int y);
	
}
