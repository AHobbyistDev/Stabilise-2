package com.stabilise.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.box.BoolBox;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.IData;

/**
 * A robust configuration/settings class. Is backed by a {@code DataCompound},
 * {@link #values}, from which the config options may be read or written to.
 * Config files are saved in the JSON format, so that people may edit them
 * easily using their favourite text editor.
 * 
 * <p>A separate {@code DataCompound} of default values is passed into this
 * class. This class uses this set of default values to determine which config
 * options are valid -- if an option doesn't show up in the defaults, or has
 * mismatched types, it may be {@link #sanitise() sanitised}.
 */
public class Config {
    
    // TODO: allow comments in a config file, so that users reading the text
    //  file know what the options are and the valid ranges for those options.
    //  Since I delegate the JSON I/O to libgdx, this probably isn't gonna
    //  happen anytime soon.
    
    public static final Format CONFIG_FORMAT = Format.JSON;
    
    
    /** Contains the actual config data. */
    public final DataCompound values = CONFIG_FORMAT.newCompound();
    
    /** Holds the types of all accepted values and their default values. */
    public final DataCompound defaults;
    
    /** The location where this config/settings file is saved. May be null. */
    private final FileHandle fileLoc;
    
    
    
    /**
     * Creates a new config/settings with the specified defaults and file
     * location.
     * 
     * @throws NullPointerException if either argument is null.
     */
    public Config(DataCompound defaults, FileHandle fileLoc) {
        DataCompound def = defaults.convert(CONFIG_FORMAT);
        this.defaults = def.immutable();
        def.copyInto(values); // fill up the values with the defaults
        this.fileLoc = Objects.requireNonNull(fileLoc);
    }
    
    /**
     * Resets the specified entry to its default value. This doesn't do
     * anything if no such default exists.
     */
    public void reset(String name) {
        IData tag = defaults.getData(name);
        if(tag == null) // isn't even a valid setting that we're resetting
            return;
        values.putData(name, tag); // overwrite with the default
    }
    
    /**
     * Resets all values to their defaults.
     */
    public void resetToDefaults() {
        values.clear();
        defaults.copyInto(values);
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
     * 
     * <p>This method is automatically invoked by {@link #load()}.
     * 
     * @return {@code true} if any entries were changed by this method.
     */
    public boolean sanitise() {
        // boxed to be modifiable from within a lambda
        BoolBox changes = new BoolBox(false);
        
        defaults.forEach((name, def) -> {
            IData value = values.getData(name);
            // If tag isn't present or is of the completely wrong type, set to
            // the default.
            if(value == null || !value.canConvertToTypeOf(def)) {
                values.putData(name, def);
                changes.set(true);
            // If tag is there and is of the wrong -- but compatible -- type,
            // just convert and put it in. A type can be of a compatibly wrong
            // type if e.g., the JSON format doesn't distinguish between
            // byte/short/int/long and float/double, so we just correct it.
            } else if(value.canConvertToTypeOf(def)) {
                values.putData(name, value.convertToTypeOf(def));
                //changes.set(true); // Don't count this as a proper change
            }
        });
        return changes.get();
    }
    
    /**
     * Removes any entries that don't have a default.
     */
    public void removeExcessEntries() {
        Iterator<Map.Entry<String, IData>> itr = values.iterator();
        while(itr.hasNext()) {
            Map.Entry<String, IData> e = itr.next();
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
    
    /**
     * Loads this config/settings file from its given file location given by
     * {@link #getFile()}, and then {@link #sanitise() sanitises} whatever is
     * loaded.
     * 
     * @return the result of {@link #sanitise()}.
     * @throws IOException if an I/O error occurs.
     */
    public boolean load() throws IOException {
        values.clear();
        boolean sanitised;
        try {
            if(fileLoc.exists())
                IOUtil.read(fileLoc, values, Compression.UNCOMPRESSED);
        } finally {
            sanitised = sanitise();
        }
        return sanitised;
    }
    
    /**
     * Saves this config/settings file.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void save() throws IOException {
        IOUtil.writeSafe(fileLoc, values, Compression.UNCOMPRESSED);
    }
    
}
