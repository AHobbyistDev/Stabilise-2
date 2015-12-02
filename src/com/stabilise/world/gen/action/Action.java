package com.stabilise.world.gen.action;

import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.Region;
import com.stabilise.world.World;

public abstract class Action {
    
    private static final TypeFactory<Action> registry = new TypeFactory<>();
    
    static {
        registry.registerUnsafe(1, ActionAddEntity.class);
        registry.registerUnsafe(2, ActionAddTileEntity.class);
    }
    
    public abstract void apply(World w, Region r);
    
    public abstract NBTTagCompound toNBT();
    public abstract Action fromNBT(NBTTagCompound tag);
    
    
    public static Action read(NBTTagCompound tag) {
        return registry.create(tag.getByte("id")).fromNBT(tag);
    }
    
}
