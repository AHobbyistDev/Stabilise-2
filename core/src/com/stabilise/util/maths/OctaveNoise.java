package com.stabilise.util.maths;

import java.util.ArrayList;
import java.util.List;
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
    
    private final List<Octave> octaves = new ArrayList<>();
    private float invTotalWeight = 1f;
    
    private final LongFunction<INoise> noiseGen;
    private long seed;
    private LongUnaryOperator seedMixer = DEFAULT_SEED_MIXER;
    
    
    /**
     * Creates a new {@code OctaveNoise} object.
     * 
     * @param seed The seed for this noise.
     * @param noiseGen The function which produces noise generators, given a
     * seed (e.g. {@code seed -> new PerlinNoise(seed)})
     * 
     * @throws NullPointerException if {@code noiseGen} is null.
     */
    public OctaveNoise(long seed, LongFunction<INoise> noiseGen) {
        this.seed = seed;
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
     * Invoking this will normalise the noise to [0,1] by rescaling each octave
     * by <tt>1/totalWeight</tt>.
     * 
     * @return this {@code OctaveNoise}, for chaining operations.
     */
    public OctaveNoise normalise() {
        float totalWeight = 0f;
        for(int i = 0; i < octaves.size(); i++)
            totalWeight += octaves.get(i).weight;
        invTotalWeight = 1/totalWeight;
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
     * @throws IllegalArgumentException if {@code period <= 0 || weight <= 0}.
     * @throws NullPointerException if {@code noiseGen} (which was specified in
     * the constructor) produced a null value.
     */
    public OctaveNoise addOctave(float period, float weight) {
        octaves.add(new Octave(
                Objects.requireNonNull(noiseGen.apply(seed)),
                1/Checks.testMinExcl(period, 0f),
                Checks.testMinExcl(weight, 0f)
        ));
        seed = seedMixer.applyAsLong(seed);
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
        float noise = 0f;
        for(int i = 0; i < octaves.size(); i++)
            noise += octaves.get(i).noise(x);
        return noise * invTotalWeight;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>This class can break the contract of this method if {@link
     * #doNotNormalise()} was invoked, in which case the returned noise will no
     * longer be confined to [0,1], but rather [0,a] for some a.
     */
    @Override
    public float noise(double x, double y) {
        float noise = 0f;
        for(int i = 0; i < octaves.size(); i++)
            noise += octaves.get(i).noise(x, y);
        return noise * invTotalWeight;
    }
    
    /**
     * Gets noise from only a single octave. Octaves are labelled from 0 to
     * numOctaves-1. 
     * 
     * @return The noise from that octave, from 0 to 1.
     * 
     * @throws IndexOutOfBoundsException
     */
    public float noiseN(double x, int octave) {
        return octaves.get(octave).noise(x);
    }
    
    /**
     * Gets noise from only a single octave. Octaves are labelled from 0 to
     * numOctaves-1.
     * 
     * @return The noise from that octave, from 0 to 1.
     */
    public float noiseN(double x, double y, int octave) {
        return octaves.get(octave).noise(x, y);
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates Perlin octave noise.
     * 
     * @param seed The seed for this noise.
     */
    public static OctaveNoise perlin(long seed) {
        return new OctaveNoise(seed, s -> new PerlinNoise(s));
    }
    
    /**
     * Creates Simplex octave noise.
     * 
     * @param seed The seed for this noise.
     */
    public static OctaveNoise simplex(long seed) {
        return new OctaveNoise(seed, s -> new SimplexNoise(s));
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    private static class Octave implements INoise {
        private final INoise noise;
        private final double freq;
        private final float weight;
        private Octave(INoise noise, double freq, float weight) {
            this.noise = noise;
            this.freq = freq;
            this.weight = weight;
        }
        @Override public float noise(double x) {
            return weight * noise.noise(x*freq);
        }
        @Override public float noise(double x, double y) {
            return weight * noise.noise(x*freq, y*freq);
        }
    }
    
}
