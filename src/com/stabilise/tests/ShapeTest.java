package com.stabilise.tests;

import java.util.concurrent.TimeUnit;

import com.stabilise.util.TaskTimer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;

public class ShapeTest {
	
	private static boolean result = false;
	
	public ShapeTest(boolean print) {
		final AABB b1 = new AABB(-0.5f, -0.5f, 1f, 1f);
		final AABB b2 = new AABB(-2f, -2f, 4f, 1f);
		
		int collisions = 16384*512;
		
		time(print, "1 i 1 as AbstractPolygon", collisions, () -> result = b1.intersects(b1));
		time(print, "2 i 2 as AbstractPolygon", collisions, () -> result = b2.intersects(b2));
		time(print, "1 i 2 as AbstractPolygon", collisions, () -> result = b1.intersects(b2));
		time(print, "2 i 1 as AbstractPolygon", collisions, () -> result = b2.intersects(b1));
		time(print, "1 i 1 as AABB", collisions, () -> result = b1.intersectsAABB(b1));
		time(print, "2 i 2 as AABB", collisions, () -> result = b2.intersectsAABB(b2));
		time(print, "1 i 2 as AABB", collisions, () -> result = b1.intersectsAABB(b2));
		time(print, "2 i 1 as AABB", collisions, () -> result = b2.intersectsAABB(b1));
		
		time(print, "1 c 1 as AbstractPolygon", collisions, () -> result = b1.contains(b1));
		time(print, "2 c 2 as AbstractPolygon", collisions, () -> result = b2.contains(b2));
		time(print, "1 c 2 as AbstractPolygon", collisions, () -> result = b1.contains(b2));
		time(print, "2 c 1 as AbstractPolygon", collisions, () -> result = b2.contains(b1));
		time(print, "1 c 1 as AABB", collisions, () -> result = b1.containsAABB(b1));
		time(print, "2 c 2 as AABB", collisions, () -> result = b2.containsAABB(b2));
		time(print, "1 c 2 as AABB", collisions, () -> result = b1.containsAABB(b2));
		time(print, "2 c 1 as AABB", collisions, () -> result = b2.containsAABB(b1));
		
	}
	
	private void time(boolean print, String name, int iterations, Runnable task) {
		TaskTimer t = new TaskTimer(name);
		t.start();
		while(iterations-- > 0)
			task.run();
		t.stop();
		if(print)
			System.out.println(t.getResult(TimeUnit.MILLISECONDS) + " (" + result + ")");
	}
	
	protected Polygon circlePoly(int vertices) {
		if(vertices < 3)
			throw new IllegalArgumentException();
		Vec2[] verts = new Vec2[vertices];
		verts[0] = Maths.VEC_X;
		double increments = Maths.TAU / vertices;
		double angle = 0D;
		for(int i = 1; i < vertices; i++) {
			angle += increments;
			verts[i] = new Vec2((float)Math.cos(angle), (float)Math.sin(angle));
		}
		return new Polygon(verts);
	}
	
	public static void main(String[] args) {
		System.out.println("Starting warmup...\n----------\n");
		// warmup
		for(int i = 0; i < 5; i++) {
			System.out.println("Doing warmup " + i);
			new ShapeTest(false);
		}
		System.out.println("\nStarting benchmark...\n----------\n");
		
		// benchmarks
		new ShapeTest(true);
	}
	
}
