package com.stabilise.entity.particle;

import com.badlogic.gdx.graphics.Color;
import com.stabilise.render.WorldRenderer;
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
    
    public String text;
    public float alpha;
    public boolean orange;
    
    private float dy;
    
    
    @Override
    protected void update(World world) {
        super.update(world);
        
        pos.addY(dy); // no real need to align
        dy *= 0.95f;
        
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
    
    @Override
    public void reset() {
        super.reset();
        text = "";
        alpha = 1.0f;
        orange = false;
        dy = 0.1f;
    }
    
}
