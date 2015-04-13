package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the the target class, method or field is <i>not</i> thread-safe.
 * 
 * <p>Though the lack of the presence of the {@link ThreadSafe} annotation is
 * typically sufficient to indicate that a class or method is not thread-safe,
 * this annotation should serve as a a strong reminder of this.
 * 
 * <p>Though thread safety or lack thereof for particular fields should be
 * documented regardless, either in class declarations or implicitly through
 * modifiers (such as {@code volatile}), annotating a field with this should
 * similarly serve as an extremely strong reminder not to let said field escape
 * thread confinement.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface NotThreadSafe {
	
}
