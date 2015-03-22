package com.stabilise.entity.particle;

import java.util.Random;

import com.stabilise.entity.FreeGameObject;
import com.stabilise.world.World;

/**
 * A utility class which handles the paramaterised generation of randomised
 * particles.
 */
public class ParticleGenerator {
	
	private ParticleGenerator() {
		// non-instantiable
	}
	
	/**
	 * Places a particle at the location of its producer, directs it, and then
	 * adds it to the world.
	 * 
	 * @param world The world.
	 * @param p The particle.
	 * @param producer The game object to treat as having produced the particle.
	 * @param minVelocity The minimum velocity to give the particle.
	 * @param maxVelocity The maximum velocity to give the particle.
	 * @param minAngle The minimum angle at which to emit the particle, in
	 * radians.
	 * @param maxAngle The maximum angle at which to emit the particle, in
	 * radians.
	 */
	public static void directParticle(World world, ParticlePhysical p, FreeGameObject producer, float minVelocity, float maxVelocity, double minAngle, double maxAngle) {
		p.x = producer.x;
		p.y = producer.y;
		directParticle(p, minVelocity, maxVelocity, minAngle, maxAngle);
		world.addParticle(p);
	}
	
	/**
	 * Directs a particle.
	 * 
	 * @param p The particle.
	 * @param minVelocity The minimum velocity to give the particle.
	 * @param maxVelocity The maximum velocity to give the particle.
	 * @param minAngle The minimum angle at which to emit the particle, in
	 * radians.
	 * @param maxAngle The maximum angle at which to emit the particle, in
	 * radians.
	 */
	public static void directParticle(ParticlePhysical p, float minVelocity, float maxVelocity, double minAngle, double maxAngle) {
		Random rng = new Random();
		
		float velocity = minVelocity + rng.nextFloat() * (maxVelocity - minVelocity);
		double angle = minAngle + rng.nextDouble() * (maxAngle - minAngle);
		
		p.dx = (float) (velocity * Math.cos(angle));
		p.dy = (float) (velocity * Math.sin(angle));
	}
	
}
