package com.stabilise.entity;

import com.stabilise.entity.component.buffs.*;
import com.stabilise.entity.component.controller.*;
import com.stabilise.entity.component.core.*;
import com.stabilise.entity.component.physics.*;
import com.stabilise.item.ItemStack;
import com.stabilise.world.World;

/**
 * A convenience class for producing certain entity types. Almost certainly
 * temporary.
 */
public class Entities {
    
    private Entities() {}
    
    private static CPhysics       p() { return new CPhysicsImpl();       }
    private static CController   co() { return CIdleController.INSTANCE; }
    
    private static Entity e(CPhysics p, CController co, CCore c) 
                                      { return new Entity(p, co, c);    }
    public  static Entity e(CCore c)  { return e(p(), co(), c);         }
    
    
    
    public static Entity player() {
        return e(new CPlayerPerson()).addComponent(new CInvulnerability());
    }
    
    public static Entity player2() {
        return e(new CPlayerAsGenericEnemy()).addComponent(new CInvulnerability());
    }
    
    public static Entity fireball(long ownerID, int damage) {
        return e(new CFireball(ownerID, damage));
    }
    
    public static Entity item(World w, ItemStack s) {
        return e(new CItem(s));
    }
    
    public static Entity enemy() {
        return e(p(), new CEnemyController(), new CGenericEnemy())
                .addComponent(new CBasicArmour());
    }
    
    public static Entity person() {
        return e(p(), new CEnemyController(), new CPerson());
    }
    
    public static Entity portal(String dimension) {
        return e(CNoPhysics.INSTANCE, co(), new CPortal(dimension));
    }
    
}
