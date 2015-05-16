package com.stabilise.util.maths;

import java.util.Random;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A utility class which generates 1-dimensional perlin noise.
 */
@NotThreadSafe
public class PerlinNoise1D {
	
	/** The pseudorandom number generator. */
	private final Random rnd;
	/** The base seed. */
	private final long seed;
	/** The wavelength of noise to generate. */
	private final float wavelength;
	
	
	/**
	 * Creates a new 1-dimensional perlin noise generator.
	 * 
	 * @param seed The seed to use for noise generation.
	 * @param wavelength The wavelength of noise to generate.
	 */
	public PerlinNoise1D(long seed, float wavelength) {
		this.wavelength = wavelength;
		this.seed = hash(seed, Double.doubleToLongBits((double)wavelength));
		
		rnd = new Random(seed);
	}
	
	/**
	 * Hashes two long values in an order-irrelevant manner.
	 */
	private long hash(long x, long y) {
		x ^= y;
		x ^= x << 32;
		return x;
	}
	
	/**
	 * Sets the seed of the RNG for noise generation at a point.
	 * 
	 * @param x The x-coordinate of the point for which to set the seed.
	 */
	private void setSeed(int x) {
		//long n = x + (x << 32);
		//n ^= (n * 15731) >> 16;
		x = (x<<13) ^ x;
		x = x * (x * x * 15731 + 789221) + 1376312589;
		rnd.setSeed(seed ^ x);
	}
	
	/**
	 * Gets the noise value at the given x-coordinate.
	 * 
	 * @param x The x-coordinate at which to sample the noise.
	 * 
	 * @return The noise value at x, between 0.0 and 1.0.
	 */
	public double noise(float x) {
		x /= wavelength;
		int flooredX = Maths.floor(x);
		
		// Note: this implementation is technically that of value noise
		// instead of perlin noise
		return Maths.interpolateSinusoidal(genValue(flooredX),
				genValue(flooredX + 1), x - flooredX);
	}
	
	/**
	 * Generates the noise value at the given gridpoint.
	 * 
	 * @param x The x-coordinate of the point.
	 * 
	 * @return The noise value at x, between 0.0 and 1.0.
	 */
	private double genValue(int x) {
		setSeed(x);
		//return 2*rnd.nextDouble() - 1;
		return rnd.nextDouble();
	}
	
}
