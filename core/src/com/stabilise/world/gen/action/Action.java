package com.stabilise.world.gen.action;

import com.stabilise.util.collect.registry.Registries;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Exportable;
import com.stabilise.world.Region;
import com.stabilise.world.World;

public abstract class Action implements Exportable {
    
    private static final TypeFactory<Action> registry = Registries.typeFactory();
    
    static {
        registry.registerUnsafe(1, ActionAddEntity.class);
        registry.registerUnsafe(2, ActionAddTileEntity.class);
    }
    
    public abstract void apply(World w, Region r);
    
    
    
    public static Action read(DataCompound c) {
        Action a = registry.create(c.getI8("id"));
        a.importFromCompound(c);
        return a;
    }
    
}
