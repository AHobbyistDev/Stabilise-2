package com.stabilise.world.loader.impl;

import java.util.ArrayList;
import java.util.List;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.world.Region;
import com.stabilise.world.gen.action.Action;
import com.stabilise.world.loader.IRegionLoader;


/**
 * Handles loading and saving of queued actions
 */
public class ActionLoader implements IRegionLoader {
    
    @Override
    public void load(Region r, DataCompound c, boolean generated) {
        c.optList("queuedActions").peek(actions -> {
            r.queuedActions = new ArrayList<>(actions.size());
            for(int i = 0; i < actions.size(); i++) {
                r.queuedActions.add(Action.read(actions.getCompound()));
            }
        });
    }
    
    @Override
    public void save(Region r, DataCompound c, boolean generated) {
        List<Action> queuedActions = r.queuedActions;
        if(queuedActions != null) {
            DataList actions = c.createList("queuedActions");
            queuedActions.forEach(a -> actions.add(a.toNBT()));
        }
    }
    
}
