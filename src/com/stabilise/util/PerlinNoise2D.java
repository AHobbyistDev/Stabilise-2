package com.stabilise.util;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

/**
 * A utility class which generates 2-dimensional perlin noise.
 * 
 * <p>Instances of this class are not thread-safe, and invocations of
 * {@link #noise(double, double)} must be synchronised externally.
 */
public class PerlinNoise2D {
	
	/** The pseudorandom number generator. */
	private Random rnd;
	/** The base seed. */
	private long seed;
	/** The wavelength of noise to generate. */
	private float wavelength;
	
	
	/**
	 * Creates a new 2-dimensional perlin noise generator.
	 * 
	 * @param seed The seed to use for noise generation.
	 * @param wavelength The wavelength of noise to generate.
	 */
	public PerlinNoise2D(long seed, float wavelength) {
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
	 * @param y The y-coordinate of the point for which to set the seed.
	 */
	private void setSeed(int x, int y) {
		//long n = x + (y << 32);
		//n ^= (n * 15731) >> 16;
		int n = x + y * 57;
		n = (n<<13) ^ n;
		n = n * (n * n * 15731 + 789221) + 1376312589;
		rnd.setSeed(seed ^ n);
	}
	
	/**
	 * Gets the noise value at the given point.
	 * 
	 * @param x The x-coordinate of the point at which to sample the noise.
	 * @param y The y-coordinate of the point at which to sample the noise.
	 * 
	 * @return The noise value at (x,y), between 0.0 and 1.0.
	 */
	public double noise(double x, double y) {
		x /= wavelength;
		y /= wavelength;
		int flooredX = (int)Math.floor(x);
		int flooredY = (int)Math.floor(y);
		
		// Gen gradients for the vertices of the square about the point
		Vector2f g00 = genGradient(flooredX, flooredY);
		Vector2f g01 = genGradient(flooredX, flooredY+1);
		Vector2f g10 = genGradient(flooredX+1, flooredY);
		Vector2f g11 = genGradient(flooredX+1, flooredY+1);
		
		// Dot the gradients at each vertex with vectors pointing from the corners to p
		Vector2f p = new Vector2f((float)(x - flooredX), (float)(y - flooredY));
		
		double v00 = Vector2f.dot(g00, p);
		double v01 = Vector2f.dot(g01, Vector2f.sub(p, new Vector2f(0f, 1f), null));
		double v10 = Vector2f.dot(g10, Vector2f.sub(p, new Vector2f(1f, 0f), null));
		double v11 = Vector2f.dot(g11, Vector2f.sub(p, new Vector2f(1f, 1f), null));
		
		// Interpolate to attain a value
		return MathUtil.interpolateBisinusoidal(v00, v01, v10, v11, p.x, p.y) * MathUtil.SQRT_2;
	}
	
	/**
	 * Generates the noise gradient at a given gridpoint.
	 * 
	 * @param x The x-coordinate of the gridpoint.
	 * @param y The y-coordinate of the gridpoint.
	 * 
	 * @return The noise gradient at (x,y).
	 */
	private Vector2f genGradient(int x, int y) {
		setSeed(x, y);
		double angle = MathUtil.TAU*rnd.nextDouble();
		return new Vector2f((float)Math.cos(angle), (float)Math.sin(angle));
		//return new Vector2f(2*rnd.nextFloat()-1, 2*rnd.nextFloat()-1);
	}
	
}
