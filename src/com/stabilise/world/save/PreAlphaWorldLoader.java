package com.stabilise.world.save;

import static com.stabilise.world.Region.REGION_SIZE;

import java.io.IOException;

import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTag;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.util.nbt.NBTTagList;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.Region.QueuedSchematic;
import com.stabilise.world.BaseWorld;
import com.stabilise.world.WorldData;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * Pre-alpha world loading!
 */
public class PreAlphaWorldLoader extends WorldLoader {
	
	/**
	 * Creates a new PreAlphaWorldLoader.
	 * 
	 * @param data The world's data object.
	 */
	public PreAlphaWorldLoader(WorldData data) {
		super(data);
	}
	
	@Override
	protected void load(Region r) {
		NBTTagCompound regionTag;
		try {
			regionTag = NBTIO.readCompressed(r.getFile());
		} catch(IOException e) {
			log.postSevere("Could not load the NBT data for region " + r.loc.x + "," + r.loc.y + "!", e);
			return;
		}
		
		r.generated = regionTag.getBoolean("generated");
		
		//System.out.println("Loaded " + r);
		//if(!r.generated)
		//	System.out.println(r + ": " + regionTag.toString());
		
		if(r.generated) {
			for(int y = 0; y < REGION_SIZE; y++) {			// Row (y)
				for(int x = 0; x < REGION_SIZE; x++) {		// Col (x)
					NBTTagCompound sliceTag = regionTag.getCompound("slice" + x + "_" + y);
					Slice s = new Slice(r.offsetX + x, r.offsetY + y, r);
					s.setTilesAsIntArray(sliceTag.getIntArray("tiles"));
					
					NBTTagList tileEntities = sliceTag.getList("tileEntities");
					s.numTileEntities = tileEntities.size();
					for(NBTTag t : tileEntities) {
						NBTTagCompound tc = (NBTTagCompound)t;
						TileEntity te = TileEntity.createTileEntityFromNBT(tc);
						s.tileEntities		// Poor syntax, but I want this to fit
							[BaseWorld.tileCoordRelativeToSliceFromTileCoord(te.y)]
							[BaseWorld.tileCoordRelativeToSliceFromTileCoord(te.x)] = te; 
					}
					
					r.slices[y][x] = s;
				}
			}
		}
		
		NBTTagList schematics = regionTag.getList("queuedSchematics");
		
		if(schematics.size() != 0) {
			for(int i = 0; i < schematics.size(); i++) {
				NBTTagCompound schematic = (NBTTagCompound)schematics.getTagAt(i);
				QueuedSchematic s = new Region.QueuedSchematic();
				s.schematicName = schematic.getString("schematicName");
				s.sliceX = schematic.getInt("sliceX");
				s.sliceY = schematic.getInt("sliceY");
				s.tileX = schematic.getInt("tileX");
				s.tileY = schematic.getInt("tileY");
				s.offsetX = schematic.getInt("offsetX");
				s.offsetY = schematic.getInt("offsetY");
				r.queuedSchematics.add(s);
			}
			
			r.hasQueuedSchematics = true;
			
			log.postDebug("Loaded " + schematics.size() + " schematics into " + r);
		}
	}
	
	@Override
	protected void save(Region r) {
		NBTTagCompound regionTag = new NBTTagCompound();
		
		regionTag.addBoolean("generated", r.generated);
		
		if(r.generated) {
			for(int y = 0; y < REGION_SIZE; y++) {			// Row (y)
				for(int x = 0; x < REGION_SIZE; x++) {		// Col (x)
					NBTTagCompound sliceTag = new NBTTagCompound();
					Slice s = r.slices[y][x];
					sliceTag.addIntArray("tiles", s.getTilesAsIntArray());
					regionTag.addCompound("slice" + x + "_" + y, sliceTag);
					
					if(s.numTileEntities > 0) {
						NBTTagList tileEntities = new NBTTagList();
						
						TileEntity t;
						for(int tileX = 0; tileX < Slice.SLICE_SIZE; tileX++) {
							for(int tileY = 0; tileY < Slice.SLICE_SIZE; tileY++) {
								if((t = s.tileEntities[tileY][tileX]) != null) {
									NBTTagCompound tileEntity = t.toNBT();
									tileEntity.addInt("id", t.getID());
									tileEntity.addInt("x", t.x);
									tileEntity.addInt("y", t.y);
									tileEntities.appendTag(tileEntity);
								}
							}
						}
						
						sliceTag.addList("tileEntities", tileEntities);
					}
				}
			}
		}
		
		if(r.queuedSchematics != null) {
			NBTTagList schematics = new NBTTagList();
			
			for(QueuedSchematic s : r.queuedSchematics) {
				NBTTagCompound schematic = new NBTTagCompound();
				schematic.addString("schematicName", s.schematicName);
				schematic.addInt("sliceX", s.sliceX);
				schematic.addInt("sliceY", s.sliceY);
				schematic.addInt("tileX", s.tileX);
				schematic.addInt("tileY", s.tileY);
				schematic.addInt("offsetX", s.offsetX);
				schematic.addInt("offsetY", s.offsetY);
				schematics.appendTag(schematic);
			}
			
			regionTag.addList("queuedSchematics", schematics);
			
			log.postDebug("Saved " + schematics.size() + " schematics in " + r);
		}
		
		try {
			NBTIO.safeWriteCompressed(r.getFile(), regionTag);
		} catch(IOException e) {
			log.postSevere("Could not save " + r + "!", e);
		}
	}

}
