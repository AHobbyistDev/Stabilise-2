package com.stabilise.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.data.MapCompound;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ITag;

/**
 * A robust Settings/Configuration class.
 */
public class Config {
	
	public static final Format CONFIG_FORMAT = Format.JSON;
	
	/** Holds the types of all accepted values and their default values. */
	private final MapCompound defaults;
	
	/** Contains the actual config data. */
	public final DataCompound values = CONFIG_FORMAT.newCompound();
	private final MapCompound valuesAsMap = values.asMapCompound();
	
	/** The location where this config/settings file is saved. May be null. */
	private final FileHandle fileLoc;
	
	
	public Config(DataCompound defaults, FileHandle fileLoc) {
		this.defaults = defaults.convert(CONFIG_FORMAT).asMapCompound();
		this.defaults.putAll(valuesAsMap); // fill up the values with the defaults
		this.fileLoc = fileLoc;
	}
	
	/**
	 * Resets the specified entry to its default value. This doesn't do
	 * anything if no such default exists.
	 */
	public void reset(String name) {
		ITag tag = defaults.getData(name);
		if(tag == null) // isn't even a valid setting that we're resetting
			return;
		valuesAsMap.putData(name, tag); // overwrite with the default
	}
	
	/**
	 * When a config/settings file is loaded, it may not be properly sanitised
	 * -- the user may have done one or more of the following:
	 * 
	 * <ul>
	 * <li>1) Removed one or more entries.
	 * <li>2) Added one or more additional entries.
	 * <li>3) Changed the value of one or more entries to the wrong data type.
	 * <li>4) Changed the value of one or more entries to a nonsense/invalid
	 *     value.
	 * </ul>
	 * 
	 * This method corrects 1) and 3) by resetting affected entries to their
	 * default values. Though we could, we don't correct 2) (see {@link
	 * #removeExcessEntries()} to do this); and to correct 4) we need a
	 * knowledge of the space of valid values, so this must be corrected
	 * elsewhere.
	 */
	public void sanitise() {
		defaults.forEachTag((name,tag) -> {
			ITag valueTag = valuesAsMap.getData("name");
			if(valueTag == null || tag.isSameType(valueTag))
				valuesAsMap.putData(name, tag);
		});
	}
	
	/**
	 * Removes any entries that don't have a default.
	 */
	public void removeExcessEntries() {
		Iterator<Map.Entry<String, ITag>> itr = valuesAsMap.iterator();
		while(itr.hasNext()) {
			Map.Entry<String, ITag> e = itr.next();
			if(!defaults.contains(e.getKey()))
				itr.remove();
		}
	}
	
	/**
	 * Gets the file reference for this config/settings.
	 */
	public FileHandle getFile() {
		return fileLoc;
	}
	
	public void load() throws IOException {
		valuesAsMap.clear();
		IOUtil.read(fileLoc, values, Compression.UNCOMPRESSED);
	}
	
	public void save() throws IOException {
		IOUtil.writeSafe(fileLoc, values, Compression.UNCOMPRESSED);
	}
	
}
