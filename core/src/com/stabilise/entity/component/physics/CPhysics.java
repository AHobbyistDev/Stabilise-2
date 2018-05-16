package com.stabilise.entity.component.physics;

import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.entity.component.Component;


/**
 * Abstract physics component class.
 * 
 * @see CPhysicsImplOld
 * @see CNoPhysics
 */
public abstract class CPhysics extends AbstractComponent {
    
    
    public abstract boolean onGround();
    
    
    @Override
    public int getWeight() {
        throw new RuntimeException("A CPhysics should not be put in the components list!");
    }
    
    @Override
    public Action resolve(Component other) {
        throw new RuntimeException("A CPhysics should not be put in the components list!");
    }
    
}
