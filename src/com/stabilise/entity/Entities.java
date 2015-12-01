package com.stabilise.entity;

import com.stabilise.entity.component.buffs.*;
import com.stabilise.entity.component.controller.*;
import com.stabilise.entity.component.core.*;
import com.stabilise.entity.component.physics.*;
import com.stabilise.item.ItemStack;
import com.stabilise.world.World;

/**
 * Provides static factory methods for entities.
 */
public class Entities {
    
    private Entities() {}
    
    private static CPhysics       p() { return new PhysicsImpl();       }
    private static CController   co() { return IdleController.INSTANCE; }
    
    private static Entity e(CPhysics p, CController co, CCore c) 
                                      { return new Entity(p, co, c);    }
    public  static Entity e(CCore c)  { return e(p(), co(), c);         }
    
    
    
    public static Entity player() {
        return e(new CPlayerPerson()).addComponent(new CInvulnerability());
    }
    
    public static Entity fireball(long ownerID, int damage) {
        return e(new CFireball(ownerID, damage));
    }
    
    public static Entity item(World w, ItemStack s) {
        return e(new CItem(s));
    }
    
    public static Entity enemy() {
        return e(p(), new EnemyController(), new CGenericEnemy())
                .addComponent(new CBasicArmour());
    }
    
    public static Entity person() {
        return e(p(), new EnemyController(), new CPerson());
    }
    
}
