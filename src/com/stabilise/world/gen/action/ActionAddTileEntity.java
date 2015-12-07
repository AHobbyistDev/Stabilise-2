package com.stabilise.world.gen.action;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.Region;
import com.stabilise.world.World;
import com.stabilise.world.tile.tileentity.TileEntity;

public class ActionAddTileEntity extends Action {
    
    public TileEntity t;
    
    @Override
    public void apply(World w, Region r) {
        w.addTileEntity(t);
    }
    
    @Override
    public DataCompound toNBT() {
        DataCompound tag = DataCompound.create();
        tag.put("t", t.toNBT());
        return tag;
    }
    
    @Override
    public Action fromNBT(DataCompound tag) {
        t = TileEntity.createTileEntityFromNBT(tag.getCompound("t"));
        return null;
    }
    
}
