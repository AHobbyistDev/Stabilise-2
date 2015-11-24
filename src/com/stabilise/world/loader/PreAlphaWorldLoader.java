package com.stabilise.world.loader;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.World.*;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTag;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.util.nbt.NBTTagList;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
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
        NBTTagCompound regionTag;
        try {
            regionTag = NBTIO.readCompressed(file);
        } catch(IOException e) {
            log.postSevere("Could not load the NBT data for region " + r.x()
                    + "," + r.y() + "!", e);
            return;
        }
        
        boolean generated = regionTag.getBoolean("generated");
        
        //System.out.println("Loaded NBT of " + r);
        //if(!r.generated)
        //    System.out.println(r + ": " + regionTag.toString());
        
        if(generated) {
            for(int y = 0; y < REGION_SIZE; y++) {            // Row (y)
                for(int x = 0; x < REGION_SIZE; x++) {        // Col (x)
                    NBTTagCompound sliceTag = regionTag.getCompound("slice" + x + "_" + y);
                    Slice s = new Slice(r.offsetX + x, r.offsetY + y,
                            sliceTag.getIntArray("tiles"),
                            sliceTag.getIntArray("walls"),
                            sliceTag.getByteArray("light"));
                    
                    NBTTagList tileEntities = sliceTag.getList("tileEntities");
                    if(tileEntities.size() > 0)
                        s.initTileEntities();
                    for(NBTTag t : tileEntities) {
                        NBTTagCompound tc = (NBTTagCompound)t;
                        TileEntity te = TileEntity.createTileEntityFromNBT(tc);
                        s.tileEntities        // I just love really long method names!
                            [tileCoordRelativeToSliceFromTileCoord(te.y)]
                            [tileCoordRelativeToSliceFromTileCoord(te.x)] = te; 
                    }
                    
                    r.slices[y][x] = s;
                }
            }
        }
        
        NBTTagList structures = regionTag.getList("queuedStructures");
        
        if(structures.size() != 0) {
            for(int i = 0; i < structures.size(); i++) {
                NBTTagCompound structure = (NBTTagCompound)structures.getTagAt(i);
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
            
            //log.postDebug("Loaded " + schematics.size() + " schematics into " + r);
        }
        
        if(generated)
            r.setGenerated();
    }
    
    @Override
    protected void save(Region r, FileHandle file) {
        NBTTagCompound regionTag = new NBTTagCompound();
        
        regionTag.addBoolean("generated", r.isGenerated());
        
        if(r.isGenerated()) {
            for(int y = 0; y < REGION_SIZE; y++) {            // Row (y)
                for(int x = 0; x < REGION_SIZE; x++) {        // Col (x)
                    NBTTagCompound sliceTag = new NBTTagCompound();
                    Slice s = r.slices[y][x];
                    sliceTag.addIntArray("tiles", Slice.to1DArray(s.tiles));
                    sliceTag.addIntArray("walls", Slice.to1DArray(s.walls));
                    sliceTag.addByteArray("light", Slice.to1DArray(s.light));
                    regionTag.addCompound("slice" + x + "_" + y, sliceTag);
                    
                    if(s.tileEntities != null) {
                        NBTTagList tileEntities = new NBTTagList();
                        
                        TileEntity t;
                        for(int tileX = 0; tileX < Slice.SLICE_SIZE; tileX++) {
                            for(int tileY = 0; tileY < Slice.SLICE_SIZE; tileY++) {
                                if((t = s.tileEntities[tileY][tileX]) != null) {
                                    tileEntities.appendTag(t.toNBT());
                                }
                            }
                        }
                        
                        sliceTag.addList("tileEntities", tileEntities);
                    }
                }
            }
        }
        
        if(r.hasQueuedStructures()) {
            NBTTagList structures = new NBTTagList();
            for(QueuedStructure s : r.getStructures()) {
                NBTTagCompound structure = new NBTTagCompound();
                structure.addString("schematicName", s.structureName);
                structure.addInt("sliceX", s.sliceX);
                structure.addInt("sliceY", s.sliceY);
                structure.addInt("tileX", s.tileX);
                structure.addInt("tileY", s.tileY);
                structure.addInt("offsetX", s.offsetX);
                structure.addInt("offsetY", s.offsetY);
                structures.appendTag(structure);
            }
            
            regionTag.addList("queuedStructures", structures);
            
            //log.postDebug("Saved " + schematics.size() + " schematics in " + r);
        }
        
        try {
            NBTIO.safeWriteCompressed(file, regionTag);
        } catch(IOException e) {
            log.postSevere("Could not save " + r + "!", e);
        }
    }

}
