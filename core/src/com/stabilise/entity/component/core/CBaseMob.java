package com.stabilise.entity.component.core;

import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.damage.GeneralSource;
import com.stabilise.entity.damage.IDamageSource;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.ETileCollision;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.particle.ParticleIndicator;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.entity.particle.manager.ParticleEmitter;
import com.stabilise.item.Item;
import com.stabilise.util.Direction;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * Basic mob implementation.
 * 
 * <p>This class will need a pretty heavy rewrite sometime in the future.
 */
public abstract class CBaseMob extends CCore {
    
    /** The default number of ticks a mob becomes invulnerable for after being
     * hit. */
    protected static final int INVULNERABILITY_TICKS = 20;
    
    /** The default number of ticks a mob remains in the world after being
     * killed, before vanishing. */
    protected static final int DEATH_TICKS = 40;
    
    /** Possible state priorities. */
    public static enum StatePriority {
        /** An ordinary state, in which a mob can do other stuff. */
        ORDINARY(0),
        /** A mob is considered 'occupied' if in a state with this priority
         * (e.g. an attack animation) and must wait for it to finish before
         * being able to do other stuff. */
        OCCUPIED(1),
        /** A state with this priority cannot be overridden by the mob (e.g.
         * hitstun, or being dead). */
        UNOVERRIDEABLE(2);
        
        /** The StatePriority's underlying integer value, for comparison
         * purposes. */
        private final int value;
        
        
        private StatePriority(int value) {
            this.value = value;
        }
        
        /**
         * @return {@code true} if something with this priority is capable of
         * overriding something with the given priority.
         */
        public boolean canOverride(StatePriority priority) {
            return value >= priority.value;
        }
    }
    
    /** States mobs may be in. */
    public static enum State {
        //                 ground,canMove,canAct,        priority
        IDLE,
        RUN,
        SLIDE_FORWARD,
        SLIDE_BACK,
        CROUCH             (true,  false, true                               ),
        JUMP_CROUCH        (true,  false, false, StatePriority.OCCUPIED      ),
        JUMP               (false, true,  true                               ),
        FALL               (false, true,  true                               ),
        LAND_CROUCH        (true,  false, false, StatePriority.OCCUPIED      ),
        BLOCK              (true,  true,  false                              ),
        DODGE_AIR          (false, true,  false, StatePriority.OCCUPIED      ),
        HITSTUN_GROUND     (true,  false, false, StatePriority.UNOVERRIDEABLE),
        HITSTUN_AIR        (false, false, false, StatePriority.UNOVERRIDEABLE),
        SIDESTEP_BACK      (true,  false, false, StatePriority.OCCUPIED      ),
        SIDESTEP_FORWARD   (true,  false, false, StatePriority.OCCUPIED      ),
        DEAD               (true,  false, false, StatePriority.UNOVERRIDEABLE),
        ATTACK_UP_GROUND   (true,  false, false, StatePriority.OCCUPIED      ),
        ATTACK_DOWN_GROUND (true,  false, false, StatePriority.OCCUPIED      ),
        ATTACK_SIDE_GROUND (true,  false, false, StatePriority.OCCUPIED      ),
        SPECIAL_UP_GROUND  (true,  false, false, StatePriority.OCCUPIED      ),
        SPECIAL_DOWN_GROUND(true,  false, false, StatePriority.OCCUPIED      ),
        SPECIAL_SIDE_GROUND(true,  false, false, StatePriority.OCCUPIED      ),
        ATTACK_UP_AIR      (false, true,  false, StatePriority.OCCUPIED      ),
        ATTACK_DOWN_AIR    (false, true,  false, StatePriority.OCCUPIED      ),
        ATTACK_SIDE_AIR    (false, true,  false, StatePriority.OCCUPIED      ),
        SPECIAL_UP_AIR     (false, true,  false, StatePriority.OCCUPIED      ),
        SPECIAL_DOWN_AIR   (false, true,  false, StatePriority.OCCUPIED      ),
        SPECIAL_SIDE_AIR   (false, true,  false, StatePriority.OCCUPIED      );
        
        /** Whether or not the state is a ground state. */
        public final boolean ground;
        /** Whether or not a mob can move while in the state. */
        public final boolean canMove;
        /** Whether or not a mob can perform an action while in the state. */
        public final boolean canAct;
        /** The priority required to change out of the state. */
        public final StatePriority priority;
        
        
        private State() {
            this(true);
        }
        
