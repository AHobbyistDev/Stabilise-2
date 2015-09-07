package com.stabilise.entity.model;

import com.stabilise.util.maths.Interpolation;

/**
 * Controls model animations.
 */
public class AnimationController {
    
    private final Model model;
    private final Bone.Position[] from;
    private final Bone.Position[] to;
    private final Interpolation func;
    private final float maxTicks;
    private int ticks;
    
    public AnimationController(Model model, Bone.Position[] from, Bone.Position[] to, Interpolation func, int ticks) {
        if(model.bones.length != from.length || from.length != to.length)
            throw new IllegalArgumentException("invalid length");
        
        this.model = model;
        this.from = from;
        this.to = to;
        this.func = func;
        maxTicks = (float)ticks;
        this.ticks = 0;
    }
    
    public void update() {
        if(++ticks == maxTicks)
            complete();
        else
            interpolate();
    }
    
    private void interpolate() {
        float x = func.transform(ticks/maxTicks);
        for(int i = 0; i < model.bones.length; i++) {
            Bone b = model.bones[i];
            b.setRotation(Interpolation.lerp(from[i].rotation, to[i].rotation, x));
            b.flush();
        }
    }
    
    private void complete() {
        for(int i = 0; i < model.bones.length; i++) {
            Bone b = model.bones[i];
            b.setRotation(to[i].rotation);
            b.flush();
        }
    }
    
}
