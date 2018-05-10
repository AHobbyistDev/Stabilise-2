package com.stabilise.tests;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.function.LongFunction;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.maths.INoise;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.util.maths.PerlinNoise;
import com.stabilise.util.maths.SimplexNoise;

public class NoiseTest {
    
    // true = simplex, false = perlin
    private static final boolean SIMPLEX = true;
    
    private long seed = 7238674329493L;
    
    private final OctaveNoise noise;
    
    private float min = 9999f;
    private float max = -9999f;

    public NoiseTest() {
        seed = new Random().nextLong();
        
        LongFunction<INoise> func = SIMPLEX ?
                s -> new SimplexNoise(s) :
                s -> new PerlinNoise(s);
        
        noise = new OctaveNoise(seed, func)
                .addOctave(32, 32)
                .addOctave(16, 16)
                .addOctave(8,  8 )
                .addOctave(4,  4 )
                .addOctave(2,  2 )
                .normalise();
        
        final int width = 480;
        final int height = 480;
        int[] pixels = new int[width*height];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                pixels[y*height+x] = strengthToRGBA(noise.noise(x,y));
            }
        }
        
        BufferedImage image = getImageFromArray(pixels, width, height);
        
        IOUtil.createDir(new FileHandle("noise/"));
        String originalName = SIMPLEX ? "noise/simplex.png" : "noise/perlin.png";
        FileHandle file = IOUtil.getNewFile(new FileHandle(originalName));
        
        // Handles the delegation of duplicate world names
        try {
            ImageIO.write(image, "png", file.file());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Wrote noise to file \"" + file.file().getAbsolutePath() + "\"");
        System.out.println("(min,max) = (" + min + "," + max + ")");
    }
    
    public int strengthToRGBA(float strength) {
        if(strength < 0 || strength > 1)
            throw new IllegalArgumentException("pls: " + strength);
        
        if(strength < min)
            min = strength;
        else if(strength > max)
            max = strength;
        
        int r,g,b,a;
        r = g = b = (int)(0xFF*strength);
        a = 0xFF;
        return (a << 24) + (r << 16) + (g << 8) + b;
    }
    
    public static BufferedImage getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }
    
    public static void main(String[] args) {
        new NoiseTest();
    }

}
