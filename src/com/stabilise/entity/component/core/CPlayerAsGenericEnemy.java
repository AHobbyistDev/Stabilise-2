package com.stabilise.entity.component.core;

import java.util.Iterator;

import com.stabilise.core.Constants;
import com.stabilise.entity.Entity;
import com.stabilise.item.BoundedContainer;
import com.stabilise.item.Container;
import com.stabilise.item.IContainer;
import com.stabilise.item.Item;
import com.stabilise.item.ItemStack;


public class CPlayerAsGenericEnemy extends CGenericEnemy implements IContainer {
    
    public final Container inventory = new BoundedContainer(Constants.INVENTORY_CAPACITY);
    
    @Override
    public void init(Entity e) {
        super.init(e);
        acceleration = 1.3f;
        airAcceleration = 1f;
    }
    
    @Override
    public int size() {
        return inventory.size();
    }
    
    @Override
    public boolean canAddStack(ItemStack stack) {
        return inventory.canAddStack(stack);
    }
    
    @Override
    public int addItem(Item item, int quantity) {
        return inventory.addItem(item, quantity);
    }
    
    @Override
    public boolean addStack(ItemStack stack) {
        return inventory.addStack(stack);
    }
    
    @Override
    public boolean contains(Item item) {
        return inventory.contains(item);
    }
    
    @Override
    public boolean contains(Item item, int minQuantity) {
        return inventory.contains(item, minQuantity);
    }
    
    @Override
    public void clear() {
        inventory.clear();
    }
    
    @Override
    public Iterator<ItemStack> iterator() {
        return inventory.iterator();
    }
    
}
