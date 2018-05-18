package com.stabilise.world.gen.action;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.Region;
import com.stabilise.world.World;
import com.stabilise.world.tile.tileentity.TileEntity;

public class ActionAddTileEntity extends Action {
    
    public TileEntity t;
    
    @Override
    public void apply(World w, Region r) {
        w.addTileEntityToUpdateList(t);
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        t = TileEntity.createFromCompound(c.getCompound("t"));
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        t.exportToCompound(c.childCompound("t"));
    }
    
}
