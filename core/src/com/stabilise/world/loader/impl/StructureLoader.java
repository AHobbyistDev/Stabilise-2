package com.stabilise.world.loader.impl;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.world.Region;
import com.stabilise.world.Region.QueuedStructure;
import com.stabilise.world.loader.IRegionLoader;


/**
 * Handles loading and saving of queued structures for regions.
 */
public class StructureLoader implements IRegionLoader {
    
    @Override
    public void load(Region r, DataCompound c, boolean generated) {
        c.optList("queuedStructures").peek(structures -> {
            for(int i = 0; i < structures.size(); i++) {
                DataCompound structure = structures.getCompound();
                QueuedStructure s = new Region.QueuedStructure();
                s.structureName = structure.getString("structureName");
                s.sliceX = structure.getI32("sliceX");
                s.sliceY = structure.getI32("sliceY");
                s.tileX = structure.getI32("tileX");
                s.tileY = structure.getI32("tileY");
                s.offsetX = structure.getI32("offsetX");
                s.offsetY = structure.getI32("offsetY");
                r.addStructure(s);
            }
        });
    }
    
    @Override
    public void save(Region r, DataCompound c, boolean generated) {
        if(r.hasQueuedStructures()) {
            DataList structures = c.childList("queuedStructures");
            for(QueuedStructure s : r.getStructures()) {
                DataCompound structure = structures.childCompound();
                structure.put("schematicName", s.structureName);
                structure.put("sliceX", s.sliceX);
                structure.put("sliceY", s.sliceY);
                structure.put("tileX", s.tileX);
                structure.put("tileY", s.tileY);
                structure.put("offsetX", s.offsetX);
                structure.put("offsetY", s.offsetY);
            }
        }
    }
    
}
