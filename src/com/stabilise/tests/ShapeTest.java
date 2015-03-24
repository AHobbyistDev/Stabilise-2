package com.stabilise.tests;

import java.util.concurrent.TimeUnit;

import com.stabilise.util.TaskTimer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.AxisAlignedBoundingBox;
import com.stabilise.util.shape.LightAABB;
import com.stabilise.util.shape.Polygon;

public class ShapeTest {
	
	private static boolean result = false;
	
	public ShapeTest(boolean print) {
		final AxisAlignedBoundingBox b1 = new AxisAlignedBoundingBox(-0.5f, -0.5f, 1f, 1f);
		final LightAABB b2 = new LightAABB(-0.5f, -0.5f, 1f, 1f);
		final AxisAlignedBoundingBox b1Pre = b1;
		final LightAABB b2Pre = b2;
		
		int collisions = 16384*512;
		
		time(print, "AABB i FastAABB as AbstractPolygon", collisions, () -> result = b1.intersects(b2));
		time(print, "AABB i FastAABB as AABB", collisions, () -> result = b1.intersectsAABB(b2));
		time(print, "AABB i FastAABB as AbstractPolygon (Precomputed)", collisions, () -> result = b1Pre.intersects(b2Pre));
		time(print, "AABB i FastAABB as AABB (Precomputed)", collisions, () -> result = b1Pre.intersectsAABB(b2Pre));
		
		time(print, "AABB c FastAABB as AbstractPolygon", collisions, () -> result = b1.contains(b2));
		time(print, "AABB c FastAABB as AABB", collisions, () -> result = b1.containsAABB(b2));
		time(print, "AABB c FastAABB as AbstractPolygon (Precomputed)", collisions, () -> result = b1Pre.contains(b2Pre));
		time(print, "AABB c FastAABB as AABB (Precomputed)", collisions, () -> result = b1Pre.containsAABB(b2Pre));
		
		
		/*
		final Polygon p = circlePoly(32);
		final Polygon pPre = p.precomputed();
		
		time(print, "Polygon v AABB", collisions, () -> p.intersects(b1));
		time(print, "Polygon v FastAABB", collisions, () -> p.intersects(b2));
		time(print, "Polygon (Pre) v AABB", collisions, () -> pPre.intersects(b1));
		time(print, "Polygon (Pre) v FastAABB", collisions, () -> pPre.intersects(b2));
		time(print, "Polygon v AABB (Pre)", collisions, () -> p.intersects(b1Pre));
		time(print, "Polygon v FastAABB (Pre)", collisions, () -> p.intersects(b2Pre));
		time(print, "Polygon (Pre) v AABB (Pre)", collisions, () -> pPre.intersects(b1Pre));
		time(print, "Polygon (Pre) v FastAABB (Pre)", collisions, () -> pPre.intersects(b2Pre));
		
		time(print, "Polygon (BotLeft) v FastAABB (Pre)", collisions, () -> p.translate(-1f, -1f).intersects(b2Pre));
		time(print, "Polygon (TopRight) v FastAABB (Pre)", collisions, () -> p.translate(1f, 1f).intersects(b2Pre));
		*/
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
