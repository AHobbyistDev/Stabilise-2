package com.stabilise.entity.component.controller;

import com.stabilise.entity.component.Component;

/**
 * A controller is responsible for directing an entity.
 */
public abstract class CController implements Component {
    
    @Override
    public int getWeight() {
        throw new RuntimeException("A CController should not be put in the components list!");
    }
    
    @Override
    public Action resolve(Component other) {
        throw new RuntimeException("A CController should not be put in the components list!");
    }
    
}
