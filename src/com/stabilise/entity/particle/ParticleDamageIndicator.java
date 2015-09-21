package com.stabilise.entity.particle;

import com.badlogic.gdx.graphics.Color;
import com.stabilise.entity.EntityMob;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A particle which indicates damage dealt to a mob.
 */
public class ParticleDamageIndicator extends Particle {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The height of the damage indicator text, in pixels. */
    @SuppressWarnings("unused")
    private static final int INDICATOR_TEXT_SIZE = 12;
    /** The colour used for damage indicator text. */
    @SuppressWarnings("unused")
    private static final Color INDICATOR_COLOUR = Color.RED;
    /** The font style to use for damage indicator text. */
    //private static final FontStyle STYLE_INDICATOR = new FontStyle(INDICATOR_TEXT_SIZE, INDICATOR_COLOUR, FontStyle.Alignment.CENTRE, 1, 0);
    
    /** The duration for which a damage indicator should last, in ticks. */
    private static final int INDICATOR_DURATION = 80;
    /** The number of ticks after which the indicator should begin to fade out. */
    private static final int INDICATOR_FADE_OUT_MARK = 40;
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The indicator's display text. */
    public final String text;
    /** The indicator's display text's alpha. */
    @SuppressWarnings("unused")
    private float alpha = 1.0f;
    
    
    /**
     * Creates a new ParticleDamageIndicator.
     * 
     * @param value The indicator's display value.
     */
    public ParticleDamageIndicator(int value) {
        this(String.valueOf(value));
    }
    
    /**
     * Creates a new ParticleDamageIndicator.
     * 
     * @param text The indicator's display text.
     */
    public ParticleDamageIndicator(String text) {
        this.text = text;
    }
    
    /**
     * Creates a new ParticleDamageIndicator.
     * 
     * @param world The world in which the ParticleDamageIndicator will be
     * placed.
     * @param value The indicator's display value.
     * @param e The mob above which to place the damage indicator.
     */
    public ParticleDamageIndicator(World world, int value, EntityMob e) {
        this(String.valueOf(value), e);
    }
    
    /**
     * Creates a new ParticleDamageIndicator.
     * 
     * @param text The indicator's display text.
     * @param e The mob above which to place the damage indicator.
     */
    public ParticleDamageIndicator(String text, EntityMob e) {
        this(text);
        
        x = e.x;
        y = e.y + e.boundingBox.maxY();
    }
    
    @Override
    public void update(World world) {
        super.update(world);
        
        y += 0.05f;            // TODO: arbitrary lift rate
        
        if(age > INDICATOR_FADE_OUT_MARK) {
            if(age == INDICATOR_DURATION)
                destroy();
            else        // No checking for / by zero here; constant values are implicitly trusted to be different
                alpha = (float)(INDICATOR_DURATION - age) / (INDICATOR_DURATION - INDICATOR_FADE_OUT_MARK);
        }
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderDamageIndicator(this);
    }
    
    @Override
    public Particle duplicate() {
        return new ParticleDamageIndicator(0);
    }
    
    /**
     * Gets the font style to use for the indicator's display text.
     * 
     * @return The font style.
     */
    /*
    public FontStyle getFontStyle() {
        // A sneaky way of getting the text's alpha to change appropriately
        // Abusing the mutability of the Colour class to circumvent the
        // supposed immutability of the font style is probably poor design, but
        // meh
        INDICATOR_COLOUR.setAlpha(alpha);
        
        return STYLE_INDICATOR;
    }
    */
    
}
