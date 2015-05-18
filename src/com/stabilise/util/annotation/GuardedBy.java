package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If placed on a field, indicates that the state of the target field is
 * guarded by the intrinsic lock of the specified object (or, if the object
 * is a {@link java.util.concurrent.locks.Lock Lock}, then possibly the lock
 * itself). If placed on a method, this indicates that all invocations of that
 * method must be made while the specified lock is acquired.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface GuardedBy {
	String value();
}