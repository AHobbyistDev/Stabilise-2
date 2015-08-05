package com.stabilise.tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import com.badlogic.gdx.utils.ObjectMap;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.StringUtil;
import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Point;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.Polygon;
import com.stabilise.util.shape.Shape;

/**
 * This class contains an assortment of tests I have conducted, primarily to
 * test performance in the interest of efficiency (and oftentimes misguidedly
 * so, probably). Test methods are left untouched as a point of reference for
 * other parts of code, and for future re-tests if such a need to reperform a
 * test is found. It should be noted that the results of performance checks may
 * vary from system to system, so if one method of doing something is faster on
 * one system, it may in fact be slower on others.
 */
public class RandomTests {
	
	// Non-instantiable
	private RandomTests() {}
	
	/**
	 * Compares fastFloor, fastCeil and fastRound in {@link Maths} to their
	 * standard counterparts.
	 */
	@SuppressWarnings("unused")
	private static void fastOperations() {
		final int elements = 10000000;
		double[] nums = new double[elements];
		int dump;
		Random rnd = new Random();
		
		for(int i = 0; i < elements; i++) {
			nums[i] = rnd.nextDouble();
		}
		
		TaskTimer c1 = new TaskTimer("(int)Math.floor()");
		TaskTimer c2 = new TaskTimer("MathUtil.fastFloor()");
		
		c1.start();
		for(double d : nums) {
			dump = (int)Math.floor(d);
		}
		c1.stop();
		c2.start();
		for(double d : nums) {
			dump = Maths.floor(d);
		}
		c2.stop();
		
		c1.printComparison(c2);
		
		c1 = new TaskTimer("(int)Math.ceil()");
		c2 = new TaskTimer("MathUtil.fastCeil()");
		
		c1.start();
		for(double d : nums) {
			dump = (int)Math.ceil(d);
		}
		c1.stop();
		c2.start();
		for(double d : nums) {
			dump = Maths.ceil(d);
		}
		c2.stop();
		
		c1.printComparison(c2);
		
		c1 = new TaskTimer("(int)Math.round()");
		c2 = new TaskTimer("MathUtil.fastRound()");
		
		c1.start();
		for(double d : nums) {
			dump = (int)Math.round(d);
		}
		c1.stop();
		c2.start();
		for(double d : nums) {
			dump = Maths.round(d);
		}
		c2.stop();
		
		c1.printComparison(c2);
	}
	
	/**
	 * simple storage class of a double and an int
	 */
	private static class DoubleIntPair {
		private final double d;
		private final int i;
		private DoubleIntPair(double d, int i) {
			this.d = d;
			this.i = i;
		}
	}
	
	// old and deprecated code from StringUtil or MathUtil - I can't remember
	private static String oldAbb(double n, int places) {
		return Double.toString(Math.round(n * places * 10) / ((double)places * 10D));
	}
	
	/**
	 * Just making sure the new abbreviation method works well and good
	 */
	@SuppressWarnings("unused")
	private static void doubleAbbreviations() {
		int someUnusedNumber;
		
		DoubleIntPair[] nums = new DoubleIntPair[] {
				new DoubleIntPair(1.2345678D, 3),
				new DoubleIntPair(1.2345678D, 12),
				new DoubleIntPair(4, 3),
				new DoubleIntPair(1.2345678D, 0),
				new DoubleIntPair(1.2345678D, 300),
				new DoubleIntPair(1.23D, 3)
		};
		
		for(DoubleIntPair p : nums) {
			System.out.println(StringUtil.cullFP(p.d, p.i) + " : " + oldAbb(p.d, p.i));
		}
	}
	
	protected static void testHashes() {
		int min = -200;
		int max = 200;
		for(int x = min; x <= max; x++) {
			for(int y = min; y <= max; y++) {
				System.out.println("["+x+","+y+"]:" + hash1(x,y));
			}
		}
	}
	
	protected static long hash1(int x, int y) {
		long n = x + ((long)y << 32);
		n = ((n >>> 32) ^ n) * 0x45d9f3b + 1376312589;
		n = ((n >>> 32) ^ n) * 0x45d9f3b;
		n = ((n >>> 32) ^ n);
		return n;
	}
	
