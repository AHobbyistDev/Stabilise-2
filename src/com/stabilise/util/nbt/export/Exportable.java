package com.stabilise.util.nbt.export;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an instance of the annotated class can be exported to NBT
 * as a field of an object passed to {@link NBTExporter#exportObj(Object)}.
 * 
 * @see ExportToNBT
 * @see NBTExporter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Exportable {
	
}