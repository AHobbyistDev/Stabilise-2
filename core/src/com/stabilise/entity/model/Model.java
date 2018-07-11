package com.stabilise.entity.model;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.collect.BiObjectIntMap;

@Incomplete
public class Model {
    
    // Rough planning:
    // 
    // Raw model with none of the state:
    // - Bones: default pos, shape, name, parent
    // 
    // Animation:
    // - Contains all keyframe and co info for a specific animation
    // 
    // Stateful model for a specific entity
    // - Current position of all bones
    // - Render order of the bones (e.g. what's on bottom, what's top)
    // - Animations being played
    //   - There can be multiple, and some only for specific bones
    //     e.g., can swing things around while running
    // 
    // Other considerations:
    // - Model must be able to accept customisation (e.g. can equip different
    //   weapons).
    // - The animation may be different and run at a different speed depending
    //   on what is equipped, e.g. animations for a sword and a bow will be
    //   completely different, and attacks with a light weapon will be faster
    //   than attacks with a heavy weapon.
    // - Need a way of keyframing certain events. For example, we will want to
    //   create particles, hitboxes, etc. at certain parts of the animation.
    // - The hitboxes and such will depend on the speed of the animation, so we
    //   will need a method of dynamic hitbox generation.
    // - Need some convenient form of model/animation export so that I can make
    //   the model and animations in flash and import them over easily.
    
    
    /** The model's bones. A child bone must have a higher index than its
     * parent so positional updates may proliferate. */
    //public Bone[] bones;
    
    /** Maps bone ids/index <-> bone names. */
    public BiObjectIntMap<String> boneNames;
    /** The value of the ith element is the ID of the parent of the ith bone,
     * or -1 if the ith bone does not have a parent. */
    public int[] parentIDs;
    
    
}
