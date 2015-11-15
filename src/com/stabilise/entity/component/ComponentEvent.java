package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.world.World;


public enum ComponentEvent {
    
    //---------------------  Cont.    Phys.    State
    COLLISION               (false,   false,   true ),
    COLLISION_VERTICAL      (false,   false,   true ),
    COLLISION_HORIZONTAL    (false,   false,   true ),
    COLLISION_TILE          (false,   false,   true ),
    DAMAGED                 (true,    false,   false),
    KILLED                  (true,    true,    false),
    DESTROYED               (true,    true,    true );
    
    private final boolean c,p,s;
    
    private ComponentEvent(boolean c, boolean p, boolean s) {
        this.c = c;
        this.p = p;
        this.s = s;
    }
    
    public void post(World w, Entity e) {
        if(c) e.controller.handle(w, e, this);
        if(p) e.physics.handle(w, e, this);
        if(s) e.core.handle(w, e, this);
    }
    
}
