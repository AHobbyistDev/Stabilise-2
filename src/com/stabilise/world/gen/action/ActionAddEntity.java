package com.stabilise.world.gen.action;

import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.Region;
import com.stabilise.world.World;


public class ActionAddEntity extends Action {
    
    @Override
    public void apply(World w, Region r) {
        
    }
    
    @Override
    public NBTTagCompound toNBT() {
        return null;
    }
    
    @Override
    public Action fromNBT(NBTTagCompound tag) {
        return null;
    }
    
}
