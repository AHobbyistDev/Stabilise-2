package com.stabilise.util.maths;

import java.util.Objects;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import com.stabilise.util.Checks;


/**
 * This class produces noise by superimposing a number of specified octaves.
 */
public class OctaveNoise implements INoise {
    
    public static final LongUnaryOperator DEFAULT_SEED_MIXER = z -> {
        // Mixing algorithm unashamedly taken from ThreadLocalRandom.
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    };
    
    private int count = 0;
    private final INoise[] octaves;
    private final double[] freqs;
    private final float[] weights;
    private float invTotalWeight = 1f;
    private boolean normalise = true;
    
    private final LongFunction<INoise> noiseGen;
    private long seed;
    private LongUnaryOperator seedMixer = DEFAULT_SEED_MIXER;
    
    
    /**
     * Creates a new {@code OctaveNoise} object.
     * 
     * @param numOctaves The number of octaves constituting this noise.
     * @param seed The seed for this noise.
     * @param noiseGen The function which produces noise generators, given a
     * seed (e.g. {@code seed -> new PerlinNoise(seed)})
     * 
     * @throws IllegalArgumentException if {@code numOctaves < 1}.
     * @throws NullPointerException if {@code noiseGen} is null.
     */
    public OctaveNoise(int numOctaves, long seed, LongFunction<INoise> noiseGen) {
        Checks.testMin(numOctaves, 1);
        this.seed = seed;
        this.octaves = new INoise[numOctaves];
        this.freqs = new double[numOctaves];
        this.weights = new float[numOctaves];
        this.noiseGen = Objects.requireNonNull(noiseGen);
    }
    
    /**
     * Sets the seed mixer. The mixer modifies the seed after each octave is
     * added, to avoid fractal noise, and the default mixer is {@link
     * #DEFAULT_SEED_MIXER}. If, however, you would prefer fractal noise, then
     * you may set the mixer to be the identity map {@code (s) -> s}.
     * 
     * @return This {@code OctaveNoise}, for chaining operations.
     * @throws NullPointerException if {@code mixer} is null.
     */
    public OctaveNoise seedMixer(LongUnaryOperator mixer) {
        this.seedMixer = Objects.requireNonNull(mixer);
        return this;
    }
    
    /**
     * Invoking this will prevent generated from being normalised to [0-1], and
     * instead will instead allow each octave to be scaled by its specified
     * weight.
     */
    public OctaveNoise doNotNormalise() {
        normalise = false;
        invTotalWeight = 1f; // in case addOctave() has already set this
        return this;
    }
    
    /**
     * Adds an octave.
     * 
     * @param period The period, or "scale" of the noise.
     * @param weight The weight of this octave. Octaves with greater weight
     * will contribute more to the final noise.
     * 
     * @return This {@code OctaveNoise}, for chaining operations.
     * @throws IllegalStateException if the number of octaves as specified in
     * the constructor have already been added (i.e., you're trying to add too
     * many).
     * @throws IllegalArgumentException if {@code period <= 0 || weight <= 0}.
     * @throws NullPointerException if {@code noiseGen} (which was specified in
     * the constructor) produced a null value.
     */
    public OctaveNoise addOctave(float period, float weight) {
        if(count == octaves.length)
            throw new IllegalArgumentException("Added too many octaves (max" + octaves.length + ")!");
        
        octaves[count] = Objects.requireNonNull(noiseGen.apply(seed));
        freqs[count] = 1/Checks.testMinExcl(period, 0f);
        weights[count] = Checks.testMinExcl(weight, 0f);
        seed = seedMixer.applyAsLong(seed);
        
        count++;
        if(normalise && count == octaves.length) {
            float totalWeight = 0f;
            for(int i = 0; i < weights.length; i++)
                totalWeight += weights[i];
            invTotalWeight = 1/totalWeight;
        }
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>This class can break the contract of this method if {@link
     * #doNotNormalise()} was invoked, in which case the returned noise will no
     * longer be confined to [0,1], but rather [0,a] for some a.
     */
    @Override
    public float noise(double x) {
        if(count != octaves.length) {
            throw new IllegalStateException("Not all octaves added!");
        }
        
        float noise = 0f;
        for(int i = 0; i < octaves.length; i++)
            noise += weights[i] * octaves[i].noise(x * freqs[i]);
        return noise * invTotalWeight;
    }
    
    /*
    public float noiseDebug(double x, double y) {
        System.out.println("Debug noise at (" + x + "," + y + ")");
        float noise = 0f;
        for(int i = 0; i < octaves.length; i++) {
            float n = octaves[i].noise(x * freqs[i], y * freqs[i]);
            System.out.println("Octave " + i + ": " + n + ", weight: " + weights[i]);
            noise += weights[i] * n;
        }
        System.out.println("Total noise: " + noise + ", invTotalWeight = " + invTotalWeight);
        System.out.println("Result: " + (noise * invTotalWeight));
        
        return noise * invTotalWeight;
    }
    */
    
    /**
     * {@inheritDoc}
     * 
     * <p>This class can break the contract of this method if {@link
     * #doNotNormalise()} was invoked, in which case the returned noise will no
     * longer be confined to [0,1], but rather [0,a] for some a.
     */
    @Override
    public float noise(double x, double y) {
        if(count != octaves.length) {
            throw new IllegalStateException("Not all octaves added!");
        }
        
        float noise = 0f;
        for(int i = 0; i < octaves.length; i++)
            noise += weights[i] * octaves[i].noise(x * freqs[i], y * freqs[i]);
        return noise * invTotalWeight;
    }
    
    /**
     * Gets noise from only a single octave. Octaves are labelled from 0 to
     * numOctaves-1, where numOctaves was specified in the constructor. 
     * 
     * @return The noise from that octave, from 0 to 1.
     * 
     * @throws ArrayIndexOutOfBoundsException
     */
    public float noiseN(double x, int octave) {
        return octaves[octave].noise(x * freqs[octave]);
    }
    
    /**
     * Gets noise from only a single octave. Octaves are labelled from 0 to
     * numOctaves-1, where numOctaves was specified in the constructor. 
     * 
     * @return The noise from that octave, from 0 to 1.
     */
    public float noiseN(double x, double y, int octave) {
        return octaves[octave].noise(x * freqs[octave], y * freqs[octave]);
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates Perlin octave noise.
     * 
     * @param numOctaves The number of octaves constituting this noise.
     * @param seed The seed for this noise.
     * 
     * @throws IllegalArgumentException if {@code numOctaves < 1}.
     */
    public static OctaveNoise perlin(int numOctaves, long seed) {
        return new OctaveNoise(numOctaves, seed, s -> new PerlinNoise(s));
    }
    
    /**
     * Creates Simplex octave noise.
     * 
     * @param numOctaves The number of octaves constituting this noise.
     * @param seed The seed for this noise.
     * 
     * @throws IllegalArgumentException if {@code numOctaves < 1}.
     */
    public static OctaveNoise simplex(int numOctaves, long seed) {
        return new OctaveNoise(numOctaves, seed, s -> new SimplexNoise(s));
    }
    
}
