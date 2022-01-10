package com.stabilise.input;

public class KeyMapping {
    
    int keycode;
    /** Never null, but can be empty. */
    final int[] heldKeys;
    
    KeyMapping(int keycode, int... heldKeys) {
        this.keycode = keycode;
        this.heldKeys = heldKeys;
    }
    
    public boolean hasHeldKeys() {
        return heldKeys.length > 0;
    }
    
    @Override
    public KeyMapping clone() {
        return new KeyMapping(keycode, heldKeys.clone());
    }
}