	@SuppressWarnings("unused")
	protected static void gdxObjectMap() {
		int elements = 1024;
		int iterations = 1024;
		HashMap<String, Object> map1 = new HashMap<String, Object>(elements);
		ObjectMap<String, Object> map2 = new ObjectMap<String, Object>(elements);
		String[] keys = new String[elements];
		Object[] vals = new Object[elements];
		Random rnd = new Random();
		int minStringSize = 8;
		int maxStringSize = 16;
		
		for(int i = 0; i < elements; i++) {
			int stringSize = minStringSize + rnd.nextInt(maxStringSize - minStringSize);
			StringBuilder sb = new StringBuilder(stringSize);
			for(int j = 0; j < stringSize; j++)
				sb.append((char)rnd.nextInt());
			keys[i] = sb.toString();
			vals[i] = new Object();
			
			//System.out.println("Genned string " + i + ": " + sb.toString());
		}
		
		TaskTimer t1 = new TaskTimer("HashMap - put");
		TaskTimer t2 = new TaskTimer("ObjectMap - put");
		
		t1.start();
		
		for(int i = 0; i < elements; i++)
			map1.put(keys[i], vals[i]);
		
		t1.stop();
		t2.start();
		
		for(int i = 0; i < elements; i++)
			map2.put(keys[i], vals[i]);
		
		t2.stop();
		
		t1.printComparison(t2);
		
		t1 = new TaskTimer("HashMap - get");
		t2 = new TaskTimer("ObjectMap - get");
		
		t1.start();
		
		for(int itr = 0; itr < iterations; itr++) {
			for(int i = 0; i < elements; i++) {
				Object o = map1.get(keys[i]);
			}
		}
		
		t1.stop();
		t2.start();
		
		for(int itr = 0; itr < iterations; itr++) {
			for(int i = 0; i < elements; i++) {
				Object o = map1.get(keys[i]);
			}
		}
		
		t2.stop();
		
		t1.printComparison(t2);
	}
	
	protected static final void hashPointCollisions2() {
		final int width = 1024;
		final int height = 1024;
		int[] pixels = new int[width*height];
		Point p = Point.mutable(0, 0);
		final int collisionYes = 0xFFFF0000;
		final int collisionNo = 0xFFFFFFFF;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				boolean collision = p.hashCode() == Point.mutable(x-width/2,y-height/2).hashCode();
				pixels[y*height+x] = collision ? collisionYes : collisionNo;
			}
		}
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
		
        File dir = new File("C:/Users/Adam/Documents/Hash Collisions/");
		dir.mkdirs();
		
		try {
			ImageIO.write(image, "png", new File(dir, p.toString() + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static void shapeTests() {
		Polygon p1 = new Polygon(
				Vec2.immutable(0f, 0f),
				Vec2.immutable(0.5f, 0f),
				Vec2.immutable(1f, 0.5f),
				Vec2.immutable(1f, 1f),
				Vec2.immutable(0.5f, 1f),
				Vec2.immutable(0f, 0.5f)
		);
		Vec2[] verts = p1.getVertices();
		Shape p2 = p1.translate(1f, 1f);
		Shape p3 = p2.translate(-1f, -1f);
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		System.out.println(p1.equals(p3) + " <=> " + p3.equals(p1));
		System.out.println(p1.equals(p2) + " <=> " + p2.equals(p1));
		System.out.println("1: " + p1.intersects(p1) + ", " + p1.contains(p1));
		System.out.println("2: " + p1.intersects(p2) + ", " + p1.contains(p2));
		System.out.println("3: " + p1.intersects(p3) + ", " + p1.contains(p3));
		verts = p1.getVertices();
		System.out.println(verts[0]);
		Shape p4 = p1.transform(new Matrix2(0f, -1, 1, 0));
		System.out.println(p4);
	}
	
	// ---------- MAIN ----------
	public static void main(String[] args) {
		
	}

}
