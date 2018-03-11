package com.stabilise.world.tile.tileentity;

import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.item.BoundedContainer;
import com.stabilise.item.Container;
import com.stabilise.item.ItemStack;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * A tile entity representing a chest and its contents.
 */
public class TileEntityChest extends TileEntity {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** A standard chest's item capacity. */
    public static final int CAPACITY = 16;
    
    //--------------------==========--------------------
    //------------=====Member Variables=====------------
    //--------------------==========--------------------
    
    /** The chest's contents. */
    public final Container items = new BoundedContainer(CAPACITY);
    
    
    @Override
    public void handleAdd(World world, Position pos) {
        // nothing to see here, move along
    }
    
    @Override
    public void handleRemove(World world, Position pos) {
        for(ItemStack s : items) {
            Entity e = Entities.item(world, s);
            e.pos.set(pos, 0.5f, 0.5f);
            world.addEntity(e);
        }
    }
    
    @Override
    protected void writeNBT(DataCompound tag) {
        tag.put("items", items.toNBT());
    }
    
    @Override
    public void fromNBT(DataCompound tag) {
        items.fromNBT(tag.getList("items"));
    }
    
    @Override
    public String toString() {
        return "Chest at " + pos.toGlobalString() + ": " + items.toString();
    }
    
}
