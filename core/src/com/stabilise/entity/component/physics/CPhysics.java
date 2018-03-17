package com.stabilise.entity.component.physics;

import com.stabilise.entity.component.Component;


/**
 * Abstract physics component class.
 * 
 * @see PhysicsImpl
 * @see NoPhysics
 */
public abstract class CPhysics implements Component {
    public abstract boolean onGround();
}
