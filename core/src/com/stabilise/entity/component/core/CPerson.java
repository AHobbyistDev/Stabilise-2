package com.stabilise.entity.component.core;

import java.util.Set;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.stabilise.core.Constants;
import com.stabilise.entity.Entities;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.effect.CEffectFire;
import com.stabilise.entity.component.effect.CEffectFireTrail;
import com.stabilise.entity.damage.DamageType;
import com.stabilise.entity.damage.IDamageSource;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.hitbox.Hitbox;
import com.stabilise.entity.particle.ParticleExplosion;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleHeal;
import com.stabilise.entity.particle.manager.ParticleEmitter;
import com.stabilise.item.Items;
import com.stabilise.item.armour.Armour;
import com.stabilise.item.weapon.Weapon;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.util.shape.Shape;
import com.stabilise.world.World;


public class CPerson extends CBaseMob {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    // ----------Attacks--------
    
    // Side attack (ground)
    public static final int ATTACK_SIDE_GROUND_DURATION = 40;
    public static final int ATTACK_SIDE_GROUND_FRAME_2_BEGIN = 16;
    public static final int ATTACK_SIDE_GROUND_FRAME_3_BEGIN = 20;
    
    public static final Shape ATTACK_SIDE_GROUND_HITBOX_1 =
           Polygon.rectangle(0.53f, 1.11f, 1.82f, 0.18f).rotate(0.099483f);
    public static final Shape ATTACK_SIDE_GROUND_HITBOX_1_FLIPPED =
            ATTACK_SIDE_GROUND_HITBOX_1.reflect();
    public static final Shape ATTACK_SIDE_GROUND_HITBOX_2 =
            Polygon.rectangle(-1.87f, 1.16f, 2.25f, 0.18f).rotate(0.15708f);
    public static final Shape ATTACK_SIDE_GROUND_HITBOX_2_FLIPPED =
            ATTACK_SIDE_GROUND_HITBOX_2.reflect();
    public static final float ATTACK_SIDE_GROUND_FORCE = 3.2f;
    
    // Up attack (ground)
    public static final int ATTACK_UP_GROUND_DURATION = 40;
    public static final int ATTACK_UP_GROUND_FRAME_2_BEGIN = 16;
    public static final AABB ATTACK_UP_GROUND_HITBOX = new AABB(-0.04f, 1.89f, 0.16f, 1.63f);
    public static final AABB ATTACK_UP_GROUND_HITBOX_FLIPPED =
            ATTACK_UP_GROUND_HITBOX.reflect();
    public static final float ATTACK_UP_GROUND_FORCE = 1.1f;
    public static final int ATTACK_UP_GROUND_HITBOX_DURATION = 14;
    
    // Down attack (ground)
    public static final int ATTACK_DOWN_GROUND_DURATION = 20;
    public static final int ATTACK_DOWN_GROUND_FRAME_2_BEGIN = 1;
    public static final Shape ATTACK_DOWN_GROUND_HITBOX =
            Polygon.rectangle(0.49f, 0.34f, 1.43f, 0.16f).rotate(-0.116937f);
    public static final Shape ATTACK_DOWN_GROUND_HITBOX_FLIPPED =
            ATTACK_DOWN_GROUND_HITBOX.reflect();
    public static final float ATTACK_DOWN_GROUND_FORCE = 1.2f;
    
    // Side attack (air)
    public static final int ATTACK_SIDE_AIR_DURATION = 30;
    public static final int ATTACK_SIDE_AIR_FRAME_2_BEGIN = 6;
    public static final Polygon ATTACK_SIDE_AIR_HITBOX_1 =
            new Polygon(2.72f,1.29f, 0.96f,0.98f, -0.45f,2.56f, 1.29f,2.35f);
    public static final Shape ATTACK_SIDE_AIR_HITBOX_1_FLIPPED =
            ATTACK_SIDE_AIR_HITBOX_1.reflect();
    public static final int ATTACK_SIDE_AIR_FRAME_3_BEGIN = 8;
    public static final Polygon ATTACK_SIDE_AIR_HITBOX_2 =
            new Polygon(2.72f,1.29f, 1.44f,-0.41f, 0.45f,0.45f, 0.96f,0.98f);
    public static final Shape ATTACK_SIDE_AIR_HITBOX_2_FLIPPED =
            ATTACK_SIDE_AIR_HITBOX_2.reflect();
    
