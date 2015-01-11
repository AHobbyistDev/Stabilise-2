package com.stabilise.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a method which has been set as private because it is
 * inelegant, or a better public alternative exists.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PrivateBecauseInelegant {
	String[] alternative();
}
