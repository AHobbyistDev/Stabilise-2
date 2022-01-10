package com.stabilise.input;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.*;

import java.io.IOException;
import java.util.*;


/**
 * This class holds the config for a specified set of controls.
 */
public class ControlConfig<T extends Enum<T> & IControl> {
    
    public static final Format CONFIG_FORMAT = Format.JSON;
    public static final Compression CONFIG_COMPRESSION = Compression.UNCOMPRESSED;
    
    T[] controls;
    private final String controlClassName;
    
    /** The maximum number of KeyMappings each control is allowed to have. */
    private int maxMappings;
    /** Maps control to its KeyMappings. */
    Map<T, List<KeyMapping>> mappings;
    
    
    public ControlConfig(Class<T> controlEnumClass) {
        this(controlEnumClass, 1);
    }
    
    public ControlConfig(Class<T> controlEnumClass, int maxMappings) {
        this.controls = controlEnumClass.getEnumConstants();
        this.controlClassName = controlEnumClass.getName();
        this.maxMappings = maxMappings;
        
        mappings = new EnumMap<>(controlEnumClass);
        for(T ctrl : controls)
            mappings.put(ctrl, new ArrayList<>(1));
        
        resetToDefaults();
    }
    
    /**
     * Loads the config from the given file.
     *
     * @return {@code false} if the config was successfully loaded from the
     * file; {@code true} if the config should be saved -- if either the file
     * couldn't be read, or if the file requires updating as modifications were
     * made.
     */
    public boolean loadConfig(FileHandle file) {
        DataCompound c = null;
        boolean read = false;
        boolean changesToSave = false;
        
        try {
            if(file.exists()) {
                c = IOUtil.read(file, CONFIG_FORMAT, CONFIG_COMPRESSION);
                read = true;
            }
        } catch(IOException e) {
            Log.get().postWarning("Could not load config for control [" +
                    controlClassName + "]: " + e.getMessage());
        }
        
        if(read) {
            for(T ctrl : controls) {
                IData d = c.getData(ctrl.identifier());
                
                if(d == null) {
                    // Missing config - just set to default
                    resetToDefault(ctrl);
                    changesToSave = true;
                } else {
                    if(d.isList()) {
                        DataList list = d.asList();
                        
                        mappings.get(ctrl).clear();
                        
                        while(list.hasNext() && mappings.get(ctrl).size() <= maxMappings)
                            changesToSave |= readConfig(list.getNext(), ctrl);
                    } else { // compound, int, or string
                        mappings.get(ctrl).clear();
                        
                        changesToSave |= readConfig(d, ctrl);
                    }
                }
            }
            
            changesToSave |= removeConflictingMappings();
        } else {
            resetToDefaults();
            changesToSave = true;
        }
        
        return changesToSave;
    }
    
    // for use from within loadConfig()
    private boolean readConfig(IData d, T ctrl) {
        boolean changesToSave = false;
        if(d.isCompound()) {
            DataCompound compound = d.asCompound();
            
            OptionalInt keycode = readStringOrInt(compound.getData("key"));
            if(!keycode.isPresent())
                return true;
            
            DataList list = compound.getList("heldKeys");
            int[] heldKeys = new int[list.size()];
            int j = 0;
            while(list.hasNext()) {
                OptionalInt heldKey = readStringOrInt(list.getNext());
                if(heldKey.isPresent()) {
                    heldKeys[j] = heldKey.getAsInt();
                    j++;
                }
            }
            
            // At least one of the held keys was bunk
            if(j != list.size()) {
                changesToSave = true;
                heldKeys = Arrays.copyOf(heldKeys, j);
            }
            
            mappings.get(ctrl).add(new KeyMapping(keycode.getAsInt(), heldKeys));
        } else {
            OptionalInt keycode = readStringOrInt(d);
            if(keycode.isPresent())
                mappings.get(ctrl).add(new KeyMapping(keycode.getAsInt()));
            else
                changesToSave = true;
        }
        
        return changesToSave;
    }
    
    private OptionalInt readStringOrInt(IData d) {
        if(d.isString()) {
            return Controller.stringToKeycode(d.asString().get());
        } else if(d.canConvertToType(IData.DataType.I32)) {
            int keycode = d.isI32()
                    ? d.asI32().get()
                    : d.convertToType(IData.DataType.I32).asI32().get();
            if(Controller.isValidKeycode(keycode))
                return OptionalInt.of(keycode);
        }
        
        return OptionalInt.empty();
    }
    
    private boolean removeConflictingMappings() {
        // TODO
        return false;
    }
    
    public void saveConfig(FileHandle file) {
        DataCompound c = CONFIG_FORMAT.newCompound();
        for(T ctrl : controls) {
            List<KeyMapping> mappingList = mappings.get(ctrl);
            if(mappingList.size() == 1) {
                KeyMapping mapping = mappingList.get(0);
                if(mapping.hasHeldKeys()) {
                    DataCompound cInner = c.childCompound(ctrl.identifier());
                    cInner.put("key", mapping.keycode);
                    DataList lInner = cInner.childList("heldKeys");
                    for(int heldKey : mapping.heldKeys)
                        lInner.add(heldKey);
                } else {
                    c.put(ctrl.identifier(), mapping.keycode);
                }
            } else {
                DataList list = c.childList(ctrl.identifier());
                for(KeyMapping mapping : mappingList) {
                    if(mapping.hasHeldKeys()) {
                        DataCompound cInner = list.childCompound();
                        cInner.put("key", mapping.keycode);
                        DataList lInner = cInner.childList("heldKeys");
                        for(int heldKey : mapping.heldKeys)
                            lInner.add(heldKey);
                    } else {
                        list.add(mapping.keycode);
                    }
                }
            }
        }
    
        try {
            IOUtil.writeSafe(file, c, CONFIG_COMPRESSION);
        } catch(IOException e) {
            Log.get().postSevere("Could not save config!", e);
        }
    }
    
    public void resetToDefaults() {
        for(T ctrl : controls)
            resetToDefault(ctrl);
    }
    
    private void resetToDefault(T ctrl) {
        List<KeyMapping> list = mappings.get(ctrl);
        list.clear();
        list.add(ctrl.defaultMapping().clone());
    }
    
}
