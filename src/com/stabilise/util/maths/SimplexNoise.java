package com.stabilise.util.maths;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A utility class which generates 2-dimensional simplex noise.
 * 
 * <p>Much of this code is from 
 * http://webstaff.itn.liu.se/~stegu/simplexnoise/SimplexNoise.java
 */
@NotThreadSafe
public class SimplexNoise {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    // Skewing and unskewing factors for 2, 3, and 4 dimensions
    private static final double SKEW_2D = 0.5*(Math.sqrt(3.0)-1.0);
    private static final double UNSKEW_2D = (3.0-Math.sqrt(3.0))/6.0;
    @SuppressWarnings("unused")
    private static final double SKEW_3D = 1.0/3.0;
    @SuppressWarnings("unused")
    private static final double UNSKEW_3D = 1.0/6.0;
    @SuppressWarnings("unused")
    private static final double SKEW_4D = (Math.sqrt(5.0)-1.0)/4.0;
    @SuppressWarnings("unused")
    private static final double UNSKEW_4D = (5.0-Math.sqrt(5.0))/20.0;
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The pseudorandom number generator. */
    private final Random rnd;
    /** The base seed. */
    private final long seed;
    /** The scale of noise to generate. */
    private final float scale;
    
    /** Gradient vector. */
    private final Vector2 grad = new Vector2();
    
    
    /**
     * Creates a simplex noise generator.
     * 
     * @param seed The seed to use for noise generation.
     * @param scale The scale of the noise to generate.
     */
    public SimplexNoise(long seed, float scale) {
        this.scale = scale;
        this.seed = hash(seed, Double.doubleToLongBits((double)scale));
        
        rnd = new Random(seed);
    }
    
    /**
     * Hashes two long values in a really simple way
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
        // TODO: Figure out a non-crappy hashing function
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
        x /= scale;
        y /= scale;
        
        double n0, n1, n2;    // Noise contributions from the three corners
        
        // Skew the input space to determine which simplex cell we're in
        double s = (x + y) * SKEW_2D;        // Skew factor for 2D
        int i = Maths.floor(x + s);
        int j = Maths.floor(y + s);
        double t = (i + j) * UNSKEW_2D;
        double X0 = i - t;        // Unskew the cell origin back to (x,y) space
        double Y0 = j - t;
        double x0 = x - X0;        // The x,y distances from the cell origin
        double y0 = y - Y0;
        
        // For the 2D case, the simplex shape is an equilateral triangle.
        // Determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j) coords
        if(x0 > y0) {
            // Lower triangle, XY order: (0,0)->(1,0)->(1,1)
            i1 = 1;
            j1 = 0;
        } else {
            // Upper triangle, YX order: (0,0)->(0,1)->(1,1)
            i1 = 0;
            j1 = 1;
        }
        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
        // c = (3-sqrt(3))/6 [the value of UNSKEW_2D]
        double x1 = x0 - i1 + UNSKEW_2D;            // Offsets for middle corner in (x,y) unskewed coords
        double y1 = y0 - j1 + UNSKEW_2D;
        double x2 = x0 - 1.0 + 2.0 * UNSKEW_2D;        // Offsets for last corner in (x,y) unskewed coords
        double y2 = y0 - 1.0 + 2.0 * UNSKEW_2D;
        
        // Work out the hashed gradient indices of the three simplex corners
        /*
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = permMod12[ii + perm[jj]];
        int gi1 = permMod12[ii + i1 + perm[jj + j1]];
        int gi2 = permMod12[ii + 1 + perm[jj + 1]];
        */
        
        // Calculate the contribution from the three corners
        double t0 = 0.5D - x0*x0 - y0*y0;
        if(t0 < 0D) {
            n0 = 0.0D;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * genGradient(i, j).dot((float)x0, (float)y0);    // (x,y) of grad3 used for 2D gradient
        }
        double t1 = 0.5D - x1*x1 - y1*y1;
        if(t1 < 0D) {
            n1 = 0.0D;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * genGradient(i+i1, j+j1).dot((float)x1, (float)y1);
        }
        double t2 = 0.5D - x2*x2 - y2*y2;
        if(t2 < 0D) {
            n2 = 0.0D;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * genGradient(i+1, j+1).dot((float)x2, (float)y2);
        }
        
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        
        //return 70.0 * (n0 + n1 + n2);            // Produces a value between -1 and 1
        return 0.5 + 35.0 * (n0 + n1 + n2) * Maths.SQRT_2;        // Produces a value between 0 and 1
    }
    
    /**
     * Generates the noise 'gradient' at a given gridpoint.
     * 
     * @param x The x-coordinate of the gridpoint.
     * @param y The y-coordinate of the gridpoint.
     * 
     * @return The noise gradient at (x,y).
     */
    private Vector2 genGradient(int x, int y) {
        setSeed(x, y);
        // We require a normalised vector; this is unsatisfactory
        //return new Vector2f(2*rnd.nextFloat()-1, 2*rnd.nextFloat()-1);
        
        double angle = Maths.TAU*rnd.nextDouble();
        return grad.set((float)(Math.cos(angle)), (float)(Math.sin(angle)));
    }
    
}
