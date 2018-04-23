package com.stabilise.util.maths;

import java.util.Random;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A utility class which generates Perlin noise.
 */
@NotThreadSafe
public class PerlinNoise implements INoise {
    
    // Your standard interpolation function
    static final Interpolation interp1 = x -> 3*x*x - 2*x*x*x;
    // A slightly better but more expensive interpolation function. Unlike
    // interp1, the second derivative is 0 at the endpoints (vs. only the
    // first derivative).
    static final Interpolation interp2 = x -> 6*x*x*x*x*x - 15*x*x*x*x + 10*x*x*x;
    // Actual sinusoidal interp
    static final Interpolation interp3 = Interpolation.SINUSOIDAL.inOut;
    
    
    
    /** The pseudorandom number generator. */
    private final Random rnd = new Random();
    /** The base seed. */
    private final long seed;
    
    /** Gradient vectors for 2D noise. */
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
     */
    public PerlinNoise(long seed) {
        this.seed = seed;
    }
    
    private void setSeed(int x) {
        //long n = x + (x << 32);
        //n ^= (n * 15731) >> 16;
        long n = (x<<13) ^ x;
        n = n * (n * n * 15731 + 789221) + 1376312589;
        rnd.setSeed(seed ^ n);
    }
    
    /**
     * Sets the seed of the RNG for noise generation at a point.
     */
    private void setSeed(int x, int y) {
        //long n = x + (y << 32);
        //n ^= (n * 15731) >> 16;
        long n = x + y * 57;
        n = (n<<13) ^ n;
        n = n * (n * n * 15731 + 789221) + 1376312589;
        rnd.setSeed(seed ^ n);
    }
    
    @Override
    public float noise(double x) {
        int flooredX = Maths.floor(x);
        
        // Note: this implementation is technically that of value noise
        // TODO: Change to perlin noise proper one day
        float dx = (float)(x - flooredX);
        return interp1.apply(genValue(flooredX), genValue(flooredX + 1), dx);
    }
    
    /**
     * Generates the noise value at the given gridpoint.
     * 
     * @return The noise value at x, between 0.0 and 1.0.
     */
    private float genValue(int x) {
        setSeed(x);
        return rnd.nextFloat();
    }
    
    @Override
    public float noise(double x, double y) {
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
        return 0.5f + Maths.biInterp(v00, v01, v10, v11, p.x, p.y, interp1);
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
