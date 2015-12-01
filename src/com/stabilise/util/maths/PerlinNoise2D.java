package com.stabilise.util.maths;

import java.util.Random;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.math.Vector2;

/**
 * A utility class which generates 2-dimensional perlin noise.
 */
@NotThreadSafe
public class PerlinNoise2D {
    
    /** The pseudorandom number generator. */
    private final Random rnd;
    /** The base seed. */
    private final long seed;
    /** The wavelength of noise to generate. */
    private final float wavelength;
    
    /** Gradient vectors. */
    private final Vector2 g00 = new Vector2(),
            g01 = new Vector2(),
            g10 = new Vector2(),
            g11 = new Vector2();
    /** The cell location vector. */
    private final Vector2 p = new Vector2();
    
    
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
        int flooredX = Maths.floor(x);
        int flooredY = Maths.floor(y);
        
        // Gen gradients for the vertices of the square about the point
        genGradient(flooredX, flooredY, g00);
        genGradient(flooredX, flooredY+1, g01);
        genGradient(flooredX+1, flooredY, g10);
        genGradient(flooredX+1, flooredY+1, g11);
        
        // We'll need to dot the gradients at each vertex with vectors pointing
        // from the corners to p
        p.set((float)(x - flooredX), (float)(y - flooredY));
        
        float v00 = g00.dot(p);
        float v01 = g01.dot(p.x, p.y - 1f);        //g01.dot(p.sub(Vector2.Y));
        float v10 = g10.dot(p.x - 1f, p.y);        //g10.dot(p.sub(Vector2.X));
        float v11 = g11.dot(p.x - 1f, p.y - 1f);//g11.dot(p.sub(MathUtil.VEC_1_1));
        
        // Interpolate to attain a value
        return Maths.interpolateBisinusoidal(v00, v01, v10, v11, p.x, p.y) * Maths.SQRT_2;
    }
    
    /**
     * Generates the noise gradient at a given gridpoint.
     * 
     * @param x The x-coordinate of the gridpoint.
     * @param y The y-coordinate of the gridpoint.
     * @param dest The destination vector in which to store the gradient.
     */
    private void genGradient(int x, int y, Vector2 dest) {
        setSeed(x, y);
        double angle = Maths.TAU*rnd.nextDouble();
        dest.set((float)Math.cos(angle), (float)Math.sin(angle));
        //dest.set(2*rnd.nextFloat()-1, 2*rnd.nextFloat()-1);
    }
    
}
