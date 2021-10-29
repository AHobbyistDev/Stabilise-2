package com.stabilise.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

import java.io.IOException;

public class ControlConfig<T extends Enum<T> & IControl> {
    
    public static final Format CONFIG_FORMAT = Format.JSON;
    public static final Compression CONFIG_COMPRESSION = Compression.UNCOMPRESSED;
    
    private FileHandle fileLoc;
    T[] controls;
    /** Maps control ID (i.e. its enum ordinal) to its KeyMapping. */
    KeyMapping[] mappings;
    private KeyMapping[] defaults;
    
    
    public ControlConfig(Class<T> controlEnumClass) {
        this.controls = controlEnumClass.getEnumConstants();
        
        mappings = new KeyMapping[controls.length];
        defaults = new KeyMapping[controls.length];
        
        for(int i = 0; i < controls.length; i++)
            defaults[i] = controls[i].defaultMapping();
        
        resetToDefaults(); // mappings = defaults
    }
    
    public void loadConfig(FileHandle file) {
        DataCompound c = CONFIG_FORMAT.newCompound();
        boolean read = false;
        try {
            if(fileLoc.exists()) {
                IOUtil.read(fileLoc, c, CONFIG_COMPRESSION);
                read = true;
            }
        } catch(IOException e) {
            Log.get().postWarning("Could not load controls config: " + e.getMessage());
        }
        
        c.setStrict(false); // in case the JSON reads it as an "incorrect" type
        
        if(read) {
            for(int i = 0; i < controls.length; i++) {
                String ctrlName = controls[i].identifier();
                if(c.containsI32(ctrlName)) {
                    mappings[i].keycode = c.getI32(ctrlName);
                } else if(c.containsString(ctrlName)) {
                    // TODO: it'll return -1 if not valid
                    mappings[i].keycode = Input.Keys.valueOf(c.getString(ctrlName));
                } else if(c.containsCompound(ctrlName)) {
                    DataCompound c2 = c.getCompound(ctrlName);
                    mappings[i].keycode = c2.getI32("key");
                    
                }
            }
        } else {
            resetToDefaults();
        }
    }
    
    public void saveConfig() {
    
    }
    
    public void resetToDefaults() {
        // We clone the KeyMapping objects so that later modification to them
        // doesn't change the defaults.
        for(int i = 0; i < mappings.length; i++)
            mappings[i] = defaults[i].clone();
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
    private boolean sanitise() {
        return false;
    }
    
}
