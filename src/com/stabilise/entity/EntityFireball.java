package com.stabilise.entity;

import com.stabilise.core.Settings;
import com.stabilise.entity.collision.LinkedHitbox;
import com.stabilise.entity.effect.EffectFire;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleGenerator;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.util.shape.Rectangle;
import com.stabilise.world.World;

/**
 * A flaming projectile which deals damage to mobs.
 */
public class EntityFireball extends EntityProjectile {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The fireball hitbox template. */
	private static final Rectangle FIREBALL_BOUNDING_BOX = new Rectangle(-0.05f, -0.05f, 0.1f, 0.1f);
	/** Default fireball damage. */
	private static final int DEFAULT_FIREBALL_DAMAGE = 10;
	
	/** The number of ticks after which a fireball despawns. */
	private static final int DESPAWN_TICKS = 300;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/**
	 * Creates a new fireball entity.
	 * 
	 * @param world The world in which the fireball will be placed.
	 * @param owner The fireball's owner.
	 */
	public EntityFireball(World world, Entity owner) {
		this(world, owner, DEFAULT_FIREBALL_DAMAGE);
	}
	
	/**
	 * Creates a new fireball entity.
	 * 
	 * @param world The world in which the fireball will be placed.
	 * @param owner The fireball's owner.
	 * @param damage The fireball's damage.
	 */
	public EntityFireball(World world, Entity owner, int damage) {
		super(world, owner, new LinkedHitbox(world, owner, FIREBALL_BOUNDING_BOX, damage));
		((LinkedHitbox)hitbox).linkedEntity = this;
		hitbox.force = 0.3f;
		hitbox.effect = new EffectFire(300);
	}
	
	@Override
	public void update() {
		super.update();
		
		float div = Math.abs(dx) + Math.abs(dy);
		if(div != 0) {
			hitbox.fx = dx / div;
			hitbox.fy = dy / div;
		}
		
		if(Settings.settingParticlesAll())
			addFlightParticles(8);
		else if(Settings.settingParticlesReduced())
			addFlightParticles(4);
		
		if(age == DESPAWN_TICKS) {
			destroy();
			world.removeHitbox(hitbox);
		}
	}
	
	@Override
	protected void impact(float dv, boolean tileCollision) {
		world.removeEntity(this);
		
		if(tileCollision) {		// Since it removes itself with an entity collision
			world.removeHitbox(hitbox);
			
			if(Settings.settingParticlesAll())
				addImpactParticles(15);
			else if(Settings.settingParticlesReduced())
				addImpactParticles(8);
		}
	}
	
	private void addFlightParticles(int particles) {
		for(int i = 0; i < particles; i++) {
			ParticleFlame p = new ParticleFlame(world);
			p.x = x;
			p.y = y;
			ParticleGenerator.directParticle(p, 0.02f, 0.05f, 0, MathsUtil.TAU);
			world.addParticle(p);
		}
	}
	
	/**
	 * Creates fire particles about the fireball's location of impact.
	 * 
	 * @param particles The number of particles to create.
	 */
	private void addImpactParticles(int particles) {
		//float velocity = (float)Math.sqrt(dx*dx + dy*dy) / 4;
		//double angle = Math.atan2(dy, dx);
		for(int i = 0; i < particles; i++) {
			ParticleFlame p = new ParticleFlame(world);
			p.x = x;
			p.y = y;
			//ParticleGenerator.directParticle(p, velocity - 0.05f, velocity + 0.15f, angle - Math.PI / 6, angle + Math.PI / 6);
			ParticleGenerator.directParticle(p, 0.08f, 0.15f, 0, MathsUtil.TAU);
			world.addParticle(p);
		}
	}
	
	@Override
	public void onAdd() {
		float div = Math.abs(dx) + Math.abs(dy);
		if(div != 0) {
			hitbox.fx = dx / div;
			hitbox.fy = dy / div;
		}
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		renderer.renderFireball(this);
	}
	
}
