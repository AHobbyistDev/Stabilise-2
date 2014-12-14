package com.stabilise.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target class or method relies on the presence of the
 * LWJGL library to function. If a class is annotated with this, it is implied
 * that its operation and hence all of its methods, though they may not
 * necessarily directly interact with LWJGL, are reliant on LWJGL.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LWJGLReliant {
	
}
