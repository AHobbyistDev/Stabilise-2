package com.stabilise.world.tile.tileentity;

import com.stabilise.entity.EntityItem;
import com.stabilise.item.BoundedContainer;
import com.stabilise.item.Container;
import com.stabilise.item.ItemStack;
import com.stabilise.util.nbt.NBTTagCompound;
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
	public Container items;
	
	
	/**
	 * Creates a new chest tile entity.
	 * 
	 * @param x The x-coordinate of the tile entity, in tile-lengths.
	 * @param y The y-coordinate of the tile entity, in tile-lengths.
	 */
	public TileEntityChest(int x, int y) {
		super(x, y);
		
		items = new BoundedContainer(CAPACITY);
	}
	
	@Override
	public void handleAdd(World world, int x, int y) {
		// nothing to see here, move along
	}
	
	@Override
	public void handleRemove(World world, int x, int y) {
		for(ItemStack s : items) {
			EntityItem e = new EntityItem(s);
			e.dx = world.getRnd().nextFloat() * 0.4f - 0.2f;
			e.dy = 0.1f + world.getRnd().nextFloat() * 0.2f;
			world.addEntity(e, x + 0.5f, y + 0.5f);
		}
	}
	
	@Override
	protected void writeNBT(NBTTagCompound tag) {
		tag.addList("items", items.toNBT());
	}
	
	@Override
	public void fromNBT(NBTTagCompound tag) {
		items.fromNBT(tag.getList("items"));
	}
	
	@Override
	public String toString() {
		return "Chest: " + items.toString();
	}
	
}