        private State(boolean ground) {
            this(ground, true, true);
        }
        
        private State(boolean ground, boolean canMove, boolean canAct) {
            this(ground, canMove, canAct, StatePriority.ORDINARY);
        }
        
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
    
    /** Convenience reference to the entity */
    public Entity e;
    
    public boolean facingRight;
    
    /** The mob's state. */
    public State state = State.IDLE;
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
    
    /** Whether or not the Mob is currently attempting to move. */
    public boolean moving = false;
    
    /** Whether or not the Mob was on the ground at the end of the last tick. */
    protected boolean wasOnGround = false;
    
    // The mob's physical properties
    
    /** Initial jump velocity. */
    public float jumpVelocity;
    /** Acceleration along the x-axis. */
    public float acceleration;
    /** How much of its horizontal acceleration an entity maintains while
     * airborne. */
    public float airAcceleration;
    /** The entity's max speed. */
    public float maxDx;
    
    /** The pre-jump crouch duration, in ticks. */
    public int jumpCrouchDuration;
    
    /** Max number of jumps we can do. Set to 2 for double-jump, etc. */
    public int maxJumpCount = 1;
    /** Our jump count. */
    public int jumpCount = 0;
    
    // Visual things
    
    protected ParticleEmitter<ParticleIndicator> srcDmgIndicator;
    protected ParticleEmitter<ParticleSmoke> srcSmoke;
    
    /** Whether or not the mob has a tint. */
    public boolean hasTint = false;
    /** The strength of the mob's 'effect tint'. */
    public float tintStrength = 0.0f;
    
    
    @Override
    public void init(Entity e) {
        this.e = e;
    }
    
