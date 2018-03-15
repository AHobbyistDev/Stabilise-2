package com.stabilise.world.loader;

import static com.stabilise.world.Region.REGION_SIZE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.nbt.NBTCompound;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.gen.action.Action;
import com.stabilise.world.Region.QueuedStructure;
import com.stabilise.world.multiverse.Multiverse;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * Pre-alpha world loading!
 * 
 * <p>TODO: Too many things which are crucial to the game mechanisms are
 * currently implemented as a part of PreAlphaWorldLoader. We need to find a
 * way to abstract the crucial parts away and leave only the nuances of world
 * loading up to particular implementations!
 */
public class PreAlphaWorldLoader extends WorldLoader {
    
    /**
     * Creates a new PreAlphaWorldLoader.
     * 
     * @param provider The world provider.
     */
    public PreAlphaWorldLoader(Multiverse<?> provider) {
        super(provider);
    }
    
    @Override
    protected void load(Region r, FileHandle file) {
        DataCompound regionTag;
        try {
            regionTag = IOUtil.read(Format.NBT, Compression.GZIP, file);
        } catch(IOException e) {
            log.postSevere("Could not load the NBT data for region " + r.x()
                    + "," + r.y() + "!", e);
            return;
        }
        
        boolean generated = regionTag.getBool("generated");
        
        //System.out.println("Loaded NBT of " + r);
        //if(!r.generated)
        //    System.out.println(r + ": " + regionTag.toString());
        
        if(generated) {
            for(int y = 0; y < REGION_SIZE; y++) {            // Row (y)
                for(int x = 0; x < REGION_SIZE; x++) {        // Col (x)
                    DataCompound sliceTag = regionTag.getCompound("slice" + x + "_" + y);
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
        
        regionTag.optList("queuedActions").peek(actions -> {
            r.queuedActions = new ArrayList<>(actions.size());
            for(int i = 0; i < actions.size(); i++) {
                r.queuedActions.add(Action.read(actions.getCompound()));
            }
        });
        
        regionTag.optList("queuedStructures").peek(structures -> {
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
        
        if(generated)
            r.setGenerated();
    }
    
    @Override
    protected void save(Region r, FileHandle file) {
        DataCompound regionTag = new NBTCompound();
        
        regionTag.put("generated", r.isGenerated());
        
        if(r.isGenerated()) {
            for(int y = 0; y < REGION_SIZE; y++) {            // Row (y)
                for(int x = 0; x < REGION_SIZE; x++) {        // Col (x)
                    DataCompound sliceTag = regionTag.createCompound("slice" + x + "_" + y);
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
            DataList actions = regionTag.createList("queuedActions");
            queuedActions.forEach(a -> actions.add(a.toNBT()));
        }
        
        if(r.hasQueuedStructures()) {
            DataList structures = regionTag.createList("queuedStructures");
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
        
        try {
            IOUtil.writeSafe(regionTag, Format.NBT, Compression.GZIP, file);
        } catch(IOException e) {
            log.postSevere("Could not save " + r + "!", e);
        }
    }

}
