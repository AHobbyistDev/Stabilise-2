package com.stabilise.entity.particle;

import com.badlogic.gdx.graphics.Color;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A particle which indicates damage dealt to a mob.
 */
public class ParticleIndicator extends Particle {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The height of the damage indicator text, in pixels. */
    public static final int TEXT_SIZE = 12;
    /** The colour used for damage indicator text. */
    public static final Color COLOUR = Color.RED;
    
    /** The duration for which a damage indicator should last, in ticks. */
    private static final int DURATION = 80;
    /** The number of ticks after which the indicator should begin to fade out. */
    private static final int FADE_OUT_MARK = 40;
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The indicator's display text. */
    public String text;
    /** The indicator's display text's alpha. */
    @SuppressWarnings("unused")
    private float alpha = 1.0f;
    
    
    @Override
    public void update(World world) {
        super.update(world);
        
        y += 0.05f;    // arbitrary lift rate
        
        if(age > FADE_OUT_MARK) {
            if(age == DURATION)
                destroy();
            else
                alpha = (float)(DURATION - age) / (DURATION - FADE_OUT_MARK);
        }
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderIndicator(this);
    }
    
}
