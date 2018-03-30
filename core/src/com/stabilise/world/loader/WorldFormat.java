package com.stabilise.world.loader;

import com.stabilise.util.Log;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.loader.impl.*;


public class WorldFormat {
    
    private WorldFormat() {} // non-instantiable
    
    
    /**
     * Sets the given WorldInfo's world format version to the latest version.
     * Invoking this is suitable when creating a new world.
     */
    public static void putLatest(WorldInfo info) {
        info.worldFormat.put("version", 1);
    }
    
    /**
     * Registers all the required IRegionLoaders on the given WorldLoader in
     * accordance with the given WorldInfo's format.
     */
    public static void registerLoaders(WorldLoader loader, WorldInfo info) {
        DataCompound format = info.worldFormat;
        
        // We'll modify this function here to actually do something with the
        // format if in the future I change the save format and want a smooth
        // compatible transition from an older to a newer version.
        
        if(format.getInt("version") != 1)
            Log.get().postInfo("World format version not 1? (not that it matters for now)");
        
        loader.addLoaderAndSaver(new BaseRegionLoader());
        loader.addLoaderAndSaver(new ActionLoader());
        loader.addLoaderAndSaver(new StructureLoader());
    }
    
}
