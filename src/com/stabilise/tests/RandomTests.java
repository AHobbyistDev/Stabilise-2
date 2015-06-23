package com.stabilise.tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.badlogic.gdx.utils.ObjectMap;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.StringUtil;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.LightLinkedList;
import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Point;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.SimplexNoise;
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
	
	@SuppressWarnings("unused")
	private static void hashMapVsLinkedHashMap() {
		final int elements = 500000;
		initTimer();
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tickTimer("sleep");
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		tickTimer("declare hashmap");
		LinkedHashMap<Integer, String> linkedMap = new LinkedHashMap<Integer, String>();
		tickTimer("declare linkedmap");
		for(int i = 0; i < elements; i++) {
			map.put(i, "blerp");
		}
		tickTimer("fill hashmap");
		for(int i = 0; i < elements; i++) {
			linkedMap.put(i, "blerp");
		}
		tickTimer("fill linkedmap");
		for(String s : map.values()) {
			// something
		}
		tickTimer("iterate hashmap");
		for(String s : linkedMap.values()) {
			// something
		}
		tickTimer("iterate linkedmap");
	}
	
	/**
	 * Just to see how these rank up against each other...
	 */
	@SuppressWarnings("unused")
	private static void comparisonOperators() {
		final int n = 1000000000;
		int a = 5;
		int b = 4;
		
		initTimer();
		try {
			Thread.sleep(300L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tickTimer("sleep");
		for(int i = 0; i < n; i++) {
			if(a == b)
				;
		}
		tickTimer("==");
		for(int i = 0; i < n; i++) {
			if(a < b)
				;
		}
		tickTimer("<");
		for(int i = 0; i < n; i++) {
			if(a <= b)
				;
		}
		tickTimer("<=");
		for(int i = 0; i < n; i++) {
			if(a > b)
				;
		}
		tickTimer(">");
		for(int i = 0; i < n; i++) {
			if(a >= b)
				;
		}
		tickTimer(">=");
		for(int i = 0; i < n; i++) {
			if(a + b == a)
				;
		}
		tickTimer("+ & ==");
		for(int i = 0; i < n; i++) {
			if(a - b == a)
				;
		}
		tickTimer("- & ==");
		for(int i = 0; i < n; i++) {
			if(a * b == a)
				;
		}
		tickTimer("* & ==");
		for(int i = 0; i < n; i++) {
			if(a / b == a)
				;
		}
		tickTimer("/ & ==");
	}
	
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
	
	@SuppressWarnings("unused")
	private static void hashingTest() {
		for(int x = -20; x <= 20; x++) {
			for(int y = -20; y <= 20; y++) {
				int n = x + y * 57;
				n = (n<<13) ^ n;
				n = n * (n * n * 15731 + 789221) + 1376312589;
				System.out.println(x + "," + y + ": " + n);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void samplePerlinNoise() {
		//PerlinNoise2D perlin = new PerlinNoise2D(new Random().nextLong(), 2f);
		SimplexNoise simplex = new SimplexNoise(new Random().nextLong(), 2f);
		
		double min = 1;
		double max = 0;
		double avg = 0;
		
		for(int x = -300; x <= 300; x++) {
			for(int y = -300; y <= 300; y++) {
				//double noise = perlin.noise(x, y);
				double noise = simplex.noise(x, y);
				if(noise < min)
					min = noise;
				else if(noise > max)
					max = noise;
				avg += noise;
			}
		}
		
		avg /= 600*600;
		
		System.out.println("Min: " + min);
		System.out.println("Max: " + max);
		System.out.println("Avg: " + avg);
	}
	
	@SuppressWarnings("unused")
	private static void arrayVsLinkedList() {
		// Increasing to 5000000 (by a factor of 5) increases the population
		// time of a LinkedList by a factor of >200.
		final int elements = 1000000;
		final int toRemove = 100;
		
		Object[] arr = new Object[elements];
		ArrayList<Object> array = new ArrayList<Object>();
		LinkedList<Object> linked = new LinkedList<Object>();
		
		initTimer();
		
		for(int i = 0; i < elements; i++) {
			arr[i] = new Object();
		}
		
		tickTimer("Instantiate objects + populate standard array");
		
		for(int i = 0; i < elements; i++) {
			array.add(arr[i]);
		}
		
		tickTimer("Populate ArrayList");
		
		for(int i = 0; i < elements; i++) {
			linked.add(arr[i]);
		}
		
		tickTimer("Populate LinkedList");
		
		for(int i = 0; i < toRemove; i++) {
			array.remove(0);
		}
		
		tickTimer("Remove 0th element from ArrayList");
		
		for(int i = 0; i < toRemove; i++) {
			linked.remove(0);
		}
		
		tickTimer("Remove 0th element from LinkedList");
	}
	
	@SuppressWarnings("unused")
	private static void divisionBy2() {
		int elements = 100000000;
		float[] nums = new float[elements];
		float dump = 0f;
		Random rnd = new Random();
		
		TaskTimer mult = new TaskTimer("multiplication by 0.5");
		TaskTimer div = new TaskTimer("division by 2");
		
		for(int i = 0; i < elements; i++) {
			nums[i] = rnd.nextFloat();
		}
		
		mult.start();
		
		for(float f : nums) {
			dump = f * 0.5f;
		}
		
		mult.stop();
		div.start();
		
		for(float f : nums) {
			dump = f / 2;
		}
		
		div.stop();
		
		mult.printResult();
		div.printResult();
		mult.printComparison(div);
	}
	
	/**
	 * testing the famous "fast inverse square root" (turns out it's slower
	 * than 1/sqrt(x))
	 */
	@SuppressWarnings("unused")
	private static void invSqrt() {
		int elements = 50000000;
		float[] nums = new float[elements];
		float dump = 0f;
		Random rnd = new Random();
		
		initTimer();
		
		for(int i = 0; i < elements; i++) {
			nums[i] = rnd.nextFloat();
		}
		
		tickTimer("Populate");
		
		for(float f : nums) {
			dump = (float) (1f / Math.sqrt(f));
		}
		
		tickTimer("(float) (1f / Math.sqrt(f))");
		
		for(float f : nums) {
			dump =  1f / (float)Math.sqrt(f);
		}
		
		tickTimer("1f / (float)Math.sqrt(f)");
		
		for(float f : nums) {
			//dump = MathUtil.invSqrt(f);
		}
		
		tickTimer("MathUtil.invSqrt(f)");
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
	
	@SuppressWarnings("unused")
	private static void listIterationAndClearing() {
		final int iterations = 10000000;
		List<Object> list = new ArrayList<Object>();
		
		TaskTimer c1 = new TaskTimer("Iterating and clearing an empty list");
		TaskTimer c2 = new TaskTimer("Verifying the list's emptiness");
		
		c1.start();
		for(int i = 0; i < iterations; i++) {
			for(Object o : list) {
				// do something
			}
			list.clear();
		}
		c1.stop();
		
		c2.start();
		for(int i = 0; i < iterations; i++) {
			if(list.size() != 0) {
				for(Object o : list) {
					// do something
				}
				list.clear();
			}
		}
		c2.stop();
		
		c1.printResult();
		c2.printResult();
		c1.printComparison(c2);
	}
	
	@SuppressWarnings("unused")
	private static void wrappedRemainder() {
		TaskTimer c1 = new TaskTimer("Util wrap");
		TaskTimer c2 = new TaskTimer("Bitmask wrap");
		
		final int elements = 100000000;
		final int numMax = 5000;
		final int numAmp = numMax / 2;
		final int divMax = 500;
		final int divAmp = divMax / 2;
		int[] nums = new int[elements];
		int[] divs = new int[elements];
		Random rnd = new Random();
		int dump;
		
		for(int i = 0; i < elements; i++) {
			nums[i] = rnd.nextInt(numMax) - numAmp;
			divs[i] = 1 << (rnd.nextInt(5) + 1);
		}
		
		c1.start();
		for(int i = 0; i < elements; i++) {
			dump = Maths.remainder(nums[i], divs[i]);
		}
		c1.stop();
		c2.start();
		for(int i = 0; i < elements; i++) {
			dump = Maths.remainder2(nums[i], divs[i]);
		}
		c2.stop();
		
		c1.printResult();
		c2.printResult();
		c1.printComparison(c2);
		
		for(int i = 0; i < elements; i++) {
			if(Maths.remainder(nums[i], divs[i]) != Maths.remainder2(nums[i], divs[i]))
				System.out.println(nums[i] + "/" + divs[i] + ": " + Maths.remainder(nums[i], divs[i]) + "||" + Maths.remainder2(nums[i], divs[i]));
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
	
	protected static void wrappedRemainder2() {
		final int elements = 10000000;
		final int numMax = 0xFFFF + 1;
		final int numMin = 0;
		final int numDiff = numMax - numMin;
		final int[] nums = new int[elements];
		final int[] divs = new int[elements];
		Random rnd = new Random();
		
		for(int i = 0; i < 32; i++) { // assign every PoT to a div
			divs[i] = 1 << i;
		}
		
		System.out.println("Populating elements...");
		for(int i = 0; i < elements; i++) {
			nums[i] = rnd.nextInt(numDiff) - numMin;
			divs[i] = divs[Maths.remainder2(i, 32)];
		}
		
		final Runnable modulus = new Runnable() {
			@Override public void run() {
				@SuppressWarnings("unused")
				int dump;
				for(int i = 0; i < elements; i++) {
					dump = nums[i] % divs[i];
				}
			}
		};
		
		final Runnable rem1 = new Runnable() {
			@Override public void run() {
				@SuppressWarnings("unused")
				int dump;
				for(int i = 0; i < elements; i++) {
					dump = Maths.remainder(nums[i], divs[i]);
				}
			}
		};
		
		final Runnable rem2 = new Runnable() {
			@Override public void run() {
				@SuppressWarnings("unused")
				int dump;
				for(int i = 0; i < elements; i++) {
					dump = Maths.remainder2(nums[i], divs[i]);
				}
			}
		};
		
		final int iterations = 64;
		
		test(new Runnable() {
			@Override
			public void run() {
				time("% operator", iterations, modulus);
				time("wrappedRemainder", iterations, rem1);
				time("wrappedRemainder2", iterations, rem2);
			}
		}, 10);
		
	}
	
	protected static final void hashPointCollisions() {
		List<Point> points = new LinkedList<Point>();
		int len = 20;
		for(int x = -len; x <= len; x++)
			for(int y = -len; y <= len; y++)
				points.add(Point.mutable(x, y));
		
		List<Point> hashCollisions = new LinkedList<Point>();
		while(points.size() > 0) {
			Point p1 = points.remove(0);
			for(Point p2 : points) {
				if(p1.hashCode() == p2.hashCode())
					hashCollisions.add(p2);
			}
			if(hashCollisions.size() > 0) {
				System.out.println(p1 + " collisions:");
				Iterator<Point> i = hashCollisions.iterator();
				while(i.hasNext()) {
					System.out.println("    " + i.next());
					i.remove();
				}
			}
		}
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
	
	@SuppressWarnings("unused")
	protected static void autoboxing() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int i = map.get(""); // will there be an autoboxing-induced NPE?
		int j = map.get("").intValue(); // definitely one here
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
	
	protected static void lightLinkedList() {
		LightLinkedList<Integer> list = new LightLinkedList<>();
		System.out.println(list);
		for(int i = 0; i <= 10; i++)
			list.add(i);
		System.out.println(list);
		list.remove(3);
		System.out.println(list);
		IteratorUtils.forEach(list, i -> {
			System.out.println("Iterating over " + i);
			if(i.intValue() % 2 == 0) {
				System.out.println("Removing " + i);
				return true;
			}
			return false;
		});
		System.out.println(list);
	}
	
	// ---------- TIMER ----------
	
	private static long prev;
	private static long now;
	
	/**
	 * Initiates the timer. The timer provides a simple method of timing how
	 * long it takes to perform a task; invoking {@link #tickTimer(String)}
	 * prints the number of nanoseconds passed since the last invocation of it
	 * (or this method).
	 * 
	 * <p>
	 * For a better method of comparing the performance of certain tasks, refer
	 * to {@link com.stabilise.util.TaskTimer}.
	 * </p>
	 */
	private static void initTimer() {
		prev = System.nanoTime();
		now = prev;
	}
	
	/**
	 * Ticks the timer and prints the number of nanoseconds since the last tick
	 * was performed.
	 * 
	 * @param task The name of the task to be treated as having been timed.
	 */
	private static void tickTimer(String task) {
		prev = now;
		now = System.nanoTime();
		System.out.println("Time to " + task + ": " + (now - prev));
	}
	
	private static void test(Runnable tester, int warmups) {
		System.out.println("\nStarting warmup...\n<<-------------------->>\n");
		printResult = false;
		for(int i = 0; i < warmups; i++) {
			System.out.println("Doing warmup " + (i+1) + "/" + warmups);
			tester.run();
		}
		System.out.println("\nWarmup completed...\n<<-------------------->>\n");
		printResult = true;
		tester.run();
	}
	
	private static boolean printResult = false;
	
	/**
	 * invoke this instead of {@link #time(boolean, String, int, Runnable)} if
	 * {@link #test(Runnable)} is beinng used to oversee testing.
	 */
	private static TaskTimer time(String name, int iterations, Runnable task) {
		return time(printResult, name, iterations, task);
	}
	
	/**
	 * times a runnable
	 * 
	 * @param print true if the result should be printed to system.out
	 * @param name the name of the task
	 * @param iterations the number of times to perform the task
	 * @param task the task
	 * 
	 * @return the TaskTimer which timed the task
	 */
	private static TaskTimer time(boolean print, String name, int iterations, Runnable task) {
		TaskTimer t = new TaskTimer(name);
		t.start();
		while(iterations-- > 0)
			task.run();
		t.stop();
		if(print)
			t.printResult(TimeUnit.MILLISECONDS);
		return t;
	}
	
	// ---------- MAIN ----------
	public static void main(String[] args) {
		//hashMapVsIntHashMap();
		//bidiHashMapTest();
		//hashMapIterations();
		//hashMapVsLinkedHashMap();
		//comparisonOperators();
		//fastOperations();
		//hashingTest();
		//samplePerlinNoise();
		//testNewFiles();
		//getFloatingPointBits(4342.1325f);
		//colourHexTest();
		//calculatingPowers();
		//arrayVsLinkedList();
		//divisionBy2();
		//invSqrt();
		//doubleAbbreviations();
		//listIterationAndClearing();
		//wrappedRemainder();
		//testHashes();
		//gdxObjectMap();
		//wrappedRemainder2();
		//hashPointCollisions();
		//hashPointCollisions2();
		//autoboxing();
		//shapeTests();
		lightLinkedList();
		
		//String s = "abcxyzABCXYZ a()a_a-a*a/a\\a.a'a\"";
		//System.out.println(s + "\n" + IOUtil.getLegalString(s));
	}

}
