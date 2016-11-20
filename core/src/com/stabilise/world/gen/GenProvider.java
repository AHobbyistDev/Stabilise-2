package com.stabilise.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.stabilise.entity.Entity;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.World;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.action.*;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

class GenProvider implements WorldProvider {
    
    @SuppressWarnings("unused")
    private final HostWorld w;
    private final Region r;
    
    private int lastX, lastY;
    private Slice lastSlice;
    
    private final Random rnd = new Random();
    
    
    GenProvider(HostWorld w, Region r) {
        this.w = w;
        this.r = r;
        
        lastX = lastY = 0;
        lastSlice = r.getSliceAt(0, 0);
    }
    
    @Override
    public void addEntity(Entity e) {
        ActionAddEntity a = new ActionAddEntity();
        a.e = e;
        actions().add(a);
    }
    
    @Override
    public Slice getSliceAt(int x, int y) {
        if(x == lastX && y == lastY) return lastSlice;
        if(!checkXBound(x) || !checkYBound(y))
            throw new IllegalArgumentException("Accessing outside region!");
        lastX = x;
        lastY = y;
        return lastSlice = r.getSliceAt(
                World.sliceCoordRelativeToRegionFromSliceCoord(x),
                World.sliceCoordRelativeToRegionFromSliceCoord(y));
    }
    
    @Override
    public void setTileAt(int x, int y, int id) {
        getTileAt(x, y).handleRemove(this, x, y);
        getSliceAtTile(x,y).setTileIDAt(
                World.tileCoordRelativeToSliceFromTileCoord(x),
                World.tileCoordRelativeToSliceFromTileCoord(y),
                id);
        Tile.getTile(id).handlePlace(this, x, y);
    }
    
    @Override
    public void setTileEntityAt(int x, int y, TileEntity t) {
        getSliceAtTile(x, y).setTileEntityAt(
                World.tileCoordRelativeToSliceFromTileCoord(x),
                World.tileCoordRelativeToSliceFromTileCoord(y),
                t);
        ActionAddTileEntity a = new ActionAddTileEntity();
        a.t = t;
        actions().add(a);
    }
    
    @Override
    public Random rnd() {
        return rnd;
    }
    
    private List<Action> actions() {
        if(r.queuedActions == null)
            r.queuedActions = new ArrayList<>();
        return r.queuedActions;
    }
    
    //private boolean checkBounds(int c) {
    //    return c >= 0 && c < Region.REGION_SIZE_IN_TILES;
    //}
    
    private boolean checkXBound(int x) {
        return x >= r.offsetX && x < r.offsetX + Region.REGION_SIZE;
    }
    
    private boolean checkYBound(int y) {
        return y >= r.offsetY && y < r.offsetY + Region.REGION_SIZE;
    }
    
}
