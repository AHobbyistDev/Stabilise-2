package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the thread which is intended to invoke the target method. The
 * presence of this annotation may imply that the operations performed by the
 * annotated block are only thread-safe when executed on the specified thread,
 * or may otherwise lead to unwanted behaviour such as deadlocking if performed
 * on another thread.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface UserThread {
	String[] value();
}
