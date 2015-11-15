package com.stabilise.entity.component.state;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.controller.PlayerController;
import com.stabilise.entity.effect.Effect;
import com.stabilise.entity.particle.ParticleDamageIndicator;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.util.Direction;
import com.stabilise.world.World;
import com.stabilise.world.AbstractWorld.ParticleSource;


public abstract class CBaseMob implements CState {
    
    /** The default number of ticks a mob becomes invulnerable for after being
     * hit. */
    protected static final int INVULNERABILITY_TICKS = 10;
    
    /** The default number of ticks a mob remains in the world after being
     * killed, before vanishing. */
    protected static final int DEATH_TICKS = 40;
    
    /** Possible state priorities. */
    public static enum StatePriority {
        // For want of better names/identifiers
        ORDINARY(0),        // An ordinary state from which a mob can do other stuff
        OCCUPIED(1),        // A mob is considered 'occupied' if in a state with
                            // this priority and must wait for it to finish before
                            // being able to do other stuff
        UNOVERRIDEABLE(2);    // This state is un-overridable by any other
        
        /** The StatePriority's underlying integer value, for comparison
         * purposes. */
        private final int value;
        
        /**
         * Sets a StatePriority.
         * 
         * @param value The priority's underlying integer value.
         */
        private StatePriority(int value) {
            this.value = value;
        }
        
        /**
         * Checks for whether or not something with the priority is capable of
         * overriding something with the given priority.
         * 
         * @param priority The priority.
         * @return {@code true} if something with the priority is capable of
         * overriding something with the given priority.
         */
        public boolean canOverride(StatePriority priority) {
            return value >= priority.value;
        }
    }
    
    /** States mobs may be in. */
    public static enum State {
        IDLE,
        RUN,
        SLIDE_FORWARD,
        SLIDE_BACK,
        CROUCH(true, false, true),
        JUMP_CROUCH(true, false, false, StatePriority.OCCUPIED),
        JUMP(false, true, true),
        FALL(false, true, true),
        LAND_CROUCH(true, false, false, StatePriority.OCCUPIED),
        BLOCK(true, true, false),
        DODGE_AIR(false, true, false, StatePriority.OCCUPIED),
        HITSTUN_GROUND(true, false, false, StatePriority.UNOVERRIDEABLE),
        HITSTUN_AIR(false, false, false, StatePriority.UNOVERRIDEABLE),
        SIDESTEP_BACK(true, false, false, StatePriority.OCCUPIED),
        SIDESTEP_FORWARD(true, false, false, StatePriority.OCCUPIED),
        DEAD(true, false, false, StatePriority.UNOVERRIDEABLE),
        ATTACK_UP_GROUND(true, false, false, StatePriority.OCCUPIED),
        ATTACK_DOWN_GROUND(true, false, false, StatePriority.OCCUPIED),
        ATTACK_SIDE_GROUND(true, false, false, StatePriority.OCCUPIED),
        SPECIAL_UP_GROUND(true, false, false, StatePriority.OCCUPIED),
        SPECIAL_DOWN_GROUND(true, false, false, StatePriority.OCCUPIED),
        SPECIAL_SIDE_GROUND(true, false, false, StatePriority.OCCUPIED),
        ATTACK_UP_AIR(false, true, false, StatePriority.OCCUPIED),
        ATTACK_DOWN_AIR(false, true, false, StatePriority.OCCUPIED),
        ATTACK_SIDE_AIR(false, true, false, StatePriority.OCCUPIED),
        SPECIAL_UP_AIR(false, true, false, StatePriority.OCCUPIED),
        SPECIAL_DOWN_AIR(false, true, false, StatePriority.OCCUPIED),
        SPECIAL_SIDE_AIR(false, true, false, StatePriority.OCCUPIED);
        
        /** Whether or not the state is a ground state. */
        public final boolean ground;
        /** Whether or not a mob can move while in the state. */
        public final boolean canMove;
        /** Whether or not a mob can perform an action while in the state. */
        public final boolean canAct;
        /** The priority required to change out of the state. */
        public final StatePriority priority;
        
        /**
         * Sets a State. The State has a default required duration of 0, a
         * default priority of 0, is a ground state, and a Mob can move and act
         * while in it.
         */
        private State() {
            this(true);
        }
        
        /**
         * Sets a State. The State has a default required duration of 0, a
         * default priority of 0, and a Mob can move and act while in it.
         * 
         * @param ground Whether or not the state is a ground state.
         */
        private State(boolean ground) {
            this(ground, true, true);
        }
        
        /**
         * Sets a State. The state has a default required duration of 0 and
         * a default priority of ordinary.
         * 
         * @param ground Whether or not the state is a ground state.
         * @param canMove Whether or not a Mob can move while in the state.
         * @param canAct Whether or not a Mob can perform an action while in
         * the state.
         */
        private State(boolean ground, boolean canMove, boolean canAct) {
            this(ground, canMove, canAct, StatePriority.ORDINARY);
        }
        
