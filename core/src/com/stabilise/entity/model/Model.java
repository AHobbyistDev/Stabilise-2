package com.stabilise.entity.model;

import com.stabilise.util.annotation.Incomplete;

@Incomplete
public class Model {
    
    /** The model's bones. A child bone must have a higher index than its
     * parent so positional updates may proliferate. */
    public final Bone[] bones;
    /** The model's controller. */
//    private AnimationController controller;
    
    
    
    public Model(int numBones) {
        this(new Bone[numBones]);
    }
    
    public Model(Bone[] bones) {
        this.bones = bones;
    }
    
    public void update() {
//        if(controller != null)
//            controller.update();
    }
    
}
