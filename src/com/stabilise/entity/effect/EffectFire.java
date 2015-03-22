package com.stabilise.entity.effect;

import com.stabilise.core.Constants;
import com.stabilise.core.Settings;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleGenerator;
import com.stabilise.world.World;

/**
 * The effect a Mob has when it is on fire.
 * 
 * <p>A mob which is on fire will take 2 damage a second and constantly produce
 * flame particles.
 */
public class EffectFire extends Effect {
	
	/**
	 * Creates a new fire effect.
	 * 
	 * @param duration The duration of the effect, in ticks.
	 */
	public EffectFire(int duration) {
		super(duration);
	}
	
	@Override
	public void update(World world, EntityMob target) {
		super.update(world, target);
		
		if(Settings.settingParticlesAll())
			createFireParticle(world, target);
		else if(Settings.settingParticlesReduced() && age % 3 == 0)
			createFireParticle(world, target);
		
		if(age % Constants.TICKS_PER_SECOND == 0)
			target.damage(world, 2, -1, 0, 0);
	}
	
	/**
	 * Creates a fire particle.
	 * 
	 * @param target The target of the effect.
	 */
	private void createFireParticle(World world, EntityMob target) {
		ParticleFlame p = new ParticleFlame();
		p.x = target.x + target.boundingBox.getV00().x + world.getRnd().nextFloat() * target.boundingBox.width;
		p.y = target.y + target.boundingBox.getV11().y + world.getRnd().nextFloat() * target.boundingBox.height;
		
		ParticleGenerator.directParticle(p, 0.02f, 0.07f, Math.PI / 6.0D, Math.PI * 5.0D / 6.0D);
		
		world.addParticle(p);
	}
	
	@Override
	public EffectFire clone() {
		EffectFire e = new EffectFire(duration);
		e.age = age;
		return e;
	}
	
}
