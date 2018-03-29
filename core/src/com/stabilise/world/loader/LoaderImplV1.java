package com.stabilise.world.loader;

import static com.stabilise.world.Region.REGION_SIZE;

import java.util.ArrayList;
import java.util.List;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.Region.QueuedStructure;
import com.stabilise.world.gen.action.Action;
import com.stabilise.world.tile.tileentity.TileEntity;

public class LoaderImplV1 implements IRegionLoader {
	
	@Override
	public void load(Region r, DataCompound c, boolean generated) {
		if(generated) {
            for(int y = 0; y < REGION_SIZE; y++) {            // Row (y)
                for(int x = 0; x < REGION_SIZE; x++) {        // Col (x)
                    DataCompound sliceTag = c.getCompound("slice" + x + "_" + y);
                    Slice s = new Slice(r.offsetX + x, r.offsetY + y,
                            sliceTag.getIntArr("tiles"),
                            sliceTag.getIntArr("walls"),
                            sliceTag.getByteArr("light"));
                    
                    DataList tileEntities = sliceTag.createList("tileEntities");
                    if(tileEntities.size() > 0)
                        s.initTileEntities();
                    for(int i = 0; i < tileEntities.size(); i++) {
                        DataCompound tc = tileEntities.getCompound();
                        TileEntity te = TileEntity.createTileEntityFromNBT(tc);
                        s.tileEntities[te.pos.getLocalTileY()][te.pos.getLocalTileX()] = te; 
                    }
                    
                    r.slices[y][x] = s;
                }
            }
        }
        
        c.optList("queuedActions").peek(actions -> {
            r.queuedActions = new ArrayList<>(actions.size());
            for(int i = 0; i < actions.size(); i++) {
                r.queuedActions.add(Action.read(actions.getCompound()));
            }
        });
        
        c.optList("queuedStructures").peek(structures -> {
            for(int i = 0; i < structures.size(); i++) {
                DataCompound structure = structures.getCompound();
                QueuedStructure s = new Region.QueuedStructure();
                s.structureName = structure.getString("structureName");
                s.sliceX = structure.getInt("sliceX");
                s.sliceY = structure.getInt("sliceY");
                s.tileX = structure.getInt("tileX");
                s.tileY = structure.getInt("tileY");
                s.offsetX = structure.getInt("offsetX");
                s.offsetY = structure.getInt("offsetY");
                r.addStructure(s);
            }
        });
	}
	
	@Override
	public void save(Region r, DataCompound c, boolean generated) {
		if(generated) {
			for(int y = 0; y < REGION_SIZE; y++) {            // Row (y)
                for(int x = 0; x < REGION_SIZE; x++) {        // Col (x)
                    DataCompound sliceTag = c.createCompound("slice" + x + "_" + y);
                    Slice s = r.slices[y][x];
                    sliceTag.put("tiles", Slice.to1DArray(s.tiles));
                    sliceTag.put("walls", Slice.to1DArray(s.walls));
                    sliceTag.put("light", Slice.to1DArray(s.light));
                    
                    if(s.tileEntities != null) {
                        DataList tileEntities = sliceTag.createList("tileEntities");
                        
                        TileEntity t;
                        for(int tileX = 0; tileX < Slice.SLICE_SIZE; tileX++) {
                            for(int tileY = 0; tileY < Slice.SLICE_SIZE; tileY++) {
                                if((t = s.tileEntities[tileY][tileX]) != null) {
                                    tileEntities.add(t.toNBT());
                                }
                            }
                        }
                    }
                }
            }
		}
		
		List<Action> queuedActions = r.queuedActions;
        if(queuedActions != null) {
            DataList actions = c.createList("queuedActions");
            queuedActions.forEach(a -> actions.add(a.toNBT()));
        }
        
        if(r.hasQueuedStructures()) {
            DataList structures = c.createList("queuedStructures");
            for(QueuedStructure s : r.getStructures()) {
                DataCompound structure = structures.createCompound();
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
