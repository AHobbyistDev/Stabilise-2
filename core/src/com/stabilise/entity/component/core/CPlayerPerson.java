package com.stabilise.entity.component.core;

import com.stabilise.core.Constants;
import com.stabilise.item.BoundedContainer;
import com.stabilise.item.Container;
import com.stabilise.item.IContainer;
import com.stabilise.item.Item;
import com.stabilise.item.ItemStack;


public class CPlayerPerson extends CPerson implements IContainer {
    
    public final Container inventory = new BoundedContainer(Constants.INVENTORY_CAPACITY);
    
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
    public String toString() {
        return "Player inventory: " + inventory;
    }
    
}
