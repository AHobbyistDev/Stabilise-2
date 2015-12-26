package com.stabilise.util.maths;

import java.util.Random;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.math.MathUtils;
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
     * Hashes two long values in a really simple and stupid way
     */
    private long hash(long x, long y) {
        x ^= y;
        x ^= x << 32;
        return x;
    }
    
    /**
     * Sets the seed of the RNG for noise generation at a point.
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
     * Gets the noise value at (x,y), between 0 and 1.
     */
    public double noise(double x, double y) {
        x /= wavelength;
        y /= wavelength;
        int flooredX = Maths.floor(x);
        int flooredY = Maths.floor(y);
        
        // Gen gradients for the vertices of the square about the point
        genGradient(flooredX,   flooredY,   g00);
        genGradient(flooredX,   flooredY+1, g01);
        genGradient(flooredX+1, flooredY,   g10);
        genGradient(flooredX+1, flooredY+1, g11);
        
        // We'll need to dot the gradients at each vertex with vectors pointing
        // from the corners to p
        p.set((float)(x - flooredX), (float)(y - flooredY));
        
        float v00 = g00.dot(p);
        float v01 = g01.dot(p.x, p.y - 1f);
        float v10 = g10.dot(p.x - 1f, p.y);
        float v11 = g11.dot(p.x - 1f, p.y - 1f);
        
        // Interpolate to attain a value
        return Maths.SQRT_2f * 
                Maths.biInterp(v00, v01, v10, v11, p.x, p.y, PerlinNoise1D.interp1);
    }
    
    /**
     * Generates the noise gradient at a given gridpoint.
     * 
     * @param dest The destination vector in which to store the gradient.
     */
    private void genGradient(int x, int y, Vector2 dest) {
        setSeed(x, y);
        float angle = Maths.TAUf * rnd.nextFloat();
        dest.set(MathUtils.cos(angle), MathUtils.sin(angle));
    }
    
}
