package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the thread which is intended to invoke the target method. The
 * presence of this annotation may serve to remind maintainers of the
 * concurrent conditions under which the annotated block may be executed, to
 * help ensure maintenance and optimisation is performed properly.
 * 
 * <p>This annotation may also be used to imply that the annotated method is
 * not thread safe, and thus may cause unwanted behaviour if multiple threads
 * access this method.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface UserThread {
    String[] value();
}