    // Up attack (air)
    public static final int ATTACK_UP_AIR_DURATION = 30;
    public static final int ATTACK_UP_AIR_FRAME_2_BEGIN = 6;
    public static final Polygon ATTACK_UP_AIR_HITBOX_1_1 =
            new Polygon(2.72f,1.51f, 1.22f,-0.45f, 0.8f,0.75f, 0.89f,1.38f);
    public static final Shape ATTACK_UP_AIR_HITBOX_1_1_FLIPPED =
            ATTACK_UP_AIR_HITBOX_1_1.reflect();
    public static final Polygon ATTACK_UP_AIR_HITBOX_1_2 =
            new Polygon(2.69f,1.95f, 0.84f,1.49f, 0.27f,2.05f, 0.58f,3.33f);
    public static final Shape ATTACK_UP_AIR_HITBOX_1_2_FLIPPED =
            ATTACK_UP_AIR_HITBOX_1_2.reflect();
    public static final int ATTACK_UP_AIR_FRAME_3_BEGIN = 8;
    public static final Polygon ATTACK_UP_AIR_HITBOX_2 =
            new Polygon(-0.8f,1.4f, -1.65f,2.4f, 0.6f,3.35f, 0.29f,2.04f);
    public static final Shape ATTACK_UP_AIR_HITBOX_2_FLIPPED =
            ATTACK_UP_AIR_HITBOX_2.reflect();
    
    // Down attack (air)
    public static final int ATTACK_DOWN_AIR_DURATION = 30;
    public static final int ATTACK_DOWN_AIR_FRAME_2_BEGIN = 6;
    public static final Polygon ATTACK_DOWN_AIR_HITBOX_1_1 =
            new Polygon(0.78f,0.91f, 1.8f,1.63f, 1.91f,-0.15f, 0.6f,0.65f);
    public static final Shape ATTACK_DOWN_AIR_HITBOX_1_1_FLIPPED =
            ATTACK_DOWN_AIR_HITBOX_1_1.reflect();
    public static final Polygon ATTACK_DOWN_AIR_HITBOX_1_2 =
            new Polygon(0.6f,0.65f, 1.91f,-0.15f, 0.44f,-0.87f, 0.25f,0.42f);
    public static final Shape ATTACK_DOWN_AIR_HITBOX_1_2_FLIPPED =
            ATTACK_DOWN_AIR_HITBOX_1_2.reflect();
    public static final int ATTACK_DOWN_AIR_FRAME_3_BEGIN = 8;
    public static final Polygon ATTACK_DOWN_AIR_HITBOX_2 =
            new Polygon(0.25f,0.42f, 0.44f,-0.87f, -1.47f,0.11f, -0.31f,0.69f);
    public static final Shape ATTACK_DOWN_AIR_HITBOX_2_FLIPPED =
            ATTACK_DOWN_AIR_HITBOX_2.reflect();
    
    // Specials
    
    // Side special (ground)
    public static final int SPECIAL_SIDE_GROUND_COST_MANA = 50;
    public static final int SPECIAL_SIDE_GROUND_DURATION = 40;
    public static final int SPECIAL_SIDE_GROUND_FRAME_2_BEGIN = 16;
    public static final Vector2 SPECIAL_SIDE_GROUND_ORIGIN = new Vector2(1.09f, 1.25f);
    
    // Up special (ground)
    public static final int SPECIAL_UP_GROUND_COST_MANA = 100;
    public static final int SPECIAL_UP_GROUND_DURATION = 40;
    public static final int SPECIAL_UP_GROUND_FRAME_2_BEGIN = 16;
    public static final Vector2 SPECIAL_UP_GROUND_ORIGIN = new Vector2(0.25f,2.25f);
    
    // Down special (ground)
    public static final int SPECIAL_DOWN_GROUND_COST_MANA = 100;
    public static final int SPECIAL_DOWN_GROUND_DURATION = 40;
    public static final int SPECIAL_DOWN_GROUND_FRAME_2_BEGIN = 16;
    public static final Polygon SPECIAL_DOWN_GROUND_HITBOX_1 =
            new Polygon(3f,0f, 0f,0f, 0f,2f, 1.5f,2f);
    public static final Polygon SPECIAL_DOWN_GROUND_HITBOX_2 =
            new Polygon(0f,0f, -3f,0f, -1.5f,2f, 0f,2f);
    
