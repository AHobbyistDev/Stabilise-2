package com.stabilise.item.weapon;

import com.stabilise.item.Item;
import com.stabilise.util.annotation.Incomplete;

/**
 * Extremely simple placeholder prototype weapon class.
 */
@Incomplete
public class Weapon extends Item {
    
    public static final Weapon SWORD_TIER_1 = new Weapon(10);
    public static final Weapon SWORD_TIER_2 = new Weapon(25);
    
    
    /** The weapon's hitbox. */
    //public Shape hitbox;
    /** The weapon's base damage. */
    public int damage;
    
    
    /**
     * Creates a new Weapon.
     */
    Weapon(int damage) {
        this.damage = damage;
    }
    
    public int getDamage() {
        return damage;
    }
    
}
