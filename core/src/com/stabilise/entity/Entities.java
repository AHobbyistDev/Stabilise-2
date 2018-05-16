package com.stabilise.entity;

import com.stabilise.entity.component.*;
import com.stabilise.entity.component.buffs.*;
import com.stabilise.entity.component.controller.*;
import com.stabilise.entity.component.core.*;
import com.stabilise.entity.component.effect.*;
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
    
    private static Entity e(CCore c, CPhysics p, CController co) 
                                      { return new Entity(c, p, co);    }
    public  static Entity e(CCore c)  { return e(c, p(), co());         }
    
    
    
    public static Entity player() {
        return e(new CPlayerPerson())
                //.addComponent(new CInvulnerability())
                .addComponent(new CUnkillable())
                .addComponent(new CSliceAnchorer());
    }
    
    public static Entity player2() {
        return e(new CPlayerAsGenericEnemy())
                .addComponent(new CInvulnerability())
                .addComponent(new CSliceAnchorer());
    }
    
    public static Entity fireball(long ownerID, int damage) {
        return e(new CFireball(ownerID, damage));
    }
    
    public static Entity item(World w, ItemStack s) {
        return e(new CItem(s));
    }
    
    public static Entity enemy() {
        //return e(p(), new CEnemyController(), new CGenericEnemy())
        //        .addComponent(new CBasicArmour());
        return e(new CPlayerPerson(), p(), new CEnemyController())
                .addComponent(new CDamageAmplifier(2));
    }
    
    public static Entity person() {
        return e(new CPerson(), p(), new CEnemyController());
    }
    
    public static Entity portal(String dimension) {
        return e(new CPortal(dimension), CNoPhysics.INSTANCE, co())
                .addComponent(new CSliceAnchorer());
    }
    
}
