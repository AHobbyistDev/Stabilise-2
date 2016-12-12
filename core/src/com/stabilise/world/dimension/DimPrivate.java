package com.stabilise.world.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multiverse.Multiverse;

/**
 * The private player-local client-only dimension.
 */
public class DimPrivate extends Dimension {
    
    public DimPrivate(Info info) {
        super(info);
    }
    
    @Override
    public WorldGenerator generatorFor(Multiverse<?> multiverse, 
            HostWorld world) {
        return null;
    }
    
}