        /**
         * Sets a State.
         * 
         * @param ground Whether or not the state is a ground state.
         * @param canMove Whether or not a Mob can move while in the state.
         * @param canAct Whether or not a Mob can perform an action while in
         * the state.
         * @param priority The state's priority value.
         */
        private State(boolean ground, boolean canMove, boolean canAct, StatePriority priority) {
            this.ground = ground;
            this.canMove = canMove;
            this.canAct = canAct;
            this.priority = priority;
        }
    };
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    public Entity e;
    
    /** The mob's state. */
    public State state;
    /** The number of ticks the mob has remained in its current state. */
    public int stateTicks = 0;
    /** The number of ticks the mob is locked in the state, unless overridden
     * by a state of greater priority. */
    public int stateLockDuration = 0;
    
    /** The Mob's max health. */
    public int maxHealth;
    /** The Mob's health. */
    public int health;
    /** Whether or not the Mob is dead. */
    public boolean dead = false;
    
    public boolean invulnerable = false;
    /** The number of ticks until the Mob loses its invulnerability. */
    public int invulnerabilityTicks = 0;
    
    /** The effect currently ailing the Mob. */
    public Effect effect = null;
    
    /** Whether or not the Mob is currently attempting to move. */
    public boolean moving = false;
    
    /** Whether or not the Mob was on the ground at the end of the last tick. */
    protected boolean wasOnGround = false;
    
    // The mob's physical properties
    
    /** Initial jump velocity. */
    public float jumpVelocity;
    /** Vertical acceleration while swimming. TODO: Temporary */
    public float swimAcceleration;
    /** Acceleration along the x-axis. */
    public float acceleration;
    /** How much of its horizontal acceleration an entity maintains while
     * airborne. */
    public float airAcceleration;
    /** The entity's max speed. */
    public float maxDx;
    
    /** The pre-jump crouch duration, in ticks. */
    public int jumpCrouchDuration;
    
    // Visual things
    
    protected ParticleSource srcDmgIndicator;
    protected ParticleSource srcSmoke;
    
    /** Whether or not the mob has a tint. */
    public boolean hasTint = false;
    /** The strength of the mob's 'effect tint'. */
    public float tintStrength = 0.0f;
    
    @Override
    public void init(World w, Entity e) {
        this.e = e;
        srcDmgIndicator = w.getParticleManager().getSource(new ParticleDamageIndicator(0));
        srcSmoke = w.getParticleManager().getSource(new ParticleSmoke());
    }
    
    @Override
    public void update(World w, Entity e) {
        stateTicks++;
        
        if(state == State.DEAD && stateTicks == DEATH_TICKS) {
            spawnSmokeParticles(e);
            e.destroy();
            return;
        }
        
        if(state == State.JUMP_CROUCH && stateTicks == stateLockDuration) {
            setState(State.JUMP, false);        // No need, checked above
            e.dy = jumpVelocity;
        }
        
        if(invulnerable) {
            if(--invulnerabilityTicks == 0)
                invulnerable = false;
        }
        
        if(effect != null) {
            effect.update(w, e);
            if(effect.destroyed)
                effect = null;
        }
        
        if(hasTint) {
            if(dead)
                tintStrength *= 0.99f;        // TODO: declare as a constant
            else
                tintStrength -= 0.05f;        // TODO: declare as a constant
            if(tintStrength <= 0)
                hasTint = false;
        }
        
        //super.update(world);
        
        wasOnGround = e.physics.onGround();
        
        if(wasOnGround) {
            if(moving) {
                if((e.facingRight && e.dx > 0) || (!e.facingRight && e.dx < 0))
                    setState(State.RUN, true);
                else
                    setState(State.SLIDE_BACK, true);
            } else {
                if(e.dx == 0)
                    setState(State.IDLE, true);
                else
                    if((e.facingRight && e.dx > 0) || (!e.facingRight && e.dx < 0))
                        setState(State.SLIDE_FORWARD, true);
                    else
                        setState(State.SLIDE_BACK, true);
            }
        } else {
            if(e.dy > 0)
                setState(State.JUMP, true);
            else
                setState(State.FALL, true);
        }
        
        // Temporary rectification of dx to prevent a mob from remaining in a slide state
        // TODO: Better solution would be ideal
        if((e.dx > 0 && e.dx < 0.001f)
                || (e.dx < 0 && e.dx > -0.001f))
            e.dx = (0);
    }
    
    /*
    @Override
    protected float getXFriction() {
        if(moving)
            return AIR_FRICTION;
        else
            return super.getXFriction();
    }
    */
    
    protected void onVerticalCollision() {
        if(e.dy < 0 && !wasOnGround && state.priority != StatePriority.UNOVERRIDEABLE) {
            if(e.dy < -jumpVelocity) {
                //if(dy < -2*jumpVelocity)
                //    damage(-(int)(dy * dy * 2), -1, 0, 0);
                setState(State.LAND_CROUCH, false, 5);        // TODO: temporary constant duration
            } else {
                stateLockDuration = 0;
            }
        }
    }
    
