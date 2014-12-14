package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.stabilise.core.Application;

/**
 * Indicates that the target class relies forms an important part of the core
 * web of classes required for the
 * {@link com.stabilise.core.Application Application} class to function.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface UsedByApplication {
	/** The classes which uses this class. */
	Class<?>[] value() default Application.class;
}
