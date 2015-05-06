package com.stabilise.tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.maths.PerlinNoise2D;

public class PerlinTest {
	
	private long seed = 7238674329493L;
	
	private final PerlinNoise2D perlin1;
	private final PerlinNoise2D perlin2;
	private final PerlinNoise2D perlin3;
	private final PerlinNoise2D perlin4;
	private final PerlinNoise2D perlin5;

	public PerlinTest() {
		seed = new Random().nextLong();
		
		perlin1 = new PerlinNoise2D(seed, 32f);
		perlin2 = new PerlinNoise2D(seed, 16f);
		perlin3 = new PerlinNoise2D(seed, 8f);
		perlin4 = new PerlinNoise2D(seed, 4f);
		perlin5 = new PerlinNoise2D(seed, 2f);
		
		final int width = 480;
		final int height = 480;
		int[] pixels = new int[width*height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				pixels[y*height+x] = strengthToRGBA(noise(x,y));
			}
		}
		
		BufferedImage image = getImageFromArray(pixels, width, height);
		
		IOUtil.createDir(new FileHandle("noise/"));
		
		// Handles the delegation of duplicate world names
		String originalName = "noise/perlin";
		String name = originalName;
		File file;
		int iteration = 0;
		while((file = new File(name + ".png")).exists()) {
			iteration++;
			name = originalName + iteration;
		}
		try {
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double noise(int x, int y) {
		//return 0.5D+perlin1.noise(x,y)/2D;
		///*
		return 0.5D+
				(perlin1.noise(x,y)*32d+
				perlin2.noise(x,y)*16d+
				perlin3.noise(x,y)*8d+
				perlin4.noise(x,y)*4d+
				perlin5.noise(x,y)*2d)/63D;		// Divisor must be sum of all multipliers
		//*/
	}
	
	public static int strengthToRGBA(double strength) {		// DOUBLE THE STRENGTH, DOUBLE THE POWAH
		if(strength < 0 || strength > 1)
			throw new IllegalArgumentException("pls: " + strength);
		
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
		new PerlinTest();
	}

}
