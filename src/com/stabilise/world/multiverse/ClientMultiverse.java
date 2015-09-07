package com.stabilise.world.multiverse;

import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.world.AbstractWorld;
import com.stabilise.world.dimension.Dimension;

@Incomplete
public class ClientMultiverse extends Multiverse<AbstractWorld> {
    
    public ClientMultiverse(Profiler profiler) {
        super(profiler);
    }
    
    @Override
    public AbstractWorld loadDimension(String name) {
        AbstractWorld world = dimensions.get(name);
        if(world != null)
            return world;
        
        Dimension dim = Dimension.getDimension(name);
        if(dim == null)
            throw new IllegalArgumentException("Invalid dimension \"" + name + "\"");
        
        world = dim.createClient(this);
        //world.prepare(); // TODO
        
        dimensions.put(name, world);
        
        return world;
    }
    
    /**
     * Returns a placeholder value of 0 since client's don't know the seed.
     */
    @Override
    public long getSeed() {
        return 0L;
    }
    
    @Override
    public void save() {
        // nothing to save for a client world
    }
    
}
