package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the the target class or method is <i>not</i> thread-safe.
 * 
 * <p>Though the lack of the presence of the {@link ThreadSafe} annotation is
 * typically sufficient to indicate that a class or method is not thread-safe,
 * this annotation should serve as a a strong reminder of this.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NotThreadSafe {
	
}
