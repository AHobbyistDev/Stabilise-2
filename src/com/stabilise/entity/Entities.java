package com.stabilise.entity;

import com.stabilise.entity.component.controller.*;
import com.stabilise.entity.component.physics.*;
import com.stabilise.entity.component.state.*;


public class Entities {
    
    private Entities() {}
    
    private static CPhysics       p() { return new CPhysicsImpl();        }
    private static CController    c() { return IdleController.INSTANCE;   }
    
    private static Entity         e() { return new Entity();               }
    private static Entity e(CState s) { return e().construct(p(), c(), s); }
    
    public static Entity player() {
        return e(new CPlayerPerson());
    }
    
    public static Entity fireball() {
        return e(new CFireball());
    }
    
}
