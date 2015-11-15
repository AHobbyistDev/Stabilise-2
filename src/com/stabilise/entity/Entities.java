package com.stabilise.entity;

import com.stabilise.entity.component.controller.*;
import com.stabilise.entity.component.physics.*;
import com.stabilise.entity.component.state.*;
import com.stabilise.item.ItemStack;
import com.stabilise.world.World;


public class Entities {
    
    private Entities() {}
    
    private static CPhysics       p() { return new CPhysicsImpl();         }
    private static CController    c() { return IdleController.INSTANCE;    }
    
    private static Entity         e() { return new Entity();               }
    public  static Entity e(CState s) { return e().construct(p(), c(), s); }
    
    public static Entity player() {
        return e(new CPlayerPerson());
    }
    
    public static Entity fireball(long ownerID, int damage) {
        return e(new CFireball(ownerID, damage));
    }
    
    public static Entity item(World w, ItemStack s) {
        return e(new CItem(s));
    }
    
    public static Entity enemy() {
        return e().construct(p(), new EnemyController(), new CGenericEnemy());
    }
    
}
