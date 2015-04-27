package com.stabilise.entity.effect;

import com.stabilise.core.Constants;
import com.stabilise.core.Settings;
import com.stabilise.entity.EntityMob;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.world.World;
import com.stabilise.world.AbstractWorld.ParticleSource;

/**
 * The effect a Mob has when it is on fire.
 * 
 * <p>A mob which is on fire will take 2 damage a second and constantly produce
 * flame particles.
 */
public class EffectFire extends Effect {
	
	private ParticleSource particleSrc;
	
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
		
		if(particleSrc == null)
			particleSrc = world.getParticleManager().getSource(new ParticleFlame());
		
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
		particleSrc.createBurst(1, 0.02f, 0.07f, (float)Math.PI / 6.0f,
				(float)Math.PI * 5.0f / 6.0f, target);
	}
	
	@Override
	public EffectFire clone() {
		EffectFire e = new EffectFire(duration);
		e.age = age;
		return e;
	}
	
}