    /**
     * Applies an effect to the Mob. The current effect will be overwritten.
     * 
     * @param effect The effect.
     */
    public void applyEffect(Effect effect) {
        this.effect = effect;
    }
    
    /**
     * Restores the Mob to full health.
     */
    public void restore() {
        health = maxHealth;
    }
    
    // ----------Begin things to be called by the Mob's controller----------
    
    /**
     * Makes the Mob attempt to move in a direction. Its manner of movement is
     * determined by its current state.
     * 
     * @param direction The direction in which to move the Mob.
     */
    public void move(Direction direction) {
        if(!state.canMove)
            return;
        
        if(direction.hasHorizontalComponent()) {
            float ddx = e.physics.onGround() ? acceleration : airAcceleration;
            
            if(direction.hasRight())
                e.dx = (e.dx + ddx);
            else
                e.dx = (e.dx - ddx);
            
            // TODO: modulate based on max dx better
            //ddx *= (maxDx - Math.abs(dx));
            if(e.dx > maxDx)
                e.dx = (maxDx);
            else if(e.dx < -maxDx)
                e.dx = (-maxDx);
            
            e.facingRight = direction.hasRight();
        }
        
        // TODO: no vertical movement implemented for now
        //if(direction.hasVerticalComponent()) {}
        
        moving = true;
    }
    
    /**
     * Commences a jump.
     * 
     * <p>
     * The Mob will not necessarily jump immediately; rather, it will enter
     * the {@link State#JUMP_CROUCH JUMP_CROUCH} state, after which it will
     * actually jump.
     * </p>
     */
    public void jump() {
        if(e.physics.onGround() && state.ground && state.canAct)
            setState(State.JUMP_CROUCH, true, jumpCrouchDuration);
    }
    
    /**
     * Attacks, if able, in a given direction.
     * 
     * @param direction The direction in which to make the attack.
     */
    public abstract void attack(Direction direction);
    
    /**
     * Performs a special attack, if able, in a given direction.
     * 
     * @param direction The direction in which to make the attack.
     */
    public abstract void specialAttack(Direction direction);
    
    // ----------End things to be called by the mob's controller----------
    
    /**
     * Damages the mob.
     * 
     * @param world
     * @param damage The damage dealt to the mob.
     * @param damagerID The ID of the entity to have caused the damage.
     * @param fx The x component of the force applied to the mob by the impact.
     * @param fy The y component of the force applied to the mob by the impact.
     */
    public void damage(World w, int damage, int damagerID, float fx, float fy) {
        if(invulnerable || dead)
            return;
        
        health -= damage;
        e.dx = (e.dx + fx);
        e.dy = (e.dy + fy);
        
        hasTint = true;
        //tintStrength = 1.0f;
        
        if(damage > 0)
            srcDmgIndicator.createAt(e.x, e.y);
        
        if(health <= 0) {
            health = 0;
            tintStrength = 0.8f;
            kill(w);
        } else {
            tintStrength = 1.0f;
            invulnerable = true;
            invulnerabilityTicks = INVULNERABILITY_TICKS;
        }
    }
    
    /**
     * Kills the mob by settings its state to {@link State#DEAD DEAD}.
     */
    public void kill(World w) {
        dead = true;
        setState(State.DEAD, false, DEATH_TICKS);
    }
    
    /**
     * Spawns smoke particles at the Mob's location.
     */
    private void spawnSmokeParticles(Entity e) {
        //srcSmoke.createBurst(30, 0.01f, 0.25f, 0f, (float)Math.PI, this);
        srcSmoke.createOutwardsBurst(30, true, true, 0.25f, 0.05f, e);
    }
    
    /**
     * Gets the Mob's current state.
     * 
     * @return The Mob's state.
     */
    public State getState() {
        return state;
    }
    
    /**
     * Sets the Mob's state, which doesn't have a lock.
     * 
     * @param state The new state.
     * @param validatePriority Whether or not the priority of the new state
     * should be checked before being set.
     */
    public void setState(State state, boolean validatePriority) {
        setState(state, validatePriority, 0);
    }
    
    /**
     * Sets the Mob's state.
     * 
     * @param state The new state.
     * @param validatePriority Whether or not the priority of the new state
     * should be checked before being set.
     * @param stateLockDuration The number of ticks for which the mob is
     * considered locked in the state.
     */
    public void setState(State state, boolean validatePriority, int stateLockDuration) {
        if(this.state == state || (validatePriority && stateTicks < this.stateLockDuration
                && !state.priority.canOverride(this.state.priority)))
            return;
        
        this.state = state;
        this.stateLockDuration = stateLockDuration;
        stateTicks = 0;
    }
    
    /**
     * Checks for whether or not the mob is under the control of a player.
     * 
     * @return {@code true} if the mob is being controlled by a player.
     */
    public boolean isPlayerControlled() {
        return e.controller instanceof PlayerController;
    }
    
}
