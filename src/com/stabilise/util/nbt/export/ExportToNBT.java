package com.stabilise.util.nbt.export;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.stabilise.util.nbt.NBTTagCompound;

/**
 * Indicates that the annotated field should be automatically exported to and
 * from NBT via {@link NBTExporter#exportObj(Object)} and {@link
 * NBTExporter#importObj(Object, NBTTagCompound)}.
 * 
 * @see Exportable
 * @see NBTExporter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExportToNBT {
	
}
