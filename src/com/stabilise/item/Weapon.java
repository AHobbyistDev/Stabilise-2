package com.stabilise.item;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.shape.Rectangle;

/**
 * [Insert a dictionary definition for weapon here along with other information
 * relevant to the game.]
 */
@Incomplete
public class Weapon extends Item {
    
    /** The mass of the weapon. */
    public int mass;
    /** The weapon's hitboxes. */
    public Rectangle[] hitboxes;
    /** The weapon's base damage. */
    public int damage;
    
    
    /**
     * Creates a new Weapon.
     */
    Weapon() {
        super();
    }
    
}