    @Override
    public void update(World w, Entity e) {
        stateTicks++;
        
        if(state == State.DEAD && stateTicks == DEATH_TICKS) {
            srcSmoke.createCentredOutwardsBurst(w.rnd(), 30, 1f, 7f, e);
            e.destroy();
            return;
        }
        
        if(state == State.JUMP_CROUCH && stateTicks == stateLockDuration)
            doJump();
        
        if(invulnerable && --invulnerabilityTicks == 0)
            invulnerable = false;
        
        if(hasTint) {
            if(dead)
                tintStrength *= 0.99f;
            else
                tintStrength -= 0.05f;
            if(tintStrength <= 0)
                hasTint = false;
        }
        
        wasOnGround = e.physics.onGround();
        
        if(Math.abs(e.dx) < 0.001)
            e.dx = 0f;
        
        if(wasOnGround) {
            if(moving) {
                if((facingRight && e.dx > 0) || (!facingRight && e.dx < 0))
                    setState(State.RUN, true);
                else
                    setState(State.SLIDE_BACK, true);
            } else {
                if(e.dx == 0f)
                    setState(State.IDLE, true);
                else
                    if((facingRight && e.dx > 0) || (!facingRight && e.dx < 0))
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
        
        moving = false;
    }
    
    protected void onVerticalCollision(World w, Entity e, ETileCollision ev) {
        if(ev.dv < 0 && !wasOnGround) {
            jumpCount = 0;
            if(state.priority != StatePriority.UNOVERRIDEABLE) {
                if(e.dy < -2*jumpVelocity) {
                    if(e.dy < -2.1*jumpVelocity)
                        damage(w, e, GeneralSource.fallDamage((int)((-e.dy-2*jumpVelocity) * 2)));
                    setState(State.LAND_CROUCH, true, 12);
                } else
                    onLand();
            }
        }
    }
    
    protected void onLand() {
        stateLockDuration = 0;
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
                e.dx += ddx;
            else
                e.dx -= ddx;
            
            // in the future: modulate better than just setting a hard cap
            if(e.dx > maxDx)
                e.dx = maxDx;
            else if(e.dx < -maxDx)
                e.dx = -maxDx;
            
            facingRight = direction.hasRight();
        }
        
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
        else if(state.canAct)
            doJump(); // straight to double-jump
    }
    
    protected void doJump() {
        if(jumpCount >= maxJumpCount)
            return;
        setState(State.JUMP, false); // no need to validate -- checked elsewhere
        jumpCount++;
        doNthJump(jumpCount);
    }
    
    protected void doNthJump(int n) {
        e.dy = jumpVelocity;
    }
    
    /**
     * Attacks, if able, in a given direction.
     */
    public abstract void attack(World w, Direction direction);
    
    /**
     * Performs a special attack, if able, in a given direction.
     */
    public abstract void specialAttack(World w, Direction direction);
    
    // ----------End things to be called by the mob's controller----------
    
    @Override
    public boolean damage(World w, Entity e, IDamageSource src) {
        if((invulnerable && !src.type().bypassesInvulFrames) || dead)
            return false;
        
        src.applyEffects(e);
        
        health -= src.damage();
        e.dx = (e.dx + src.impulseX());
        e.dy = (e.dy + src.impulseY());
        
        ParticleIndicator p = srcDmgIndicator.createAlwaysAt(
                srcDmgIndicator.dummyPos.set(e.pos, 0f, e.aabb.maxY()));
        p.text = String.valueOf(src.damage());
        p.orange = src.damage() == 0;
        
        hasTint = true;
        if(health <= 0) {
            health = 0;
            tintStrength = 0.6f;
            kill(w, e, src);
        } else {
            tintStrength = 0.75f;
            if(src.invincibilityFrames()) {
                invulnerable = true;
                invulnerabilityTicks = INVULNERABILITY_TICKS;
            }
        }
        
        return true;
    }
    
    /**
     * Kills the mob by setting its state to {@link State#DEAD DEAD}.
     */
    @Override
    public void kill(World w, Entity e, IDamageSource src) {
        if(e.post(w, EDamaged.killed(src))) {
            dead = true;
            setState(State.DEAD, false, DEATH_TICKS);
        } else
            health = 1; // some component doesn't want us dead!
    }
    
    protected void dropItem(World w, Entity e, Item item, int quantity, float chance) {
        if(w.chance(chance)) {
            Entity ei = Entities.item(w, item.stackOf(quantity));
            ei.pos.set(e.pos);
            w.addEntity(ei);
        }
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
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.TILE_COLLISION_V)
            onVerticalCollision(w, e, (ETileCollision)ev);
        else if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD) {
            srcDmgIndicator = w.particleEmitter(ParticleIndicator.class);
            srcSmoke = w.particleEmitter(ParticleSmoke.class);
        } else if(ev.type() == EntityEvent.Type.DAMAGED) {
            if(damage(w, e, ((EDamaged)ev).src))
                ev.handled = true; // don't cancel propagation to other components
        }
        return false;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        facingRight = c.getBool("facingRight");
        
        state = State.values()[c.getI32("state")];
        stateTicks = c.getI32("stateTicks");
        stateLockDuration = c.getI32("stateLockDuration");
        
        maxHealth = c.getI32("maxHealth");
        health = c.getI32("health");
        dead = c.getBool("dead");
        invulnerable = c.getBool("invul");
        invulnerabilityTicks = c.getI32("invulTicks");
        
        moving = c.getBool("moving");
        jumpVelocity = c.getF32("jumpVelocity");
        acceleration = c.getF32("acceleration");
        airAcceleration = c.getF32("airAcceleration");
        maxDx = c.getF32("maxDx");
        
        jumpCrouchDuration = c.getI32("jumpCrouchDuration");
        maxJumpCount = c.getI32("maxJumpCount");
        jumpCount = c.getI32("jumpCount");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        c.put("facingRight", facingRight);
        
        c.put("state", state.ordinal());
        c.put("stateTicks", stateTicks);
        c.put("stateLockDuration", stateLockDuration);
        
        c.put("maxHealth", maxHealth);
        c.put("health", health);
        c.put("dead", dead);
        c.put("invul", invulnerable);
        c.put("invulTicks", invulnerabilityTicks);
        
        c.put("moving", moving);
        c.put("jumpVelocity", jumpVelocity);
        c.put("acceleration", acceleration);
        c.put("airAcceleration", airAcceleration);
        c.put("maxDx", maxDx);
        
        c.put("jumpCrouchDuration", jumpCrouchDuration);
        c.put("maxJumpCount", maxJumpCount);
        c.put("jumpCount", jumpCount);
    }
    
}
