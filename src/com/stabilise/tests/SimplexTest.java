package com.stabilise.tests;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.IOUtil;
import com.stabilise.util.maths.SimplexNoise;

public class SimplexTest {
	
	private long seed = 7238674329493L;
	
	private final SimplexNoise simplex1;
	private final SimplexNoise simplex2;
	private final SimplexNoise simplex3;
	private final SimplexNoise simplex4;
	private final SimplexNoise simplex5;
	private final SimplexNoise simplex6;
	private final SimplexNoise simplex7;
	
	
	public SimplexTest() {
		seed = new Random().nextLong();
		
		simplex1 = new SimplexNoise(seed, 128);
		simplex2 = new SimplexNoise(seed, 64);
		simplex3 = new SimplexNoise(seed, 32);
		simplex4 = new SimplexNoise(seed, 16);
		simplex5 = new SimplexNoise(seed, 8);
		simplex6 = new SimplexNoise(seed, 4);
		simplex7 = new SimplexNoise(seed, 2);
		
		final int width = 480;
		final int height = 480;
		int[] pixels = new int[width*height];
		
		double min = 1d;
		double max = 0d;
		double avg = 0d;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				double noise = noise(x, y);
				if(noise < min)
					min = noise;
				if(noise > max)
					max = noise;
				avg += noise;
				pixels[y*height+x] = strengthToRGBA(noise);
			}
		}
		
		avg /= width*height;
		
		System.out.println("Min: " + min);
		System.out.println("Max: " + max);
		System.out.println("Avg: " + avg);
		
		BufferedImage image = getImageFromArray(pixels, width, height);
		
		IOUtil.createDir(new FileHandle("noise/"));
		FileHandle file = IOUtil.getNewFile(new FileHandle("noise/simplex.png"));
		try {
			ImageIO.write(image, "png", file.file());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double noise(double x, double y) {
		//return simplex1.noise(x, y);
		///*
		return (simplex1.noise(x, y) * 128D +
				simplex2.noise(x, y) * 64D +
				simplex3.noise(x, y) * 32D +
				simplex4.noise(x, y) * 16D +
				simplex5.noise(x, y) * 8D +
				simplex6.noise(x, y) * 4D +
				simplex7.noise(x, y) * 2D)/255D;
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
		new SimplexTest();
	}

}
