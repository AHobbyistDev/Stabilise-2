//package com.stabilise.entity.old;
//
//import com.stabilise.core.Settings;
//import com.stabilise.entity.EntityProjectile;
//import com.stabilise.entity.collision.Hitbox;
//import com.stabilise.entity.collision.LinkedHitbox;
//import com.stabilise.entity.effect.EffectFire;
//import com.stabilise.entity.particle.ParticleExplosion;
//import com.stabilise.entity.particle.ParticleFlame;
//import com.stabilise.opengl.render.WorldRenderer;
//import com.stabilise.util.maths.Maths;
//import com.stabilise.util.shape.Polygon;
//import com.stabilise.world.World;
//import com.stabilise.world.AbstractWorld.ParticleSource;
//
///**
// * A flaming projectile which deals damage to mobs.
// */
//public class EntityBigFireball extends EntityProjectile {
//    
//    //--------------------==========--------------------
//    //-----=====Static Constants and Variables=====-----
//    //--------------------==========--------------------
//    
//    /** The fireball hitbox template. */
//    private static final Polygon BIG_FIREBALL_BOUNDING_BOX = Polygon.rectangle(-0.05f, -0.05f, 0.1f, 0.1f);
//    /** Default fireball damage. */
//    private static final int DEFAULT_FIREBALL_DAMAGE = 10;
//    
//    // These hitboxes roughtly construct the four quadrants of a circle of radius 4 tiles
//    /** The first hitbox produced by the fireball. */
//    public static final Polygon HITBOX_1 = new Polygon(0,0, 0,4, 3,3, 4,0);
//    /** The second hitbox produced by the fireball. */
//    public static final Polygon HITBOX_2 = new Polygon(0,0, 0,4, -3,3, -4,0);
//    /** The third hitbox produced by the fireball. */
//    public static final Polygon HITBOX_3 = new Polygon(0,0, 0,-4, -3,-3, -4,0);
//    /** The fourth hitbox produced by the fireball. */
//    public static final Polygon HITBOX_4 = new Polygon(0,0, 0,-4, 3,-3, 4,0);
//    
//    /** The number of ticks after which a fireball despawns. */
//    private static final int DESPAWN_TICKS = 600;
//    
//    //--------------------==========--------------------
//    //-------------=====Member Variables=====-----------
//    //--------------------==========--------------------
//    
//    /** The fireball's damage. */
//    private int damage;
//    
//    private final ParticleSource particleSrc;
//    private final ParticleSource explosionSrc;
//    
//    
//    /** TODO: temporary. */
//    public EntityBigFireball() {
//        super(null, null, null);
//        particleSrc = null;
//        explosionSrc = null;
//    }
//    
//    /**
//     * Creates a new fireball entity.
//     * 
//     * @param world The world in which the fireball will be placed.
//     * @param owner The fireball's owner.
//     */
//    public EntityBigFireball(World world, EntityOld owner) {
//        this(world, owner, DEFAULT_FIREBALL_DAMAGE);
//    }
//    
//    /**
//     * Creates a new fireball entity.
//     * 
//     * @param world The world.
//     * @param owner The fireball's owner.
//     * @param damage The fireball's damage.
//     */
//    public EntityBigFireball(World world, EntityOld owner, int damage) {
//        super(world, owner, new LinkedHitbox(owner, BIG_FIREBALL_BOUNDING_BOX, damage));
//        ((LinkedHitbox)hitbox).linkedEntity = this;
//        hitbox.force = 0.5f;
//        hitbox.effect = new EffectFire(420);
//        
//        this.damage = damage;
//        
//        particleSrc = world.getParticleManager().getSource(new ParticleFlame());
//        explosionSrc = world.getParticleManager().getSource(new ParticleExplosion(0.5f, 12));
//    }
//    
//    @Override
//    public void update(World world) {
//        super.update(world);
//        
//        float div = Math.abs(dx) + Math.abs(dy);
//        if(div != 0) {
//            hitbox.fx = dx / div;
//            hitbox.fy = dy / div;
//        }
//        
//        if(Settings.settingParticlesAll())
//            addFlightParticles(world, 8);
//        else if(Settings.settingParticlesReduced())
//            addFlightParticles(world, 4);
//        
//        if(age == DESPAWN_TICKS)
//            destroy();
//    }
//    
//    @Override
//    protected void impact(World world, float dv, boolean tileCollision) {
//        destroy();
//        
//        if(tileCollision) {        // Since it removes itself with an entity collision
//            if(Settings.settingParticlesAll())
//                addImpactParticles(world, 15);
//            else if(Settings.settingParticlesReduced())
//                addImpactParticles(world, 8);
//        }
//    }
//    
//    private void addFlightParticles(World world, int particles) {
//        particleSrc.createBurst(particles, x, y, 0.02f, 0.05f, 0f, (float)Maths.TAU);
//    }
//    
//    /**
//     * Creates fire particles about the fireball's location of impact.
//     * 
//     * @param particles The number of particles to create.
//     */
//    private void addImpactParticles(World world, int particles) {
//        particleSrc.createBurst(particles, x, y, 0.08f, 0.15f, 0f, (float)Maths.TAU);
//    }
//    
//    @Override
//    public void onAdd() {
//        float div = Math.abs(dx) + Math.abs(dy);
//        if(div != 0) {
//            hitbox.fx = dx / div;
//            hitbox.fy = dy / div;
//        }
//    }
//    
//    @Override
//    public void render(WorldRenderer renderer) {
//        renderer.renderBigFireball(this);
//    }
//    
//    @Override
//    public void destroy() {
//        super.destroy();
//        
//        EffectFire fire = new EffectFire(300);
//        
//        Hitbox h1 = new Hitbox(owner, HITBOX_1, damage);
//        h1.force = 0.5f;
//        h1.fx = 0.707f;        // approx 1/sqrt(2)
//        h1.fy = 0.707f;
//        h1.effect = fire;
//        h1.hits = -1;
//        Hitbox h2 = new Hitbox(owner, HITBOX_2, damage);
//        h2.force = 0.5f;
//        h2.fx = -0.707f;
//        h2.fy = 0.707f;
//        h2.effect = fire;
//        h2.hits = -1;
//        Hitbox h3 = new Hitbox(owner, HITBOX_3, damage);
//        h3.force = 0.5f;
//        h3.fx = -0.707f;
//        h3.fy = -0.707f;
//        h3.effect = fire;
//        h3.hits = -1;
//        Hitbox h4 = new Hitbox(owner, HITBOX_4, damage);
//        h4.force = 0.5f;
//        h4.fx = 0.707f;
//        h4.fy = -0.707f;
//        h4.effect = fire;
//        h4.hits = -1;
//        
//        world.addHitbox(h1, x, y);
//        world.addHitbox(h2, x, y);
//        world.addHitbox(h3, x, y);
//        world.addHitbox(h4, x, y);
//        
//        explosionSrc.createAt(x, y);
//        
//        // Destroy some tiles about the explosion
//        float radius = 5.5f;
//        float radiusSquared = radius * radius;
//        int minX = (int)(x - radius);
//        int maxX = (int)Math.ceil(x + radius);
//        int minY = (int)(y - radius);
//        int maxY = (int)Math.ceil(y + radius);
//        
//        for(int tx = minX; tx <= maxX; tx++) {
//            for(int ty = minY; ty <= maxY; ty++) {
//                double xDiff = x - tx;
//                double yDiff = y - ty;
//                if(xDiff*xDiff + yDiff*yDiff <= radiusSquared)
//                    //world.setTileAt(tx, ty, 0);
//                    world.blowUpTile(tx, ty, 12);
//            }
//        }
//    }
//    
//}
