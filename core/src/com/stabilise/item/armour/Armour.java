package com.stabilise.item.armour;

import com.stabilise.item.Item;
import com.stabilise.util.annotation.Incomplete;


/**
 * Extremely simple placeholder prototype armour class.
 */
@Incomplete
public class Armour extends Item {
    
    public enum ArmourType {
        HEAD, BODY, ARMS, LEGS
    }
    
    
    public static final Armour
            TIER_1_HEAD = new Armour(1, "BasicHead", ArmourType.HEAD, 0.05f),
            TIER_1_BODY = new Armour(2, "BasicBody", ArmourType.BODY, 0.07f),
            TIER_1_ARMS = new Armour(3, "BasicArms", ArmourType.ARMS, 0.04f),
            TIER_1_LEGS = new Armour(4, "BasicLegs", ArmourType.LEGS, 0.04f),
            TIER_2_HEAD = new Armour(1, "EliteHead", ArmourType.HEAD, 0.15f),
            TIER_2_BODY = new Armour(2, "EliteBody", ArmourType.BODY, 0.20f),
            TIER_2_ARMS = new Armour(3, "EliteArms", ArmourType.ARMS, 0.15f),
            TIER_2_LEGS = new Armour(4, "EliteLegs", ArmourType.LEGS, 0.15f);
    
    
    
    public final ArmourType type;
    /** Percentage damage reduction, from 0 to 1. */
    public final float reduction;
    
    
    public Armour(int id, String name, ArmourType type, float reduction) {
        super(id, name, 1);
        this.type = type;
        this.reduction = reduction;
    }
    
}
