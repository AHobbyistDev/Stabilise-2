package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the state of the specified field is technically unguarded and
 * hence vulnerable to concurrent interference from foolish or malicious
 * sources. As a rule of thumb, this annotation should never have any place in
 * your program ever, and if it does, find a way to redesign it!
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface Unguarded {
	
}