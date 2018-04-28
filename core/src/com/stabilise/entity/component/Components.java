package com.stabilise.entity.component;

import java.util.function.Supplier;

import com.stabilise.entity.component.buffs.*;
import com.stabilise.entity.component.controller.*;
import com.stabilise.entity.component.core.*;
import com.stabilise.entity.component.effect.*;
import com.stabilise.entity.component.physics.*;
import com.stabilise.util.Checks;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;


public class Components {
    
    private Components() {} // non-instantiable
    
    
    public static final TypeFactory<Component> COMPONENT_TYPES =
            new TypeFactory<>(new RegistryParams("EntityComponentsRegistry", 64));
    
    /**
     * Registers all component types.
     */
    public static void registerComponentTypes() {
        // ID ranges are arbitrary, and also the repetition when registering is
        // annoying. Is there anything that can be done about that?
        
        // Internal functionality components (ids: 0-10)
        register(0, CSliceAnchorer.class, CSliceAnchorer::new);
        register(1, CNearbyPortal.class, CNearbyPortal::new);
        register(2, CPhantom.class, CPhantom::new);
        
        // Controller components (ids: 11-15)
        register(11, CIdleController.class, () -> CIdleController.INSTANCE);
        register(12, CPlayerController.class, () -> { throw Checks.unsupported("unsupported for now"); });
        register(13, CEnemyController.class, CEnemyController::new);
        
        // Physics components (ids: 16-20)
        register(16, CNoPhysics.class, () -> CNoPhysics.INSTANCE);
        register(17, CPhysicsImpl.class, CPhysicsImpl::new);
        
        // Core components (ids: 21-50)
        register(21, CPortal.class, CPortal::new);
        register(22, CPerson.class, CPerson::new);
        register(23, CItem.class, CItem::new);
        register(24, CGenericEnemy.class, CGenericEnemy::new);
        register(25, CFireball.class, CFireball::new);
        
        // Misc components (ids: 51+)
        register(51, CInvulnerability.class, CInvulnerability::new);
        register(52, CBasicArmour.class, CBasicArmour::new);
        register(53, CEffectFire.class, () -> { throw Checks.unsupported("unsupported for now"); });
        
        
        
        
        COMPONENT_TYPES.lock();
    }
    
    private static void register(int id, Class<? extends Component> clazz,
            Supplier<Component> constructor) {
        COMPONENT_TYPES.register(id, clazz, constructor);
    }
    
}
