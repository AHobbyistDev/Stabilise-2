package com.stabilise.world.gen;

import com.stabilise.util.annotation.ThreadUnsafeMethod;
import com.stabilise.world.gen.InstancedWorldgen.InstancedWorldgenSupplier;


/**
 * This is a convenience class which delegates the registering of {@code
 * IWorldGenerators} to a {@code WorldGenerator}.
 */
public class GeneratorRegistrant {
    
    private final WorldGenerator gen;
    
    public GeneratorRegistrant(WorldGenerator gen) {
        this.gen = gen;
    }
    
    /**
     * Registers a generator. Generators are run in the order they are
     * registered.
     */
    @ThreadUnsafeMethod
    public void add(IWorldGenerator generator) {
        if(generator instanceof InstancedWorldgenSupplier)
            gen.log.postWarning("Registering a constructed instance of \"" +
                    generator.getClass().getSimpleName() + "\" even though"+
                    "it is a subclass of InstancedWorldgen. Mistake?");
        gen.addGenerator(generator);
    }
    
    /**
     * Registers a generator. Generators are run in the order they are
     * registered.
     */
    @ThreadUnsafeMethod
    public void add(InstancedWorldgenSupplier generator) {
        gen.addGenerator((r,w,s) -> generator.get(w, s).generate(r));
    }
    
}
