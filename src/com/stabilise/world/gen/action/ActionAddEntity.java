package com.stabilise.world.gen.action;

import com.stabilise.entity.Entity;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.Region;
import com.stabilise.world.World;


public class ActionAddEntity extends Action {
    
    public Entity e;
    
    @Override
    public void apply(World w, Region r) {
        
    }
    
    @Override
    public DataCompound toNBT() {
        return DataCompound.create();
    }
    
    @Override
    public Action fromNBT(DataCompound tag) {
        return null;
    }
    
}
