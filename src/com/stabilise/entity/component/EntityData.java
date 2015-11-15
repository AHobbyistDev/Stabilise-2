package com.stabilise.entity.component;

import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.component.state.CState;


public class EntityData {
    
    public CPhysics    physics;
    public CController controller;
    public CState      state;
    
}
