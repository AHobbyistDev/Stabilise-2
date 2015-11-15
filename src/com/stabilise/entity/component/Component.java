package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.world.World;


public interface Component {
    
    void init(World w, Entity e);
    void update(World w, Entity e);
    
}
