package com.stabilise.entity;

import com.stabilise.core.Settings;
import com.stabilise.entity.collision.LinkedHitbox;
import com.stabilise.entity.effect.EffectFire;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleGenerator;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.Rectangle;
import com.stabilise.world.IWorld;

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
	
	
	/**
	 * Creates a new fireball entity.
	 * 
	 * @param world The world in which the fireball will be placed.
	 * @param owner The fireball's owner.
	 */
	public EntityFireball(IWorld world, Entity owner) {
		this(world, owner, DEFAULT_FIREBALL_DAMAGE);
	}
	
	/**
	 * Creates a new fireball entity.
	 * 
	 * @param world The world in which the fireball will be placed.
	 * @param owner The fireball's owner.
	 * @param damage The fireball's damage.
	 */
	public EntityFireball(IWorld world, Entity owner, int damage) {
		super(world, owner, new LinkedHitbox(owner, FIREBALL_BOUNDING_BOX, damage));
		((LinkedHitbox)hitbox).linkedEntity = this;
		hitbox.force = 0.3f;
		hitbox.effect = new EffectFire(300);
	}
	
	@Override
	public void update(IWorld world) {
		super.update(world);
		
		float div = Math.abs(dx) + Math.abs(dy);
		if(div != 0) {
			hitbox.fx = dx / div;
			hitbox.fy = dy / div;
		}
		
		if(Settings.settingParticlesAll())
			addFlightParticles(world, 8);
		else if(Settings.settingParticlesReduced())
			addFlightParticles(world, 4);
		
		if(age == DESPAWN_TICKS)
			destroy();
	}
	
	@Override
	protected void impact(IWorld world, float dv, boolean tileCollision) {
		destroy();
		
		if(tileCollision) {		// Since it removes itself with an entity collision
			if(Settings.settingParticlesAll())
				addImpactParticles(world, 15);
			else if(Settings.settingParticlesReduced())
				addImpactParticles(world, 8);
		}
	}
	
	private void addFlightParticles(IWorld world, int particles) {
		for(int i = 0; i < particles; i++) {
			ParticleFlame p = new ParticleFlame();
			p.x = x;
			p.y = y;
			ParticleGenerator.directParticle(p, 0.02f, 0.05f, 0, Maths.TAU);
			world.addParticle(p);
		}
	}
	
	/**
	 * Creates fire particles about the fireball's location of impact.
	 * 
	 * @param particles The number of particles to create.
	 */
	private void addImpactParticles(IWorld world, int particles) {
		//float velocity = (float)Math.sqrt(dx*dx + dy*dy) / 4;
		//double angle = Math.atan2(dy, dx);
		for(int i = 0; i < particles; i++) {
			ParticleFlame p = new ParticleFlame();
			p.x = x;
			p.y = y;
			//ParticleGenerator.directParticle(p, velocity - 0.05f, velocity + 0.15f, angle - Math.PI / 6, angle + Math.PI / 6);
			ParticleGenerator.directParticle(p, 0.08f, 0.15f, 0, Maths.TAU);
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
