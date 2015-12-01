package com.stabilise.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.stabilise.entity.Entity;
import com.stabilise.world.Slice;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.action.Action;
import com.stabilise.world.tile.tileentity.TileEntity;

class GenProvider implements WorldProvider {
    
    private List<Action> actions = new ArrayList<Action>();
    
    @Override
    public void addEntity(Entity e) {
        
    }
    
    @Override
    public Slice getSliceAt(int x, int y) {
        return null;
    }
    
    @Override
    public void setTileAt(int x, int y, int id) {
        
    }
    
    @Override
    public void setTileEntityAt(int x, int y, TileEntity t) {
        
    }
    
    @Override
    public Random getRnd() {
        return null;
    }
    
}