    // Side special (air)
    public static final int SPECIAL_SIDE_AIR_COST_MANA = 50;
    public static final int SPECIAL_SIDE_AIR_DURATION = 30;
    public static final int SPECIAL_SIDE_AIR_FRAME_2_BEGIN = 10;
    public static final Vector2 SPECIAL_SIDE_AIR_ORIGIN = new Vector2(1.02f,1.33f);
    
    // Up special (air)
    public static final int SPECIAL_UP_AIR_COST_MANA = 100;
    public static final int SPECIAL_UP_AIR_DURATION = 30;
    public static final int SPECIAL_UP_AIR_FRAME_2_BEGIN = 10;
    public static final Vector2 SPECIAL_UP_AIR_ORIGIN = new Vector2(0.27f,2.18f);
    
    // Down special (air)
    public static final int SPECIAL_DOWN_AIR_COST_MANA = 100;
    public static final int SPECIAL_DOWN_AIR_DURATION = 30;
    public static final int SPECIAL_DOWN_AIR_FRAME_2_BEGIN = 10;
    public static final Vector2 SPECIAL_DOWN_AIR_ORIGIN = new Vector2(0.33f,0.36f);
    
    
    //private static final AABB AABB = new AABB(-0.4f, 0f, 0.8f, 1.8f);
    private static final AABB AABB = new AABB(-0.375f, 0f, 0.75f, 1.75f);
    //private static final AABB AABB = new AABB(-0.05f, -0.05f, 0.1f, 0.1f);
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The person's max mana. */
    public int maxMana;
    /** The person's mana. */
    public int mana;
    
    /** Whether the mob's health changed since the last tick. */
    public boolean healthChanged = false;
    /** Whether the mob's mana changed since the last tick. */
    public boolean manaChanged = false;
    
    /** The number of ticks since the mob last lost health. */
    public int ticksSinceHealthLoss = 0;
    /** The number of ticks since the mob last lost mana. */
    public int ticksSinceManaLoss = 0;
    
    
    public Armour amrHead = Armour.TIER_1_HEAD;
    public Armour amrBody = Armour.TIER_1_BODY;
    public Armour amrArms = Armour.TIER_1_ARMS;
    public Armour amrLegs = Armour.TIER_1_LEGS;
    public Weapon weapon  = Weapon.SWORD_TIER_1;
    
    
    /** The amount of damage dealt by an attack - for carrying over multiple
     * frames. */
    private int curAtkDamageDealt = 0;
    /** Collision set for the current attack - for carrying over multiple
     * frames. */
    private Set<Long> curAtkCollisionSet;
    
    private ParticleEmitter<?> fireParticles;
    private ParticleEmitter<?> healParticles;
    
    @Override
    public AABB getAABB() {
        return AABB;
    }
    
    @Override
    public void init(Entity e) {
        super.init(e);
        
        maxHealth = 100;
        health = 100;
        maxMana = 500000;
        mana = 500000;
        
        maxJumpCount = 2;
        jumpVelocity = 16f;
        jumpCrouchDuration = 8;
        acceleration = 1.5f;
        //airAcceleration = acceleration * 0.15f;
        airAcceleration = acceleration;
        maxDx = 15f;
        
        state = State.IDLE;
    }
    
