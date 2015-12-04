package com.stabilise.world.gen.action;

import com.stabilise.util.collect.registry.Registries;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.Region;
import com.stabilise.world.World;

public abstract class Action {
    
    private static final TypeFactory<Action> registry = Registries.typeFactory();
    
    static {
        registry.registerUnsafe(1, ActionAddEntity.class);
        registry.registerUnsafe(2, ActionAddTileEntity.class);
    }
    
    public abstract void apply(World w, Region r);
    
    public abstract DataCompound toNBT(DataCompound tag);
    public abstract Action fromNBT(DataCompound tag);
    
    
    public static Action read(DataCompound tag) {
        return registry.create(tag.getByte("id")).fromNBT(tag);
    }
    
}
