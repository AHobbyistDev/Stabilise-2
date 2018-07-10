package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EThroughPortalIntra;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Checks;
import com.stabilise.util.Log;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;


/**
 * A component that tracks an entity and the world it is in. This component is
 * mainly here for convenience of external-to-the-world code, such as the world
 * renderer and such.
 */
public class CEntityTracker extends AbstractComponent {
    

    /** A persistent reference to the entity. Updated when the entity changes
     * dimensions and is internally swapped out with a phantom. */
    public Entity entity = null;
    /** A reference to the world that the entity is in. Updated when the entity
     * changes dimensions (via a portal). */
    public World world = null;
    
    
    
    @Override
    public void init(Entity e) {
        entity = e; // initial set
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        // No convenient way to do an initial set of world, so this'll have to
        // do.
        if(world == null)
            world = w;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev instanceof EThroughPortalIntra)
            handleThroughPortal(w, e, (EThroughPortalIntra)ev);
        return false;
    }
    
    protected void handleThroughPortal(World w, Entity e, EThroughPortalIntra ev) {
        world = w;
        entity = e;
    }
    
    @Override
    public int getWeight() {
        return Component.WEIGHT_TRACKER;
    }
    
    @Override
    public Action resolve(Component c) {
        Log.get().postWarning("[CEntityTracker] Multiple trackers on the same entity?");
        return Action.KEEP_BOTH;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        Checks.TODO();
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        Checks.TODO();
    }
    
}