    /**
     * Temporary function that improves the temporary weapon/armour that we
     * have equipped.
     */
    public CPerson upgradeEquipment() {
        amrHead = Armour.TIER_2_HEAD;
        amrBody = Armour.TIER_2_BODY;
        amrArms = Armour.TIER_2_ARMS;
        amrLegs = Armour.TIER_2_LEGS;
        weapon = Weapon.SWORD_TIER_2;
        
        return this;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        super.update(w, e, dt);
        
        ticksSinceHealthLoss++;
        ticksSinceManaLoss++;
        
        // Regen health/mana/stamina
        if(ticksSinceHealthLoss >= 80) {
            if(ticksSinceHealthLoss >= 360 || ticksSinceHealthLoss % 3 == 0) {
                if(increaseHealth(1)) {
                    if(healParticles == null)
                        healParticles = w.particleEmitter(ParticleHeal.class);
                    healParticles.createBurst(w,
                            1, 0.2f, 2.0f,
                            Maths.PIf / 6.0f,
                            Maths.PIf * 5.0f / 6.0f,
                            e
                    );
                }
            }
        }
        
        if(ticksSinceManaLoss >= 60) {
            if(ticksSinceManaLoss >= 300 || ticksSinceManaLoss % 2 == 0)
                increaseMana(1);
        }
        
        // State-specific scripts
        switch(state) {
            case ATTACK_SIDE_GROUND:
                if(stateTicks == ATTACK_SIDE_GROUND_FRAME_2_BEGIN) {
                    curAtkDamageDealt = weapon.getDamage(); //w.rnd().nextInt(16) + 5;
                    curAtkCollisionSet = Hitbox.createCollisionSet();
                    
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_SIDE_GROUND_HITBOX_1
                            : ATTACK_SIDE_GROUND_HITBOX_1_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h.hits = -1;
                    h.force = ATTACK_SIDE_GROUND_FORCE;
                    h.fx = facingRight ? 1.0f : -1.0f;
                    w.addHitbox(h, e.pos);
                } else if(stateTicks == ATTACK_SIDE_GROUND_FRAME_3_BEGIN) {
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_SIDE_GROUND_HITBOX_2
                            : ATTACK_SIDE_GROUND_HITBOX_2_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h.hits = -1;
                    h.force = ATTACK_SIDE_GROUND_FORCE;
                    h.fx = facingRight ? -1.0f : 1.0f;
                    w.addHitbox(h, e.pos);
                }
                break;
            case ATTACK_UP_GROUND:
                if(stateTicks == ATTACK_UP_GROUND_FRAME_2_BEGIN) {
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_UP_GROUND_HITBOX
                            : ATTACK_UP_GROUND_HITBOX_FLIPPED,
                            weapon.getDamage() + w.rnd().nextInt(10)); //w.rnd().nextInt(16) + 20);
                    h.hits = -1;
                    h.force = ATTACK_UP_GROUND_FORCE;
                    h.fy = 1.0f;
                    h.persistent = true;
                    h.persistenceTimer = ATTACK_UP_GROUND_HITBOX_DURATION;
                    w.addHitbox(h, e.pos);
                }
                break;
            case ATTACK_DOWN_GROUND:
                if(stateTicks == ATTACK_DOWN_GROUND_FRAME_2_BEGIN) {
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_DOWN_GROUND_HITBOX
                            : ATTACK_DOWN_GROUND_HITBOX_FLIPPED,
                            weapon.getDamage()); //w.rnd().nextInt(16) + 5);
                    h.hits = -1;
                    h.force = ATTACK_DOWN_GROUND_FORCE;
                    h.fx = facingRight ? 1.0f : -1.0f;
                    w.addHitbox(h, e.pos);
                }
                break;
            case ATTACK_SIDE_AIR:
                if(stateTicks == ATTACK_SIDE_AIR_FRAME_2_BEGIN) {
                    curAtkDamageDealt = weapon.getDamage(); //w.rnd().nextInt(16) + 5;
                    curAtkCollisionSet = Hitbox.createCollisionSet();
                    
                    Hitbox h = new Hitbox(e.id(), facingRight ?
                            ATTACK_SIDE_AIR_HITBOX_1
                            : ATTACK_SIDE_AIR_HITBOX_1_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h.hits = -1;
                    h.force = 0.3f;
                    h.fx = facingRight ? 1.0f : -1.0f;
                    w.addHitbox(h, e.pos);
                } else if(stateTicks == ATTACK_SIDE_AIR_FRAME_3_BEGIN) {
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_SIDE_AIR_HITBOX_2
                            : ATTACK_SIDE_AIR_HITBOX_2_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h.hits = -1;
                    h.force = 0.3f;
                    h.fx = facingRight ? 1.0f : -1.0f;
                    w.addHitbox(h, e.pos);
                }
                break;
            case ATTACK_UP_AIR:
                if(stateTicks == ATTACK_UP_AIR_FRAME_2_BEGIN) {
                    curAtkDamageDealt = weapon.getDamage(); //w.rnd().nextInt(16) + 5;
                    curAtkCollisionSet = Hitbox.createCollisionSet();
                    
                    Hitbox h1 = new Hitbox(e.id(), facingRight
                            ? ATTACK_UP_AIR_HITBOX_1_1
                            : ATTACK_UP_AIR_HITBOX_1_1_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h1.hits = -1;
                    h1.force = 0.3f;
                    //h1.fx = facingRight ? 0.86f : -0.85f;
                    h1.fy = 1.0f;
                    w.addHitbox(h1, e.pos);
                    
                    Hitbox h2 = new Hitbox(e.id(), facingRight
                            ? ATTACK_UP_AIR_HITBOX_1_2
                            : ATTACK_UP_AIR_HITBOX_1_2_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h2.hits = -1;
                    h2.force = 0.3f;
                    //h2.fx = facingRight ? 0.5f : -0.5f;
                    h2.fy = 1.0f;
                    w.addHitbox(h2, e.pos);
                } else if(stateTicks == ATTACK_UP_AIR_FRAME_3_BEGIN) {
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_UP_AIR_HITBOX_2
                            : ATTACK_UP_AIR_HITBOX_2_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h.hits = -1;
                    h.force = 0.3f;
                    h.fy = 1.0f;
                    w.addHitbox(h, e.pos);
                }
                break;
            case ATTACK_DOWN_AIR:
                if(stateTicks == ATTACK_DOWN_AIR_FRAME_2_BEGIN) {
                    curAtkDamageDealt = weapon.getDamage(); //w.rnd().nextInt(16) + 5;
                    curAtkCollisionSet = Hitbox.createCollisionSet();
                    
                    Hitbox h1 = new Hitbox(e.id(), facingRight
                            ? ATTACK_DOWN_AIR_HITBOX_1_1
                            : ATTACK_DOWN_AIR_HITBOX_1_1_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h1.hits = -1;
                    h1.force = 0.3f;
                    h1.fy = -1.0f;
                    w.addHitbox(h1, e.pos);
                    
                    Hitbox h2 = new Hitbox(e.id(), facingRight
                            ? ATTACK_DOWN_AIR_HITBOX_1_2
                            : ATTACK_DOWN_AIR_HITBOX_1_2_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h2.hits = -1;
                    h2.force = 0.3f;
                    h2.fy = -1.0f;
                    w.addHitbox(h2, e.pos);
                } else if(stateTicks == ATTACK_DOWN_AIR_FRAME_3_BEGIN) {
                    Hitbox h = new Hitbox(e.id(), facingRight
                            ? ATTACK_DOWN_AIR_HITBOX_2
                            : ATTACK_DOWN_AIR_HITBOX_2_FLIPPED,
                            curAtkDamageDealt, curAtkCollisionSet);
                    h.hits = -1;
                    h.force = 0.3f;
                    h.fy = -1.0f;
                    w.addHitbox(h, e.pos);
                }
                break;
            case SPECIAL_SIDE_GROUND:
                if(stateTicks == SPECIAL_SIDE_GROUND_FRAME_2_BEGIN)
                    fireball(w, e, SPECIAL_SIDE_GROUND_COST_MANA, SPECIAL_SIDE_GROUND_ORIGIN);
                break;
            case SPECIAL_UP_GROUND:
                if(stateTicks == SPECIAL_UP_GROUND_FRAME_2_BEGIN)
                    fireballStorm(w, e, SPECIAL_UP_GROUND_COST_MANA,
                            SPECIAL_UP_GROUND_ORIGIN);
                break;
            case SPECIAL_DOWN_GROUND:
                if(stateTicks == SPECIAL_DOWN_GROUND_FRAME_2_BEGIN) {
                    if(useMana(SPECIAL_DOWN_GROUND_COST_MANA)) {
                        curAtkDamageDealt = w.rnd().nextInt(16) + 5;
                        curAtkCollisionSet = Hitbox.createCollisionSet();
                        
                        Hitbox h1 = new Hitbox(e.id(), SPECIAL_DOWN_GROUND_HITBOX_1,
                                curAtkDamageDealt, curAtkCollisionSet);
                        h1.hits = -1;
                        h1.force = 14f;
                        h1.fx = 0.5f;
                        h1.fy = 0.7f;
                        h1.effects = tgt -> tgt.addComponent(new CEffectFire(60*7, 3));
                        w.addHitbox(h1, e.pos);
                        
                        Hitbox h2 = new Hitbox(e.id(), SPECIAL_DOWN_GROUND_HITBOX_2,
                                curAtkDamageDealt, curAtkCollisionSet);
                        h2.hits = -1;
                        h2.force = 14f;
                        h2.fx = -0.5f;
                        h2.fy = 0.7f;
                        h2.effects = tgt -> tgt.addComponent(new CEffectFire(60*7, 3));
                        w.addHitbox(h2, e.pos);
                        
                        fireParticles.createBurst(w, 300, e.pos, 0.1f, 5f, 0, (float)Math.PI);
                        w.particleEmitter(ParticleExplosion.class).createAt(w, e.pos);
                        
                        // TODO: shake
                        //w.getCamera().shake(0.1f, 30);
                    } else {
                        fireParticles.createBurst(w, 100, e.pos, 0.1f, 5f, 0, (float)Math.PI);
                    }
                }
                break;
            case SPECIAL_SIDE_AIR:
                if(stateTicks == SPECIAL_SIDE_AIR_FRAME_2_BEGIN)
                    fireball(w, e, SPECIAL_SIDE_AIR_COST_MANA, SPECIAL_SIDE_AIR_ORIGIN);
                break;
            case SPECIAL_UP_AIR:
                if(stateTicks == SPECIAL_UP_AIR_FRAME_2_BEGIN)
                    fireballStorm(w, e, SPECIAL_UP_AIR_COST_MANA, SPECIAL_UP_AIR_ORIGIN);
                break;
            case SPECIAL_DOWN_AIR:
                if(stateTicks == SPECIAL_DOWN_AIR_FRAME_2_BEGIN) {
                    fireballRain(w, e, SPECIAL_DOWN_AIR_COST_MANA, SPECIAL_DOWN_AIR_ORIGIN);
                    e.dy += 10f;
                    e.addComponent(new CEffectFireTrail(Constants.TICKS_PER_SECOND / 3));
                }
                break;
            default:
                // nothing here
                break;
        }
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderPerson(e, this);
    }
    
    @Override
    protected void doNthJump(int n) {
        super.doNthJump(n);
        if(n > 1)
            e.addComponent(new CEffectFireTrail(Constants.TICKS_PER_SECOND / 2));
    }
    
    private void fireball(World w, Entity e, int manaCost, Vector2 originPoint) {
        if(useMana(manaCost)) {
            Entity f = Entities.fireball(e.id(), 5 + w.rnd().nextInt(5));
            
            if(facingRight) {
                f.pos.set(e.pos, originPoint.x, originPoint.y);
                f.dx = 30f + w.rnd().nextFloat() * 10f;
            } else {
                f.pos.set(e.pos, -originPoint.x, originPoint.y);
                f.dx = -30f - w.rnd().nextFloat() * 10f;
            }
            
            f.dy = 1.0f + w.rnd().nextFloat() * 1.8f;
            
            w.addEntity(f);
        } else {
            float minAngle, maxAngle;
            Position dummyPos = fireParticles.dummyPos;
            
            if(facingRight) {
                dummyPos.set(e.pos, originPoint.x, originPoint.y);
                minAngle = -Maths.PIf / 6;
                maxAngle = Maths.PIf / 6;
            } else {
                dummyPos.set(e.pos, -originPoint.x, originPoint.y);
                minAngle = Maths.PIf * 5/6;
                maxAngle = Maths.PIf * 7/6;
            }
            
            fireParticles.createBurst(w, 6, dummyPos, 1f, 5f, minAngle, maxAngle);
        }
    }
    
    /**
     * Creates a fireball storm.
     * 
     * @param manaCost The mana cost of the storm.
     * @param originPoint The point from which the storm is to originate.
     */
    private void fireballStorm(World w, Entity e, int manaCost, Vector2 originPoint) {
        float px = facingRight ? originPoint.x : -originPoint.x;
        
        if(useMana(manaCost)) {
            int max = 30 + w.rnd().nextInt(11);
            for(int i = 0; i < max; i++) {
                float angle = (w.rnd().nextFloat() * 0.47f + 0.03f) * MathUtils.PI;
                float velocity = 18.5f + w.rnd().nextFloat() * 6.5f;
                boolean right = w.rnd().nextBoolean();
                Entity f = Entities.fireball(e.id(), 5 + w.rnd().nextInt(10));
                f.dx = right ? MathUtils.cos(angle) * velocity : -MathUtils.cos(angle) * velocity;
                f.dy = MathUtils.sin(angle)*velocity;
                f.pos.set(e.pos, px, originPoint.y);
                w.addEntity(f);
            }
        } else {
            Position dummyPos = fireParticles.dummyPos;
            fireParticles.createBurst(w, 12, dummyPos.set(e.pos, px, originPoint.y),
                    1f, 5f, MathUtils.PI / 3f, MathUtils.PI * 0.6666f);
        }
    }
    
    private void fireballRain(World w, Entity e, int manaCost, Vector2 originPoint) {
        float px = facingRight ? originPoint.x : -originPoint.x;
        
        if(useMana(manaCost)) {
            int n = 3 + w.rnd().nextInt(4);
            float minAngle = (1.5f - 0.25f) * MathUtils.PI;
            float maxAngle = (1.5f + 0.25f) * MathUtils.PI;
            float inc = (maxAngle - minAngle) / (float)(n - 1);
            float angle = minAngle;
            for(int i = 0; i < n; i++) {
                float a = angle + (float)w.rnd().nextGaussian() * MathUtils.PI / 20; 
                angle += inc;
                float v = 18.5f + w.rnd().nextFloat() * 6.5f;
                Entity f = Entities.fireball(e.id(), 5 + w.rnd().nextInt(10));
                f.dx = MathUtils.cos(a) * v;
                f.dy = MathUtils.sin(a)*v;
                f.pos.set(e.pos, px, originPoint.y);
                w.addEntity(f);
            }
        } else {
            Position dummyPos = fireParticles.dummyPos;
            fireParticles.createBurst(w, 12, dummyPos.set(e.pos, px, originPoint.y), 
                    1f, 5f, (1.5f - (1f/6))*MathUtils.PI, (1.5f + (1f/6))*MathUtils.PI);
        }
    }
    
    @Override
    protected void onLand() {
        switch(state) {
            case ATTACK_SIDE_AIR:
                if(ATTACK_SIDE_AIR_DURATION - stateTicks > 10)
                    setState(State.LAND_CROUCH, false, 15);
                else
                    stateLockDuration = 0;
                break;
            case ATTACK_UP_AIR:
                if(ATTACK_UP_AIR_DURATION - stateTicks > 10)
                    setState(State.LAND_CROUCH, false, 15);
                else
                    stateLockDuration = 0;
                break;
            case ATTACK_DOWN_AIR:
                if(ATTACK_DOWN_AIR_DURATION - stateTicks > 10)
                    setState(State.LAND_CROUCH, false, 15);
                else
                    stateLockDuration = 0;
                break;
            default:
                stateLockDuration = 0;
                break;
        }
    }
    
    @Override
    public void attack(World w, Direction direction) {
        if(!state.canAct)
            return;
        
        if(e.physics.onGround()) {
            switch(direction) {
                case LEFT:
                case RIGHT:
                    setState(State.ATTACK_SIDE_GROUND, true, ATTACK_SIDE_GROUND_DURATION);
                    facingRight = (direction.hasRight());
                    break;
                case UP:
                    setState(State.ATTACK_UP_GROUND, true, ATTACK_UP_GROUND_DURATION);
                    break;
                case DOWN:
                    setState(State.ATTACK_DOWN_GROUND, true, ATTACK_DOWN_GROUND_DURATION);
                    break;
                default:
                    // nothing
                    break;
            }
        } else {
            switch(direction) {
                case LEFT:
                case RIGHT:
                    setState(State.ATTACK_SIDE_AIR, true, ATTACK_SIDE_AIR_DURATION);
                    facingRight = (direction.hasRight());
                    break;
                case UP:
                    setState(State.ATTACK_UP_AIR, true, ATTACK_UP_AIR_DURATION);
                    break;
                case DOWN:
                    setState(State.ATTACK_DOWN_AIR, true, ATTACK_DOWN_AIR_DURATION);
                    break;
                default:
                    // nothing
                    break;
            }
        }
    }
    
    @Override
    public void specialAttack(World w, Direction direction) {
        if(!state.canAct)
            return;
        
        if(e.physics.onGround()) {
            switch(direction) {
                case LEFT:
                case RIGHT:
                    setState(State.SPECIAL_SIDE_GROUND, true, SPECIAL_SIDE_GROUND_DURATION);
                    facingRight = (direction.hasRight());
                    break;
                case UP:
                    setState(State.SPECIAL_UP_GROUND, true, SPECIAL_UP_GROUND_DURATION);
                    break;
                case DOWN:
                    setState(State.SPECIAL_DOWN_GROUND, true, SPECIAL_DOWN_GROUND_DURATION);
                    break;
                default:
                    // nothing
                    break;
            }
        } else {
            switch(direction) {
                case LEFT:
                case RIGHT:
                    setState(State.SPECIAL_SIDE_AIR, true, SPECIAL_SIDE_AIR_DURATION);
                    facingRight = (direction.hasRight());
                    break;
                case UP:
                    setState(State.SPECIAL_UP_AIR, true, SPECIAL_UP_AIR_DURATION);
                    break;
                case DOWN:
                    setState(State.SPECIAL_DOWN_AIR, true, SPECIAL_DOWN_AIR_DURATION);
                    break;
                default:
                    // nothing
                    break;
            }
        }
    }
    
    @Override
    public void restore() {
        increaseHealth(maxHealth);
        increaseMana(maxMana);
    }
    
    /**
     * Attempts to consume some of the person's mana.
     * 
     * @param mana The amount of mana to use.
     * 
     * @return {@code true} if the person had an amount of mana greater than
     * or equal to the given value; {@code false} otherwise.
     */
    private boolean useMana(int mana) {
        ticksSinceManaLoss = 0;
        
        if(this.mana < mana)
            return false;
        
        manaChanged = true;
        this.mana -= mana;
        
        return true;
    }
    
    /**
     * Increases the person's health.
     * 
     * @param amount The amount by which to increase the person's health.
     * 
     * @return true if the health was increased
     */
    private boolean increaseHealth(int amount) {
        if(health == maxHealth)
            return false;
        
        health += amount;
        if(health > maxHealth)
            health = maxHealth;
        
        healthChanged = true;
        return true;
    }
    
    /**
     * Increases the person's mana.
     * 
     * @param amount The amount by which to increase the person's mana.
     */
    private void increaseMana(int amount) {
        if(mana == maxMana)
            return;
        
        mana += amount;
        if(mana > maxMana)
            mana = maxMana;
        
        manaChanged = true;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD)
            fireParticles = w.particleEmitter(ParticleFlame.class);
        else if(ev.type() == EntityEvent.Type.KILLED) {
            dropItem(w, e, Items.APPLE, 1, 0.02f);
            dropItem(w, e, Items.SWORD, 1, 0.02f);
            dropItem(w, e, Items.ARROW, 1, 0.02f);
        }
        return super.handle(w, e, ev);
    }
    
    @Override
    public boolean damage(World w, Entity e, IDamageSource src) {
        if(super.damage(w, e, src)) {
            ticksSinceHealthLoss = 0;
            return true;
        }
        return false;
    }
    
    @Override
    protected void applyDamageReduction(IDamageSource src) {
        if(src.type().equals(DamageType.ATTACK)) {
            float reduction = amrHead.reduction + amrBody.reduction
                    + amrArms.reduction + amrLegs.reduction;
            src.setDamage((int)(src.damage()*(1-reduction)));
        }
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        super.importFromCompound(c);
        
        maxMana = c.getI32("maxMana");
        mana = c.getI32("mana");
        
        ticksSinceHealthLoss = c.getI32("ticksSinceHpLoss");
        ticksSinceManaLoss = c.getI32("ticksSinceMpLoss");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        super.exportToCompound(c);
        
        c.put("maxMana", maxMana);
        c.put("mana", mana);
        
        c.put("ticksSinceHpLoss", ticksSinceHealthLoss);
        c.put("ticksSinceMpLoss", ticksSinceManaLoss);
    }
    
}
